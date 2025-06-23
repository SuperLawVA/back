# 🚀 하이브리드 계약서 파싱 시스템 가이드

## 📋 개요

이 프로젝트는 **정규식 기반 파싱 + LLM 보완 파싱**의 하이브리드 전략을 사용하여 계약서를 분석합니다.

### 🔄 처리 흐름

```
파일 업로드 → GCP Document AI (OCR) → 정규식 파싱 → LLM 보완 → JSON 응답
```

### 🎯 핵심 기능

- **기본 모드**: 정규식만 사용 (빠르고 안정적)
- **Enhanced 모드**: 정규식 + OpenAI LLM 보완 (더 정확하고 완성도 높음)
- **자동 템플릿 감지**: 아파트/오피스텔/빌라 등 계약서 유형별 최적화
- **누락 필드 자동 보완**: LLM이 정규식에서 놓친 정보를 추가 추출

---

## ⚙️ 설정 방법

### 1. OpenAI API 키 설정

**환경변수로 설정 (권장)**

```bash
export OPENAI_API_KEY="your-openai-api-key-here"
export OPENAI_ENABLED=true
```

**또는 application.yml에서 직접 설정**

```yaml
openai:
  enabled: true
  api:
    key: "your-openai-api-key-here"
    url: "https://api.openai.com/v1/chat/completions"
  model: "gpt-4o-mini"
  max-tokens: 1500
  temperature: 0.1
  timeout: 30000
```

### 2. GCP Document AI 설정

```yaml
gcp:
  enabled: true
  project-id: "your-gcp-project-id"
  location: "us"
  processor-id: "your-processor-id"
  credentials:
    path: "classpath:service-account-key.json"
```

---

## 🚀 사용법

### API 엔드포인트: `/api/upload/ocr`

**기본 파싱 (정규식만)**

```bash
curl -X POST "http://localhost:8080/api/upload/ocr" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@contract.pdf" \
  -F "userId=1" \
  -F "enhanceQuality=false"
```

**Enhanced 파싱 (정규식 + LLM)**

```bash
curl -X POST "http://localhost:8080/api/upload/ocr" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@contract.pdf" \
  -F "userId=1" \
  -F "enhanceQuality=true"
```

### 파라미터 설명

| 파라미터         | 필수 | 설명                                    | 기본값 |
| ---------------- | ---- | --------------------------------------- | ------ |
| `file`           | ✅   | 업로드할 계약서 파일 (PDF, JPG, PNG 등) | -      |
| `userId`         | ✅   | 사용자 ID                               | -      |
| `enhanceQuality` | ❌   | Enhanced 파싱 사용 여부                 | `true` |

---

## 📊 응답 데이터 구조

### 기본 파싱 응답 (enhanceQuality=false)

```json
{
  "contractType": "월세",
  "dates": {
    "contractDate": "2024년 1월 15일",
    "startDate": "2024년 2월 1일",
    "endDate": "2026년 1월 31일"
  },
  "property": {
    "address": "서울특별시 강남구 역삼동 123-45",
    "detailAddress": "아파트 101동 501호"
  },
  "payment": {
    "deposit": "50,000,000",
    "depositKr": "오천만원"
  },
  "contractMetadata": {
    "model": "document-parser-v3",
    "version": "1.2.0",
    "generationTime": 0.8
  }
}
```

### Enhanced 파싱 응답 (enhanceQuality=true)

```json
{
  "contractType": "월세",
  "dates": {
    "contractDate": "2024년 1월 15일",
    "startDate": "2024년 2월 1일",
    "endDate": "2026년 1월 31일"
  },
  "property": {
    "address": "서울특별시 강남구 역삼동 123-45",
    "detailAddress": "아파트 101동 501호"
  },
  "payment": {
    "deposit": "50,000,000",
    "depositKr": "오천만원",
    "monthlyRent": "2,000,000"
  },
  "lessor": {
    "name": "박지혜",
    "phoneNumber": "010-3344-6099"
  },
  "lessee": {
    "name": "김철수",
    "phoneNumber": "010-1234-5678"
  },
  "broker1": {
    "office": "강남부동산중개법인",
    "representative": "이영희",
    "licenseNumber": "11110-2023-00123"
  },
  "contractMetadata": {
    "model": "enhanced-hybrid-parser-v2",
    "version": "2.2.0",
    "generationTime": 3.2
  }
}
```

---

## 🧪 테스트 방법

### 1. 단위 테스트 실행

**하이브리드 파싱 서비스 테스트**

```bash
./gradlew test --tests "HybridContractAnalysisTest"
```

**통합 API 테스트**

```bash
./gradlew test --tests "HybridOcrIntegrationTest"
```

### 2. OpenAI 서비스 활성화 테스트

**OpenAI 없이 테스트 (기본 모드)**

```bash
export OPENAI_ENABLED=false
./gradlew test
```

**OpenAI 포함 테스트 (Enhanced 모드)**

```bash
export OPENAI_ENABLED=true
export OPENAI_API_KEY="your-api-key"
./gradlew test
```

### 3. 수동 테스트

**Swagger UI 사용**

1. 애플리케이션 실행 후 `http://localhost:8080/swagger-ui.html` 접속
2. `📁 문서 OCR API` 섹션의 `/api/upload/ocr` 엔드포인트 선택
3. 파일 업로드 및 파라미터 설정 후 실행

**Postman 사용**

```
POST http://localhost:8080/api/upload/ocr
Body: form-data
- file: [계약서 파일]
- userId: 1
- enhanceQuality: true
```

---

## 🔍 모니터링 및 디버깅

### 로그 확인

**OpenAI 서비스 활성화 상태 확인**

```
INFO  - ✅ OpenAI 서비스가 활성화되었습니다.
INFO  - 🤖 Enhanced Contract Analysis 서비스 사용 (LLM 보완 활성화)
```

**기본 파싱 모드**

```
INFO  - ⚠️ OpenAI 서비스가 비활성화되어 있습니다.
INFO  - 🔍 기본 Contract Analysis 서비스 사용 (정규식만 사용)
```

**LLM 보완 처리**

```
INFO  - 📊 3개 누락 필드를 LLM으로 보완 처리: [lessor_name, lessor_phone, broker_office]
INFO  - ✅ OpenAI로부터 3 필드에 대한 응답을 받았습니다.
```

### 성능 모니터링

| 지표         | 기본 파싱 | Enhanced 파싱 |
| ------------ | --------- | ------------- |
| 응답 시간    | 0.5-2초   | 2-8초         |
| 정확도       | 70-80%    | 85-95%        |
| 추출 필드 수 | 8-12개    | 15-25개       |

---

## ⚠️ 주의사항

### OpenAI API 사용량 관리

- **gpt-4o-mini** 모델 사용으로 비용 최적화
- 누락된 필드만 선택적으로 LLM 요청
- 타임아웃 설정으로 무한 대기 방지

### 에러 처리

- OpenAI 서비스 실패 시 자동으로 기본 파싱 결과 반환
- GCP Document AI 실패 시 명확한 에러 메시지 제공
- 파일 타입 검증 및 크기 제한

### 보안

- API 키는 환경변수로 관리
- 업로드된 파일은 메모리에서만 처리 (임시 저장 없음)
- 민감한 정보 로깅 방지

---

## 🔧 문제 해결

### OpenAI 서비스가 활성화되지 않을 때

```yaml
# application.yml 확인
openai:
  enabled: true # false → true로 변경
  api:
    key: "실제_API_키_입력" # 빈 값이 아닌지 확인
```

### GCP Document AI 연결 실패

```yaml
# GCP 설정 확인
gcp:
  enabled: true
  project-id: "올바른_프로젝트_ID"
  processor-id: "올바른_프로세서_ID"
  credentials:
    path: "classpath:service-account-key.json" # 파일 존재 확인
```

### 메모리 부족 오류

```yaml
# JVM 옵션 조정
-Xmx2G -XX:MaxMetaspaceSize=512m
```

---

## 📈 업그레이드 계획

### Phase 1 (현재)

- ✅ 정규식 + OpenAI 하이브리드 파싱
- ✅ 계약서 템플릿 자동 감지
- ✅ 누락 필드 LLM 보완

### Phase 2 (예정)

- 🔄 Claude, Gemini 등 다중 LLM 지원
- 🔄 실시간 파싱 품질 피드백
- 🔄 사용자별 파싱 정확도 학습

### Phase 3 (예정)

- 🔄 계약서 위험도 분석
- 🔄 조항별 유불리 판단
- 🔄 수정 제안 기능

---

## 🤝 기여하기

버그 리포트, 기능 제안, 풀 리퀘스트 모두 환영합니다!

1. 이슈 생성
2. 기능 브랜치 생성
3. 테스트 추가
4. PR 제출

---

## 📞 지원

문의사항이나 기술 지원이 필요하시면 개발팀에 연락해주세요.

**마지막 업데이트**: 2024년 12월 20일
