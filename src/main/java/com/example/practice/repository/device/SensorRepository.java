package com.example.practice.repository.device;

import com.example.practice.entity.device.Sensor;
import com.example.practice.entity.device.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    List<Sensor> findAllByDevice_DeviceId(Long deviceId);

    Optional<Sensor> findByDevice_DeviceIdAndSensorType(Long deviceId, SensorType sensorType);

    boolean existsByDevice_DeviceIdAndSensorType(Long deviceId, SensorType sensorType);



    /**
     * 특정 장치에 등록된 모든 센서를 readings까지 한 번에 fetch
     * (N+1 방지)
     */
    @Query("SELECT s FROM Sensor s LEFT JOIN FETCH s.readings WHERE s.device.deviceId = :deviceId")
    List<Sensor> findAllWithReadingsByDeviceId(@Param("deviceId") Long deviceId);
}
