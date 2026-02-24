package com.example.practice.dto.crops;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AiPredictResponse(
        String disease,
        BigDecimal confidence,
        @JsonProperty("is_unknown") boolean isUnknown,
        @JsonProperty("is_abnormal") boolean isAbnormal,
        @JsonProperty("abnormal_reason") String abnormalReason,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("model_name") String modelName,
        @JsonProperty("task_type") String taskType,
        String label,
        @JsonProperty("bbox_json") List<Object> bboxJson,
        @JsonProperty("inferred_at") OffsetDateTime inferredAt,
        List<TopKItem> top3,
        @JsonProperty("leaf_count") Integer leafCount,
        @JsonProperty("fruit_count") Integer fruitCount,
        @JsonProperty("size_cm") BigDecimal sizeCm,
        String summary
) {
    public record TopKItem(String label, BigDecimal p) {
    }
}
