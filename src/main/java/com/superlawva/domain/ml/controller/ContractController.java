package com.superlawva.domain.ml.controller;

import com.superlawva.domain.ml.dto.ContractCreateRequest;
import com.superlawva.domain.ml.dto.ContractResponse;
import com.superlawva.domain.ml.dto.ContractUpdateRequest;
import com.superlawva.domain.ml.service.ContractService;
import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.response.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/contract")
@RequiredArgsConstructor
@Tag(name = "계약서 관리", description = "계약서 생성, 조회, 수정, 삭제 API")
public class ContractController {
    private final ContractService contractService;

    @PostMapping("/create")
    @Operation(
        summary = "📝 계약서 생성", 
        description = "사용자 ID, 사용자 쿼리, 계약 조항을 입력하여 계약서를 생성합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "계약서 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "계약서 생성 성공 예시",
                    summary = "계약서가 성공적으로 생성됨",
                    value = "{\"success\": true, \"data\": {\"id\": \"123\", \"userId\": \"user123\", \"contractType\": \"임대차\", \"articles\": [\"제1조 임대목적\", \"제2조 임대기간\"], \"createdDate\": \"2024-01-15T10:30:00\", \"modifiedDate\": \"2024-01-15T10:30:00\"}, \"status\": {\"httpStatus\": \"CREATED\", \"code\": \"201\", \"message\": \"계약서가 성공적으로 생성되었습니다.\"}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "잘못된 요청 예시",
                    summary = "필수 필드 누락",
                    value = "{\"success\": false, \"error\": {\"httpStatus\": \"BAD_REQUEST\", \"code\": \"COMMON400\", \"message\": \"잘못된 요청입니다.\"}, \"status\": {\"httpStatus\": \"BAD_REQUEST\", \"code\": \"COMMON400\", \"message\": \"잘못된 요청입니다.\"}}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(
            @Valid @RequestBody ContractCreateRequest request) {
        log.info("📝 계약서 생성 요청 - User ID: {}", request.getUserId());
        
        ContractResponse response = contractService.createContract(request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "🔍 계약서 조회", 
        description = "계약서 ID로 특정 계약서의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "계약서 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "계약서 조회 성공 예시",
                    summary = "계약서 정보 조회됨",
                    value = "{\"success\": true, \"data\": {\"id\": \"123\", \"userId\": \"user123\", \"contractType\": \"임대차\", \"articles\": [\"제1조 임대목적\", \"제2조 임대기간\"], \"createdDate\": \"2024-01-15T10:30:00\", \"modifiedDate\": \"2024-01-15T10:30:00\"}, \"status\": {\"httpStatus\": \"OK\", \"code\": \"200\", \"message\": \"계약서를 성공적으로 조회했습니다.\"}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "계약서 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "계약서 없음 예시",
                    summary = "존재하지 않는 계약서",
                    value = "{\"success\": false, \"error\": {\"httpStatus\": \"NOT_FOUND\", \"code\": \"CONTRACT404\", \"message\": \"해당 계약서를 찾을 수 없습니다.\"}, \"status\": {\"httpStatus\": \"NOT_FOUND\", \"code\": \"CONTRACT404\", \"message\": \"해당 계약서를 찾을 수 없습니다.\"}}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<ContractResponse>> getContractById(
            @Parameter(description = "계약서 ID", example = "123") @PathVariable String id) {
        log.info("🔍 계약서 조회 요청 - Contract ID: {}", id);
        
        ContractResponse response = contractService.getContractById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "👤 사용자별 계약서 목록 조회", 
        description = "특정 사용자가 생성한 모든 계약서 목록을 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용자별 계약서 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "사용자별 계약서 목록 예시",
                    summary = "사용자의 계약서 목록",
                    value = "{\"success\": true, \"data\": [{\"id\": \"123\", \"userId\": \"user123\", \"contractType\": \"임대차\", \"articles\": [\"제1조 임대목적\"], \"createdDate\": \"2024-01-15T10:30:00\"}], \"status\": {\"httpStatus\": \"OK\", \"code\": \"200\", \"message\": \"계약서 목록을 성공적으로 조회했습니다.\"}}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getContractsByUserId(
            @Parameter(description = "사용자 ID", example = "user123") @PathVariable String userId) {
        log.info("👤 사용자별 계약서 조회 요청 - User ID: {}", userId);
        
        List<ContractResponse> response = contractService.getContractsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "📝 계약서 수정", 
        description = "계약서 ID로 계약서 내용을 수정합니다. 부분 수정이 가능합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "계약서 수정 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "계약서 수정 성공 예시",
                    summary = "계약서가 성공적으로 수정됨",
                    value = "{\"success\": true, \"data\": {\"id\": \"123\", \"userId\": \"user123\", \"contractType\": \"월세\", \"articles\": [\"제1조 임대목적 (수정)\", \"제2조 임대기간\"], \"createdDate\": \"2024-01-15T10:30:00\", \"modifiedDate\": \"2024-01-15T15:45:00\"}, \"status\": {\"httpStatus\": \"OK\", \"code\": \"200\", \"message\": \"계약서가 성공적으로 수정되었습니다.\"}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "계약서 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "계약서 없음 예시",
                    summary = "수정할 계약서가 존재하지 않음",
                    value = "{\"success\": false, \"error\": {\"httpStatus\": \"NOT_FOUND\", \"code\": \"CONTRACT404\", \"message\": \"해당 계약서를 찾을 수 없습니다.\"}, \"status\": {\"httpStatus\": \"NOT_FOUND\", \"code\": \"CONTRACT404\", \"message\": \"해당 계약서를 찾을 수 없습니다.\"}}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<ContractResponse>> updateContract(
            @Parameter(description = "계약서 ID", example = "123") @PathVariable String id,
            @Valid @RequestBody ContractUpdateRequest request) {
        log.info("📝 계약서 수정 요청 - Contract ID: {}", id);
        
        ContractResponse response = contractService.updateContract(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "🗑️ 계약서 삭제", 
        description = "계약서 ID로 계약서를 삭제합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "계약서 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "계약서 삭제 성공 예시",
                    summary = "계약서가 성공적으로 삭제됨",
                    value = "{\"success\": true, \"data\": \"계약서가 성공적으로 삭제되었습니다.\", \"status\": {\"httpStatus\": \"OK\", \"code\": \"200\", \"message\": \"계약서가 성공적으로 삭제되었습니다.\"}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "계약서 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "계약서 없음 예시",
                    summary = "삭제할 계약서가 존재하지 않음",
                    value = "{\"success\": false, \"error\": {\"httpStatus\": \"NOT_FOUND\", \"code\": \"CONTRACT404\", \"message\": \"해당 계약서를 찾을 수 없습니다.\"}, \"status\": {\"httpStatus\": \"NOT_FOUND\", \"code\": \"CONTRACT404\", \"message\": \"해당 계약서를 찾을 수 없습니다.\"}}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<String>> deleteContract(
            @Parameter(description = "계약서 ID", example = "123") @PathVariable String id) {
        log.info("🗑️ 계약서 삭제 요청 - Contract ID: {}", id);
        
        contractService.deleteContract(id);
        return ResponseEntity.ok(ApiResponse.success("계약서가 성공적으로 삭제되었습니다."));
    }
}