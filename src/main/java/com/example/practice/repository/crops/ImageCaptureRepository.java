package com.example.practice.repository.crops;

import com.example.practice.entity.crops.ImageCapture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageCaptureRepository extends JpaRepository<ImageCapture, Long> {

    void deleteAllByCameraIdIn(List<Long> cameraIds);

    @Query("""
            SELECT ic FROM ImageCapture ic
            JOIN Camera c ON ic.cameraId = c.cameraId
            JOIN Device d ON c.device.deviceId = d.deviceId
            WHERE d.farmId = :farmId
            ORDER BY ic.capturedAt DESC
            LIMIT 1
            """)
    Optional<ImageCapture> findLatestByFarmId(@Param("farmId") Long farmId);
}
