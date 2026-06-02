package com.example.practice.service.device;

import com.example.practice.dto.Device.IrrigationCommand;
import com.example.practice.dto.Device.IrrigationRequest;
import com.example.practice.dto.Device.IrrigationResponse;
import com.example.practice.entity.device.CommandStatus;
import com.example.practice.entity.device.CommandType;
import com.example.practice.entity.device.Device;
import com.example.practice.entity.device.DeviceCommand;
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
public class IrrigationService {

    private final DeviceCommandRepository commandRepository;
    private final DeviceRepository deviceRepository;

    /**
     * 수동 관수 명령 등록
     * POST /api/irrigation
     * { "deviceId": 1, "command": "PUMP_ON", "durationSeconds": 10 }
     * { "deviceId": 1, "command": "PUMP_OFF" }
     */
    @Transactional
    public IrrigationResponse createCommand(IrrigationRequest request) {
        request.validate();

        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("등록된 디바이스가 없습니다."));

        CommandType commandType = (request.getCommand() == IrrigationCommand.PUMP_ON)
                ? CommandType.PUMP_ON
                : CommandType.PUMP_OFF;

        DeviceCommand cmd;
        if (commandType == CommandType.PUMP_ON) {
            cmd = DeviceCommand.createWithDuration(
                    device.getDeviceId(),
                    commandType,
                    request.getDurationSeconds()
            );
        } else {
            cmd = DeviceCommand.create(
                    device.getDeviceId(),
                    commandType
            );
        }

        commandRepository.save(cmd);

        log.info("[관수] deviceId={} commandType={} durationSeconds={}",
                device.getDeviceId(), commandType, request.getDurationSeconds());

        return IrrigationResponse.from(cmd);
    }

    /**
     * 라즈베리파이 → PENDING 관수 명령 조회
     * GET /api/irrigation/pending?deviceId=1
     */
    public List<IrrigationResponse> getPendingCommands(Long deviceId) {
        return commandRepository
                .findAllByDeviceIdAndStatusOrderByCreatedAtAsc(deviceId, CommandStatus.PENDING)
                .stream()
                .filter(cmd -> cmd.getCommandType() == CommandType.PUMP_ON
                        || cmd.getCommandType() == CommandType.PUMP_OFF)
                .map(IrrigationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 라즈베리파이 → 명령 실행 완료 보고
     * PATCH /api/irrigation/{commandId}/ack?deviceId=1&success=true
     */
    @Transactional
    public IrrigationResponse acknowledge(Long commandId, Long deviceId, boolean success) {
        DeviceCommand cmd = commandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 명령입니다. commandId=" + commandId));

        if (!cmd.getDeviceId().equals(deviceId)) {
            throw new IllegalArgumentException("해당 장치의 명령이 아닙니다.");
        }

        if (!cmd.isPending()) {
            throw new IllegalStateException("이미 처리된 명령입니다.");
        }

        if (success) {
            cmd.markExecuted();
            log.info("[관수] commandId={} 실행 완료", commandId);
        } else {
            cmd.markFailed();
            log.warn("[관수] commandId={} 실행 실패", commandId);
        }

        return IrrigationResponse.from(cmd);
    }
}