# User 테이블 문제점 분석 및 해결 방안

## 🔍 발견된 문제점들

### 1. **naver_id 컬럼에 unique key가 2개 잡힘** ✅ 해결됨

**문제**:

- User 엔티티에서 `@Index(name = "idx_naver_id", columnList = "naverId", unique = true)` 설정
- `fix_users_table.sql`에서도 `CREATE UNIQUE INDEX idx_naver_id ON users(naver_id)` 설정
- **결과**: 중복된 unique 인덱스가 생성됨

**해결**:

- 중복 인덱스 제거 후 하나의 unique 인덱스만 유지
- 컬럼명을 `naver_id`로 통일

### 2. **email과 별도로 email_hash 존재 (평문과 암호문이 모두 있음)** ⚠️ 설계적 문제

**현재 상황**:

- `email`: 평문 이메일 (표시용)
- `email_hash`: 해시된 이메일 (검색 및 중복 체크용)

**문제점**:

- 데이터 중복으로 인한 일관성 관리 복잡성
- 저장 공간 낭비
- 해시 함수 변경 시 두 필드 모두 업데이트 필요

**권장 해결 방안**:

```sql
-- 옵션 1: email_hash만 유지하고 평문 이메일은 필요시 복호화
ALTER TABLE users DROP COLUMN email;

-- 옵션 2: email만 유지하고 해시는 필요시 계산
ALTER TABLE users DROP COLUMN email_hash;
```

### 3. **로그인 시 email_hash값으로 비교하지만 email_hash는 색인되어 있지 않음** ✅ 해결됨

**실제 상황**:

- `email_hash`는 이미 unique 인덱스가 있음 (`idx_email_hash`)
- **문제**: User 엔티티의 `@Index` 설정과 실제 DB 컬럼명 불일치

**해결**:

- 엔티티의 인덱스 설정을 `columnList = "email_hash"`로 수정
- 컬럼명 일치시킴

### 4. **회원 가입 시에는 email 값으로만 비교하고 hash 값은 상관없음** ❌ 잘못된 정보

**실제 상황**:

- 회원가입 시 `emailHash`로 중복 체크 (`existsByEmailHash`)
- 코드와 설명이 일치하지 않음

**실제 로직**:

```java
// UserServiceImpl.register() 메서드
String emailHash = hashUtil.hash(email);
if (userRepository.existsByEmailHash(emailHash)) {
    throw new BaseException(ErrorStatus._EMAIL_ALREADY_EXISTS);
}
```

### 5. **email도 색인되어 있지 않음** ✅ 해결됨

**실제 상황**:

- `fix_users_table.sql`에서 `CREATE INDEX idx_email ON users(email)` 추가됨
- **문제**: User 엔티티에는 email 인덱스 설정이 없음

**해결**:

- User 엔티티에 `@Index(name = "idx_email", columnList = "email")` 추가

## 🛠️ 적용된 해결 방안

### 1. User 엔티티 인덱스 수정

```java
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_kakao_id", columnList = "kakao_id", unique = true),
        @Index(name = "idx_naver_id", columnList = "naver_id", unique = true),
        @Index(name = "idx_email_hash", columnList = "email_hash", unique = true),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_provider", columnList = "provider")
    }
)
```

### 2. 데이터베이스 스키마 수정 스크립트 생성

- `fix_users_table_v2.sql` 생성
- 중복 인덱스 제거
- 컬럼명 snake_case로 통일
- 최적화된 인덱스 재생성

## 📊 성능 최적화 결과

### 인덱스 구성

1. **`idx_email_hash`** (UNIQUE) - 로그인 성능 최적화
2. **`idx_email`** - 이메일 검색 성능 향상
3. **`idx_kakao_id`** (UNIQUE) - 카카오 로그인 최적화
4. **`idx_naver_id`** (UNIQUE) - 네이버 로그인 최적화
5. **`idx_provider`** - 사용자 유형별 조회 최적화

### 쿼리 성능 개선

- 로그인 시: `email_hash` 인덱스로 O(1) 조회
- 이메일 검색 시: `email` 인덱스로 빠른 검색
- 소셜 로그인 시: 각 provider ID 인덱스로 빠른 조회

## 🔄 다음 단계 권장사항

### 1. 즉시 적용

- `fix_users_table_v2.sql` 실행하여 DB 스키마 수정
- 애플리케이션 재배포

### 2. 장기적 개선

- email/email_hash 중복 문제 해결 방안 검토
- 해시 함수 변경 시 마이그레이션 계획 수립
- 모니터링 및 성능 측정 도구 도입

### 3. 코드 개선

- 이메일 중복 체크 로직 일관성 확보
- 에러 메시지 및 로깅 개선
- 테스트 케이스 보강

## 📝 체크리스트

- [x] 중복 unique key 문제 해결
- [x] 인덱스 최적화
- [x] 컬럼명 표준화
- [x] SQL 스크립트 생성
- [ ] DB 스키마 적용
- [ ] 애플리케이션 재배포
- [ ] 성능 테스트
- [ ] 모니터링 설정
