# 🚀 하이브리드 파싱 테스트 스크립트 (PowerShell)
# 사용법: .\test-hybrid-parsing.ps1 [API_KEY]

param(
    [string]$ApiKey = ""
)

Write-Host "🎯 하이브리드 계약서 파싱 테스트 시작..." -ForegroundColor Green

# API 키 설정
if ($ApiKey -ne "") {
    $env:OPENAI_API_KEY = $ApiKey
    $env:OPENAI_ENABLED = "true"
    Write-Host "✅ OpenAI API 키가 설정되었습니다. (Enhanced 모드 활성화)" -ForegroundColor Green
} else {
    $env:OPENAI_ENABLED = "false"
    Write-Host "⚠️ OpenAI API 키가 없습니다. (기본 모드만 테스트)" -ForegroundColor Yellow
}

# 프로젝트 빌드
Write-Host "🔨 프로젝트 빌드 중..." -ForegroundColor Blue
try {
    .\gradlew.bat clean build -x test
    if ($LASTEXITCODE -ne 0) {
        throw "빌드 실패"
    }
    Write-Host "✅ 빌드 완료!" -ForegroundColor Green
} catch {
    Write-Host "❌ 빌드 실패! 빌드 오류를 확인하세요." -ForegroundColor Red
    exit 1
}

# 테스트 실행
Write-Host "🧪 하이브리드 파싱 테스트 실행 중..." -ForegroundColor Blue

Write-Host ""
Write-Host "📋 1. OpenAI 서비스 활성화 상태 테스트" -ForegroundColor Cyan
.\gradlew.bat test --tests "HybridContractAnalysisTest.testOpenAiServiceActivation"

Write-Host ""
Write-Host "📋 2. 기본 정규식 파싱 테스트" -ForegroundColor Cyan
.\gradlew.bat test --tests "HybridContractAnalysisTest.testBasicRegexParsing"

Write-Host ""
Write-Host "📋 3. Enhanced 하이브리드 파싱 테스트" -ForegroundColor Cyan
.\gradlew.bat test --tests "HybridContractAnalysisTest.testEnhancedHybridParsing"

Write-Host ""
Write-Host "📋 4. 기본 vs Enhanced 비교 테스트" -ForegroundColor Cyan
.\gradlew.bat test --tests "HybridContractAnalysisTest.testBasicVsEnhancedComparison"

if ($env:OPENAI_ENABLED -eq "true" -and $env:OPENAI_API_KEY -ne "") {
    Write-Host ""
    Write-Host "📋 5. LLM 보완 특정 시나리오 테스트" -ForegroundColor Cyan
    .\gradlew.bat test --tests "HybridContractAnalysisTest.testLLMEnhancementScenarios"
    
    Write-Host ""
    Write-Host "📋 6. API 통합 테스트 (Enhanced 모드)" -ForegroundColor Cyan
    .\gradlew.bat test --tests "HybridOcrIntegrationTest.testEnhancedHybridParsingAPI"
    
    Write-Host ""
    Write-Host "📋 7. 성능 비교 테스트" -ForegroundColor Cyan
    .\gradlew.bat test --tests "HybridOcrIntegrationTest.testHybridParsingPerformanceComparison"
    
    Write-Host ""
    Write-Host "🎉 모든 테스트 완료! (Enhanced 모드)" -ForegroundColor Green
    Write-Host "📊 결과 요약:" -ForegroundColor Yellow
    Write-Host "   - 기본 파싱: 정규식 기반, 빠른 처리" -ForegroundColor White
    Write-Host "   - Enhanced 파싱: 정규식 + LLM 보완, 높은 정확도" -ForegroundColor White
    Write-Host "   - LLM 보완으로 누락된 필드 자동 추출" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "⚠️ OpenAI API 키가 없어 Enhanced 모드 테스트는 건너뜁니다." -ForegroundColor Yellow
    Write-Host "💡 Enhanced 모드 테스트를 원하시면 다음과 같이 실행하세요:" -ForegroundColor Cyan
    Write-Host "   .\test-hybrid-parsing.ps1 'your-openai-api-key'" -ForegroundColor White
    
    Write-Host ""
    Write-Host "📋 5. API 통합 테스트 (기본 모드)" -ForegroundColor Cyan
    .\gradlew.bat test --tests "HybridOcrIntegrationTest.testBasicRegexParsingAPI"
    
    Write-Host ""
    Write-Host "✅ 기본 모드 테스트 완료!" -ForegroundColor Green
    Write-Host "📊 결과 요약:" -ForegroundColor Yellow
    Write-Host "   - 정규식 기반 파싱만 사용" -ForegroundColor White
    Write-Host "   - 안정적이고 빠른 처리" -ForegroundColor White
    Write-Host "   - OpenAI 설정 시 더 정확한 파싱 가능" -ForegroundColor White
}

Write-Host ""
Write-Host "🔗 추가 테스트 방법:" -ForegroundColor Magenta
Write-Host "   1. 애플리케이션 실행: .\gradlew.bat bootRun" -ForegroundColor White
Write-Host "   2. Swagger UI 접속: http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "   3. /api/upload/ocr 엔드포인트에서 파일 업로드 테스트" -ForegroundColor White
Write-Host ""
Write-Host "📚 자세한 사용법은 HYBRID_PARSING_GUIDE.md를 참고하세요." -ForegroundColor Cyan 