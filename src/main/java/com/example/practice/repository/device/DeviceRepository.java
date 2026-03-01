package com.example.practice.repository.device;

import com.example.practice.entity.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceUuid(String deviceUuid);

    List<Device> findAllByFarmId(Long farmId);

    boolean existsByDeviceUuid(String deviceUuid);
}
