# ==============================================================================
# Script Kiem Tra & Nghiem Thu He Thong Dockerized (5 Services)
# He Thong AI-Powered Adaptive Learning & Student Performance Prediction
# ==============================================================================

param(
    [string]$FrontendUrl = "http://localhost:5173",
    [string]$BackendUrl  = "http://localhost:8080/api",
    [string]$MlUrl       = "http://localhost:8000",
    [string]$MlflowUrl   = "http://localhost:5000"
)

$ErrorActionPreference = "Continue"

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " BAT DAU KIEM THU DOCKER CONTAINERS VA ENDPOINTS HE THONG" -ForegroundColor Cyan
Write-Host "==================================================================`n" -ForegroundColor Cyan

$TotalTests = 0
$PassedTests = 0
$FailedTests = 0

function Assert-Step {
    param(
        [string]$TestName,
        [bool]$Condition,
        [string]$Details = ""
    )
    $script:TotalTests++
    if ($Condition) {
        $script:PassedTests++
        Write-Host "[PASS] $TestName" -ForegroundColor Green
        if ($Details) {
            Write-Host "       -> $Details" -ForegroundColor Gray
        }
    } else {
        $script:FailedTests++
        Write-Host "[FAIL] $TestName" -ForegroundColor Red
        if ($Details) {
            Write-Host "       -> Loi: $Details" -ForegroundColor Yellow
        }
    }
}

# ------------------------------------------------------------------------------
# 1. KIEM TRA TRANG THAI CUA 5 CONTAINERS
# ------------------------------------------------------------------------------
Write-Host "--- [BUOC 1] KIEM TRA TRANG THAI DOCKER CONTAINERS ---" -ForegroundColor Yellow

$containers = @("adaptive-postgres", "adaptive-ml-service", "adaptive-backend", "adaptive-frontend", "adaptive-mlflow")

foreach ($c in $containers) {
    try {
        $status = docker inspect --format '{{.State.Status}}' $c 2>$null
        $health = docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' $c 2>$null
        $isOk = ($status -eq "running")
        Assert-Step -TestName "Container $c dang chay" -Condition $isOk -Details "Status: $status, Health: $health"
    } catch {
        Assert-Step -TestName "Container $c dang chay" -Condition $false -Details $_.Exception.Message
    }
}

# ------------------------------------------------------------------------------
# 2. KIEM TRA POSTGRES DATABASE CONTAINER
# ------------------------------------------------------------------------------
Write-Host "`n--- [BUOC 2] KIEM TRA POSTGRES DATABASE CONTAINER ---" -ForegroundColor Yellow

try {
    $dbCheck = docker exec adaptive-postgres pg_isready -U adaptive_user -d adaptive_learning_db 2>$null
    $isDbReady = ($dbCheck -match "accepting connections")
    Assert-Step -TestName "PostgreSQL Database san sang nhan ket noi" -Condition $isDbReady -Details $dbCheck
} catch {
    Assert-Step -TestName "PostgreSQL Database san sang nhan ket noi" -Condition $false -Details $_.Exception.Message
}

# ------------------------------------------------------------------------------
# 3. KIEM TRA FASTAPI ML MICROSERVICE (PORT 8000)
# ------------------------------------------------------------------------------
Write-Host "`n--- [BUOC 3] KIEM TRA FASTAPI ML SERVICE (PORT 8000) ---" -ForegroundColor Yellow

try {
    $mlHealth = Invoke-RestMethod -Uri "$MlUrl/health" -Method Get -TimeoutSec 10
    Assert-Step -TestName "ML Service /health UP" -Condition ($mlHealth.status -eq "UP" -and $mlHealth.model_loaded -eq $true) -Details "Status: $($mlHealth.status), Models: $($mlHealth.available_models -join ', ')"
} catch {
    Assert-Step -TestName "ML Service /health UP" -Condition $false -Details $_.Exception.Message
}

# ------------------------------------------------------------------------------
# 4. KIEM TRA MLFLOW TRACKING SERVER (PORT 5000)
# ------------------------------------------------------------------------------
Write-Host "`n--- [BUOC 4] KIEM TRA MLFLOW TRACKING SERVER (PORT 5000) ---" -ForegroundColor Yellow

try {
    $mlflowRes = Invoke-WebRequest -Uri "$MlflowUrl" -UseBasicParsing -TimeoutSec 10
    Assert-Step -TestName "MLflow Server Dashboard hoat dong (HTTP 200)" -Condition ($mlflowRes.StatusCode -eq 200) -Details "HTTP Status: $($mlflowRes.StatusCode)"
} catch {
    Assert-Step -TestName "MLflow Server Dashboard hoat dong (HTTP 200)" -Condition $false -Details $_.Exception.Message
}

# ------------------------------------------------------------------------------
# 5. KIEM TRA SPRING BOOT BACKEND (PORT 8080)
# ------------------------------------------------------------------------------
Write-Host "`n--- [BUOC 5] KIEM TRA SPRING BOOT BACKEND (PORT 8080) ---" -ForegroundColor Yellow

try {
    $beHealth = Invoke-RestMethod -Uri "$BackendUrl/health" -Method Get -TimeoutSec 10
    Assert-Step -TestName "Backend /api/health UP" -Condition ($beHealth.status -eq "UP") -Details "Status: $($beHealth.status), Service: $($beHealth.service)"
} catch {
    Assert-Step -TestName "Backend /api/health UP" -Condition $false -Details $_.Exception.Message
}

$AdvisorAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("advisor:advisor123"))
$AdvisorHeaders = @{
    "Authorization" = "Basic $AdvisorAuth"
    "Content-Type"  = "application/json"
}

try {
    $studentsRes = Invoke-RestMethod -Uri "$BackendUrl/students" -Method Get -Headers $AdvisorHeaders -TimeoutSec 10
    $count = $studentsRes.data.Count
    Assert-Step -TestName "Backend nap du lieu 20 sinh vien tu PostgreSQL" -Condition ($count -ge 20) -Details "Da nap: $count sinh vien"
} catch {
    Assert-Step -TestName "Backend nap du lieu 20 sinh vien tu PostgreSQL" -Condition $false -Details $_.Exception.Message
}

# ------------------------------------------------------------------------------
# 6. KIEM TRA DOCKER NETWORK: BACKEND -> FASTAPI ML PREDICTION
# ------------------------------------------------------------------------------
Write-Host "`n--- [BUOC 6] KIEM TRA DOCKER NETWORK (BACKEND -> ML SERVICE LIVE PREDICTION) ---" -ForegroundColor Yellow

try {
    $bodyJson = '{"modelName": "xgboost"}'
    $predRes = Invoke-RestMethod -Uri "$BackendUrl/predictions/predict/3" -Method Post -Headers $AdvisorHeaders -Body $bodyJson -TimeoutSec 15
    $pred = $predRes.data
    Assert-Step -TestName "Backend goi ML Service qua Docker Network thanh cong" -Condition ($pred.predictedLevel -eq "Low") -Details "Predicted: $($pred.predictedLevel), Confidence: $([Math]::Round($pred.confidence * 100, 2))%, Model: $($pred.modelName)"
} catch {
    Assert-Step -TestName "Backend goi ML Service qua Docker Network thanh cong" -Condition $false -Details $_.Exception.Message
}

# ------------------------------------------------------------------------------
# 7. KIEM TRA FRONTEND NGINX WEB SERVER (PORT 5173)
# ------------------------------------------------------------------------------
Write-Host "`n--- [BUOC 7] KIEM TRA FRONTEND NGINX WEB SERVER (PORT 5173) ---" -ForegroundColor Yellow

try {
    $feRes = Invoke-WebRequest -Uri "$FrontendUrl/" -UseBasicParsing -TimeoutSec 10
    Assert-Step -TestName "Frontend React Portal phuc vu boi Nginx (HTTP 200)" -Condition ($feRes.StatusCode -eq 200) -Details "HTTP Status: $($feRes.StatusCode)"
} catch {
    Assert-Step -TestName "Frontend React Portal phuc vu boi Nginx (HTTP 200)" -Condition $false -Details $_.Exception.Message
}

try {
    $spaRes = Invoke-WebRequest -Uri "$FrontendUrl/learning-paths" -UseBasicParsing -TimeoutSec 10
    Assert-Step -TestName "Nginx SPA Routing try_files hoat dong dung (HTTP 200)" -Condition ($spaRes.StatusCode -eq 200) -Details "HTTP Status: $($spaRes.StatusCode)"
} catch {
    Assert-Step -TestName "Nginx SPA Routing try_files hoat dong dung (HTTP 200)" -Condition $false -Details $_.Exception.Message
}

# ------------------------------------------------------------------------------
# TONG KET
# ------------------------------------------------------------------------------
Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " KET QUA KIEM THU DOCKER HOAN CHINH" -ForegroundColor Cyan
Write-Host " Tong so bai test: $TotalTests" -ForegroundColor White
Write-Host " Thanh cong:       $PassedTests" -ForegroundColor Green
Write-Host " That bai:         $FailedTests" -ForegroundColor $(if ($FailedTests -eq 0) { "Green" } else { "Red" })
Write-Host " Ty le thanh cong: $([Math]::Round(($PassedTests / $TotalTests) * 100, 2))%" -ForegroundColor Yellow
Write-Host "==================================================================`n" -ForegroundColor Cyan

