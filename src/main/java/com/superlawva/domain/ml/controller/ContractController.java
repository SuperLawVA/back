package com.superlawva.domain.ml.controller;

import com.superlawva.domain.ml.dto.ContractCreateRequest;
import com.superlawva.domain.ml.dto.ContractResponse;
import com.superlawva.domain.ml.dto.ContractUpdateRequest;
import com.superlawva.domain.ml.service.ContractService;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.response.status.ErrorStatus;
import com.superlawva.global.response.status.SuccessStatus;
import com.superlawva.global.security.annotation.LoginUser;
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

    @PostMapping
    @Operation(
        summary = "📝 AI 계약서 생성", 
        description = """
        사용자의 요구사항을 기반으로 AI가 맞춤형 계약서를 생성합니다.
        
        ## 🎯 프론트엔드 구현 가이드
        
        ### 1. 기본 사용법
        ```javascript
        const createContract = async (contractData) => {
            const response = await fetch('/contract', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    title: "우리집 임대차 계약서",
                    contractType: "MONTHLY_RENT",
                    requirements: [
                        "반려동물 키우기 허용",
                        "소음 관련 제한사항 추가"
                    ],
                    propertyInfo: {
                        address: "서울시 강남구 테헤란로 123",
                        propertyType: "아파트",
                        area: 84.5,
                        floor: 5
                    }
                })
            });
            return await response.json();
        };
        ```
        
        ### 2. 계약서 유형 (contractType)
        - `JEONSE`: 전세 계약
        - `MONTHLY_RENT`: 월세 계약  
        - `SALE`: 매매 계약
        - `CUSTOM`: 커스텀 계약
        """
    )
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "JWT")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "✅ 계약서 생성 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "계약서 생성 성공 예시",
                    summary = "AI가 생성한 계약서 정보",
                    value = """
                    {
                        "success": true,
                        "data": {
                            "id": "123",
                            "title": "우리집 임대차 계약서",
                            "contractType": "MONTHLY_RENT",
                            "articles": [
                                "제1조 (목적) 본 계약은 임대차에 관한 사항을 정함",
                                "제2조 (임대료) 월 임대료는 50만원으로 한다"
                            ],
                            "specialTerms": [
                                "반려동물 사육이 허용됨",
                                "오후 10시 이후 소음 금지"
                            ],
                            "createdDate": "2024-01-15T10:30:00",
                            "status": "DRAFT"
                        },
                        "message": "계약서가 성공적으로 생성되었습니다."
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "❌ 잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "유효성 검증 실패 예시",
                    summary = "필수 필드 누락 또는 형식 오류",
                    value = """
                    {
                        "success": false,
                        "error": {
                            "code": "VALIDATION_ERROR",
                            "message": "입력값 검증에 실패했습니다.",
                            "details": {
                                "contractType": "계약서 유형은 필수입니다.",
                                "requirements": "최소 1개 이상의 요구사항을 입력해주세요."
                            }
                        }
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Valid @RequestBody ContractCreateRequest request) {
        log.info("📝 AI 계약서 생성 요청 - User ID: {}", userId);
        
        if (userId == null) {
            throw new BaseException(ErrorStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        
        // userId를 request에 설정
        request.setUserId(String.valueOf(userId));
        
        ContractResponse response = contractService.createContract(request);
        log.info("✅ 계약서 생성 완료 - Contract ID: {}", response.getId());
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

    @GetMapping("/my")
    @Operation(
        summary = "📋 내 계약서 목록 조회", 
        description = "현재 로그인한 사용자의 모든 계약서 목록을 조회합니다. JWT 토큰이 필요합니다."
    )
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "JWT")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "내 계약서 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "내 계약서 목록 예시",
                    summary = "로그인한 사용자의 계약서 목록",
                    value = "{\"success\": true, \"data\": [{\"id\": \"123\", \"userId\": \"user123\", \"contractType\": \"임대차\", \"articles\": [\"제1조 임대목적\"], \"createdDate\": \"2024-01-15T10:30:00\"}, {\"id\": \"124\", \"userId\": \"user123\", \"contractType\": \"월세\", \"articles\": [\"제1조 임대목적\"], \"createdDate\": \"2024-01-16T14:20:00\"}], \"status\": {\"httpStatus\": \"OK\", \"code\": \"200\", \"message\": \"내 계약서 목록을 성공적으로 조회했습니다.\"}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "인증 실패 예시",
                    summary = "JWT 토큰이 없거나 유효하지 않음",
                    value = "{\"success\": false, \"error\": {\"httpStatus\": \"UNAUTHORIZED\", \"code\": \"AUTH401\", \"message\": \"인증이 필요합니다.\"}, \"status\": {\"httpStatus\": \"UNAUTHORIZED\", \"code\": \"AUTH401\", \"message\": \"인증이 필요합니다.\"}}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getMyContracts(
            @Parameter(hidden = true) @LoginUser Long userId) {
        log.info("📋 내 계약서 목록 조회 요청 - User ID: {}", userId);
        
        if (userId == null) {
            log.warn("❌ 인증되지 않은 사용자의 계약서 조회 시도");
            throw new BaseException(ErrorStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        
        List<ContractResponse> response = contractService.getContractsByUserId(String.valueOf(userId));
        log.info("✅ 내 계약서 목록 조회 완료 - User ID: {}, Count: {}", userId, response.size());
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