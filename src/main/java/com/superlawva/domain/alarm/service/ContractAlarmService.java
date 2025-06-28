package com.superlawva.domain.alarm.service;

import com.superlawva.domain.alarm.dto.AlarmDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

/**
 * 계약 정보를 외부 API에서 불러오는 서비스
 * MongoDB에서 계약 데이터를 조회함
 */
@Service
public class ContractAlarmService {
    private final RestTemplate restTemplate;
    
    @Value("${api.servers.contract.base-url:${contract.api.base-url:http://localhost:8080}}")
    private String contractApiBaseUrl;

    public ContractAlarmService() {
        this.restTemplate = new RestTemplate();
    }

    public List<AlarmDTO> getActiveContracts() {
        String url = contractApiBaseUrl + "/api/contracts/active";
        ResponseEntity<List<AlarmDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AlarmDTO>>() {}
        );
        return response.getBody();
    }

    public Optional<AlarmDTO> getContractById(String contractId) {
        try {
            String url = contractApiBaseUrl + "/api/contracts/" + contractId;
            AlarmDTO contract = restTemplate.getForObject(url, AlarmDTO.class);
            return Optional.ofNullable(contract);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<AlarmDTO> getContractsByUserId(Long userId) {
        String url = contractApiBaseUrl + "/api/contracts/user/" + userId;
        ResponseEntity<List<AlarmDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AlarmDTO>>() {}
        );
        return response.getBody();
    }
} 