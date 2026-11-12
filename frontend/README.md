# Adaptive Learning System - Frontend Web Portal

Giao diện Web Portal hiện đại cho hệ thống **AI-Powered Adaptive Learning & Student Performance Prediction**, xây dựng bằng **React 18, Vite, React Router v6, Axios và Lucide Icons**.

---

## 1. Danh Sách 9 Trang Nghiệp Vụ Hoàn Chỉnh

1. **Login (`/login`):** Đăng nhập với cơ chế chuyển vai trò nhanh (Sinh viên, Cố vấn học tập, Quản trị viên).
2. **Dashboard Tổng quan (`/`):** Bảng điều khiển KPI tổng thể, biểu đồ Donut trực quan hóa phân bố trình độ (Low, Medium, High), hộp cảnh báo sinh viên At-Risk.
3. **Danh sách Sinh viên (`/students`):** Tra cứu, lọc theo cấp độ học lực, tìm kiếm theo tên/mã sinh viên, xem điểm TB và mở modal thêm sinh viên.
4. **Hồ sơ Chi tiết Sinh viên (`/students/:id`):** Thông tin sinh viên, điểm GPA, kết quả phân loại AI, đo lường chỉ số thói quen học tập (chuyên cần, tự học, LMS).
5. **Kết quả Học tập (`/performance`):** Bảng điểm tổng hợp theo từng môn học và học kỳ, đánh giá học lực.
6. **Dự đoán Trí tuệ Nhân tạo (`/predictions`):** AI Studio tương tác: chọn sinh viên, chọn thuật toán (XGBoost, Decision Tree, K-Means Hybrid), chạy suy luận thời gian thực và xem lịch sử audit trail.
7. **Lộ trình Học tập Thích ứng (`/learning-paths`):** Lộ trình cải thiện cá nhân hóa sinh tự động bởi Rule-based Recommendation Engine, danh sách nhiệm vụ phân loại theo mức độ ưu tiên (`HIGH`, `MEDIUM`, `LOW`) và checklist đánh dấu hoàn thành.
8. **Cố vấn Học tập (`/advisor`):** Bảng điều khiển chuyên biệt cho Cố vấn: danh sách sinh viên nguy cơ trượt môn (At-Risk Queue), chẩn đoán nguyên nhân AI, nút gửi email cảnh báo và nhật ký can thiệp cố vấn.
9. **Quản trị Hệ thống (`/admin`):** Giám sát trạng thái 3 microservices (FastAPI, Spring Boot, PostgreSQL), bảng so sánh hiệu năng thực tế 3 mô hình AI (Accuracy, Precision, Recall, F1-Score) và quản lý danh mục môn học.

---

## 2. Điểm Nhấn Kiến Trúc & Thiết Kế

- **Tách biệt tầng API (Decoupled Services):** Tách toàn bộ logic gọi Axios vào thư mục `src/api/` (`studentService`, `predictionService`, `dashboardService`,...).
- **Chế độ Dự phòng Độc lập (Demo Mode Fallback):** Nếu Backend chưa bật hoặc mất kết nối, hệ thống tự động sử dụng Mock Data chất lượng cao (`mockData.js`) kèm thông báo rõ ràng `[CHẾ ĐỘ DEMO]`, giúp bảo vệ đồ án và demo luôn mượt mà.
- **Biểu đồ SVG Thuần:** Các biểu đồ Donut (`LevelDonutChart`), biểu đồ cột (`PerformanceBarChart`) và thanh tiến độ (`HabitRadarBar`) được vẽ trực tiếp bằng SVG nhẹ, chuẩn xác và không bị lỗi phụ thuộc thư viện.
- **Responsive & Thẩm mỹ cao:** Giao diện thẻ Card hiện đại, màu sắc trực quan (Đỏ cho At-Risk, Vàng cho Medium, Xanh cho High).

---

## 3. Hướng Dẫn Cài Đặt và Khởi Chạy

```bash
cd frontend

# 1. Cài đặt thư viện
npm install

# 2. Khởi chạy máy chủ phát triển (Dev Server)
npm run dev

# 3. Biên dịch cho môi trường Production
npm run build
```

- Web Portal chạy tại: [http://localhost:5173](http://localhost:5173)
- Kết nối mặc định tới Spring Boot Backend tại: `http://localhost:8080/api`

