-- ============================================================================
-- SuperLawVA Users 테이블 수정 스크립트
-- 문제점: 중복 unique key, 불필요한 인덱스, 컬럼명 표준화
-- ============================================================================

USE superlawva;

-- 1. 기존 중복 인덱스 제거
-- ============================================================================

-- kakao_id 중복 unique 제거 (테이블 레벨 인덱스만 유지)
ALTER TABLE users DROP INDEX IF EXISTS kakaoId;

-- naver_id 중복 unique 제거 (테이블 레벨 인덱스만 유지)  
ALTER TABLE users DROP INDEX IF EXISTS naverId;

-- email_hash 중복 unique 제거 (테이블 레벨 인덱스만 유지)
ALTER TABLE users DROP INDEX IF EXISTS emailHash;

-- 2. 컬럼명 표준화 (snake_case로 통일)
-- ============================================================================

-- 컬럼명을 snake_case로 변경
ALTER TABLE users CHANGE COLUMN kakaoId kakao_id BIGINT NULL;
ALTER TABLE users CHANGE COLUMN naverId naver_id VARCHAR(255) NULL;
ALTER TABLE users CHANGE COLUMN emailHash email_hash VARCHAR(255) NOT NULL;
ALTER TABLE users CHANGE COLUMN createdAt created_at DATETIME NOT NULL;
ALTER TABLE users CHANGE COLUMN updatedAt updated_at DATETIME NULL;
ALTER TABLE users CHANGE COLUMN emailVerified email_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- 3. 적절한 인덱스 재생성
-- ============================================================================

-- kakao_id 인덱스 (NULL 값 허용, 중복 방지)
CREATE UNIQUE INDEX idx_kakao_id ON users(kakao_id);

-- naver_id 인덱스 (NULL 값 허용, 중복 방지)  
CREATE UNIQUE INDEX idx_naver_id ON users(naver_id);

-- email_hash 인덱스 (로그인 성능 최적화)
CREATE UNIQUE INDEX idx_email_hash ON users(email_hash);

-- email 인덱스 추가 (검색 성능 향상)
CREATE INDEX idx_email ON users(email);

-- provider 인덱스 추가 (사용자 유형별 조회 최적화)
CREATE INDEX idx_provider ON users(provider);

-- 4. 테이블 구조 최종 확인
-- ============================================================================

DESCRIBE users;

-- 5. 인덱스 확인
-- ============================================================================

SHOW INDEX FROM users;

-- ============================================================================
-- 수정 완료 
-- 
-- 변경사항:
-- 1. 중복 unique key 제거 (kakao_id, naver_id, email_hash)
-- 2. 컬럼명 snake_case로 표준화  
-- 3. 적절한 인덱스 재구성
-- 4. email 필드에도 인덱스 추가 (검색 성능 향상)
-- 5. provider 인덱스 추가
-- ============================================================================ 