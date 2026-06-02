package com.example.practice.service.crops;

import com.example.practice.common.config.OpenAiProperties;
import com.example.practice.dto.Device.SensorCheckDetail;
import com.example.practice.dto.Device.SensorValueSnapshot;
import com.example.practice.dto.crops.AiCropRecommendRequest;
import com.example.practice.dto.crops.AiCropRecommendResponse;
import com.example.practice.dto.crops.AiCropRecommendation;
import com.example.practice.dto.crops.CropRecommendResponse;
import com.example.practice.dto.crops.CropRecommendation;
import com.example.practice.dto.crops.Difficulty;
import com.example.practice.entity.device.EnvData;
import com.example.practice.entity.device.SensorType;
import com.example.practice.entity.environment.CropEnvironmentStandard;
import com.example.practice.repository.crops.CropEnvironmentStandardRepository;
import com.example.practice.repository.device.EnvDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CropRecommendService {

    private static final int MIN_MATCH_COUNT = 3;

    private final EnvDataRepository envDataRepository;
    private final CropEnvironmentStandardRepository cropEnvironmentStandardRepository;
    private final WebClient openAiWebClient;
    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;

    public CropRecommendResponse recommend(Long deviceId) {

        // ── 1. env_data에서 가장 최근 1건 조회 ───────────────────────
        EnvData latest = envDataRepository
                .findTopByDevice_DeviceIdOrderByCreatedAtDesc(deviceId)
                .orElseThrow(() -> new IllegalStateException(
                        "deviceId=" + deviceId + "에 환경 데이터가 없습니다."));

        // ── 2. EnvData → SensorType Map 변환 ─────────────────────────
        Map<SensorType, BigDecimal> latestValues = toSensorMap(latest);

        if (latestValues.isEmpty()) {
            throw new IllegalStateException("deviceId=" + deviceId + "에 측정 데이터가 없습니다.");
        }

        // ── 3. 현재 센서값 스냅샷 생성 (응답용) ──────────────────────
        SensorValueSnapshot snapshot = SensorValueSnapshot.from(latestValues);

        // ── 4. 모든 작물 환경 기준과 비교 ────────────────────────────
        List<CropEnvironmentStandard> allStandards =
                cropEnvironmentStandardRepository.findAll();

        List<CropRecommendation> recommendations = new ArrayList<>();
        for (CropEnvironmentStandard standard : allStandards) {
            evaluate(latestValues, standard).ifPresent(recommendations::add);
        }

        // ── 5. 매칭 수 내림차순 정렬 ─────────────────────────────────
        recommendations.sort(Comparator
                .comparingInt(CropRecommendation::getMatchedCount).reversed()
                .thenComparingDouble(CropRecommendation::getMatchRate).reversed());

        String message = recommendations.isEmpty()
                ? "현재 센서 환경에 적합한 작물이 없습니다."
                : recommendations.size() + "개의 작물이 현재 환경에 적합합니다.";

        return CropRecommendResponse.builder()
                .deviceId(deviceId)
                .sensorSnapshot(snapshot)
                .minMatchRequired(MIN_MATCH_COUNT)
                .recommendations(recommendations)
                .message(message)
                .build();
    }

    public AiCropRecommendResponse recommendByAi(AiCropRecommendRequest request) {
        try {
            AiCropRecommendResponse response = requestAiRecommendations(request);
            if (response.recommendations() == null || response.recommendations().isEmpty()) {
                return fallbackRecommendations(request);
            }
            return response;
        } catch (Exception e) {
            return fallbackRecommendations(request);
        }
    }

    private AiCropRecommendResponse requestAiRecommendations(AiCropRecommendRequest request) throws Exception {
        String model = openAiProperties.getModel();
        if (model == null || model.isBlank()) {
            model = "gpt-5-mini";
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("input", buildAiInput(request));
        requestBody.put("instructions", buildAiInstructions());
        requestBody.put("max_output_tokens", 900);

        Map<String, Object> reasoning = new HashMap<>();
        reasoning.put("effort", "low");
        requestBody.put("reasoning", reasoning);

        Map<String, Object> response = openAiWebClient.post()
                .uri("/v1/responses")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String outputText = stripJsonFence(extractOutputText(response));
        if (outputText == null || outputText.isBlank()) {
            throw new IllegalStateException("empty OpenAI recommendation response");
        }
        return objectMapper.readValue(outputText, AiCropRecommendResponse.class);
    }

    private String buildAiInput(AiCropRecommendRequest request) {
        return """
                location: %s
                place: %s
                careTime: %s
                purpose: %s
                harvestCycle: %s
                """.formatted(
                request.location(),
                request.place().getLabel(),
                request.careTime().getLabel(),
                request.purpose().getLabel(),
                request.harvestCycle().getLabel()
        );
    }

    private String buildAiInstructions() {
        return """
                너는 한국 가정/소규모 스마트팜 작물 추천 전문가다.
                사용자의 위치, 재배 장소, 관리 가능 시간, 재배 목적, 선호 수확 주기를 바탕으로 작물 3개를 추천해라.
                현재 한국의 계절과 지역 기후를 고려하되, 확실하지 않은 날씨 수치는 단정하지 마라.
                추천 작물은 사용자가 실제로 키우기 쉬운 채소, 허브, 과채류 중심으로 고른다.
                반드시 JSON만 반환하고, 마크다운 코드블록이나 설명 문장은 넣지 마라.
                JSON 형식:
                {
                  "recommendations": [
                    {
                      "rank": 1,
                      "cropName": "상추",
                      "reason": "관리 시간이 짧아도 잘 자라고 빠른 수확이 가능해요.",
                      "difficulty": "EASY",
                      "harvestCycle": "지속 수확",
                      "estimatedHarvestDays": 30
                    }
                  ],
                  "message": "현재 환경에 잘 맞는 작물을 찾았어요!"
                }
                difficulty는 EASY, MEDIUM, HARD 중 하나만 사용해라.
                reason은 한국어 한 문장으로 35자 이내로 작성해라.
                recommendations는 정확히 3개만 반환해라.
                """;
    }

    private AiCropRecommendResponse fallbackRecommendations(AiCropRecommendRequest request) {
        List<AiCropRecommendation> recommendations = switch (request.purpose()) {
            case HEALING_AESTHETIC -> List.of(
                    new AiCropRecommendation(1, "바질", "향이 좋아 실내 힐링용으로 잘 맞아요.", Difficulty.EASY, "지속 수확", 35),
                    new AiCropRecommendation(2, "로즈마리", "관상과 향을 함께 즐기기 좋아요.", Difficulty.MEDIUM, "장기 재배", 90),
                    new AiCropRecommendation(3, "민트", "생장이 빠르고 활용도가 높아요.", Difficulty.EASY, "지속 수확", 30)
            );
            case EDUCATION_OBSERVATION -> List.of(
                    new AiCropRecommendation(1, "방울토마토", "꽃과 열매 변화를 관찰하기 좋아요.", Difficulty.MEDIUM, "중기 재배", 70),
                    new AiCropRecommendation(2, "상추", "성장 속도가 빨라 관찰이 쉬워요.", Difficulty.EASY, "지속 수확", 30),
                    new AiCropRecommendation(3, "강낭콩", "싹과 줄기 성장이 뚜렷해요.", Difficulty.EASY, "중기 재배", 60)
            );
            case COST_EFFECTIVE -> List.of(
                    new AiCropRecommendation(1, "대파", "다시 자라 활용도가 높아요.", Difficulty.EASY, "지속 수확", 30),
                    new AiCropRecommendation(2, "상추", "구매 빈도가 높아 직접 키우기 좋아요.", Difficulty.EASY, "지속 수확", 30),
                    new AiCropRecommendation(3, "바질", "소량 구매가 비싸 직접 재배가 유리해요.", Difficulty.EASY, "지속 수확", 35)
            );
            default -> List.of(
                    new AiCropRecommendation(1, "상추", "관리가 쉽고 빠른 수확이 가능해요.", Difficulty.EASY, "지속 수확", 30),
                    new AiCropRecommendation(2, "방울토마토", "수확 재미가 있고 활용도가 높아요.", Difficulty.MEDIUM, "중기 재배", 70),
                    new AiCropRecommendation(3, "바질", "향기롭고 요리에 바로 쓰기 좋아요.", Difficulty.EASY, "지속 수확", 35)
            );
        };

        return new AiCropRecommendResponse(recommendations, "현재 환경에 잘 맞는 작물을 찾았어요!");
    }

    private String extractOutputText(Map<String, Object> response) {
        if (response == null) {
            return null;
        }

        Object output = response.get("output");
        if (!(output instanceof List<?> outputList)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Object outputItem : outputList) {
            if (!(outputItem instanceof Map<?, ?> outputMap)) {
                continue;
            }
            Object content = outputMap.get("content");
            if (!(content instanceof List<?> contentList)) {
                continue;
            }
            for (Object contentItem : contentList) {
                if (!(contentItem instanceof Map<?, ?> contentMap)) {
                    continue;
                }
                Object type = contentMap.get("type");
                Object text = contentMap.get("text");
                if ("output_text".equals(type) && text instanceof String textValue) {
                    if (!sb.isEmpty()) {
                        sb.append("\n");
                    }
                    sb.append(textValue);
                }
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private String stripJsonFence(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7).trim();
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3).trim();
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
        }
        return trimmed;
    }

    // ──────────────────────────────────────────────────────────────────
    // EnvData → SensorType별 값 Map 변환
    // ──────────────────────────────────────────────────────────────────
    private Map<SensorType, BigDecimal> toSensorMap(EnvData envData) {
        Map<SensorType, BigDecimal> map = new EnumMap<>(SensorType.class);

        if (envData.getTemp() != null)
            map.put(SensorType.SOIL_TEMPERATURE, envData.getTemp());
        if (envData.getSoilMoisture() != null)
            map.put(SensorType.SOIL_MOISTURE, envData.getSoilMoisture());
        if (envData.getEc() != null)
            map.put(SensorType.EC, envData.getEc());
        if (envData.getPh() != null)
            map.put(SensorType.PH, envData.getPh());
        if (envData.getCo2() != null)
            map.put(SensorType.CO2, envData.getCo2());
        if (envData.getIlluminance() != null)
            map.put(SensorType.ILLUMINANCE, envData.getIlluminance());

        return map;
    }

    // ── 이하 evaluate, addDetail 메서드는 기존 코드 그대로 ────────────

    private Optional<CropRecommendation> evaluate(Map<SensorType, BigDecimal> values,
                                                  CropEnvironmentStandard std) {
        List<SensorCheckDetail> details = new ArrayList<>();

        addDetail(details, SensorType.SOIL_TEMPERATURE, "토양 온도",  values, std.getMinTemp(),         std.getMaxTemp());
        addDetail(details, SensorType.PH,               "pH",        values, std.getMinPh(),           std.getMaxPh());
        addDetail(details, SensorType.SOIL_MOISTURE,    "토양 수분",  values, std.getMinSoilMoisture(), std.getMaxSoilMoisture());
        addDetail(details, SensorType.CO2,              "CO₂",       values, std.getMinCo2(),          std.getMaxCo2());
        addDetail(details, SensorType.EC,               "EC",        values, std.getMinEc(),           std.getMaxEc());
        addDetail(details, SensorType.ILLUMINANCE,      "조도",       values, std.getMinLight(),        std.getMaxLight());

        int totalChecked = details.size();
        int matchedCount = (int) details.stream().filter(SensorCheckDetail::isInRange).count();

        if (totalChecked == 0 || matchedCount < MIN_MATCH_COUNT) {
            return Optional.empty();
        }

        double matchRate = Math.round((double) matchedCount / totalChecked * 1000.0) / 10.0;

        return Optional.of(CropRecommendation.builder()
                .cropCode(std.getCropCode())
                .matchedCount(matchedCount)
                .totalChecked(totalChecked)
                .matchRate(matchRate)
                .details(details)
                .build());
    }

    private void addDetail(List<SensorCheckDetail> details,
                           SensorType type, String label,
                           Map<SensorType, BigDecimal> values,
                           Double min, Double max) {
        BigDecimal rawValue = values.get(type);
        if (rawValue == null) return;
        if (min == null && max == null) return;

        double value = rawValue.doubleValue();
        boolean inRange = (min == null || value >= min)
                && (max == null || value <= max);

        details.add(SensorCheckDetail.builder()
                .sensorType(type.name())
                .label(label)
                .unit(type.getDefaultUnit())
                .value(value)
                .min(min)
                .max(max)
                .inRange(inRange)
                .build());
    }
}
