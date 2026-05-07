package com.example.practice.repository.crops;

import com.example.practice.entity.crops.VisionInference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface VisionInferenceRepository extends JpaRepository<VisionInference, Long> {

    @Query(value = """
        select vi.*
        from vision_inference vi
        join image_capture ic on vi.capture_id = ic.capture_id
        join camera c on ic.camera_id = c.camera_id
        join device d on c.device_id = d.device_id
        where d.farm_id = :farmId
          and vi.task_type = :taskType
          and ic.captured_at between :from and :to
          and vi.deleted_at is null
        order by coalesce(vi.inferred_at, vi.created_at, ic.captured_at) desc
        limit 1
        """, nativeQuery = true)
    Optional<VisionInference> findLatestByFarmIdAndTaskTypeAndCapturedAtBetween(
            @Param("farmId") Long farmId,
            @Param("taskType") String taskType,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    void deleteAllByCapture_CameraIdIn(List<Long> cameraIds);
}
