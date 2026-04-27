package com.example.practice.controller.device;

import com.example.practice.common.response.ApiResponse;
import com.example.practice.dto.Device.EnvDataResponse;
import com.example.practice.dto.Device.EnvDataSaveRequest;
import com.example.practice.service.device.EnvDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "env-data", description = "환경데이터 API")
@RestController
@RequestMapping("/api/env-data")
@RequiredArgsConstructor
public class EnvDataController {

    private final EnvDataService envDataService;

    /**
     * 라즈베리파이 묶음 데이터 수신
     * env_data 저장 + sensor_reading 저장 + lastSeenAt 갱신을 한 번에 처리
     */
    @Operation(summary = "묶음 데이터 수신")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EnvDataResponse> save(@Valid @RequestBody EnvDataSaveRequest request) {
        return ApiResponse.success(envDataService.save(request));
    }

    /** 단일 환경 데이터 조회 */
    @Operation(summary = "단일 환경 데이터 조회")
    @GetMapping("/{envDataId}")
    public ApiResponse<EnvDataResponse> getById(@PathVariable Long envDataId) {
        return ApiResponse.success(envDataService.getById(envDataId));
    }

    /** 장치별 환경 데이터 페이징 조회 */
    @Operation(summary = "장치별 환경 데이터 페이징 조회", description = """
            내가 원하는 데이터 page 수 설정 하고\
            페이지 안의 데이터 수 얼만큼 들어갈 것인지 사이즈 설정한 후\
            sort 안에 들어가는 파라미터 설명 "createdAt,desc"\t생성시간 내림차순\t최신 데이터 먼저 (기본값)
            "createdAt,asc"\t생성시간 오름차순\t오래된 데이터 먼저
            "measuredAt,desc"\t측정시간 내림차순\t최근 측정 먼저
            "temp,desc"\t온도 높은 순\t뜨거운 환경 먼저
            "humidity,asc"\t습도 낮은 순\t건조한 환경 먼저""")

    @GetMapping
    public ApiResponse<Page<EnvDataResponse>> getPage(
            @RequestParam Long deviceId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ApiResponse.success(envDataService.getPageByDeviceId(deviceId, pageable));
    }

    @GetMapping("/latest/{deviceId}")
    public ResponseEntity<Long> getLatestEnvDataId(@PathVariable Long deviceId) {
        return ResponseEntity.ok(envDataService.getLatestEnvDataIdByDeviceId(deviceId));
    }
}
