# 📄 MongoDB 문서 저장 시스템 가이드

## 🎯 개요

이 프로젝트는 **하이브리드 데이터베이스 구조**를 사용합니다:

- **MySQL/H2**: 사용자 관리, 로그, OCR 결과 등 구조화된 데이터
- **MongoDB**: 문서 저장, 파일 관리, AI 생성 문서 메타데이터

## 🚀 MongoDB 실행 방법

### 1. Docker Compose로 실행 (권장)

```bash
# MongoDB와 Mongo Express 실행
docker-compose -f docker-compose-mongodb.yml up -d

# 로그 확인
docker-compose -f docker-compose-mongodb.yml logs -f

# 중지
docker-compose -f docker-compose-mongodb.yml down
```

### 2. 직접 설치

```bash
# MongoDB 설치 (macOS)
brew install mongodb-community

# MongoDB 실행
brew services start mongodb-community

# 또는 직접 실행
mongod --config /usr/local/etc/mongod.conf
```

## 📊 MongoDB 관리

### Mongo Express (웹 UI)

- URL: http://localhost:8081
- 사용자명/비밀번호: admin/password (Docker 환경)

### MongoDB Shell 접속

```bash
# Docker 컨테이너에 접속
docker exec -it superlawva-mongodb mongosh

# 또는 로컬 설치 시
mongosh mongodb://localhost:27017/superlawva_docs
```

### 주요 컬렉션

1. **documents** - 기본 문서 정보

   ```javascript
   db.documents.find().pretty();
   db.documents.countDocuments();
   ```

2. **generated_documents** - AI 생성 문서 메타데이터

   ```javascript
   db.generated_documents.find().pretty();
   ```

3. **fs.files, fs.chunks** - GridFS 파일 저장
   ```javascript
   db.fs.files.find().pretty();
   ```

## 🗂️ 데이터 구조

### Document 컬렉션

```json
{
  "_id": "ObjectId",
  "user_id": 1,
  "original_filename": "계약서.pdf",
  "document_type": "LEASE_JEONSE",
  "mime_type": "application/pdf",
  "file_size_bytes": 1024000,
  "storage_type": "GRIDFS",
  "gridfs_file_id": "ObjectId",
  "status": "UPLOADED",
  "created_at": "2024-01-01T00:00:00Z",
  "metadata": {
    "uploadTime": 1704067200000,
    "version": "1.0"
  }
}
```

### GeneratedDocument 컬렉션

```json
{
  "_id": "ObjectId",
  "user_id": 1,
  "document_id": "ObjectId",
  "generation_type": "CONTRACT_GENERATION",
  "model_name": "contract-generator-v1",
  "generation_time_seconds": 2.5,
  "quality_score": 85,
  "status": "GENERATED"
}
```

## 🔧 API 엔드포인트

### 문서 관리 API

```
POST   /api/documents              # 문서 생성
GET    /api/documents              # 문서 목록 조회
GET    /api/documents/{id}         # 문서 상세 조회
DELETE /api/documents/{id}         # 문서 삭제
```

### AI 문서 생성 API

```
POST   /api/generate/contract      # AI 계약서 생성
POST   /api/generate/proof-content # AI 증명서 생성
GET    /api/generate/documents     # 생성된 문서 목록
```

### OCR 처리 API

```
POST   /api/upload/ocr             # 파일 업로드 + OCR + AI 분석
```

## 📁 파일 저장 방식

### GridFS vs 인라인 저장

- **1MB 이상**: GridFS 저장 (대용량 파일)
- **1MB 미만**: 인라인 저장 (Document 내부)

### 파일 업로드 과정

1. 파일 업로드 → MongoDB Document 생성
2. 파일 크기 확인
3. GridFS 또는 인라인 저장 선택
4. OCR 처리 및 AI 분석

## 🛠️ 환경 설정

### application.yml 설정

```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: superlawva_docs
      # username: admin
      # password: password
```

### 프로덕션 환경

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/superlawva_docs}
```

## 🔍 모니터링 및 디버깅

### 로그 설정

```yaml
logging:
  level:
    org.springframework.data.mongodb: DEBUG
```

### 성능 모니터링

```javascript
// 느린 쿼리 모니터링
db.setProfilingLevel(1, { slowms: 100 });

// 인덱스 사용 확인
db.documents.find({ user_id: 1 }).explain("executionStats");
```

## 🚨 문제 해결

### 1. 연결 실패

```bash
# MongoDB 실행 상태 확인
docker ps | grep mongodb
brew services list | grep mongodb
```

### 2. 메모리 부족

```yaml
# Docker Compose에서 메모리 제한 설정
services:
  mongodb:
    deploy:
      resources:
        limits:
          memory: 1G
```

### 3. GridFS 파일 조회

```javascript
// GridFS 파일 목록
db.fs.files.find().pretty();

// 특정 파일 조회
db.fs.files.find({ filename: "계약서.pdf" });
```

## 📈 성능 최적화

### 1. 인덱스 최적화

```javascript
// 복합 인덱스 생성
db.documents.createIndex({ user_id: 1, created_at: -1 });

// 인덱스 사용률 확인
db.documents.getIndexes();
```

### 2. 쿼리 최적화

```javascript
// 효율적인 쿼리 패턴
db.documents
  .find({
    user_id: 1,
    status: "UPLOADED",
  })
  .sort({ created_at: -1 })
  .limit(20);
```

## 🔄 데이터 마이그레이션

기존 MySQL 데이터를 MongoDB로 마이그레이션하는 스크립트:

```javascript
// 마이그레이션 예시
db.documents.insertMany([
  // MySQL에서 추출한 데이터를 MongoDB 형식으로 변환
]);
```

## 📝 추가 참고사항

- MongoDB는 문서 저장 전용으로 사용
- 기존 MySQL/H2는 사용자, 로그 등에 계속 사용
- GridFS는 16MB 이상 파일 자동 청킹
- 인덱스는 자동으로 생성되지만 성능 최적화를 위해 추가 설정 가능
