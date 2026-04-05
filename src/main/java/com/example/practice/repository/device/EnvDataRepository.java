package com.example.practice.repository.device;

import com.example.practice.entity.device.EnvData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EnvDataRepository extends JpaRepository<EnvData, Long> {

    Page<EnvData> findAllByDevice_DeviceIdOrderByCreatedAtDesc(Long deviceId, Pageable pageable);
    Optional<EnvData> findTopByDevice_FarmIdOrderByMeasuredAtDesc(Long farmId);
    Optional<EnvData> findTopByOrderByCreatedAtDesc();

    Optional<EnvData> findTopByDevice_DeviceIdOrderByCreatedAtDesc(Long deviceId);

    @Query("""
            SELECT e FROM EnvData e
            WHERE e.device.farmId = :farmId
            ORDER BY e.createdAt DESC
            LIMIT 1
            """)
    Optional<EnvData> findLatestByFarmId(@Param("farmId") Long farmId);
}

