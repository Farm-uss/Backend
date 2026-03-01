package com.example.practice.dto.crops;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiPredictResponse(
        @JsonAlias({"predictions", "verdict"})
        String disease,
        @JsonAlias({"score", "probability", "p"})
        BigDecimal confidence,
        @JsonProperty("is_unknown") boolean isUnknown,
        @JsonProperty("is_abnormal") boolean isAbnormal,
        @JsonProperty("abnormal_reason") String abnormalReason,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("model_name") String modelName,
        @JsonProperty("task_type") String taskType,
        String label,
        @JsonAlias({"bbox"})
        @JsonProperty("bbox_json") List<Object> bboxJson,
        @JsonProperty("inferred_at") OffsetDateTime inferredAt,
        @JsonAlias({"top_k"})
        List<TopKItem> top3,
        @JsonAlias({"leafCount"})
        @JsonProperty("leaf_count") Integer leafCount,
        @JsonAlias({"fruitCount"})
        @JsonProperty("fruit_count") Integer fruitCount,
        @JsonAlias({"sizeCm", "leaf_area"})
        @JsonProperty("size_cm") BigDecimal sizeCm,
        String summary
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TopKItem(String label, BigDecimal p) {
    }
}
