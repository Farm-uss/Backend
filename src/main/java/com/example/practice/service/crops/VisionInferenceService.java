package com.example.practice.service.crops;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.crops.AiPredictResponse;
import com.example.practice.dto.crops.DiseaseCheckData;
import com.example.practice.dto.crops.GrowthCheckData;
import com.example.practice.dto.crops.GrowthInferenceCheckResponse;
import com.example.practice.dto.crops.VisionInferenceCheckResponse;
import com.example.practice.entity.crops.Crops;
import com.example.practice.entity.crops.DiseaseGuide;
import com.example.practice.entity.crops.DiseaseGuideType;
import com.example.practice.entity.crops.DiseaseInfo;
import com.example.practice.entity.crops.GrowthMeasurement;
import com.example.practice.entity.crops.ImageCapture;
import com.example.practice.entity.crops.VisionInference;
import com.example.practice.repository.crops.CropsRepository;
import com.example.practice.repository.crops.DiseaseInfoRepository;
import com.example.practice.repository.crops.GrowthMeasurementRepository;
import com.example.practice.repository.crops.ImageCaptureRepository;
import com.example.practice.repository.crops.VisionInferenceRepository;
import com.example.practice.repository.farm.FarmMemberRepository;
import com.example.practice.service.aws.AwsS3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisionInferenceService {
    private static final String TASK_DISEASE_CLASSIFICATION = "DISEASE_CLASSIFICATION";
    private static final String TASK_GROWTH_MEASUREMENT = "GROWTH_MEASUREMENT";
    private static final BigDecimal GROWTH_CONFIDENCE_THRESHOLD = BigDecimal.valueOf(0.6);

    private final FarmMemberRepository farmMemberRepository;
    private final CropsRepository cropsRepository;
    private final ImageCaptureRepository imageCaptureRepository;
    private final VisionInferenceRepository visionInferenceRepository;
    private final GrowthMeasurementRepository growthMeasurementRepository;
    private final DiseaseInfoRepository diseaseInfoRepository;
    private final AwsS3Service awsS3Service;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${ai.inference.base-url:http://localhost:8001}")
    private String aiBaseUrl;

    @Value("${ai.inference.predict-path:/predict}")
    private String aiPredictPath;

    @Value("${ai.inference.timeout-ms:5000}")
    private long aiTimeoutMs;

    private static final Map<String, String> DISEASE_CODE_ALIASES = Map.of(
            "leaf_blight", "a7",
            "powdery_mildew", "a3",
            "rust", "a5"
    );

    @Transactional
    public VisionInferenceCheckResponse inferDiseaseAndSave(
            Long farmId,
            Long cropsId,
            Long userId,
            MultipartFile image,
            Long cameraId,
            OffsetDateTime measuredAt
    ) {
        Crops crop = cropsRepository.findByFarmIdAndCropsIdWithGrowthStandard(farmId, cropsId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "crops not found in farm"));
        Integer cropCode = parseCropCode(crop);

        PreparedInferenceContext prepared = prepareInference(
                farmId,
                cropsId,
                userId,
                image,
                cameraId,
                TASK_DISEASE_CLASSIFICATION,
                cropCode,
                measuredAt
        );
        VisionInference inference = saveVisionInference(prepared.uploadedImage(), prepared.aiResponse());
        DiseaseCheckData data = toCheckData(prepared.uploadedImage(), inference, prepared.aiResponse());
        return VisionInferenceCheckResponse.ok(data);
    }

    @Transactional
    public GrowthInferenceCheckResponse inferGrowthAndSave(
            Long farmId,
            Long cropsId,
            Long userId,
            MultipartFile image,
            Long cameraId,
            Integer cropCode,
            OffsetDateTime measuredAt
    ) {
        PreparedInferenceContext prepared = prepareInference(
                farmId,
                cropsId,
                userId,
                image,
                cameraId,
                TASK_GROWTH_MEASUREMENT,
                cropCode,
                measuredAt
        );
        VisionInference inference = saveVisionInference(prepared.uploadedImage(), prepared.aiResponse());
        saveGrowthMeasurement(prepared.crop(), prepared.capturedAt(), prepared.aiResponse());
        GrowthCheckData data = toGrowthCheckData(prepared.uploadedImage(), inference, prepared.aiResponse());
        return GrowthInferenceCheckResponse.ok(data);
    }

    @Transactional
    private PreparedInferenceContext prepareInference(
            Long farmId,
            Long cropsId,
            Long userId,
            MultipartFile image,
            Long cameraId,
            String taskType,
            Integer cropCode,
            OffsetDateTime measuredAt
    ) {
        if (!farmMemberRepository.existsByFarmIdAndUserId(farmId, userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "farm access denied");
        }

        Crops crop = cropsRepository.findByFarmIdAndCropsIdWithGrowthStandard(farmId, cropsId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "crops not found in farm"));

        if (image == null || image.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "image is required");
        }

        byte[] imageBytes = readUploadedImage(image);
        String originalFilename = image.getOriginalFilename();
        String contentType = image.getContentType();

        OffsetDateTime capturedAt = measuredAt == null ? OffsetDateTime.now(ZoneOffset.UTC) : measuredAt;
        ImageCapture uploadedImage = new ImageCapture();
        uploadedImage.setCapturedAt(capturedAt);
        uploadedImage.setCameraId(cameraId);
        uploadedImage.setImagePath(uploadUploadedImage(imageBytes, originalFilename, contentType));
        uploadedImage = imageCaptureRepository.save(uploadedImage);

        String requestedTaskType = normalizeTaskType(taskType);
        validateTaskInputs(requestedTaskType, cropCode);
        AiPredictResponse aiResponse = callAiServer(
                imageBytes,
                originalFilename,
                contentType,
                uploadedImage.getCaptureId(),
                requestedTaskType,
                cropCode
        );

        return new PreparedInferenceContext(crop, capturedAt, uploadedImage, aiResponse);
    }

    private VisionInference saveVisionInference(ImageCapture uploadedImage, AiPredictResponse aiResponse) {
        VisionInference inference = new VisionInference();
        inference.setCapture(uploadedImage);
        inference.setModelName(aiResponse.modelName());
        inference.setTaskType(aiResponse.taskType());
        inference.setLabel(aiResponse.label());
        inference.setConfidence(aiResponse.confidence());
        inference.setBboxJson(aiResponse.bboxJson());
        inference.setInferredAt(aiResponse.inferredAt());
        inference.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        inference.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        inference.setIsAbnormal(aiResponse.isAbnormal());
        inference.setAbnormalReason(aiResponse.abnormalReason());
        return visionInferenceRepository.save(inference);
    }

    private void saveGrowthMeasurement(Crops crop, OffsetDateTime capturedAt, AiPredictResponse aiResponse) {
        GrowthMeasurement measurement = new GrowthMeasurement();
        measurement.setCrops(crop);
        measurement.setMeasuredAt(capturedAt);
        measurement.setLeafCount(aiResponse.leafCount());
        measurement.setFruitCount(aiResponse.fruitCount());
        measurement.setLeafArea(aiResponse.sizeCm());
        measurement.setAiConfidence(aiResponse.confidence());
        measurement.setAiSummary(aiResponse.summary());
        measurement.setAiVerdict(aiResponse.disease());
        measurement.setAiLabel(aiResponse.label());
        measurement.setAiRawJson(truncate(writeJson(aiResponse), 255));
        measurement.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        measurement.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        growthMeasurementRepository.save(measurement);
    }

    private DiseaseCheckData toCheckData(ImageCapture uploadedImage, VisionInference inference, AiPredictResponse aiResponse) {
        String normalizedDisease = normalizeDiseaseKey(aiResponse.disease(), aiResponse.label());
        int diseaseStatus = isHealthyLike(normalizedDisease) ? 0 : 1;
        int confidencePercent = toPercent(aiResponse.confidence());

        if (diseaseStatus == 0) {
            return new DiseaseCheckData(
                    0,
                    "healthy",
                    "정상",
                    "현재 병해충 징후가 뚜렷하지 않습니다.",
                    confidencePercent,
                    List.of(),
                    List.of(),
                    List.of(),
                    uploadedImage.getCaptureId(),
                    inference.getInferenceId(),
                    aiResponse.modelName(),
                    aiResponse.modelVersion(),
                    aiResponse.inferredAt()
            );
        }

        DiseaseInfo info = diseaseInfoRepository.findByDiseaseIdAndActiveTrue(normalizedDisease).orElse(null);
        List<String> causes = guideList(info, DiseaseGuideType.CAUSE);
        List<String> symptoms = guideList(info, DiseaseGuideType.SYMPTOM);
        List<String> solutions = guideList(info, DiseaseGuideType.SOLUTION);
        String diseaseId = info != null ? info.getDiseaseId() : normalizedDisease;
        String diseaseName = info != null ? info.getDiseaseName() : "질병 미확정";
        String diseaseDescription = info != null
                ? safeText(info.getDiseaseDescription())
                : "해당 질병 코드에 대한 설명 데이터가 등록되지 않았습니다.";
        if (diseaseDescription == null) {
            diseaseDescription = "해당 질병 코드에 대한 설명 데이터가 등록되지 않았습니다.";
        }

        return new DiseaseCheckData(
                1,
                diseaseId,
                diseaseName,
                diseaseDescription,
                confidencePercent,
                causes,
                symptoms,
                solutions,
                uploadedImage.getCaptureId(),
                inference.getInferenceId(),
                aiResponse.modelName(),
                aiResponse.modelVersion(),
                aiResponse.inferredAt()
        );
    }

    private GrowthCheckData toGrowthCheckData(ImageCapture uploadedImage, VisionInference inference, AiPredictResponse aiResponse) {
        int leafCount = leafCountByThreshold(aiResponse.topConfidences(), GROWTH_CONFIDENCE_THRESHOLD);
        if (leafCount < 0) {
            leafCount = aiResponse.leafCount() == null ? 0 : Math.max(aiResponse.leafCount(), 0);
        }
        BigDecimal sizePxTotal = firstNonNull(aiResponse.sizePxTotal(), aiResponse.sizeCm(), BigDecimal.ZERO);
        int confidencePercent = toPercent(aiResponse.confidence());

        return new GrowthCheckData(
                leafCount,
                sizePxTotal,
                GROWTH_CONFIDENCE_THRESHOLD,
                confidencePercent,
                uploadedImage.getCaptureId(),
                inference.getInferenceId(),
                aiResponse.modelName(),
                aiResponse.modelVersion(),
                aiResponse.inferredAt()
        );
    }

    private AiPredictResponse callAiServer(
            byte[] imageBytes,
            String originalFilename,
            String contentType,
            Long captureId,
            String taskType,
            Integer cropCode
    ) {
        try {
            ByteArrayResource fileResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return originalFilename == null ? "capture.jpg" : originalFilename;
                }
            };

            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("image", fileResource)
                    .contentType(resolveContentType(contentType));
            bodyBuilder.part("task_type", taskType);
            bodyBuilder.part("capture_id", captureId);
            if (cropCode != null) {
                bodyBuilder.part("crop_code", cropCode);
            }

            JsonNode rawResponse = webClientBuilder.baseUrl(aiBaseUrl).build()
                    .post()
                    .uri(aiPredictPath)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofMillis(Math.max(aiTimeoutMs, 1000)))
                    .onErrorMap(TimeoutException.class,
                            ex -> new AppException(HttpStatus.GATEWAY_TIMEOUT, "ai server timeout"))
                    .onErrorMap(WebClientRequestException.class,
                            ex -> new AppException(HttpStatus.SERVICE_UNAVAILABLE, "ai server is unavailable"))
                    .onErrorMap(WebClientResponseException.class,
                            ex -> new AppException(HttpStatus.BAD_GATEWAY,
                                    "ai server error: " + ex.getStatusCode().value()))
                    .block();

            if (rawResponse == null) {
                throw new AppException(HttpStatus.BAD_GATEWAY, "empty response from ai server");
            }

            AiPredictResponse parsed = objectMapper.treeToValue(rawResponse, AiPredictResponse.class);
            return normalizeAiResponse(parsed, rawResponse, taskType);
        } catch (JsonProcessingException e) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "invalid ai response format");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "failed to call ai server");
        }
    }

    private AiPredictResponse normalizeAiResponse(AiPredictResponse parsed, JsonNode raw, String requestedTaskType) {
        String disease = firstNonBlank(
                parsed.disease(),
                parsed.label(),
                textValue(raw, "predictions"),
                "unknown"
        );

        String label = firstNonBlank(parsed.label(), textValue(raw, "label"), disease);

        BigDecimal confidence = firstNonNull(
                parsed.confidence(),
                decimalValue(raw, "confidence"),
                decimalValue(raw, "score"),
                decimalValue(raw, "probability"),
                top1Probability(parsed.top3())
        );
        if (confidence == null) {
            confidence = BigDecimal.ZERO;
        }

        String abnormalReason = firstNonBlank(parsed.abnormalReason(), textValue(raw, "abnormal_reason"));
        Boolean abnormalFlag = booleanValue(raw, "is_abnormal");
        boolean abnormal = abnormalFlag != null ? abnormalFlag : inferAbnormal(disease, abnormalReason);

        Boolean unknownFlag = booleanValue(raw, "is_unknown");
        boolean unknown = unknownFlag != null ? unknownFlag : "unknown".equalsIgnoreCase(disease);

        String modelName = firstNonBlank(parsed.modelName(), textValue(raw, "model_name"), "external-model");
        String modelVersion = firstNonBlank(parsed.modelVersion(), textValue(raw, "model_version"), "unknown");
        String taskType = firstNonBlank(parsed.taskType(), textValue(raw, "task_type"), requestedTaskType);

        List<Object> bboxJson = parsed.bboxJson();
        if (bboxJson == null || bboxJson.isEmpty()) {
            bboxJson = listValue(raw, "bbox_json", "bbox");
        }

        OffsetDateTime inferredAt = parsed.inferredAt() == null
                ? OffsetDateTime.now(ZoneOffset.UTC)
                : parsed.inferredAt();

        Integer leafCount = firstNonNull(parsed.leafCount(), intValue(raw, "leaf_count"), intValue(raw, "leafCount"));
        Integer fruitCount = firstNonNull(parsed.fruitCount(), intValue(raw, "fruit_count"), intValue(raw, "fruitCount"));
        BigDecimal sizeCm = firstNonNull(parsed.sizeCm(), decimalValue(raw, "size_cm"), decimalValue(raw, "sizeCm"));
        String summary = firstNonBlank(parsed.summary(), textValue(raw, "summary"));
        List<BigDecimal> topConfidences = parsed.topConfidences();
        if (topConfidences == null || topConfidences.isEmpty()) {
            topConfidences = decimalListValue(raw, "topConfidences", "top_confidences");
        }
        BigDecimal sizePxTotal = firstNonNull(
                parsed.sizePxTotal(),
                decimalValue(raw, "sizePxTotal"),
                decimalValue(raw, "size_px_total")
        );

        return new AiPredictResponse(
                disease,
                confidence,
                unknown,
                abnormal,
                abnormalReason,
                modelVersion,
                modelName,
                taskType,
                label,
                bboxJson,
                inferredAt,
                parsed.top3(),
                leafCount,
                fruitCount,
                sizeCm,
                summary,
                topConfidences,
                sizePxTotal
        );
    }

    private boolean inferAbnormal(String disease, String abnormalReason) {
        if (abnormalReason != null && !abnormalReason.isBlank()) {
            return true;
        }
        return !isHealthyLike(disease);
    }

    private boolean isHealthyLike(String disease) {
        if (disease == null) {
            return true;
        }
        String normalized = disease.trim().toLowerCase(Locale.ROOT);
        return "00".equals(normalized)
                || "healthy".equals(normalized)
                || "normal".equals(normalized)
                || "unknown".equals(normalized);
    }

    private int toPercent(BigDecimal confidence) {
        if (confidence == null) {
            return 0;
        }
        BigDecimal multiplied = confidence.multiply(BigDecimal.valueOf(100));
        return multiplied.setScale(0, java.math.RoundingMode.HALF_UP).intValue();
    }

    private String normalizeDiseaseKey(String disease, String label) {
        String key = firstNonBlank(disease, label, "unknown");
        String normalized = key.toLowerCase(Locale.ROOT);
        return DISEASE_CODE_ALIASES.getOrDefault(normalized, normalized);
    }

    private String uploadUploadedImage(byte[] imageBytes, String originalFilename, String contentType) {
        try {
            String uploadedUrl = awsS3Service.upload(imageBytes, originalFilename, contentType, "captures");
            if (uploadedUrl != null && !uploadedUrl.isBlank()) {
                return uploadedUrl;
            }
        } catch (RuntimeException ignored) {
            // S3 설정이 없는 로컬 환경에서는 기존 로컬 저장 경로로 fallback.
        }
        return saveCaptureImage(imageBytes, originalFilename);
    }

    private List<String> guideList(DiseaseInfo info, DiseaseGuideType guideType) {
        if (info == null || info.getGuides() == null) {
            return List.of();
        }
        return info.getGuides().stream()
                .filter(guide -> guide != null && guideType.equals(guide.getGuideType()))
                .sorted((a, b) -> Integer.compare(
                        a.getSortOrder() == null ? Integer.MAX_VALUE : a.getSortOrder(),
                        b.getSortOrder() == null ? Integer.MAX_VALUE : b.getSortOrder()
                ))
                .map(DiseaseGuide::getContent)
                .map(this::safeText)
                .filter(value -> value != null)
                .collect(Collectors.toList());
    }

    private String safeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal top1Probability(List<AiPredictResponse.TopKItem> top3) {
        if (top3 == null || top3.isEmpty() || top3.get(0) == null) {
            return null;
        }
        return top3.get(0).p();
    }

    private String textValue(JsonNode raw, String key) {
        JsonNode node = raw.get(key);
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText(null);
        return value == null || value.isBlank() ? null : value;
    }

    private BigDecimal decimalValue(JsonNode raw, String key) {
        JsonNode node = raw.get(key);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isTextual()) {
            try {
                return new BigDecimal(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Integer intValue(JsonNode raw, String key) {
        JsonNode node = raw.get(key);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isInt() || node.isLong()) {
            return node.asInt();
        }
        if (node.isTextual()) {
            try {
                return Integer.parseInt(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Boolean booleanValue(JsonNode raw, String key) {
        JsonNode node = raw.get(key);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isTextual()) {
            String value = node.asText().trim();
            if ("true".equalsIgnoreCase(value)) {
                return true;
            }
            if ("false".equalsIgnoreCase(value)) {
                return false;
            }
        }
        return null;
    }

    private List<Object> listValue(JsonNode raw, String primaryKey, String fallbackKey) {
        JsonNode node = raw.get(primaryKey);
        if (node == null || node.isNull()) {
            node = raw.get(fallbackKey);
        }
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            List<Object> converted = new ArrayList<>();
            for (JsonNode item : node) {
                converted.add(objectMapper.convertValue(item, Object.class));
            }
            return converted;
        }
        return null;
    }

    private List<BigDecimal> decimalListValue(JsonNode raw, String primaryKey, String fallbackKey) {
        JsonNode node = raw.get(primaryKey);
        if (node == null || node.isNull()) {
            node = raw.get(fallbackKey);
        }
        if (node == null || node.isNull() || !node.isArray()) {
            return null;
        }

        List<BigDecimal> values = new ArrayList<>();
        for (JsonNode item : node) {
            if (item == null || item.isNull()) {
                continue;
            }
            if (item.isNumber()) {
                values.add(item.decimalValue());
                continue;
            }
            if (item.isTextual()) {
                try {
                    values.add(new BigDecimal(item.asText().trim()));
                } catch (NumberFormatException ignored) {
                    // Skip invalid item.
                }
            }
        }
        return values;
    }

    private int leafCountByThreshold(List<BigDecimal> topConfidences, BigDecimal threshold) {
        if (topConfidences == null || topConfidences.isEmpty()) {
            return -1;
        }

        int count = 0;
        for (BigDecimal confidence : topConfidences) {
            if (confidence != null && confidence.compareTo(threshold) >= 0) {
                count++;
            }
        }
        return count;
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String normalizeTaskType(String taskType) {
        if (taskType == null || taskType.isBlank()) {
            return TASK_DISEASE_CLASSIFICATION;
        }
        return taskType.trim().toUpperCase(Locale.ROOT);
    }

    private void validateTaskInputs(String taskType, Integer cropCode) {
        if ((TASK_GROWTH_MEASUREMENT.equals(taskType) || TASK_DISEASE_CLASSIFICATION.equals(taskType)) && cropCode == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "cropCode is required for " + taskType);
        }
    }

    private Integer parseCropCode(Crops crop) {
        if (crop == null || crop.getCropCode() == null || crop.getCropCode().isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "cropCode is not configured for crop");
        }
        try {
            return Integer.valueOf(crop.getCropCode().trim());
        } catch (NumberFormatException e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "invalid cropCode configured for crop");
        }
    }

    private record PreparedInferenceContext(
            Crops crop,
            OffsetDateTime capturedAt,
            ImageCapture uploadedImage,
            AiPredictResponse aiResponse
    ) {
    }

    private String saveCaptureImage(byte[] imageBytes, String originalFilename) {
        String captureDirPath = uploadDir.endsWith("/") ? uploadDir + "captures/" : uploadDir + "/captures/";
        File captureDir = new File(captureDirPath);
        if (!captureDir.exists() && !captureDir.mkdirs()) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to create capture directory");
        }

        String originalName = originalFilename == null ? "capture.jpg" : originalFilename;
        String extension = extractExtension(originalName);
        String savedName = UUID.randomUUID() + extension;
        File target = new File(captureDir, savedName);

        try {
            java.nio.file.Files.write(target.toPath(), imageBytes);
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to save capture image");
        }
        return target.getAbsolutePath();
    }

    private byte[] readUploadedImage(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "failed to read uploaded image");
        }
    }

    private MediaType resolveContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception ignored) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String extractExtension(String name) {
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) {
            return ".jpg";
        }
        return name.substring(idx);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen);
    }
}
