# 📋 SuperLawVA API 엔드포인트 정리

## ✅ **Swagger 문서와 실제 코드 일치성 검증 완료**

### 🔐 **Authentication API (/auth)**

| 메서드   | 엔드포인트     | 설명             | 상태코드      | 응답 형태                     |
| -------- | -------------- | ---------------- | ------------- | ----------------------------- |
| **POST** | `/auth/signup` | 📝 일반 회원가입 | 200, 400, 409 | ApiResponse                   |
| **POST** | `/auth/login`  | 🔑 일반 로그인   | 200, 401, 404 | ApiResponse<LoginResponseDTO> |

### 👤 **User Management API (/users)**

| 메서드     | 엔드포인트           | 설명                    | 상태코드      | 응답 형태                          |
| ---------- | -------------------- | ----------------------- | ------------- | ---------------------------------- |
| **GET**    | `/users/info`        | 👤 내 정보 조회         | 200, 401      | ApiResponse<UserResponseDTO>       |
| **PUT**    | `/users/info`        | ✏️ 내 정보 수정         | 200, 400, 401 | ApiResponse<UserResponseDTO>       |
| **DELETE** | `/users/me`          | 🗑️ 회원 탈퇴            | 200, 401      | ApiResponse<Void>                  |
| **PATCH**  | `/users/me/password` | 🔐 비밀번호 변경        | 200, 400, 401 | ApiResponse<String>                |
| **POST**   | `/users/dashboard`   | 📊 대시보드 정보        | 200, 401      | ApiResponse<LoginResponseDTO>      |
| **GET**    | `/users`             | 전체 회원 조회 (관리자) | 200, 401      | ApiResponse<List<UserResponseDTO>> |
| **GET**    | `/users/{id}`        | 특정 회원 조회 (관리자) | 200, 401, 404 | ApiResponse<UserResponseDTO>       |
| **PUT**    | `/users/{id}`        | 회원 정보 수정 (관리자) | 200, 401, 404 | ApiResponse<UserResponseDTO>       |
| **DELETE** | `/users/{id}`        | 회원 삭제 (관리자)      | 204, 401, 404 | void                               |

### 🤖 **Chatbot API (/chatbot)**

| 메서드     | 엔드포인트                             | 설명                 | 상태코드           | 응답 형태                                    |
| ---------- | -------------------------------------- | -------------------- | ------------------ | -------------------------------------------- |
| **POST**   | `/chatbot/chat`                        | 💬 챗봇과 대화하기   | 200, 400, 401, 500 | ResponseEntity<String>                       |
| **GET**    | `/chatbot/chat/history`                | 📋 내 대화 이력 조회 | 200, 401           | ResponseEntity<Page<ChatMessageEntity>>      |
| **GET**    | `/chatbot/session/{sessionId}/history` | 🔗 세션별 대화 내역  | 200, 401, 403      | ResponseEntity<List<ChatMessageEntity>>      |
| **GET**    | `/chatbot/sessions`                    | 📝 내 세션 목록      | 200, 400, 401, 403 | ResponseEntity<List<SessionListResponseDTO>> |
| **POST**   | `/chatbot/session`                     | 🆕 새 세션 생성      | 200, 400, 401, 403 | ResponseEntity<ChatSessionEntity>            |
| **DELETE** | `/chatbot/session/{sessionId}`         | 🗑️ 세션 완전 삭제    | 200, 401, 403      | ResponseEntity<SessionDeleteResponseDTO>     |

### 🔍 **Search API (/search)**

| 메서드   | 엔드포인트       | 설명              | 상태코드           | 응답 형태                         |
| -------- | ---------------- | ----------------- | ------------------ | --------------------------------- |
| **POST** | `/search/search` | 🔍 법령/판례 검색 | 200, 400, 401, 500 | ResponseEntity<SearchResponseDTO> |

### 🔔 **Alarm API (/alarms)**

| 메서드     | 엔드포인트               | 설명                    | 상태코드      | 응답 형태                              |
| ---------- | ------------------------ | ----------------------- | ------------- | -------------------------------------- |
| **GET**    | `/alarms`                | 내 알람 목록 조회       | 200, 401      | ResponseEntity<List<AlarmResponseDTO>> |
| **GET**    | `/alarms/stats`          | 내 알람 통계 조회       | 200, 401      | ResponseEntity<AlarmStatsDTO>          |
| **PUT**    | `/alarms/read-all`       | 모든 알람 읽음 처리     | 200, 401      | ResponseEntity<Map<String, Object>>    |
| **PUT**    | `/alarms/{alarmId}/read` | 알람 읽음 처리          | 200, 401, 404 | ResponseEntity<Map<String, Object>>    |
| **DELETE** | `/alarms/{alarmId}`      | 알람 삭제               | 200, 401, 404 | ResponseEntity<Map<String, Object>>    |
| **POST**   | `/alarms`                | 알람 수동 생성 (관리자) | 200, 400, 401 | ResponseEntity<Map<String, Object>>    |

### 📊 **Status API (/status)**

| 메서드  | 엔드포인트       | 설명                   | 상태코드 | 응답 형태                         |
| ------- | ---------------- | ---------------------- | -------- | --------------------------------- |
| **GET** | `/status/status` | 📊 ML 서비스 상태 확인 | 200, 503 | ResponseEntity<StatusResponseDTO> |

### 📚 **Legal Terms API (/words)**

| 메서드   | 엔드포인트       | 설명                 | 상태코드 | 응답 형태                                  |
| -------- | ---------------- | -------------------- | -------- | ------------------------------------------ |
| **GET**  | `/words/search`  | 용어 검색            | 200      | ResponseEntity<WordsSearchResponseDto>     |
| **GET**  | `/words/popular` | 인기 키워드 조회     | 200      | ResponseEntity<PopularKeywordsResponseDto> |
| **POST** | `/words/upload`  | 용어 업로드 (관리자) | 201, 409 | ResponseEntity<WordsDto>                   |
| **GET**  | `/words/{word}`  | 특정 용어 상세 조회  | 200      | ResponseEntity<WordsDto>                   |
| **GET**  | `/words/health`  | 용어 서비스 헬스체크 | 200      | ResponseEntity<String>                     |

### 📷 **OCR API (/ocr)**

| 메서드   | 엔드포인트                          | 설명                    | 상태코드      | 응답 형태         |
| -------- | ----------------------------------- | ----------------------- | ------------- | ----------------- |
| **POST** | `/ocr/ocr3`                         | OCR 문서 처리           | 200, 400, 500 | ResponseEntity<?> |
| **GET**  | `/ocr/ocr3/{id}`                    | 계약서 조회             | 200, 500      | ResponseEntity<?> |
| **GET**  | `/ocr/ocr3/contracts/all`           | 모든 계약서 조회        | 200, 500      | ResponseEntity<?> |
| **GET**  | `/ocr/ocr3/contracts/{id}`          | ID로 계약서 조회        | 200, 404, 500 | ResponseEntity<?> |
| **GET**  | `/ocr/ocr3/user/{userId}/contracts` | 사용자별 계약서 조회    | 200, 500      | ResponseEntity<?> |
| **POST** | `/ocr/ocr3/user/{userId}`           | 사용자 ID 포함 OCR 처리 | 200, 400, 500 | ResponseEntity<?> |

### 🏥 **Health Check API**

| 메서드  | 엔드포인트         | 설명               | 상태코드 | 응답 형태                           |
| ------- | ------------------ | ------------------ | -------- | ----------------------------------- |
| **GET** | `/health`          | 기본 헬스체크      | 200      | ResponseEntity<Map<String, Object>> |
| **GET** | `/health/detailed` | 상세 헬스체크      | 200      | ResponseEntity<Map<String, Object>> |
| **GET** | `/`                | 루트 경로 헬스체크 | 200      | ResponseEntity<Map<String, Object>> |

---

## ✅ **수정 완료된 사항**

### 1. **API 경로 일치성 수정**

- ✅ **SearchController**: `/api/v1/search` → `/search/search`
- ✅ **StatusController**: `/api/v1/status` → `/status/status`
- ✅ **ChatbotController**: `/api/v1/chat` → `/chatbot/chat`

### 2. **응답 필드명 통일**

- ✅ **BasicAuthController**: `userName` → `nickname`

### 3. **상태코드 표준화**

- ✅ **ErrorStatus enum과 일치**: `COMMON400`, `COMMON401`, `COMMON403`, `COMMON500`
- ✅ **회원가입**: 409 (이미 존재하는 이메일)
- ✅ **로그인**: 401 (비밀번호 불일치), 404 (사용자 없음)
- ✅ **챗봇 API**: 403 (권한 없음), 500 (서버 오류)

### 4. **Swagger 문서 개선**

- ✅ **프론트엔드 구현 가이드** 추가
- ✅ **상세한 에러 처리 방법** 안내
- ✅ **실제 요청/응답 예시** 제공
- ✅ **JWT 토큰 사용법** 상세 설명

---

## 🔒 **공통 보안 설정**

### **JWT 인증**

- **헤더**: `Authorization: Bearer {JWT토큰}`
- **유효기간**: 24시간 (86,400,000ms)
- **알고리즘**: HS256 (HMAC SHA-256)

### **에러 응답 형식**

```json
{
  "isSuccess": false,
  "code": "COMMON401",
  "message": "인증이 필요합니다.",
  "result": null
}
```

### **성공 응답 형식**

```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공했습니다.",
  "result": {
    /* 실제 데이터 */
  }
}
```

---

## 🎯 **프론트엔드 개발 가이드**

### **1. API 호출 공통 패턴**

```javascript
const callAPI = async (endpoint, method = "GET", body = null) => {
  const token = localStorage.getItem("access_token");

  const options = {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    ...(body && { body: JSON.stringify(body) }),
  };

  const response = await fetch(endpoint, options);

  if (response.status === 401) {
    localStorage.removeItem("access_token");
    window.location.href = "/login";
    return;
  }

  return response.json();
};
```

### **2. 에러 처리**

```javascript
const handleAPIError = (error) => {
  switch (error.code) {
    case "COMMON401":
      alert("로그인이 필요합니다.");
      window.location.href = "/login";
      break;
    case "COMMON403":
      alert("접근 권한이 없습니다.");
      break;
    case "COMMON400":
      alert("요청 데이터를 확인해주세요.");
      break;
    case "COMMON500":
      alert("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
      break;
    default:
      alert(error.message || "알 수 없는 오류가 발생했습니다.");
  }
};
```

### **3. 주요 기능별 API 조합**

- **회원가입/로그인**: `/auth/signup` → `/auth/login` → 토큰 저장
- **대시보드**: `/users/dashboard` → 사용자 정보 + 알림 + 계약 + 채팅
- **챗봇 대화**: `/chatbot/sessions` → `/chatbot/chat` → `/chatbot/session/{sessionId}/history`
- **검색**: `/search/search` → 법령/판례 결과 표시
- **계약서 처리**: `/ocr/ocr3` → OCR 결과 저장/조회

---

## 📊 **총 API 통계**

- **총 엔드포인트 수**: 34개
- **인증 필요**: 23개 (JWT 토큰)
- **공개 API**: 11개 (헬스체크, 회원가입/로그인, 상태확인 등)
- **관리자 전용**: 6개
- **실시간 기능**: 2개 (챗봇, 상태확인)
