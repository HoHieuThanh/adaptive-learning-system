$auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("advisor:advisor123"))
$headers = @{
    "Authorization" = "Basic $auth"
    "Content-Type"  = "application/json"
}

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host " KIỂM THỬ CHỨC NĂNG GỢI Ý LỘ TRÌNH HỌC TẬP THÍCH ỨNG" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

# 1. Sinh viên Low (SV003 - Lê Hoàng Cường)
Write-Host "`n[1] LỘ TRÌNH HỌC TẬP CỦA SINH VIÊN LOW (SV003 - Lê Hoàng Cường)" -ForegroundColor Yellow
$p3 = (Invoke-RestMethod -Uri "http://localhost:8080/api/learning-paths/student/3/latest" -Headers $headers).data
Write-Host "Tiêu đề lộ trình:       $($p3.title)"
Write-Host "Mục tiêu học tập:       $($p3.learningGoal)"
Write-Host "Thời gian dự kiến:      $($p3.estimatedWeeks)"
Write-Host "Tiến độ ban đầu:        $($p3.progressPercentage)%"
Write-Host "Số lượng khuyến nghị:   $($p3.recommendations.Count)"

Write-Host "`nChi tiết khuyến nghị 1:" -ForegroundColor White
$r1 = $p3.recommendations[0]
Write-Host "  - Tiêu đề:            $($r1.title)"
Write-Host "  - Môn học:            $($r1.subjectCode)"
Write-Host "  - Mức ưu tiên:        $($r1.priority)"
Write-Host "  - Trạng thái:         $($r1.status)"
Write-Host "  - Mục tiêu cụ thể:    $($r1.learningObjective)"
Write-Host "  - Thời lượng dự kiến: $($r1.estimatedDuration)"
Write-Host "  - Loại tài nguyên:    $($r1.resourceType)"
Write-Host "  - Lý do đề xuất:      $($r1.explanation)" -ForegroundColor Green

# 2. Cập nhật tiến độ của đề xuất 1 sang COMPLETED
Write-Host "`n[2] CẬP NHẬT TRẠNG THÁI ĐỀ XUẤT 1 SANG COMPLETED" -ForegroundColor Yellow
$recId = $r1.id
$updateBody = '{"status": "COMPLETED"}'
$updateRes = (Invoke-RestMethod -Uri "http://localhost:8080/api/learning-paths/recommendations/$recId/status" -Method Patch -Headers $headers -Body $updateBody).data
Write-Host "-> Trạng thái mới của đề xuất ID $($recId): $($updateRes.status)" -ForegroundColor Green

# 3. Kiểm tra lại tiến độ của cả lộ trình
Write-Host "`n[3] KIỂM TRA LẠI TIẾN ĐỘ CỦA LỘ TRÌNH SAU KHI CẬP NHẬT" -ForegroundColor Yellow
$p3Updated = (Invoke-RestMethod -Uri "http://localhost:8080/api/learning-paths/student/3/latest" -Headers $headers).data
Write-Host "-> Tiến độ lộ trình mới: $($p3Updated.progressPercentage)%" -ForegroundColor Green
Write-Host "-> Đã hoàn thành:        $($p3Updated.completedRecommendations) / $($p3Updated.totalRecommendations) đề xuất" -ForegroundColor Green

# 4. Sinh viên High (SV001 - Nguyễn Văn An)
Write-Host "`n[4] LỘ TRÌNH HỌC TẬP CỦA SINH VIÊN HIGH (SV001 - Nguyễn Văn An)" -ForegroundColor Yellow
$p1 = (Invoke-RestMethod -Uri "http://localhost:8080/api/learning-paths/student/1/latest" -Headers $headers).data
Write-Host "Tiêu đề lộ trình:       $($p1.title)"
Write-Host "Mục tiêu học tập:       $($p1.learningGoal)"
Write-Host "Thời gian dự kiến:      $($p1.estimatedWeeks)"
Write-Host "Đề xuất đầu tiên:       $($p1.recommendations[0].title)"
Write-Host "  - Loại tài nguyên:    $($p1.recommendations[0].resourceType)"
Write-Host "  - Lý do đề xuất:      $($p1.recommendations[0].explanation)" -ForegroundColor Green

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " HOÀN TẤT KIỂM THỬ CHỨC NĂNG GỢI Ý LỘ TRÌNH HỌC TẬP!" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
