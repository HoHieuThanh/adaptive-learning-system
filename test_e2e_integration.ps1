# ==============================================================================
# Script Kiem Thu Tu Dong Toan Dien End-to-End (E2E Integration Test)
# He Thong AI-Powered Adaptive Learning & Student Performance Prediction
# ==============================================================================

param(
    [string]$MlServiceUrl = "http://localhost:8000",
    [string]$BackendUrl = "http://localhost:8080/api",
    [string]$FrontendUrl = "http://localhost:5173"
)

$ErrorActionPreference = "Continue"

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " BAT DAU KIEM THU TICH HOP HE THONG END-TO-END (3 THANH PHAN)" -ForegroundColor Cyan
Write-Host " ML Service:  $MlServiceUrl" -ForegroundColor Gray
Write-Host " Backend API: $BackendUrl" -ForegroundColor Gray
Write-Host " Frontend:    $FrontendUrl" -ForegroundColor Gray
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
# 1. KIEM THU ML SERVICE TRUC TIEP (FASTAPI PORT 8000)
# ------------------------------------------------------------------------------
Write-Host "`n--- [BUOC 1] KIEM THU TRANG THAI ML SERVICE (FASTAPI PORT 8000) ---" -ForegroundColor Yellow

try {
    $mlHealth = Invoke-RestMethod -Uri "$MlServiceUrl/health" -Method Get -TimeoutSec 5
    Assert-Step -TestName "ML Service /health UP" -Condition ($mlHealth.status -eq "UP" -and $mlHealth.model_loaded -eq $true) -Details "Status: $($mlHealth.status), Model loaded: $($mlHealth.model_loaded)"
    Assert-Step -TestName "Ho tro day du 3 mo hinh AI" -Condition ($mlHealth.available_models -contains "xgboost" -and $mlHealth.available_models -contains "decision_tree" -and $mlHealth.available_models -contains "kmeans_hybrid") -Details "Models: $($mlHealth.available_models -join ', ')"
} catch {
    Assert-Step -TestName "ML Service /health UP" -Condition $false -Details $_.Exception.Message
}

try {
    $mlMetrics = Invoke-RestMethod -Uri "$MlServiceUrl/metrics" -Method Get -TimeoutSec 5
    $xgbAcc = [Math]::Round($mlMetrics.XGBoost.accuracy * 100, 2)
    Assert-Step -TestName "ML Metrics tu dataset UCI that (XGBoost Test Accuracy)" -Condition ($xgbAcc -gt 90) -Details "XGBoost Test Accuracy: $xgbAcc%, F1-score: $([Math]::Round($mlMetrics.XGBoost.f1_weighted * 100, 2))%"
} catch {
    Assert-Step -TestName "ML Metrics tu dataset UCI that (XGBoost Test Accuracy)" -Condition $false -Details $_.Exception.Message
}

# ------------------------------------------------------------------------------
# 2. KIEM THU BACKEND SPRING BOOT (PORT 8080)
# ------------------------------------------------------------------------------
Write-Host "`n--- [BUOC 2] KIEM THU TRANG THAI BACKEND SPRING BOOT (PORT 8080) ---" -ForegroundColor Yellow

try {
    $beHealth = Invoke-RestMethod -Uri "$BackendUrl/health" -Method Get -TimeoutSec 5
    Assert-Step -TestName "Spring Boot Backend /api/health UP" -Condition ($beHealth.status -eq "UP") -Details "Backend Status: $($beHealth.status), Service: $($beHealth.service)"
} catch {
    Assert-Step -TestName "Spring Boot Backend /api/health UP" -Condition $false -Details $_.Exception.Message
}

# Cau hinh Basic Auth header cho Advisor (advisor / advisor123)
$AdvisorAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("advisor:advisor123"))
$AdvisorHeaders = @{
    "Authorization" = "Basic $AdvisorAuth"
    "Content-Type"  = "application/json"
}

# ------------------------------------------------------------------------------
# 3. KIEM THU QUAN LY DU LIEU SINH VIEN (20 SINH VIEN)
# ------------------------------------------------------------------------------
Write-Host "`n--- [BUOC 3] KIEM THU TRUY VAN DU LIEU 20 SINH VIEN TU DATABASE ---" -ForegroundColor Yellow

$students = $null
try {
    $res = Invoke-RestMethod -Uri "$BackendUrl/students" -Method Get -Headers $AdvisorHeaders -TimeoutSec 5
    $students = $res.data
    Assert-Step -TestName "Nap du toi thieu 20 sinh vien da dang hoc luc" -Condition ($students.Count -ge 20) -Details "Da nap thanh cong: $($students.Count) sinh vien (SV001 - SV020)"
} catch {
    Assert-Step -TestName "Nap du toi thieu 20 sinh vien da dang hoc luc" -Condition $false -Details $_.Exception.Message
}

# ------------------------------------------------------------------------------
# 4. KIEM THU TICH HOP END-TO-END DU DOAN AI VA SINH LO TRINH THICH UNG
# ------------------------------------------------------------------------------
Write-Host "`n--- [BUOC 4] KIEM THU LUONG DU DOAN AI VA LO TRINH THICH UNG (LIVE XGBOOST) ---" -ForegroundColor Yellow

# Test Case 4A: Sinh vien At-Risk (SV003 - Le Hoang Cuong, GPA thap, nhieu buoi vang)
try {
    Write-Host "-> Gui yeu cau du doan cho sinh vien SV003 (Le Hoang Cuong - At-Risk)..." -ForegroundColor Cyan
    $bodyJson = '{"modelName": "xgboost"}'
    $predRes = Invoke-RestMethod -Uri "$BackendUrl/predictions/predict/3" -Method Post -Headers $AdvisorHeaders -Body $bodyJson -TimeoutSec 10
    $pred = $predRes.data

    Assert-Step -TestName "[AI Prediction] Sinh vien SV003 phan loai nhan 'Low'" -Condition ($pred.predictedLevel -eq "Low") -Details "Predicted: $($pred.predictedLevel), Confidence: $([Math]::Round($pred.confidence * 100, 2))%, Model: $($pred.modelName)"
    Assert-Step -TestName "[AI Model] Do tin cay du doan cao (>= 80%)" -Condition ($pred.confidence -ge 0.8) -Details "Confidence: $([Math]::Round($pred.confidence * 100, 2))%"

    # Kiem tra Lo trinh hoc tap tuong ung
    $lpRes = Invoke-RestMethod -Uri "$BackendUrl/learning-paths/student/3/latest" -Method Get -Headers $AdvisorHeaders -TimeoutSec 5
    $path = $lpRes.data
    Assert-Step -TestName "[Adaptive Path] Tao lo trinh can thiep cho sinh vien Low" -Condition ($path.id -gt 0 -and $path.studentId -eq 3 -and $path.status -eq "IN_PROGRESS") -Details "ID: $($path.id), Status: $($path.status), Title: $($path.title)"
    Assert-Step -TestName "[Recommendation] Sinh cac khuyen nghi kem muc do HIGH" -Condition ($path.recommendations.Count -gt 0 -and ($path.recommendations | Where-Object { $_.priority -eq "HIGH" }).Count -gt 0) -Details "So khuyen nghi: $($path.recommendations.Count), Co khuyen nghi uu tien HIGH"
} catch {
    Assert-Step -TestName "[AI Prediction & Path] Luong sinh vien SV003" -Condition $false -Details $_.Exception.Message
}

# Test Case 4B: Sinh vien Gioi (SV001 - Nguyen Van An, GPA cao, tu hoc nhieu)
try {
    Write-Host "`n-> Gui yeu cau du doan cho sinh vien SV001 (Nguyen Van An - Xuat sac)..." -ForegroundColor Cyan
    $predResHigh = Invoke-RestMethod -Uri "$BackendUrl/predictions/predict/1" -Method Post -Headers $AdvisorHeaders -Body '{"modelName": "xgboost"}' -TimeoutSec 10
    $predHigh = $predResHigh.data

    Assert-Step -TestName "[AI Prediction] Sinh vien SV001 phan loai nhan 'High'" -Condition ($predHigh.predictedLevel -eq "High") -Details "Predicted: $($predHigh.predictedLevel), Confidence: $([Math]::Round($predHigh.confidence * 100, 2))%, Model: $($predHigh.modelName)"

    $lpResHigh = Invoke-RestMethod -Uri "$BackendUrl/learning-paths/student/1/latest" -Method Get -Headers $AdvisorHeaders -TimeoutSec 5
    $pathHigh = $lpResHigh.data
    Assert-Step -TestName "[Adaptive Path] Tao lo trinh nang cao cho sinh vien High" -Condition ($pathHigh.id -gt 0 -and $pathHigh.studentId -eq 1 -and $pathHigh.status -eq "IN_PROGRESS") -Details "ID: $($pathHigh.id), Status: $($pathHigh.status), Title: $($pathHigh.title)"
} catch {
    Assert-Step -TestName "[AI Prediction & Path] Luong sinh vien SV001" -Condition $false -Details $_.Exception.Message
}

# ------------------------------------------------------------------------------
# 5. KIEM THU LICH SU DU DOAN VA DASHBOARD THONG KE
# ------------------------------------------------------------------------------
Write-Host "`n--- [BUOC 5] KIEM THU LICH SU DU DOAN VA DASHBOARD THONG KE ---" -ForegroundColor Yellow

try {
    $histRes = Invoke-RestMethod -Uri "$BackendUrl/predictions/student/3" -Method Get -Headers $AdvisorHeaders -TimeoutSec 5
    $histList = $histRes.data
    Assert-Step -TestName "Luu lich su du doan vao co so du lieu" -Condition ($histList.Count -ge 1) -Details "So ban ghi lich su cua SV003: $($histList.Count)"
} catch {
    Assert-Step -TestName "Luu lich su du doan vao co so du lieu" -Condition $false -Details $_.Exception.Message
}

try {
    $dashRes = Invoke-RestMethod -Uri "$BackendUrl/dashboard/stats" -Method Get -Headers $AdvisorHeaders -TimeoutSec 5
    $stats = $dashRes.data
    Assert-Step -TestName "Dashboard thong ke tong quan day du cac chi so" -Condition ($stats.totalStudents -ge 20 -and $stats.totalPredictions -gt 0) -Details "Total Students: $($stats.totalStudents), High: $($stats.highCount), Medium: $($stats.moderateCount), Low/At-Risk: $($stats.atRiskCount)"
} catch {
    Assert-Step -TestName "Dashboard thong ke tong quan day du cac chi so" -Condition $false -Details $_.Exception.Message
}

# ------------------------------------------------------------------------------
# 6. KIEM THU PHAN QUYEN (RBAC) VA BAO MAT
# ------------------------------------------------------------------------------
Write-Host "`n--- [BUOC 6] KIEM THU PHAN QUYEN (RBAC) VA BAO MAT ---" -ForegroundColor Yellow

try {
    $badAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("invalid_user:wrong_pass"))
    $null = Invoke-RestMethod -Uri "$BackendUrl/students" -Method Get -Headers @{"Authorization" = "Basic $badAuth"} -TimeoutSec 5
    Assert-Step -TestName "Chan xac thuc sai thong tin (401)" -Condition $false -Details "Khong chan duoc request trai phep"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Assert-Step -TestName "Chan xac thuc sai thong tin (401)" -Condition ($statusCode -eq 401) -Details "HTTP Status: $statusCode Unauthorized"
}

# ------------------------------------------------------------------------------
# TONG KET
# ------------------------------------------------------------------------------
Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " KET QUA TONG HOP KIEM THU TICH HOP END-TO-END" -ForegroundColor Cyan
Write-Host " Tong so bai test: $TotalTests" -ForegroundColor White
Write-Host " Thanh cong:       $PassedTests" -ForegroundColor Green
Write-Host " That bai:         $FailedTests" -ForegroundColor $(if ($FailedTests -eq 0) { "Green" } else { "Red" })
Write-Host " Ty le thanh cong: $([Math]::Round(($PassedTests / $TotalTests) * 100, 2))%" -ForegroundColor Yellow
Write-Host "==================================================================`n" -ForegroundColor Cyan
