package com.example.practice.repository.device;

import com.example.practice.entity.device.CommandStatus;
import com.example.practice.entity.device.DeviceCommand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface    DeviceCommandRepository extends JpaRepository<DeviceCommand, Long> {

    // 라즈베리파이가 폴링할 때 사용 - PENDING 명령 목록
    List<DeviceCommand> findAllByDeviceIdAndStatusOrderByCreatedAtAsc(
            Long deviceId, CommandStatus status);

    void deleteAllByDeviceIdIn(List<Long> deviceIds);
}
