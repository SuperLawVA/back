package com.superlawva.domain.ml.controller;

import com.superlawva.domain.ml.dto.ContractCreateRequest;
import com.superlawva.domain.ml.dto.ContractResponse;
import com.superlawva.domain.ml.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contract")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;

    @PostMapping("/create")
    @Operation(summary = "계약서 직접 작성 및 특약 생성", description = "userId, userQuery, articles로 계약서를 생성합니다.")
    public ResponseEntity<ContractResponse> createContract(@Valid @RequestBody ContractCreateRequest request) {
        return ResponseEntity.ok(contractService.createContract(request));
    }

      @PutMapping("/{id}")
    @Operation(summary = "계약서 수정", description = "ContractId로 계약서 내용 수정")
    public ResponseEntity<ContractResponse> updateContract(@PathVariable String id, @RequestBody ContractCreateRequest request) {
        return ResponseEntity.ok(contractService.updateContract(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "계약서 삭제", description = "ContractId로 계약서 삭제")
    public ResponseEntity<Map<String, String>> deleteContract(@PathVariable String id) {
        contractService.deleteContract(id);
        return ResponseEntity.ok(Map.of("message", "계약서가 성공적으로 삭제되었습니다.", "id", id));
    }
} 