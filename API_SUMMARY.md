# SuperLawVA API 요약 문서

## 📋 개요

SuperLawVA는 법률 AI 서비스로, 계약서 분석, 법령/판례 검색, 챗봇 서비스 등을 제공하는 REST API입니다.

## 🔧 기술 스택

- **Framework**: Spring Boot 3.4.5
- **Database**: MySQL (JPA/Hibernate)
- **Security**: Spring Security + JWT
- **Documentation**: OpenAPI 3.0 (Swagger)
- **External APIs**: ML API, Gemini API

## 🚀 주요 기능

### 1. 인증 시스템

- **일반 로그인**: 이메일/비밀번호 기반
- **소셜 로그인**: 카카오, 네이버 OAuth2
- **JWT 토큰**: 24시간 유효기간

### 2. 계약서 분석

- **OCR 처리**: 이미지 → 텍스트 추출
- **AI 분석**: Gemini API를 통한 계약서 구조화
- **문서 생성**: 계약서, 내용증명서 등 자동 생성

### 3. 법령/판례 검색

- **벡터 검색**: ML 팀 API 연동
- **유사도 기반**: 검색 결과 유사도 순 정렬
- **통합 검색**: 법령 + 판례 동시 검색

### 4. 챗봇 서비스

- **세션 관리**: 사용자별 채팅 세션
- **AI 응답**: 법률 질문에 대한 AI 답변
- **히스토리**: 대화 기록 저장

## 📊 API 엔드포인트

### 인증 API

- `POST /auth/signup` - 회원가입
- `POST /auth/login` - 로그인
- `POST /auth/kakao` - 카카오 로그인
- `POST /auth/naver` - 네이버 로그인

### 계약서 API

- `POST /api/upload/ocr3` - OCR 처리
- `POST /api/upload/ocr3/user/{userId}` - 사용자별 OCR 처리
- `GET /api/contracts/{id}` - 계약서 조회
- `GET /api/contracts/user/{userId}` - 사용자별 계약서 목록

### 검색 API

- `POST /search/search` - 법령/판례 검색

### 챗봇 API

- `POST /chatbot/chat` - 채팅 메시지 전송
- `GET /chatbot/sessions` - 세션 목록 조회
- `DELETE /chatbot/sessions/{sessionId}` - 세션 삭제

### 문서 생성 API

- `POST /documents/generate/contract` - 계약서 생성
- `POST /documents/generate/proof` - 내용증명서 생성
- `GET /documents/generated` - 생성된 문서 목록

### 알림 API

- `GET /alarms` - 알림 목록 조회
- `POST /alarms` - 알림 생성
- `PUT /alarms/{id}/read` - 알림 읽음 처리

## ⚠️ 오류 코드 체계

### 공통 오류 코드

- `COMMON400`: 잘못된 요청
- `COMMON401`: 인증 필요
- `COMMON403`: 접근 금지
- `COMMON404`: 리소스 없음
- `COMMON408`: 요청 시간 초과
- `COMMON500`: 서버 내부 오류

### 도메인별 오류 코드

- **USER**: 사용자 관련 (USER400, USER401, USER404, USER409)
- **DOCUMENT**: 문서 관련 (DOCUMENT400, DOCUMENT404, DOCUMENT500)
- **OCR**: OCR 처리 관련 (OCR400, OCR500)
- **SEARCH**: 검색 관련 (SEARCH400, SEARCH500)
- **CHAT**: 챗봇 관련 (CHAT400, CHAT403, CHAT404, CHAT500)
- **ALARM**: 알림 관련 (ALARM400, ALARM404)
- **ML**: ML API 관련 (ML408, ML500)
- **KAKAO**: 카카오 로그인 관련 (KAKAO400, KAKAO500)
- **NAVER**: 네이버 로그인 관련 (NAVER500)
- **EMAIL**: 이메일 인증 관련 (EMAIL400, EMAIL404)
- **MAIL**: 메일 전송 관련 (MAIL500)

## 🔐 보안

### JWT 토큰

- **알고리즘**: HS256
- **유효기간**: 24시간
- **페이로드**: 사용자 ID, 이메일, 발급/만료 시간

### 인증 헤더

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## 📝 테스트 계정

### 개발용 계정

- **일반 사용자**: `test@example.com` / `password123`
- **관리자**: `admin@example.com` / `admin123`
- **데모 사용자**: `demo@example.com` / `demo123`

## 🚨 주의사항

### 1. API 호출 제한

- 검색 API: 최대 20개 결과
- 파일 업로드: 이미지 파일만 지원
- 세션 관리: 사용자별 독립적 세션

### 2. 오류 처리

- 모든 API는 표준화된 오류 응답 형식 사용
- 오류 코드와 메시지는 ErrorStatus enum 참조
- 5xx 오류는 서버 내부 오류로 관리자 문의 필요

### 3. 데이터베이스

- MySQL 사용 (MongoDB에서 마이그레이션 완료)
- JPA/Hibernate ORM 사용
- 트랜잭션 관리 필수

## 🔄 최근 변경사항

### MongoDB → MySQL 마이그레이션

- 모든 엔티티를 JPA 어노테이션으로 변경
- MongoRepository → JpaRepository 변경
- GridFS → 파일 시스템 저장소 변경

### ML API 연동 개선

- 다중 ML 서버 지원 (기본 + 통합 AI 법률 서비스)
- API별 기능 분리 및 최적화
- 오류 처리 및 재시도 로직 강화

### 오류 코드 표준화

- 모든 오류 코드를 일관성 있게 정리
- 도메인별 접두사 사용 (USER, DOCUMENT, OCR 등)
- Swagger 문서와 실제 코드 일치

## 📞 지원

### 개발팀 연락처

- **백엔드**: backend@superlawva.com
- **ML 팀**: ml@superlawva.com
- **프론트엔드**: frontend@superlawva.com

### 문서 업데이트

- 이 문서는 API 변경 시마다 업데이트됩니다
- 최신 버전은 Swagger UI에서 확인 가능
- 변경사항은 CHANGELOG.md에서 확인 가능
