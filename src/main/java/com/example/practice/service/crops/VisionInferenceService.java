package com.example.practice.service.crops;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.crops.AiPredictResponse;
import com.example.practice.dto.crops.VisionInferenceResponse;
import com.example.practice.entity.crops.Crops;
import com.example.practice.entity.crops.GrowthMeasurement;
import com.example.practice.entity.crops.ImageCapture;
import com.example.practice.entity.crops.VisionInference;
import com.example.practice.repository.crops.CropsRepository;
import com.example.practice.repository.crops.GrowthMeasurementRepository;
import com.example.practice.repository.crops.ImageCaptureRepository;
import com.example.practice.repository.crops.VisionInferenceRepository;
import com.example.practice.repository.farm.FarmMemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VisionInferenceService {

    private final FarmMemberRepository farmMemberRepository;
    private final CropsRepository cropsRepository;
    private final ImageCaptureRepository imageCaptureRepository;
    private final VisionInferenceRepository visionInferenceRepository;
    private final GrowthMeasurementRepository growthMeasurementRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${ai.inference.base-url:http://localhost:8001}")
    private String aiBaseUrl;

    @Value("${ai.inference.predict-path:/predict}")
    private String aiPredictPath;

    @Transactional
    public VisionInferenceResponse inferAndSave(
            Long farmId,
            Long cropsId,
            Long userId,
            MultipartFile image,
            Long cameraId,
            String taskType,
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
        ImageCapture capture = new ImageCapture();
        capture.setCapturedAt(capturedAt);
        capture.setCameraId(cameraId);
        capture.setImagePath(saveCaptureImage(imageBytes, originalFilename));
        capture = imageCaptureRepository.save(capture);

        String requestedTaskType = (taskType == null || taskType.isBlank()) ? "DISEASE_CLASSIFICATION" : taskType;
        AiPredictResponse aiResponse = callAiServer(imageBytes, originalFilename, contentType, capture.getCaptureId(), requestedTaskType);

        VisionInference inference = new VisionInference();
        inference.setCapture(capture);
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
        inference = visionInferenceRepository.save(inference);

        GrowthMeasurement measurement = new GrowthMeasurement();
        measurement.setCrops(crop);
        measurement.setMeasuredAt(capturedAt);
        measurement.setLeafCount(aiResponse.leafCount());
        measurement.setFruitCount(aiResponse.fruitCount());
        measurement.setLeafArea(aiResponse.sizeCm());
        measurement.setAiConfidence(aiResponse.confidence());
        measurement.setAiSummary(aiResponse.summary());
        measurement.setAiRawJson(truncate(writeJson(aiResponse), 255));
        measurement.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        measurement.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        growthMeasurementRepository.save(measurement);

        return new VisionInferenceResponse(
                capture.getCaptureId(),
                inference.getInferenceId(),
                aiResponse.disease(),
                aiResponse.confidence(),
                aiResponse.isAbnormal(),
                aiResponse.abnormalReason(),
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
            String taskType
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

            AiPredictResponse response = webClientBuilder.baseUrl(aiBaseUrl).build()
                    .post()
                    .uri(aiPredictPath)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(AiPredictResponse.class)
                    .block();

            if (response == null) {
                throw new AppException(HttpStatus.BAD_GATEWAY, "empty response from ai server");
            }
            return response;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "failed to call ai server");
        }
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
