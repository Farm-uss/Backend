package com.example.practice.service.device;

import com.example.practice.dto.Device.CommandRequest;
import com.example.practice.dto.Device.CommandResponse;
import com.example.practice.entity.device.CommandStatus;
import com.example.practice.entity.device.CommandType;
import com.example.practice.entity.device.DeviceCommand;
import com.example.practice.entity.device.Device;
import com.example.practice.repository.device.DeviceCommandRepository;
import com.example.practice.repository.device.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceCommandService {

    private final DeviceCommandRepository commandRepository;
    private final DeviceRepository deviceRepository;

    /**
     * 프론트 → LED 명령 등록
     * deviceId 없이 호출 - 등록된 첫 번째 device에 명령 전달
     * POST /api/led
     */
    @Transactional
    public CommandResponse createCommand(CommandType commandType) {
        Device device = deviceRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("등록된 디바이스가 없습니다."));

        DeviceCommand cmd = DeviceCommand.create(device.getDeviceId(), commandType);
        commandRepository.save(cmd);

        log.info("[LED] deviceId={} 명령 등록: {}", device.getDeviceId(), commandType);
        return CommandResponse.from(cmd);
    }

    @Transactional
    public CommandResponse createCommand(CommandRequest request) {
        return createCommand(request.getCommandType());
    }

    /**
     * 라즈베리파이 → PENDING 명령 조회 (3초마다 폴링)
     * GET /api/led/pending?deviceId=1
     */
    public List<CommandResponse> getPendingCommands(Long deviceId) {
        return commandRepository
                .findAllByDeviceIdAndStatusOrderByCreatedAtAsc(deviceId, CommandStatus.PENDING)
                .stream()
                .map(CommandResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 라즈베리파이 → 명령 실행 완료 보고
     * PATCH /api/led/{commandId}/ack?deviceId=1&success=true
     */
    @Transactional
    public CommandResponse acknowledge(Long commandId, Long deviceId, boolean success) {
        DeviceCommand cmd = commandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 명령입니다. commandId=" + commandId));

        if (!cmd.getDeviceId().equals(deviceId)) {
            throw new IllegalArgumentException("해당 장치의 명령이 아닙니다.");
        }

        if (success) {
            cmd.markExecuted();
            log.info("[LED] commandId={} 실행 완료", commandId);
        } else {
            cmd.markFailed();
            log.warn("[LED] commandId={} 실행 실패", commandId);
        }

        return CommandResponse.from(cmd);
    }
}