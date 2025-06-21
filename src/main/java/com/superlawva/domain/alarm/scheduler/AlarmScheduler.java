// AlarmScheduler.java - 수정됨: 중복 변수명 수정 및 오타 수정
package com.superlawva.domain.alarm.scheduler;

import com.superlawva.domain.alarm.dto.AlarmDTO;
import com.superlawva.domain.alarm.service.AlarmService;
import com.superlawva.domain.alarm.service.ContractAlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 알람 스케줄러 클래스
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmScheduler {

    private final AlarmService alarmService; // 알람 서비스
    private final ContractAlarmService contractAlarmService; // 계약 알람 서비스

    /**
     * 매일 오전 9시에 모든 활성 계약에 대한 알람 생성
     */
    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시 실행
    public void generateContractAlarms() {
        log.info("Starting daily alarm generation"); // 알람 생성 시작 로그

        try {
            List<AlarmDTO> activeContracts = contractAlarmService.getActiveContracts(); // 활성 계약 조회
            log.info("Found {} active contracts", activeContracts.size()); // 활성 계약 수 로그

            // 각 계약에 대해 알람 생성
            for (AlarmDTO contract : activeContracts) {
                try {
                    alarmService.generateAlarmsForContract(contract); // 계약별 알람 생성
                } catch (Exception e) {
                    log.error("Failed to generate alarms for contract: {}", contract.getId(), e); // 오류 로그
                }
            }

            log.info("Completed daily alarm generation"); // 알람 생성 완료 로그
        } catch (Exception e) {
            log.error("Failed to generate daily alarms", e); // 전체 오류 로그
        }
    }

    /**
     * 매시간 정각에 발송할 알람 확인 및 처리
     * (실제 알람 발송 로직은 별도 구현 필요)
     */
    @Scheduled(cron = "0 0 * * * *") // 매시간 정각 실행
    public void processPendingAlarms() {
        log.info("Processing pending alarms"); // 알람 처리 시작 로그
        // 실제 알람 발송 로직 구현
        // 예: 푸시 알림, 이메일, SMS 등
    }
}