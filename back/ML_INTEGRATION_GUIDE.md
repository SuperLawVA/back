# 🤖 SuperLawva ML 연동 가이드

## 📋 **개요**

SuperLawva Backend와 ML 서버 간의 연동을 통해 AI 기반 계약서 분석 및 내용증명서 생성 서비스를 제공합니다.

## 🏗️ **전체 아키텍처**

```
Frontend → Backend API → ML API Server → AI Model Engine
    ↑         ↓              ↓              ↓
    ←    MongoDB         Response        Processing
       (generated_contract)   ←              ←
```

### **데이터 플로우**

1. **Frontend** → Backend: 계약서 분석/내용증명서 생성 요청
2. **Backend** → MongoDB: `contract` 컬렉션에서 계약서 데이터 조회
3. **Backend** → ML Server: 계약서 데이터 전송 (REST API)
4. **ML Server** → AI Engine: ML 모델로 분석/생성 처리
5. **ML Server** → Backend: 분석/생성 결과 응답
6. **Backend** → MongoDB: `generated_contract` 컬렉션에 결과 저장
7. **Backend** → Frontend: 최종 결과 API 응답

## 🔧 **Backend 구현 현황**

### **패키지 구조**

```
src/main/java/com/superlawva/domain/ml/
├── client/
│   └── MLApiClient.java              # ML API 통신 클라이언트
├── controller/
│   └── MLAnalysisController.java     # REST API 컨트롤러
├── dto/
│   ├── MLAnalysisRequest.java        # ML로 보낼 요청 DTO
│   └── MLAnalysisResponse.java       # ML에서 받을 응답 DTO
└── service/
    └── MLAnalysisService.java        # 비즈니스 로직 서비스
```

### **API 엔드포인트**

#### 🤖 **계약서 분석**

```http
POST /api/ml/analyze/contract/{contractId}?userId={userId}
```

#### 📝 **내용증명서 생성**

```http
POST /api/ml/generate/proof/{contractId}?userId={userId}
```

#### 📄 **결과 조회**

```http
GET /api/ml/results/{generatedDocumentId}
```

#### 🔍 **상태 확인**

```http
GET /api/ml/health
```

## 🔗 **ML 서버 연동 명세**

### **ML 서버 요구사항**

#### **Base URL**

```
http://localhost:8000 (개발 환경)
http://ml-server.superlawva.com (운영 환경)
```

#### **필수 구현 API**

##### 1️⃣ **계약서 분석 API**

```http
POST /api/v1/analyze/contract
Content-Type: application/json
```

**요청 형태:**

```json
{
  "contract_id": "675506b4c4a60073fbff00c2d",
  "user_id": "test-user-123",
  "request_type": "CONTRACT_ANALYSIS",
  "contract_data": {
    "contract_type": "부동산임대차계약서",
    "dates": {
      /* 계약 날짜 정보 */
    },
    "property": {
      /* 부동산 정보 */
    },
    "payment": {
      /* 금액 정보 */
    },
    "lessor": {
      /* 임대인 정보 */
    },
    "lessee": {
      /* 임차인 정보 */
    },
    "articles": {
      /* 계약 조항들 */
    },
    "agreements": {
      /* 특약사항 */
    }
  },
  "additional_params": {
    "analysis_type": "COMPREHENSIVE",
    "output_format": "DETAILED",
    "language": "ko",
    "priority": "NORMAL"
  }
}
```

##### 2️⃣ **내용증명서 생성 API**

```http
POST /api/v1/generate/proof
Content-Type: application/json
```

**요청 형태:** (계약서 분석과 동일, `request_type`만 `"PROOF_GENERATION"`)

### **ML 응답 형태**

```json
{
  "success": true,
  "message": "계약서 분석이 완료되었습니다.",
  "contract_id": "675506b4c4a60073fbff00c2d",
  "user_id": "test-user-123",
  "request_type": "CONTRACT_ANALYSIS",
  "analysis_result": {
    "risk_score": 0.75,
    "risk_level": "MEDIUM",
    "risk_factors": ["위험 요소들"],
    "legal_issues": [
      {
        "category": "PAYMENT",
        "severity": "MEDIUM",
        "description": "보증금 반환 기한이 명시되지 않음",
        "clause_reference": "제7조",
        "suggestion": "보증금 반환 기한을 명확히 명시할 것을 권장"
      }
    ],
    "recommendations": ["개선 권장사항들"],
    "compliance_check": {
      "is_compliant": true,
      "violations": [],
      "missing_clauses": [],
      "regulatory_issues": []
    },
    "summary": "전반적인 분석 요약"
  },
  "generated_content": {
    "content_type": "LEGAL_ANALYSIS",
    "title": "부동산임대차계약서 분석 보고서",
    "content": "분석 결과 상세 내용...",
    "sections": [
      {
        "section_name": "위험도 분석",
        "section_content": "...",
        "section_type": "BODY"
      }
    ],
    "templates_used": ["contract_analysis_template_v2"],
    "confidence_score": 0.92
  },
  "processing_info": {
    "model_name": "SuperLawva-Contract-Analyzer-v2",
    "model_version": "2.1.0",
    "processing_time_seconds": 12.5,
    "token_count": 1842,
    "quality_score": 0.89,
    "processed_at": "2024-12-08T10:30:00Z",
    "additional_metadata": {}
  }
}
```

## ⚙️ **설정 방법**

### **1. application.yml 설정**

```yaml
ml:
  api:
    base-url: http://localhost:8000 # ML 서버 URL
    timeout: 120000 # 2분 타임아웃
    retry-attempts: 3
    connection-timeout: 10000 # 10초
    read-timeout: 60000 # 1분
  features:
    contract-analysis: true
    proof-generation: true
    async-processing: false # 동기 처리
  quality:
    min-confidence-score: 0.7 # 최소 신뢰도
    max-processing-time: 180 # 최대 처리 시간(초)
```

### **2. 환경 변수 설정**

```bash
# ML API 설정
ML_API_BASE_URL=http://ml-server:8000
ML_API_TIMEOUT=120000
ML_CONTRACT_ANALYSIS_ENABLED=true
ML_PROOF_GENERATION_ENABLED=true
ML_MIN_CONFIDENCE=0.7
```

## 🧪 **테스트 방법**

### **1. ML API 상태 확인**

```bash
curl -X GET http://localhost:8080/api/ml/health
```

### **2. 계약서 분석 테스트**

```bash
curl -X POST "http://localhost:8080/api/ml/analyze/contract/675506b4c4a60073fbff00c2d?userId=test-user-123"
```

### **3. 내용증명서 생성 테스트**

```bash
curl -X POST "http://localhost:8080/api/ml/generate/proof/675506b4c4a60073fbff00c2d?userId=test-user-123"
```

### **4. HTTP 테스트 파일 사용**

```bash
# IntelliJ나 VSCode에서 test-ml-api.http 파일 실행
```

## 📊 **MongoDB 데이터 구조**

### **입력: `contract` 컬렉션**

```json
{
  "_id": "675506b4c4a60073fbff00c2d",
  "contractType": "부동산임대차계약서",
  "dates": {
    /* 날짜 정보 */
  },
  "property": {
    /* 부동산 정보 */
  },
  "payment": {
    /* 금액 정보 */
  },
  "lessor": {
    /* 임대인 */
  },
  "lessee": {
    /* 임차인 */
  },
  "articles": {
    /* 조항들 */
  },
  "agreements": {
    /* 특약사항 */
  }
}
```

### **출력: `generated_contract` 컬렉션**

```json
{
  "_id": "generated-doc-id",
  "userId": "test-user-123",
  "documentId": "675506b4c4a60073fbff00c2d",
  "generationType": "CONTRACT_ANALYSIS",
  "requestData": "{ /* 요청 데이터 JSON */ }",
  "generationMetadata": "{ /* ML 응답 메타데이터 */ }",
  "modelName": "SuperLawva-Contract-Analyzer-v2",
  "modelVersion": "2.1.0",
  "generationTimeSeconds": 12.5,
  "tokenCount": 1842,
  "qualityScore": 0.89,
  "status": "GENERATED",
  "createdAt": "2024-12-08T10:30:00Z",
  "updatedAt": "2024-12-08T10:30:00Z"
}
```

## 🚀 **운영 가이드**

### **성능 최적화**

- **타임아웃 설정**: ML 처리 시간에 맞춰 적절한 타임아웃 설정
- **비동기 처리**: 대용량 처리 시 `async-processing: true` 설정 고려
- **재시도 로직**: 네트워크 오류 시 자동 재시도 (`retry-attempts: 3`)

### **모니터링**

- **로그 확인**: `logs/` 디렉토리에서 ML API 호출 로그 모니터링
- **품질 점수**: `quality_score` 필드로 ML 응답 품질 추적
- **처리 시간**: `processing_time_seconds` 필드로 성능 모니터링

### **오류 처리**

- **ML 서버 다운**: 적절한 에러 메시지와 함께 500 응답
- **타임아웃**: 처리 시간 초과 시 명확한 오류 메시지
- **신뢰도 부족**: `min-confidence-score` 미달 시 경고 메시지

## 🔄 **다음 단계**

1. **ML 팀과 API 명세 협의**: DTO 구조 최종 확정
2. **Mock 서버 구축**: ML 서버 완성 전 테스트용 Mock API
3. **비동기 처리 구현**: 장시간 처리를 위한 Queue 기반 처리
4. **캐싱 시스템**: Redis를 활용한 결과 캐싱
5. **배치 처리**: 다수 계약서 동시 처리 기능

## 📞 **문의사항**

ML 연동 관련 문의사항이 있으시면 Backend 팀으로 연락 주세요.

- **Backend API**: `/api/ml/*` 엔드포인트
- **ML Server API**: `/api/v1/*` 엔드포인트 (ML 팀 구현 필요)
- **테스트 파일**: `test-ml-api.http`
