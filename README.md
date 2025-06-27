# SuperLawVA

법률 서비스를 위한 Spring Boot 백엔드 애플리케이션

## 🚀 배포

- **배포 브랜치**: `back/production`
- **배포 서버**: http://43.203.127.128:8080
- **자동 배포**: `back/production` 브랜치에 푸시하면 GitHub Actions를 통해 자동 배포

## 📋 주요 기능

- 사용자 인증 (JWT)
- OAuth2 소셜 로그인 (카카오, 네이버)
- 사용자 관리
- 로깅 시스템
- 이메일 인증.

## 🛠 기술 스택

- **Backend**: Spring Boot 3.3.0, Java 17
- **Database**: MySQL, Redis
- **Security**: Spring Security, JWT
- **Build**: Gradle
- **Deploy**: Docker, GitHub Actions, AWS EC2

## 🌐 API 엔드포인트

- **헬스체크**: `GET /actuator/health`
- **회원가입**: `POST /auth/signup`
- **로그인**: `POST /auth/login`
- **사용자 관리**: `/users/**`
- **로깅**: `/log/**`

## 🔧 환경 변수

필요한 환경 변수들은 GitHub Secrets에 저장되어 있습니다:

- `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`, `AES_SECRET_KEY`
- `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`
- `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`
- `MAIL_USERNAME`, `MAIL_PASSWORD`

<!-- ECR 배포 테스트 -->
