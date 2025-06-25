# SuperLawVA HTTPS 키스토어 생성 스크립트
Write-Host "🔐 SuperLawVA HTTPS 키스토어 생성 중..." -ForegroundColor Green

# 디렉토리 확인
if (-not (Test-Path "src\main\resources")) {
    Write-Host "❌ src\main\resources 디렉토리가 없습니다." -ForegroundColor Red
    exit 1
}

# 기존 키스토어 백업
if (Test-Path "src\main\resources\keystore.p12") {
    Write-Host "📦 기존 키스토어 백업 중..." -ForegroundColor Yellow
    Copy-Item "src\main\resources\keystore.p12" "src\main\resources\keystore.p12.backup"
}

Write-Host "🔑 키스토어 생성 중..." -ForegroundColor Cyan

# keytool 명령어 실행
try {
    $keytoolArgs = @(
        "-genkeypair",
        "-alias", "superlawva",
        "-keyalg", "RSA",
        "-keysize", "2048",
        "-storetype", "PKCS12",
        "-keystore", "src\main\resources\keystore.p12",
        "-validity", "365",
        "-storepass", "superlawva123!",
        "-keypass", "superlawva123!",
        "-dname", "CN=localhost, OU=SuperLawVA, O=SuperLawVA, L=Seoul, S=Seoul, C=KR"
    )
    
    & keytool @keytoolArgs
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 키스토어가 성공적으로 생성되었습니다!" -ForegroundColor Green
        Write-Host "📁 위치: src\main\resources\keystore.p12" -ForegroundColor White
        Write-Host "🔒 패스워드: superlawva123!" -ForegroundColor White
        Write-Host "🏷️ 별칭: superlawva" -ForegroundColor White
        Write-Host ""
        Write-Host "🚀 HTTPS로 애플리케이션을 실행하려면:" -ForegroundColor Cyan
        Write-Host "   .\gradlew.bat bootRun --args='--spring.profiles.active=ssl'" -ForegroundColor White
        Write-Host ""
        Write-Host "🌐 접속 URL: https://localhost:8443" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "⚠️  브라우저에서 '안전하지 않음' 경고가 나타나면:" -ForegroundColor Yellow
        Write-Host "   Chrome: '고급' → '안전하지 않음(localhost)으로 이동'" -ForegroundColor White
        Write-Host "   또는 'thisisunsafe' 타이핑" -ForegroundColor White
    } else {
        Write-Host "❌ 키스토어 생성에 실패했습니다." -ForegroundColor Red
    }
} catch {
    Write-Host "❌ keytool 명령어 실행 중 오류가 발생했습니다." -ForegroundColor Red
    Write-Host "Java가 설치되어 있고 PATH에 설정되어 있는지 확인하세요." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "아무 키나 누르면 종료됩니다..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") 