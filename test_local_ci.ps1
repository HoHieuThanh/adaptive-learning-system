# ==============================================================================
# SCRIPT KIEM THU LOCAL CI TRUOC KHI PUSH LEN GITHUB ACTIONS
# He Thong AI-Powered Adaptive Learning & Student Performance Prediction
# ==============================================================================

$ErrorActionPreference = "Continue"

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " BAT DAU CHAY THU TOAN BO PIPELINE CI TAI MOI TRUONG LOCAL" -ForegroundColor Cyan
Write-Host "==================================================================`n" -ForegroundColor Cyan

$PassedStages = 0
$TotalStages = 3

# ------------------------------------------------------------------------------
# 1. FRONTEND CI
# ------------------------------------------------------------------------------
Write-Host "--- [STAGE 1/3] FRONTEND: UNIT TEST VA BUILD VITE ---" -ForegroundColor Yellow
Push-Location frontend
try {
    Write-Host "-> Dang chay npm test..." -ForegroundColor Gray
    npm test
    if ($LASTEXITCODE -ne 0) { throw "npm test that bai" }

    Write-Host "-> Dang chay npm run build..." -ForegroundColor Gray
    npm run build
    if ($LASTEXITCODE -ne 0) { throw "npm run build that bai" }

    Write-Host "[PASS] Stage 1: Frontend CI thanh cong!`n" -ForegroundColor Green
    $PassedStages++
} catch {
    Write-Host "[FAIL] Stage 1: Frontend CI gap loi: $_`n" -ForegroundColor Red
} finally {
    Pop-Location
}

# ------------------------------------------------------------------------------
# 2. BACKEND CI
# ------------------------------------------------------------------------------
Write-Host "--- [STAGE 2/3] BACKEND: MAVEN TEST VA DONG GOI JAR ---" -ForegroundColor Yellow
Push-Location backend
try {
    Write-Host "-> Dang chay Maven test..." -ForegroundColor Gray
    .\mvnw.cmd test -B
    if ($LASTEXITCODE -ne 0) { throw "Maven test that bai" }

    Write-Host "-> Dang chay Maven package..." -ForegroundColor Gray
    .\mvnw.cmd package -DskipTests -B
    if ($LASTEXITCODE -ne 0) { throw "Maven package that bai" }

    Write-Host "[PASS] Stage 2: Backend CI thanh cong!`n" -ForegroundColor Green
    $PassedStages++
} catch {
    Write-Host "[FAIL] Stage 2: Backend CI gap loi: $_`n" -ForegroundColor Red
} finally {
    Pop-Location
}

# ------------------------------------------------------------------------------
# 3. ML SERVICE CI
# ------------------------------------------------------------------------------
Write-Host "--- [STAGE 3/3] ML SERVICE: PYTEST & MODEL VALIDATION QUALITY GATES ---" -ForegroundColor Yellow
Push-Location ml-service
try {
    Write-Host "-> Dang chay pytest kem kiem tra nguong chat luong mo hinh (Accuracy >= 80%, F1 >= 0.80)..." -ForegroundColor Gray
    .\venv\Scripts\python.exe -m pytest tests -v
    if ($LASTEXITCODE -ne 0) { throw "pytest that bai" }

    Write-Host "[PASS] Stage 3: ML Service CI thanh cong!`n" -ForegroundColor Green
    $PassedStages++
} catch {
    Write-Host "[FAIL] Stage 3: ML Service CI gap loi: $_`n" -ForegroundColor Red
} finally {
    Pop-Location
}

# ------------------------------------------------------------------------------
# TONG KET
# ------------------------------------------------------------------------------
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host " KET QUA KIEM THU LOCAL CI" -ForegroundColor Cyan
Write-Host " Tong so stages: $TotalStages" -ForegroundColor White
Write-Host " Thanh cong:     $PassedStages" -ForegroundColor Green
Write-Host " That bai:       $($TotalStages - $PassedStages)" -ForegroundColor $(if ($PassedStages -eq $TotalStages) { "Green" } else { "Red" })
Write-Host " Ty le dat:      $([Math]::Round(($PassedStages / $TotalStages) * 100, 2))%" -ForegroundColor Yellow
Write-Host "==================================================================`n" -ForegroundColor Cyan

if ($PassedStages -eq $TotalStages) {
    Write-Host "San sang push len GitHub! GitHub Actions se PASS 100%!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "Co loi xay ra trong qua trinh kiem thu local. Vui long kiem tra!" -ForegroundColor Red
    exit 1
}

