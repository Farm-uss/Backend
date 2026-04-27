package com.example.practice.service.notification;

import com.example.practice.entity.device.EnvData;
import com.example.practice.repository.device.EnvDataRepository;
import com.example.practice.repository.farm.FarmRepository;
import com.example.practice.service.notification.AlertCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduler {

    private final FarmRepository farmRepository;
    private final EnvDataRepository envDataRepository;
    private final AlertCheckService alertCheckService;

    /**
     * 10분마다 실행.
     * 모든 농장의 최신 EnvData를 조회해 환경 기준과 비교 후 알림 생성.
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000)  // 이전 실행 완료 후 10분
    public void checkAllFarms() {
        log.info("[AlertScheduler] 환경 알림 정기 체크 시작");

        // 활성화된 농장 ID 목록 조회
        // Farm 엔티티에 isActive 같은 상태 필드가 있다면 조건 추가 권장
        List<Long> farmIds = farmRepository.findAll()
                .stream()
                .map(farm -> farm.getId())  // Farm 엔티티의 실제 ID getter로 변경
                .toList();

        int notifiedCount = 0;

        for (Long farmId : farmIds) {
            try {
                // 농장의 가장 최신 EnvData 1건 조회
                EnvData latest = envDataRepository.findLatestByFarmId(farmId)
                        .orElse(null);

                if (latest == null) {
                    log.debug("[AlertScheduler] farmId={} 데이터 없음 - 건너뜀", farmId);
                    continue;
                }

                alertCheckService.checkAndNotify(latest, farmId);
                notifiedCount++;

            } catch (Exception e) {
                // 한 농장 실패가 전체 배치에 영향 주지 않도록 격리
                log.error("[AlertScheduler] farmId={} 체크 실패: {}", farmId, e.getMessage());
            }
        }

        log.info("[AlertScheduler] 환경 알림 정기 체크 완료 - 처리 농장 수={}", notifiedCount);
    }
}