# OCR2 REST API 사용 가이드

## 🚀 파일 업로드 및 JSON 결과 확인 방법

### 방법 1: 프로그래밍 방식 (현재 사용 가능)

#### 📁 1. 파일 업로드 데모 실행

```bash
# 환경 변수 설정
export OCR2_GEMINI_API_KEY="your-gemini-api-key"
export OCR2_DOC_AI_PROJECT_ID="ocrt-461104"
export GOOGLE_APPLICATION_CREDENTIALS="path/to/service-account-key.json"

# 데모 실행
java com.superlawva.domain.ocr2.demo.FileUploadDemo
```

#### 📋 2. 메뉴 선택

데모 실행 후 다음 메뉴가 나타납니다:

```
📋 선택하세요:
1. 파일 업로드 분석    ← 이미지/PDF 파일 업로드
2. 텍스트 직접 분석    ← OCR 텍스트 직접 입력
3. 서비스 정보 확인    ← OCR2 서비스 정보
4. 샘플 파일 테스트    ← 미리 준비된 샘플로 테스트
0. 종료
```

#### 📄 3. JSON 결과 예시

분석이 완료되면 다음과 같은 JSON 결과를 확인할 수 있습니다:

```json
{
  "success": true,
  "message": "계약서 분석이 완료되었습니다.",
  "data": {
    "contract_data": {
      "_id": "uuid-generated",
      "contract_type": "전세",
      "dates": {
        "contract_date": null,
        "start_date": "2024-01-01",
        "end_date": "2025-12-31"
      },
      "property": {
        "address": "서울특별시 강남구 역삼동 101-12",
        "building": {
          "building_type": "오피스텔",
          "building_area": "84.21"
        }
      },
      "payment": {
        "deposit": 50000000,
        "deposit_kr": "오천만원정"
      },
      "lessor": {
        "name": "김영희",
        "mobile_number": "010-1234-5678"
      },
      "lessee": {
        "name": "이민수",
        "mobile_number": "010-9876-5432"
      },
      "articles": [
        "제1조 (목적) 이 계약은 임대인과 임차인이 상기 부동산에 관하여 임대차계약을 체결함에 있어 그 조건을 명시한 것이다."
      ],
      "agreements": [
        "반려동물 사육을 금지한다.",
        "임차인은 계약 만료 시 원상복구 의무가 있다.",
        "전세권 설정이 가능하다."
      ]
    }
  },
  "processing_time": "12.34초",
  "model": "Document AI + Gemini 2.5 Flash",
  "version": "v3.1.0"
}
```

---

### 방법 2: REST API (Spring Boot 환경)

Spring Boot 환경에서는 다음과 같은 REST API를 사용할 수 있습니다:

#### 🔧 1. Spring Boot 설정

`Ocr2Controller.java`에서 주석을 해제합니다:

```java
@RestController
@RequestMapping("/api/ocr2")
public class Ocr2Controller {
    // ... 기존 코드 ...
}
```

#### 📡 2. API 엔드포인트

**파일 업로드 분석**

```http
POST /api/ocr2/analyze
Content-Type: multipart/form-data

Parameters:
- file: 계약서 이미지/PDF 파일
- user_id: 사용자 ID (선택사항)
```

**텍스트 직접 분석**

```http
POST /api/ocr2/analyze-text
Content-Type: application/json

{
  "text": "분석할 계약서 텍스트...",
  "source": "직접 입력"
}
```

**서비스 정보**

```http
GET /api/ocr2/info
```

#### 💻 3. cURL 사용 예시

**파일 업로드**:

```bash
curl -X POST http://localhost:8080/api/ocr2/analyze \
  -F "file=@contract.jpg" \
  -F "user_id=test_user"
```

**텍스트 분석**:

```bash
curl -X POST http://localhost:8080/api/ocr2/analyze-text \
  -H "Content-Type: application/json" \
  -d '{
    "text": "임대차계약서\n임대인: 김영희\n보증금: 5000만원",
    "source": "API 테스트"
  }'
```

#### 🌐 4. JavaScript 사용 예시

**파일 업로드**:

```javascript
const formData = new FormData();
formData.append("file", fileInput.files[0]);
formData.append("user_id", "test_user");

fetch("/api/ocr2/analyze", {
  method: "POST",
  body: formData,
})
  .then((response) => response.json())
  .then((data) => {
    console.log("분석 결과:", data);

    if (data.success) {
      console.log("계약서 분석 완료!");
      console.log("JSON 데이터:", JSON.stringify(data, null, 2));
    } else {
      console.error("분석 실패:", data.message);
    }
  });
```

**텍스트 분석**:

```javascript
fetch("/api/ocr2/analyze-text", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    text: document.getElementById("contractText").value,
    source: "Web Interface",
  }),
})
  .then((response) => response.json())
  .then((data) => {
    document.getElementById("result").innerHTML = `<pre>${JSON.stringify(
      data,
      null,
      2
    )}</pre>`;
  });
```

---

## 🎯 빠른 시작 가이드

### 1단계: 환경 설정

```bash
export OCR2_GEMINI_API_KEY="your-gemini-api-key"
export OCR2_DOC_AI_PROJECT_ID="ocrt-461104"
export GOOGLE_APPLICATION_CREDENTIALS="service-account-key.json"
```

### 2단계: 데모 실행

```bash
java com.superlawva.domain.ocr2.demo.FileUploadDemo
```

### 3단계: 파일 준비

- 지원 형식: JPG, PNG, PDF
- 최대 크기: 10MB
- 권장: 계약서 이미지 또는 스캔본

### 4단계: 분석 및 결과 확인

1. 메뉴에서 "1. 파일 업로드 분석" 선택
2. 파일 경로 입력 (예: `C:\contract.jpg`)
3. 분석 완료 후 JSON 결과 확인

---

## 📊 JSON 결과 구조 분석

분석 결과의 주요 필드들:

```json
{
  "success": true,                    // 분석 성공 여부
  "message": "분석 완료 메시지",        // 상태 메시지
  "data": {                          // 실제 계약서 분석 데이터
    "contract_data": {
      "contract_type": "전세|월세",     // 계약 유형
      "dates": { ... },              // 계약 관련 날짜들
      "property": { ... },           // 부동산 정보
      "payment": { ... },            // 금액 정보
      "lessor": { ... },             // 임대인 정보
      "lessee": { ... },             // 임차인 정보
      "articles": [ ... ],           // 계약 조항들
      "agreements": [ ... ]          // 특약 사항들
    }
  },
  "processing_time": "12.34초",       // 처리 시간
  "model": "Document AI + Gemini 2.5", // 사용된 모델
  "version": "v3.1.0"                // 스키마 버전
}
```

---

## 🚨 문제 해결

### Q: "환경 변수가 설정되지 않았습니다" 오류

**A**: 필수 환경 변수를 설정하세요:

```bash
export OCR2_GEMINI_API_KEY="your-api-key"
export OCR2_DOC_AI_PROJECT_ID="ocrt-461104"
export GOOGLE_APPLICATION_CREDENTIALS="path/to/key.json"
```

### Q: "파일을 찾을 수 없습니다" 오류

**A**: 파일 경로를 정확히 입력하세요:

- Windows: `C:\Users\username\Desktop\contract.jpg`
- Mac/Linux: `/Users/username/Desktop/contract.jpg`

### Q: 분석 결과가 비어있음

**A**:

1. 이미지 품질 확인 (해상도 300 DPI 이상 권장)
2. 파일 형식 확인 (JPG, PNG, PDF만 지원)
3. Gemini API 키 유효성 확인

### Q: Spring Boot에서 REST API가 작동하지 않음

**A**:

1. `@RestController` 어노테이션 주석 해제
2. Spring Boot 관련 의존성 추가
3. 컨트롤러 패키지 스캔 범위 확인

---

## 📋 체크리스트

파일 업로드 전 확인사항:

- [ ] 환경 변수 설정 완료
- [ ] Google Cloud 인증 파일 준비
- [ ] Gemini API 키 유효성 확인
- [ ] 업로드할 파일 준비 (JPG/PNG/PDF, 10MB 이하)
- [ ] Java 17+ 환경 확인
- [ ] 인터넷 연결 상태 양호

이제 OCR2 패키지를 사용하여 계약서 파일을 업로드하고 JSON 결과를 확인할 수 있습니다! 🎉
