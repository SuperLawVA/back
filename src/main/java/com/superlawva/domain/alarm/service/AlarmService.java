package com.superlawva.domain.alarm.service;

import com.superlawva.domain.alarm.dto.AlarmDTO;
import com.superlawva.domain.alarm.dto.AlarmRequestDTO;
import com.superlawva.domain.alarm.dto.AlarmResponseDTO;
import com.superlawva.domain.alarm.dto.AlarmStatsDTO;
import com.superlawva.domain.alarm.entity.AlarmEntity;
import com.superlawva.domain.alarm.entity.AlarmType;
import com.superlawva.domain.alarm.repository.AlarmRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmService {
    private final AlarmRepository alarmRepository;
    private final ContractAlarmService contractAlarmService;
    private final ObjectMapper objectMapper;

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

    public List<AlarmResponseDTO> getUnreadAlarms(Long userId) {
        return alarmRepository.findByUserIdAndIsReadFalseAndDeletedAtIsNull(userId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long alarmId) {
        alarmRepository.findById(alarmId).ifPresent(alarm -> {
            alarm.setRead(true);
            alarmRepository.save(alarm);
        });
    }

    @Transactional
    public void deleteAlarm(Long alarmId) {
        alarmRepository.findById(alarmId).ifPresent(alarm -> {
            alarm.setDeletedAt(LocalDateTime.now());
            alarmRepository.save(alarm);
        });
    }

    @Transactional
    public void generateAlarmsForContract(AlarmDTO contract) {
        generateContractEndAlarms(contract);
        generatePaymentAlarms(contract);
        if (contract.getPayment().getMonthlyRent() != null && contract.getPayment().getMonthlyRent() > 0) {
            generateMonthlyRentAlarms(contract);
        }
    }

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

    private void generateMonthlyRentAlarms(AlarmDTO contract) {
        String contractId = contract.getId();
        Long userId = contract.getUserId();
        AlarmDTO.PaymentInfo payment = contract.getPayment();

        int rentDay = parseRentDay(payment.getMonthlyRentDate());
        if (rentDay > 0) {
            LocalDateTime current = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime contractEnd = contract.getDates().getEndDate().atStartOfDay();

            while (current.isBefore(contractEnd)) {
                LocalDateTime rentDate = current.withDayOfMonth(Math.min(rentDay, current.toLocalDate().lengthOfMonth()));
                LocalDateTime alarmDate = rentDate.minusWeeks(1);

                if (alarmDate.isAfter(LocalDateTime.now())) {
                    createAlarmIfNotExists(contractId, userId, AlarmType.월세납부, alarmDate,
                            createPaymentExtraInfo("amount", payment.getMonthlyRent(), "rent_date", rentDate.toLocalDate().toString()));
                }
                current = current.plusMonths(1);
            }
        }
    }

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

    private String createExtraInfo(String key, String value) {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put(key, value);
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            return "{}";
        }
    }

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

    private LocalDateTime parsePaymentDate(String dateStr) {
        if (dateStr != null && dateStr.contains("년") && dateStr.contains("월") && dateStr.contains("일")) {
            String[] parts = dateStr.replace("년", "-").replace("월", "-").replace("일", "").split("-");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                return LocalDateTime.of(year, month, day, 9, 0);
            }
        }
        throw new IllegalArgumentException("Invalid date format: " + dateStr);
    }

    private int parseRentDay(String rentDateStr) {
        if (rentDateStr == null) return -1;
        if (rentDateStr.equals("말일")) return 31;
        if (rentDateStr.endsWith("일")) {
            try {
                return Integer.parseInt(rentDateStr.replace("일", ""));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

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

    public AlarmStatsDTO getAlarmStats(Long userId) {
        List<AlarmEntity> allAlarms = alarmRepository.findByUserIdAndIsReadFalseAndDeletedAtIsNull(userId);
        
        return AlarmStatsDTO.builder()
                .userId(userId)
                .totalAlarms(allAlarms.size())
                .unreadAlarms((int) allAlarms.stream().filter(alarm -> !alarm.isRead()).count())
                .urgentAlarms((int) allAlarms.stream().filter(alarm -> 
                    alarm.getAlarmType() == AlarmType.종료예정 || 
                    alarm.getAlarmType() == AlarmType.중도금납부 || 
                    alarm.getAlarmType() == AlarmType.잔금납부).count())
                .todayAlarms((int) allAlarms.stream().filter(alarm -> 
                    alarm.getCreatedAt().toLocalDate().equals(LocalDateTime.now().toLocalDate())).count())
                .weekAlarms((int) allAlarms.stream().filter(alarm -> 
                    alarm.getCreatedAt().isAfter(LocalDateTime.now().minusWeeks(1))).count())
                .monthAlarms((int) allAlarms.stream().filter(alarm -> 
                    alarm.getCreatedAt().isAfter(LocalDateTime.now().minusMonths(1))).count())
                .build();
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        List<AlarmEntity> unreadAlarms = alarmRepository.findByUserIdAndIsReadFalseAndDeletedAtIsNull(userId);
        for (AlarmEntity alarm : unreadAlarms) {
            alarm.setRead(true);
        }
        alarmRepository.saveAll(unreadAlarms);
        return unreadAlarms.size();
    }
} 