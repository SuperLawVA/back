package com.superlawva.domain.alarm.service;

import com.superlawva.domain.alarm.dto.AlarmDTO;
import com.superlawva.domain.ocr3.entity.ContractData;
import com.superlawva.domain.ocr3.repository.ContractDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 계약 정보를 외부 API가 아닌 내부 DB에서 직접 불러오는 서비스
 */
@Service
@RequiredArgsConstructor
public class ContractAlarmService {

    private final ContractDataRepository contractDataRepository;

    public List<AlarmDTO> getActiveContracts() {
        LocalDate today = LocalDate.now();
        String todayString = today.format(DateTimeFormatter.ISO_LOCAL_DATE); // yyyy-MM-dd 형식
        List<ContractData> activeContracts = contractDataRepository.findByDates_EndDateAfter(todayString);
        return activeContracts.stream()
                .map(this::toAlarmDTO)
                .collect(Collectors.toList());
    }

    public Optional<AlarmDTO> getContractById(String contractId) {
        return contractDataRepository.findById(Long.parseLong(contractId))
                .map(this::toAlarmDTO);
    }

    public List<AlarmDTO> getContractsByUserId(Long userId) {
        return contractDataRepository.findByUserId(String.valueOf(userId)).stream()
                .map(this::toAlarmDTO)
                .collect(Collectors.toList());
    }

    private AlarmDTO toAlarmDTO(ContractData entity) {
        if (entity == null) {
            return null;
        }

        AlarmDTO.ContractDates dates = AlarmDTO.ContractDates.builder()
                .contractDate(parseDate(entity.getDates().getContractDate()))
                .startDate(parseDate(entity.getDates().getStartDate()))
                .endDate(parseDate(entity.getDates().getEndDate()))
                .build();

        AlarmDTO.PaymentInfo payment = AlarmDTO.PaymentInfo.builder()
                .deposit(entity.getPayment().getDeposit())
                .downPayment(entity.getPayment().getDownPayment())
                .intermediatePayment(entity.getPayment().getIntermediatePayment())
                .intermediatePaymentDate(entity.getPayment().getIntermediatePaymentDate())
                .remainingBalance(entity.getPayment().getRemainingBalance())
                .remainingBalanceDate(entity.getPayment().getRemainingBalanceDate())
                .monthlyRent(entity.getPayment().getMonthlyRent())
                .monthlyRentDate(entity.getPayment().getMonthlyRentDate())
                .build();

        return AlarmDTO.builder()
                .id(entity.getId().toString())
                .userId(Long.parseLong(entity.getUserId()))
                .contractType(entity.getContractType())
                .dates(dates)
                .payment(payment)
                .build();
    }

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            // "yyyy.MM.dd", "yyyy-MM-dd", "yyyy/MM/dd" 등 다양한 포맷 지원 시도
            return LocalDate.parse(dateString.replace(".", "-").replace("/", "-"), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            // 파싱 실패 시 null 반환
            return null;
        }
    }
} 