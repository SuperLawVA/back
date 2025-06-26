package com.superlawva.domain.ml.service;

import com.superlawva.domain.ml.client.MLApiClient;
import com.superlawva.domain.ml.entity.MLAnalysisResult;
import com.superlawva.domain.ml.repository.MLAnalysisResultRepository;
import com.superlawva.domain.ocr3.entity.ContractData;
import com.superlawva.domain.ocr3.repository.ContractDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MLAnalysisService {

    private final MLApiClient mlApiClient;
    private final ContractDataRepository contractDataRepository;
    private final MLAnalysisResultRepository mlAnalysisResultRepository;
    private final ObjectMapper objectMapper;

    /**
     * 계약서 분석 및 결과 저장 (기존 메서드)
     */
    @Transactional
    public Map<String, Object> analyzeContract(String contractId, String userId) {
        log.info("🤖 계약서 분석 시작 - Contract ID: {}, User ID: {}", contractId, userId);

        try {
            // 1. MongoDB에서 계약서 데이터 조회
            ContractData contractData = contractDataRepository.findById(contractId)
                    .orElseThrow(() -> new RuntimeException("계약서를 찾을 수 없습니다: " + contractId));

            // 2. ML API 요청 데이터 구성 (contract_type 변환 포함)
            Map<String, Object> mlRequest = buildContractAnalysisRequest(contractData, userId);

            // 3. ML API 호출
            Map<String, Object> mlResponse = mlApiClient.analyzeContract(mlRequest);

            // 4. 분석 결과를 MongoDB analysis 컬렉션에 저장
            MLAnalysisResult savedAnalysis = saveAnalysisResult(contractData, mlResponse, userId);

            log.info("✅ 계약서 분석 완료 - Contract ID: {}, Analysis ID: {}", contractId, savedAnalysis.getId());

            // 5. 분석 결과 반환 (프론트엔드에서 바로 사용 가능)
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("contractId", contractId);
            result.put("analysisId", savedAnalysis.getId());
            result.put("userId", userId);
            result.put("analysisResult", savedAnalysis);
            result.put("mlResponse", mlResponse);
            result.put("timestamp", LocalDateTime.now(ZoneOffset.UTC));

            return result;

        } catch (Exception e) {
            log.error("❌ 계약서 분석 실패 - Contract ID: {}", contractId, e);

            // 실패 결과 반환
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("contractId", contractId);
            errorResult.put("userId", userId);
            errorResult.put("error", e.getMessage());
            errorResult.put("timestamp", LocalDateTime.now(ZoneOffset.UTC));

            return errorResult;
        }
    }

    /**
     * READ - 분석 ID로 개별 조회
     */
    public MLAnalysisResult getAnalysisById(String analysisId) {
        log.info("🔍 분석 결과 조회 - Analysis ID: {}", analysisId);

        Optional<MLAnalysisResult> result = mlAnalysisResultRepository.findById(analysisId);

        if (result.isPresent()) {
            log.info("✅ 분석 결과 조회 성공 - Analysis ID: {}", analysisId);
            return result.get();
        } else {
            log.warn("⚠️ 분석 결과를 찾을 수 없음 - Analysis ID: {}", analysisId);
            return null;
        }
    }

    /**
     * READ - 사용자별 분석 결과 전체 조회
     * 참고: userId는 raw_ml_response에서 추출해야 함
     */
    public List<MLAnalysisResult> getAnalysesByUserId(String userId) {
        log.info("👤 사용자별 분석 결과 조회 - User ID: {}", userId);

        // 모든 분석 결과를 가져와서 raw_ml_response에서 user_id로 필터링
        List<MLAnalysisResult> allResults = mlAnalysisResultRepository.findAll();
        List<MLAnalysisResult> filteredResults = allResults.stream()
                .filter(result -> {
                    String analysisUserId = getAnalysisUserId(result);
                    return analysisUserId.equals(userId);
                })
                .collect(Collectors.toList());

        log.info("✅ {}개의 분석 결과 조회됨 - User ID: {}", filteredResults.size(), userId);
        return filteredResults;
    }



    /**
     * DELETE - 분석 결과 삭제 (보안: userId 검증)
     */
    @Transactional
    public boolean deleteAnalysis(String analysisId, String userId) {
        log.info("🗑️ 분석 결과 삭제 시도 - Analysis ID: {}, User ID: {}", analysisId, userId);

        try {
            Optional<MLAnalysisResult> analysisOpt = mlAnalysisResultRepository.findById(analysisId);

            if (analysisOpt.isPresent()) {
                MLAnalysisResult analysis = analysisOpt.get();

                // 보안 검증: 요청한 사용자가 분석의 소유자인지 확인 (raw_ml_response에서 확인)
                String analysisUserId = getAnalysisUserId(analysis);
                if (!analysisUserId.equals(userId)) {
                    log.error("❌ 권한 없음 - 요청 User ID: {}, 분석 소유자 ID: {}", userId, analysisUserId);
                    throw new RuntimeException("해당 분석 결과를 삭제할 권한이 없습니다.");
                }

                mlAnalysisResultRepository.deleteById(analysisId);
                log.info("✅ 분석 결과 삭제 성공 - Analysis ID: {}", analysisId);
                return true;

            } else {
                log.warn("⚠️ 삭제할 분석 결과를 찾을 수 없음 - Analysis ID: {}", analysisId);
                return false;
            }
        } catch (Exception e) {
            log.error("❌ 분석 결과 삭제 실패", e);
            throw e;
        }
    }



    /**
     * ML 분석 결과를 MongoDB analysis 컬렉션에 저장 (간소화된 버전)
     */
    private MLAnalysisResult saveAnalysisResult(ContractData contractData, Map<String, Object> mlResponse, String userId) {
        try {
            log.info("💾 ML 분석 결과를 MongoDB analysis 컬렉션에 저장 시작");

            // MLAnalysisResult Entity 생성
            MLAnalysisResult analysisResult = new MLAnalysisResult();

            // 기본 정보 설정
            analysisResult.setContractId(contractData.get_id());
            analysisResult.setAnalysisType("CONTRACT_ANALYSIS");
            analysisResult.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
            analysisResult.setStatus("SUCCESS");

            // 원본 ML 응답 전체 저장 (모든 데이터 포함)
            analysisResult.setRawMlResponse(mlResponse);

            // MongoDB에 저장
            MLAnalysisResult savedResult = mlAnalysisResultRepository.save(analysisResult);

            log.info("✅ ML 분석 결과 저장 완료:");
            log.info("   🆔 저장된 ID: {}", savedResult.getId());
            log.info("   📋 Contract ID: {}", savedResult.getContractId());
            log.info("   📅 생성일: {}", savedResult.getCreatedDate());

            return savedResult;

        } catch (Exception e) {
            log.error("❌ ML 분석 결과 저장 실패", e);
            log.error("   📋 Contract ID: {}", contractData.get_id());
            log.error("   💬 오류 메시지: {}", e.getMessage());

            // 실패한 경우에도 기록을 남김
            try {
                MLAnalysisResult failedResult = new MLAnalysisResult();
                failedResult.setContractId(contractData.get_id());
                failedResult.setAnalysisType("CONTRACT_ANALYSIS");
                failedResult.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
                failedResult.setStatus("FAILED");
                failedResult.setErrorMessage(e.getMessage());
                failedResult.setRawMlResponse(mlResponse);

                return mlAnalysisResultRepository.save(failedResult);

            } catch (Exception saveError) {
                log.error("❌ 실패 기록 저장도 실패", saveError);
                throw saveError;
            }
        }
    }

    /**
     * 계약서 분석용 ML 요청 데이터 구성 (MongoDB BSON 타입을 JSON 호환 형식으로 변환)
     */
    private Map<String, Object> buildContractAnalysisRequest(ContractData contractData, String userId) {
        Map<String, Object> request = new HashMap<>();
        request.put("contract_id", contractData.get_id());
        request.put("user_id", userId);

        // ⭐ contract_type을 ML API가 인식할 수 있는 형식으로 변환
        String normalizedContractType = normalizeContractType(contractData.getContractType());
        request.put("contract_type", normalizedContractType);

        // 계약서 상세 정보 (MongoDB 타입을 ML API 호환 형식으로 변환)
        Map<String, Object> contractInfo = new HashMap<>();
        contractInfo.put("id", contractData.get_id());
        contractInfo.put("user_id", userId);
        contractInfo.put("contract_type", normalizedContractType);

        // Dates 변환
        contractInfo.put("dates", convertDates(contractData.getDates()));

        // Property 변환
        contractInfo.put("property", convertProperty(contractData.getProperty()));

        // Payment 변환
        contractInfo.put("payment", convertPayment(contractData.getPayment()));

        // Lessor/Lessee 변환
        contractInfo.put("lessor", convertParty(contractData.getLessor()));
        contractInfo.put("lessee", convertParty(contractData.getLessee()));

        // Brokers 변환
        contractInfo.put("broker1", convertBroker(contractData.getBroker1()));
        contractInfo.put("broker2", convertBroker(contractData.getBroker2()));

        // Articles/Agreements (이미 String 배열이므로 그대로 사용)
        contractInfo.put("articles", contractData.getArticles());
        contractInfo.put("agreements", contractData.getAgreements());

        // 추가 필드들
        contractInfo.put("generated", contractData.getGenerated());
        contractInfo.put("file_url", contractData.getFileUrl());

        // LocalDateTime을 문자열로 변환
        contractInfo.put("created_date", convertDateTime(contractData.getCreatedDate()));
        contractInfo.put("modified_date", convertDateTime(contractData.getModifiedDate()));

        // Contract Metadata 변환
        contractInfo.put("contract_metadata", convertContractMetadata(contractData.getContractMetadata()));

        request.put("contract_data", contractInfo);
        request.put("debug_mode", false);

        log.info("📤 ML API 요청 데이터 구성 완료 - Contract Type: {} -> {}",
                contractData.getContractType(), normalizedContractType);

        return request;
    }

    /**
     * Dates 객체를 ML API 호환 형식으로 변환
     */
    private Map<String, Object> convertDates(ContractData.Dates dates) {
        if (dates == null) return null;

        Map<String, Object> result = new HashMap<>();
        result.put("contract_date", dates.getContractDate());
        result.put("start_date", dates.getStartDate());
        result.put("end_date", dates.getEndDate());
        return result;
    }

    /**
     * Property 객체를 ML API 호환 형식으로 변환
     */
    private Map<String, Object> convertProperty(ContractData.Property property) {
        if (property == null) return null;

        Map<String, Object> result = new HashMap<>();
        result.put("address", property.getAddress());
        result.put("detail_address", property.getDetailAddress());
        result.put("rent_section", property.getRentSection());
        result.put("rent_area", property.getRentArea());

        // Land 변환
        if (property.getLand() != null) {
            Map<String, Object> land = new HashMap<>();
            land.put("land_type", property.getLand().getLandType());
            land.put("land_right_rate", property.getLand().getLandRightRate());
            // Double 타입을 숫자로 변환
            land.put("land_area", property.getLand().getLandArea());
            result.put("land", land);
        }

        // Building 변환
        if (property.getBuilding() != null) {
            Map<String, Object> building = new HashMap<>();
            building.put("building_constructure", property.getBuilding().getBuildingConstructure());
            building.put("building_type", property.getBuilding().getBuildingType());

            // 문자열인 building_area를 숫자로 변환 시도
            String buildingArea = property.getBuilding().getBuildingArea();
            if (buildingArea != null) {
                try {
                    building.put("building_area", Double.parseDouble(buildingArea));
                } catch (NumberFormatException e) {
                    building.put("building_area", buildingArea); // 변환 실패 시 원본 문자열 사용
                }
            } else {
                building.put("building_area", null);
            }
            result.put("building", building);
        }

        return result;
    }

    /**
     * Payment 객체를 ML API 호환 형식으로 변환 (Long 타입을 숫자로 변환)
     */
    private Map<String, Object> convertPayment(ContractData.Payment payment) {
        if (payment == null) return null;

        Map<String, Object> result = new HashMap<>();
        result.put("deposit", payment.getDeposit());
        result.put("deposit_kr", payment.getDepositKr());
        result.put("down_payment", payment.getDownPayment());
        result.put("down_payment_kr", payment.getDownPaymentKr());
        result.put("intermediate_payment", payment.getIntermediatePayment());
        result.put("intermediate_payment_kr", payment.getIntermediatePaymentKr());
        result.put("intermediate_payment_date", payment.getIntermediatePaymentDate());
        result.put("remaining_balance", payment.getRemainingBalance());
        result.put("remaining_balance_kr", payment.getRemainingBalanceKr());
        result.put("remaining_balance_date", payment.getRemainingBalanceDate());
        result.put("monthly_rent", payment.getMonthlyRent());
        result.put("monthly_rent_date", payment.getMonthlyRentDate());
        result.put("payment_plan", payment.getPaymentPlan());
        return result;
    }

    /**
     * Party 객체를 ML API 호환 형식으로 변환
     */
    private Map<String, Object> convertParty(ContractData.Party party) {
        if (party == null) return new HashMap<>();

        Map<String, Object> result = new HashMap<>();
        result.put("name", party.getName());
        result.put("id_number", party.getIdNumber());
        result.put("address", party.getAddress());
        result.put("detail_address", party.getDetailAddress());
        result.put("phone_number", party.getPhoneNumber());
        result.put("mobile_number", party.getMobileNumber());

        // Agent 변환
        Map<String, Object> agent = new HashMap<>();
        if (party.getAgent() != null) {
            agent.put("name", party.getAgent().getName());
        }
        result.put("agent", agent);

        return result;
    }

    /**
     * Broker 객체를 ML API 호환 형식으로 변환
     */
    private Map<String, Object> convertBroker(ContractData.Broker broker) {
        if (broker == null) return new HashMap<>();

        Map<String, Object> result = new HashMap<>();
        result.put("office", broker.getOffice());
        result.put("license_number", broker.getLicenseNumber());
        result.put("address", broker.getAddress());
        result.put("representative", broker.getRepresentative());
        result.put("fao_broker", broker.getFaoBroker());
        return result;
    }

    /**
     * ContractMetadata 객체를 ML API 호환 형식으로 변환
     */
    private Map<String, Object> convertContractMetadata(ContractData.ContractMetadata metadata) {
        if (metadata == null) return null;

        Map<String, Object> result = new HashMap<>();
        result.put("model", metadata.getModel());
        result.put("generation_time", metadata.getGenerationTime());
        result.put("user_agent", metadata.getUserAgent());
        result.put("version", metadata.getVersion());
        return result;
    }

    /**
     * LocalDateTime을 문자열로 변환 (MongoDB의 배열 형식을 처리)
     */
    private String convertDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;

        // LocalDateTime을 ISO 문자열로 변환
        return dateTime.toString();
    }

    /**
     * Contract Type을 ML API 형식으로 변환
     */
    private String normalizeContractType(String contractType) {
        if (contractType == null) {
            return "전세"; // 기본값
        }

        // ML API가 인식하는 형식으로 변환
        switch (contractType.trim()) {
            case "월세":
            case "월세계약":
            case "monthly_rent":
                return "월세";
            case "전세":
            case "전세계약":
            case "jeonse":
                return "전세";
            default:
                log.warn("⚠️ 알 수 없는 계약 유형: {}. 기본값 '전세' 사용", contractType);
                return "전세";
        }
    }

    /**
     * MLAnalysisResult에서 userId 추출 (raw_ml_response에서)
     */
    private String getAnalysisUserId(MLAnalysisResult analysis) {
        if (analysis.getRawMlResponse() != null && analysis.getRawMlResponse().containsKey("user_id")) {
            Object userId = analysis.getRawMlResponse().get("user_id");
            return userId != null ? userId.toString() : "";
        }
        return "";
    }

    /**
     * ML API 연결 테스트
     */
    public Map<String, Object> testMLApiConnection() {
        log.info("🔍 ML API 연결 상태 테스트 시작");

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now(ZoneOffset.UTC));

        try {
            // ML API 클라이언트의 헬스 체크 기능 사용
            boolean isHealthy = mlApiClient.isHealthy();

            result.put("success", isHealthy);
            result.put("mlApiUrl", "http://3.34.41.104:8000");
            result.put("status", isHealthy ? "Connected" : "Connection Failed");

            if (isHealthy) {
                log.info("✅ ML API 서버 연결 성공");
                result.put("message", "ML API 서버에 정상적으로 연결되었습니다.");
            } else {
                log.warn("❌ ML API 서버 연결 실패");
                result.put("message", "ML API 서버에 연결할 수 없습니다.");
            }

        } catch (Exception e) {
            log.error("ML API 연결 테스트 중 오류 발생", e);
            result.put("success", false);
            result.put("status", "Error");
            result.put("error", e.getMessage());
            result.put("message", "ML API 연결 테스트 중 오류가 발생했습니다.");
        }

        return result;
    }

    /**
     * 계약서 분석 테스트 (상세 로깅 포함)
     */
    public Map<String, Object> testContractAnalysis(String contractId, String userId) {
        log.info("🧪 계약서 분석 테스트 시작");
        log.info("   📋 Contract ID: {}", contractId);
        log.info("   👤 User ID: {}", userId);

        Map<String, Object> testResult = new HashMap<>();
        testResult.put("contractId", contractId);
        testResult.put("userId", userId);
        testResult.put("timestamp", LocalDateTime.now(ZoneOffset.UTC));

        try {
            // 1단계: MongoDB에서 계약서 데이터 조회
            log.info("📊 1단계: MongoDB에서 계약서 데이터 조회 중...");
            ContractData contractData = contractDataRepository.findById(contractId)
                    .orElseThrow(() -> new RuntimeException("계약서를 찾을 수 없습니다: " + contractId));

            log.info("✅ 계약서 데이터 조회 성공:");
            log.info("   - ID: {}", contractData.get_id());
            log.info("   - User ID: {}", contractData.getUserId());
            log.info("   - Contract Type: {}", contractData.getContractType());
            log.info("   - Created Date: {}", contractData.getCreatedDate());

            testResult.put("step1_mongodbQuery", "SUCCESS");
            testResult.put("contractData", Map.of(
                    "id", contractData.get_id(),
                    "userId", contractData.getUserId(),
                    "contractType", contractData.getContractType(),
                    "hasArticles", contractData.getArticles() != null,
                    "hasAgreements", contractData.getAgreements() != null
            ));

            // 2단계: ML API 요청 데이터 구성
            log.info("📦 2단계: ML API 요청 데이터 구성 중...");
            Map<String, Object> mlRequest = buildContractAnalysisRequest(contractData, userId);

            log.info("✅ ML API 요청 데이터 구성 완료:");
            log.info("   - contract_id: {}", mlRequest.get("contract_id"));
            log.info("   - contract_type: {}", mlRequest.get("contract_type"));
            log.info("   - user_id: {}", mlRequest.get("user_id"));

            testResult.put("step2_requestBuild", "SUCCESS");
            testResult.put("mlRequestData", Map.of(
                    "contract_id", mlRequest.get("contract_id"),
                    "contract_type", mlRequest.get("contract_type"),
                    "user_id", mlRequest.get("user_id"),
                    "hasContractData", mlRequest.containsKey("contract_data")
            ));

            // 3단계: ML API 호출 시도
            log.info("🤖 3단계: ML API 호출 시도 중...");

            try {
                Map<String, Object> mlResponse = mlApiClient.analyzeContract(mlRequest);

                log.info("✅ ML API 호출 성공!");
                log.info("   - Response ID: {}", mlResponse.get("id"));
                log.info("   - Created Date: {}", mlResponse.get("created_date"));

                testResult.put("step3_mlApiCall", "SUCCESS");
                testResult.put("mlResponse", Map.of(
                        "id", mlResponse.get("id"),
                        "created_date", mlResponse.get("created_date"),
                        "hasArticles", mlResponse.containsKey("articles"),
                        "hasAgreements", mlResponse.containsKey("agreements")
                ));

                testResult.put("success", true);
                testResult.put("message", "모든 단계가 성공적으로 완료되었습니다.");

            } catch (Exception mlApiError) {
                log.error("❌ ML API 호출 실패:");
                log.error("   - 오류 클래스: {}", mlApiError.getClass().getSimpleName());
                log.error("   - 오류 메시지: {}", mlApiError.getMessage());

                testResult.put("step3_mlApiCall", "FAILED");
                testResult.put("mlApiError", Map.of(
                        "errorClass", mlApiError.getClass().getSimpleName(),
                        "errorMessage", mlApiError.getMessage(),
                        "isConnectionError", mlApiError.getMessage().contains("Connection refused"),
                        "isTimeoutError", mlApiError.getMessage().contains("timeout"),
                        "is500Error", mlApiError.getMessage().contains("500")
                ));

                testResult.put("success", false);
                testResult.put("message", "ML API 호출 단계에서 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("❌ 계약서 분석 테스트 실패", e);
            testResult.put("success", false);
            testResult.put("error", e.getMessage());
            testResult.put("message", "테스트 중 오류가 발생했습니다.");
        }

        return testResult;
    }
}