package com.example.practice.repository.device;

import com.example.practice.entity.device.SensorReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    Page<SensorReading> findAllBySensor_SensorIdOrderByMeasuredAtDesc(Long sensorId, Pageable pageable);

    List<SensorReading> findAllBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
            Long sensorId, OffsetDateTime from, OffsetDateTime to);

    /**
     * 특정 장치의 모든 센서 readings를 기간 조건으로 조회
     * (여러 센서 타입을 한번에 조회할 때 사용)
     */
    @Query("""
            SELECT sr FROM SensorReading sr
            JOIN FETCH sr.sensor s
            WHERE s.device.deviceId = :deviceId
              AND sr.measuredAt BETWEEN :from AND :to
            ORDER BY sr.measuredAt ASC
            """)
    List<SensorReading> findAllByDeviceIdAndPeriod(
            @Param("deviceId") Long deviceId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);
}
