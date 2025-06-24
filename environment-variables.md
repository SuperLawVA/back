# 🌍 환경변수 설정 가이드

이 문서는 SuperLawVA Backend 애플리케이션에서 사용하는 환경변수들을 설명합니다.
개인정보 보호를 위해 모든 민감한 정보는 환경변수로 관리됩니다.

## 📋 필수 환경변수

### 🌍 기본 환경 설정

```bash
SPRING_PROFILES_ACTIVE=local  # 실행 프로필 (local/prod)
SERVER_PORT=8080              # 서버 포트
FRONTEND_URL=http://localhost:3000  # 프론트엔드 URL
```

### 📧 이메일 설정

```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com        # Gmail 계정
MAIL_PASSWORD=your-16-digit-app-password  # Gmail 앱 비밀번호
```

**📌 Gmail 앱 비밀번호 생성 방법:**

1. Google 계정 관리 → 보안 → 2단계 인증 활성화
2. 앱 비밀번호 생성 → "메일" 선택
3. 생성된 16자리 비밀번호 사용

### 🔐 보안 키 설정

```bash
JWT_SECRET=your-jwt-secret-key-minimum-32-characters-long
JWT_ACCESS_TOKEN_VALIDITY=3600000    # 1시간 (밀리초)
JWT_REFRESH_TOKEN_VALIDITY=604800000 # 7일 (밀리초)
AES_SECRET_KEY=your-aes-secret-key-exactly-32-chars  # 정확히 32자
```

### 🔗 OAuth 설정

#### 카카오 로그인

```bash
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret
KAKAO_REDIRECT_URI=http://localhost:5173/oauth/kakao/callback
```

#### 네이버 로그인

```bash
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret
NAVER_REDIRECT_URI=http://localhost:5173/oauth/naver/callback
```

### 🗄️ 데이터베이스 설정

#### 운영환경 (MySQL)

```bash
DATABASE_URL=jdbc:mysql://your-host:3306/your-db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=your-database-username
DB_PASSWORD=your-database-password
```

#### 로컬 개발환경 (H2)

```bash
LOCAL_DATABASE_URL=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
LOCAL_DB_USERNAME=sa
LOCAL_DB_PASSWORD=
```

### 📄 MongoDB 설정

```bash
# 로컬 MongoDB
MONGODB_URI=mongodb://admin:password123@localhost:27017/superlawva_docs?authSource=admin

# MongoDB Atlas (운영환경)
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/database?retryWrites=true&w=majority
```

### ⚡ Redis 설정

```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=                # 비어있으면 인증 없음
REDIS_SSL_ENABLED=false        # SSL 사용 여부
```

### ☁️ Google Cloud Platform 설정

```bash
GCP_ENABLED=true
GCP_PROJECT_ID=your-gcp-project-id
GCP_LOCATION=us
GCP_PROCESSOR_ID=your-processor-id
GCP_CREDENTIALS_PATH=classpath:service-account-key.json
```

### 🤖 AI 서비스 설정

#### Gemini AI

```bash
GEMINI_API_KEY=your-gemini-api-key
GEMINI_MODEL_NAME=gemini-2.0-flash-exp
GEMINI_API_URL=https://generativelanguage.googleapis.com
```

#### ML API

```bash
ML_API_BASE_URL=http://3.34.41.104:8000
ML_API_CONNECTION_TIMEOUT=10000
ML_API_READ_TIMEOUT=60000
```

### 📋 기타 API 설정

```bash
CONTRACT_API_BASE_URL=http://your-contract-api-server
```

## 🚀 환경변수 설정 방법

### 1. IntelliJ IDEA

```
Run/Debug Configuration → Environment Variables → Add
```

### 2. 시스템 환경변수 (Windows)

```cmd
setx SPRING_PROFILES_ACTIVE "local"
setx MAIL_USERNAME "your-email@gmail.com"
```

### 3. 시스템 환경변수 (Linux/Mac)

```bash
export SPRING_PROFILES_ACTIVE=local
export MAIL_USERNAME=your-email@gmail.com
```

### 4. .env 파일 (프로젝트 루트)

```bash
# .env 파일 생성 후 위 환경변수들 추가
# 주의: .env 파일은 .gitignore에 포함되어야 함
```

## ⚠️ 보안 주의사항

1. **환경변수 파일 관리**:

   - `.env` 파일은 절대 Git에 커밋하지 마세요
   - `environment-variables.md`는 실제 값 없이 가이드만 제공

2. **키 생성 권장사항**:

   - JWT_SECRET: 최소 32자 이상의 복잡한 문자열
   - AES_SECRET_KEY: 정확히 32자의 랜덤 문자열

3. **운영환경 설정**:
   - GitHub Secrets에 모든 환경변수 등록
   - EC2 환경변수로도 설정 가능

## 🔧 트러블슈팅

### 환경변수가 인식되지 않을 때

1. IDE 재시작
2. 환경변수 이름 확인 (대소문자 구분)
3. 애플리케이션 재시작

### 데이터베이스 연결 실패

1. DATABASE_URL 형식 확인
2. 네트워크 연결 상태 확인
3. 데이터베이스 서버 상태 확인
