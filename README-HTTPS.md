# SuperLawVA HTTPS 설정 가이드

## 🚀 빠른 시작

### 1단계: 키스토어 생성

```powershell
.\create-keystore.ps1
```

### 2단계: HTTPS로 실행

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=ssl"
```

### 3단계: 접속 확인

브라우저에서 https://localhost:8443 접속

## 📋 설정 정보

### 키스토어 정보

- **파일**: `src/main/resources/keystore.p12`
- **패스워드**: `superlawva123!`
- **별칭**: `superlawva`
- **유효기간**: 365일

### 접속 URL

- **HTTPS**: https://localhost:8443
- **HTTP**: http://localhost:8080

## 🔧 수동 설정

### 키스토어 수동 생성

```powershell
keytool -genkeypair -alias superlawva -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src\main\resources\keystore.p12 -validity 365 -storepass superlawva123! -keypass superlawva123! -dname "CN=localhost, OU=SuperLawVA, O=SuperLawVA, L=Seoul, S=Seoul, C=KR"
```

### 프로파일별 실행

```powershell
# HTTP (기본)
.\gradlew.bat bootRun

# HTTPS
.\gradlew.bat bootRun --args="--spring.profiles.active=ssl"

# 로컬 개발
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

## ⚠️ 브라우저 경고 해결

자체 서명 인증서이므로 "안전하지 않음" 경고가 나타납니다:

### Chrome

1. "고급" 클릭
2. "안전하지 않음(localhost)으로 이동" 클릭
3. 또는 주소창에 `thisisunsafe` 타이핑

### Firefox

1. "고급" 클릭
2. "예외 추가" 클릭
3. "localhost:8443" 추가

## 🐛 문제 해결

### 키스토어 생성 실패

- Java가 설치되어 있는지 확인: `java -version`
- PATH에 Java가 설정되어 있는지 확인

### HTTPS 접속 불가

- 키스토어 파일이 있는지 확인
- 패스워드가 `superlawva123!`인지 확인
- 포트 8443이 사용 가능한지 확인

### OAuth2 오류

HTTPS 사용 시 카카오/네이버 개발자 콘솔에서 리다이렉트 URI를 HTTPS로 변경:

- https://localhost:8443/login/oauth2/code/kakao
- https://localhost:8443/login/oauth2/code/naver
