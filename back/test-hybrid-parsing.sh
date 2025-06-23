#!/bin/bash

# 🚀 하이브리드 파싱 테스트 스크립트
# 사용법: ./test-hybrid-parsing.sh [API_KEY]

echo "🎯 하이브리드 계약서 파싱 테스트 시작..."

# API 키 설정
if [ -n "$1" ]; then
    export OPENAI_API_KEY="$1"
    export OPENAI_ENABLED=true
    echo "✅ OpenAI API 키가 설정되었습니다. (Enhanced 모드 활성화)"
else
    export OPENAI_ENABLED=false
    echo "⚠️ OpenAI API 키가 없습니다. (기본 모드만 테스트)"
fi

# 프로젝트 빌드
echo "🔨 프로젝트 빌드 중..."
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "❌ 빌드 실패! 빌드 오류를 확인하세요."
    exit 1
fi

echo "✅ 빌드 완료!"

# 테스트 실행
echo "🧪 하이브리드 파싱 테스트 실행 중..."

echo ""
echo "📋 1. OpenAI 서비스 활성화 상태 테스트"
./gradlew test --tests "HybridContractAnalysisTest.testOpenAiServiceActivation" --info

echo ""
echo "📋 2. 기본 정규식 파싱 테스트"
./gradlew test --tests "HybridContractAnalysisTest.testBasicRegexParsing" --info

echo ""
echo "📋 3. Enhanced 하이브리드 파싱 테스트"
./gradlew test --tests "HybridContractAnalysisTest.testEnhancedHybridParsing" --info

echo ""
echo "📋 4. 기본 vs Enhanced 비교 테스트"
./gradlew test --tests "HybridContractAnalysisTest.testBasicVsEnhancedComparison" --info

if [ "$OPENAI_ENABLED" = "true" ] && [ -n "$OPENAI_API_KEY" ]; then
    echo ""
    echo "📋 5. LLM 보완 특정 시나리오 테스트"
    ./gradlew test --tests "HybridContractAnalysisTest.testLLMEnhancementScenarios" --info
    
    echo ""
    echo "📋 6. API 통합 테스트 (Enhanced 모드)"
    ./gradlew test --tests "HybridOcrIntegrationTest.testEnhancedHybridParsingAPI" --info
    
    echo ""
    echo "📋 7. 성능 비교 테스트"
    ./gradlew test --tests "HybridOcrIntegrationTest.testHybridParsingPerformanceComparison" --info
    
    echo ""
    echo "🎉 모든 테스트 완료! (Enhanced 모드)"
    echo "📊 결과 요약:"
    echo "   - 기본 파싱: 정규식 기반, 빠른 처리"
    echo "   - Enhanced 파싱: 정규식 + LLM 보완, 높은 정확도"
    echo "   - LLM 보완으로 누락된 필드 자동 추출"
else
    echo ""
    echo "⚠️ OpenAI API 키가 없어 Enhanced 모드 테스트는 건너뜁니다."
    echo "💡 Enhanced 모드 테스트를 원하시면 다음과 같이 실행하세요:"
    echo "   ./test-hybrid-parsing.sh 'your-openai-api-key'"
    
    echo ""
    echo "📋 5. API 통합 테스트 (기본 모드)"
    ./gradlew test --tests "HybridOcrIntegrationTest.testBasicRegexParsingAPI" --info
    
    echo ""
    echo "✅ 기본 모드 테스트 완료!"
    echo "📊 결과 요약:"
    echo "   - 정규식 기반 파싱만 사용"
    echo "   - 안정적이고 빠른 처리"
    echo "   - OpenAI 설정 시 더 정확한 파싱 가능"
fi

echo ""
echo "🔗 추가 테스트 방법:"
echo "   1. 애플리케이션 실행: ./gradlew bootRun"
echo "   2. Swagger UI 접속: http://localhost:8080/swagger-ui.html"
echo "   3. /api/upload/ocr 엔드포인트에서 파일 업로드 테스트"
echo ""
echo "📚 자세한 사용법은 HYBRID_PARSING_GUIDE.md를 참고하세요." 