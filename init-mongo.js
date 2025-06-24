// MongoDB 초기화 스크립트
// contract 및 generated_contract 컬렉션 설정

print("🚀 MongoDB 초기화 시작...");

// superlawva_docs 데이터베이스로 전환
db = db.getSiblingDB("superlawva_docs");

// contract 컬렉션 생성 및 인덱스 설정 (OCR3 전용)
db.createCollection("contract");
db.contract.createIndex({ user_id: 1 }); // 👤 사용자별 계약서 조회용

print("✅ contract 컬렉션 및 인덱스 생성 완료");

// generated_contract 컬렉션 생성 및 인덱스 설정
db.createCollection("generated_contract");
db.generated_contract.createIndex({ user_id: 1 }); // 👤 사용자별 생성 계약서 조회용

print("✅ generated_contract 컬렉션 및 인덱스 생성 완료");

// 샘플 계약서 데이터 삽입 (테스트용)
const sampleContract = {
  user_id: "1", // 🔗 MySQL user 테이블의 user_id 참조
  contract_type: "전세",
  dates: {
    contract_date: "2024-06-19",
    start_date: "2024-07-01",
    end_date: "2026-06-30",
  },
  property: {
    address: "서울시 강남구 역삼동 123-45",
    building: {
      building_type: "아파트",
      building_area: "84.21",
    },
  },
  payment: {
    deposit: 300000000,
    deposit_kr: "삼억원정",
  },
  lessor: { name: "김임대" },
  lessee: { name: "이임차" },
  generated: false,
  file_url: "file://sample-contract.jpg",
  created_date: new Date(),
  modified_date: new Date(),
  contract_metadata: {
    model: "doc-ai:test + gemini:test",
    generation_time: 5.0,
    version: "v3.1.0",
  },
};

db.contract.insertOne(sampleContract);
print("✅ 샘플 계약서 데이터 삽입 완료");

const sampleGeneratedContract = {
  user_id: "1", // 🔗 MySQL user 테이블의 user_id 참조
  contract_id: "sample-contract-id", // 🔗 contract 컬렉션의 _id 참조
  generation_type: "CONTRACT_GENERATION",
  request_data: '{"contractType": "전세", "sample": true}',
  generation_metadata: '{"generationTime": 2.5, "contractType": "전세"}',
  model_name: "contract-generator-v1",
  model_version: "1.0.0",
  generation_time_seconds: 2.5,
  token_count: 500,
  quality_score: 85,
  status: "GENERATED",
  created_at: new Date(),
  updated_at: new Date(),
  additional_metadata: {
    sample: true,
    test_environment: true,
  },
};

db.generated_contract.insertOne(sampleGeneratedContract);
print("✅ 샘플 생성 계약서 데이터 삽입 완료");

// 컬렉션 통계 출력
print("\n📊 컬렉션 통계:");
print("contract 문서 수:", db.contract.countDocuments({}));
print("generated_contract 문서 수:", db.generated_contract.countDocuments({}));

print("\n🎉 MongoDB 초기화 완료!");
print("📌 접속 정보:");
print("  - MongoDB URL: mongodb://localhost:27017/superlawva_docs");
print("  - 관리자 계정: admin / password");
print("  - Mongo Express: http://localhost:8081");
print("  - 데이터베이스: superlawva_docs");
print("  - 컬렉션: contract, generated_contract");
print("  - 핵심 인덱스: user_id (사용자별 조회)");

/*
// 🗂️ 제거된 컬렉션 및 인덱스 (사용 안 함)
//
// documents 컬렉션 (제거됨 - 필요없음)
// db.createCollection("documents");
// db.documents.createIndex({ user_id: 1 });
// db.documents.createIndex({ created_at: -1 });
// db.documents.createIndex({ document_type: 1 });
// db.documents.createIndex({ status: 1 });
// db.documents.createIndex({ gridfs_file_id: 1 }, { sparse: true });
//
// contract 컬렉션 추가 인덱스 (제거됨 - user_id만 필요)
// db.contract.createIndex({ created_date: -1 });
// db.contract.createIndex({ contract_type: 1 });
// db.contract.createIndex({ "lessor.name": 1 }, { sparse: true });
// db.contract.createIndex({ "lessee.name": 1 }, { sparse: true });
//
// generated_contract 컬렉션 추가 인덱스 (제거됨 - user_id만 필요)
// db.generated_contract.createIndex({ contract_id: 1 });
// db.generated_contract.createIndex({ generation_type: 1 });
// db.generated_contract.createIndex({ created_at: -1 });
// db.generated_contract.createIndex({ status: 1 });
// db.generated_contract.createIndex({ user_rating: 1 }, { sparse: true });
// db.generated_contract.createIndex({ model_name: 1 });
*/
