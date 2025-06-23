package com.superlawva.domain.alarm.service;

import com.superlawva.domain.alarm.dto.AlarmDTO;
import com.superlawva.domain.alarm.dto.AlarmRequestDTO;
import com.superlawva.domain.alarm.dto.AlarmResponseDTO;
import com.superlawva.domain.alarm.entity.AlarmEntity;
import com.superlawva.domain.alarm.enums.AlarmType;
import com.superlawva.domain.alarm.repository.AlarmRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final ContractAlarmService contractAlarmService;
    private final ObjectMapper objectMapper;

    /**
     * 단일 알람 생성
     */
    @Transactional
    public void createAlarm(AlarmRequestDTO dto) {
        AlarmEntity alarmEntity = AlarmEntity.builder()
                .userId(dto.getUserId())
                .contractId(dto.getContractId())
                .alarmType(dto.getAlarmType())
                .note(dto.getAlarmType().getDefaultNote())
                .extraInfo(dto.getExtraInfo())
                .alarmDate(dto.getAlarmDate())
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .isSent(false)
                .build();
        alarmRepository.save(alarmEntity);
    }

    /**
     * 사용자 ID로 읽지 않은 알람 목록 조회
     */
    public List<AlarmResponseDTO> getUnreadAlarms(Long userId) {
        return alarmRepository.findByUserIdAndIsReadFalseAndDeletedAtIsNull(userId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 알람 읽음 처리
     */
    @Transactional
    public void markAsRead(Long alarmId) {
        alarmRepository.findById(alarmId).ifPresent(alarm -> {
            alarm.setRead(true);
            alarmRepository.save(alarm);
        });
    }

    /**
     * 알람 삭제 처리 (소프트 딜리트)
     */
    @Transactional
    public void deleteAlarm(Long alarmId) {
        alarmRepository.findById(alarmId).ifPresent(alarm -> {
            alarm.setDeletedAt(LocalDateTime.now());
            alarmRepository.save(alarm);
        });
    }

    /**
     * 계약 정보를 기반으로 다양한 알람 생성
     */
    @Transactional
    public void generateAlarmsForContract(AlarmDTO contract) {
        generateContractEndAlarms(contract);
        generatePaymentAlarms(contract);
        if (contract.getPayment().getMonthlyRent() != null && contract.getPayment().getMonthlyRent() > 0) {
            generateMonthlyRentAlarms(contract);
        }
    }

    /**
     * 계약 종료 관련 알람 생성
     */
    private void generateContractEndAlarms(AlarmDTO contract) {
        LocalDateTime endDateTime = contract.getDates().getEndDate().atStartOfDay();
        String contractId = contract.getId();
        Long userId = contract.getUserId();

        createAlarmIfNotExists(contractId, userId, AlarmType.묵시갱신_시작, endDateTime.minusMonths(6),
                createExtraInfo("contract_end_date", contract.getDates().getEndDate().toString()));

        createAlarmIfNotExists(contractId, userId, AlarmType.자동연장, endDateTime.minusMonths(2),
                createExtraInfo("contract_end_date", contract.getDates().getEndDate().toString()));

        createAlarmIfNotExists(contractId, userId, AlarmType.종료예정, endDateTime.minusMonths(1),
                createExtraInfo("contract_end_date", contract.getDates().getEndDate().toString()));
    }

    /**
     * 중도금/잔금 납부 알람 생성
     */
    private void generatePaymentAlarms(AlarmDTO contract) {
        String contractId = contract.getId();
        Long userId = contract.getUserId();
        AlarmDTO.PaymentInfo payment = contract.getPayment();

        if (payment.getIntermediatePayment() != null && payment.getIntermediatePayment() > 0) {
            try {
                LocalDateTime intermediateDate = parsePaymentDate(payment.getIntermediatePaymentDate());
                createAlarmIfNotExists(contractId, userId, AlarmType.중도금납부,
                        intermediateDate.minusWeeks(2),
                        createPaymentExtraInfo("amount", payment.getIntermediatePayment(), "date", payment.getIntermediatePaymentDate()));
            } catch (Exception e) {
                log.warn("Failed to parse intermediate payment date: {}", payment.getIntermediatePaymentDate());
            }
        }

        if (payment.getRemainingBalance() != null && payment.getRemainingBalance() > 0) {
            try {
                LocalDateTime remainingDate = parsePaymentDate(payment.getRemainingBalanceDate());
                createAlarmIfNotExists(contractId, userId, AlarmType.잔금납부,
                        remainingDate.minusWeeks(2),
                        createPaymentExtraInfo("amount", payment.getRemainingBalance(), "date", payment.getRemainingBalanceDate()));
            } catch (Exception e) {
                log.warn("Failed to parse remaining balance date: {}", payment.getRemainingBalanceDate());
            }
        }
    }

    /**
     * 월세 납부 알람 반복 생성
     */
    private void generateMonthlyRentAlarms(AlarmDTO contract) {
        String contractId = contract.getId();
        Long userId = contract.getUserId();
        AlarmDTO.PaymentInfo payment = contract.getPayment();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime contractEnd = contract.getDates().getEndDate().atStartOfDay();

        LocalDateTime current = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        while (current.isBefore(contractEnd)) {
            YearMonth ym = YearMonth.from(current);
            int rentDay = parseRentDay(payment.getMonthlyRentDate(), ym);

            if (rentDay > 0) {
                try {
                    LocalDateTime rentDate = current.withDayOfMonth(Math.min(rentDay, ym.lengthOfMonth()));
                    LocalDateTime alarmDate = rentDate.minusWeeks(1);

                    if (alarmDate.isAfter(now)) {
                        createAlarmIfNotExists(contractId, userId, AlarmType.월세납부, alarmDate,
                                createPaymentExtraInfo("amount", payment.getMonthlyRent(), "rent_date", rentDate.toLocalDate().toString()));
                    }
                } catch (DateTimeException e) {
                    log.warn("Invalid rent day [{}] for year-month [{}]: {}", rentDay, ym, e.getMessage());
                }
            }
            current = current.plusMonths(1);
        }
    }

    /**
     * 알람 중복 생성 방지를 위한 조건부 저장
     */
    private void createAlarmIfNotExists(String contractId, Long userId, AlarmType alarmType,
                                        LocalDateTime alarmDate, String extraInfo) {
        if (!alarmRepository.existsByContractIdAndAlarmTypeAndDeletedAtIsNull(contractId, alarmType)) {
            AlarmEntity alarm = AlarmEntity.builder()
                    .userId(userId)
                    .contractId(contractId)
                    .alarmType(alarmType)
                    .note(alarmType.getDefaultNote())
                    .extraInfo(extraInfo)
                    .alarmDate(alarmDate)
                    .createdAt(LocalDateTime.now())
                    .isRead(false)
                    .isSent(false)
                    .build();
            alarmRepository.save(alarm);
        }
    }

    /**
     * 단일 키-값 형태의 extraInfo 생성 (JSON 문자열 반환)
     */
    private String createExtraInfo(String key, String value) {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put(key, value);
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 납부 정보 기반 extraInfo 생성 (JSON 문자열 반환)
     */
    private String createPaymentExtraInfo(String amountKey, Long amount, String dateKey, String date) {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put(amountKey, amount);
            info.put(dateKey, date);
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * "2024년6월20일" 같은 문자열을 LocalDateTime으로 파싱
     */
    private LocalDateTime parsePaymentDate(String dateStr) {
        if (dateStr != null && dateStr.contains("년") && dateStr.contains("월") && dateStr.contains("일")) {
            String[] parts = dateStr.replace("년", "-").replace("월", "-").replace("일", "").split("-");
            if (parts.length == 3) {
                try {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    return LocalDateTime.of(year, month, day, 9, 0);
                } catch (Exception e) {
                    log.warn("날짜 파싱 실패 (내용: {}): {}", dateStr, e.getMessage());
                }
            }
        }
        log.warn("잘못된 날짜 형식: {}", dateStr);
        throw new IllegalArgumentException("Invalid date format: " + dateStr);
    }

    /**
     * "말일", "15일" 같은 문자열을 정확한 일(day)로 변환
     */
    private int parseRentDay(String rentDateStr, YearMonth ym) {
        if (rentDateStr == null) return -1;
        if (rentDateStr.equals("말일")) {
            return ym.lengthOfMonth();
        }
        if (rentDateStr.endsWith("일")) {
            try {
                return Integer.parseInt(rentDateStr.replace("일", ""));
            } catch (NumberFormatException e) {
                log.warn("월세 일자 파싱 실패: '{}'", rentDateStr);
                return -1;
            }
        }
        log.warn("지원하지 않는 월세 납부일 형식: '{}'", rentDateStr);
        return -1;
    }

    /**
     * AlarmEntity를 응답 DTO로 변환
     */
    private AlarmResponseDTO toResponseDTO(AlarmEntity alarm) {
        return AlarmResponseDTO.builder()
                .alarmId(alarm.getAlarmId())
                .contractId(alarm.getContractId())
                .alarmType(alarm.getAlarmType())
                .note(alarm.getNote())
                .extraInfo(alarm.getExtraInfo())
                .isRead(alarm.isRead())
                .alarmDate(alarm.getAlarmDate())
                .createdAt(alarm.getCreatedAt())
                .build();
    }
}
