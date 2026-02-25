package com.example.practice.entity.crops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "vision_inference")
public class VisionInference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inference_id")
    private Long inferenceId;

    @Column(name = "model_name", length = 255)
    private String modelName;

    @Column(name = "task_type", length = 255)
    private String taskType;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "confidence")
    private BigDecimal confidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bbox_json", columnDefinition = "jsonb")
    private List<Object> bboxJson;

    @Column(name = "inferred_at")
    private OffsetDateTime inferredAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "is_abnormal")
    private Boolean isAbnormal;

    @Column(name = "abnormal_reason", columnDefinition = "text")
    private String abnormalReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capture_id", nullable = false)
    private ImageCapture capture;
}
