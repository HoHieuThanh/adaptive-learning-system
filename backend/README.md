# Adaptive Learning System - Backend Service

Dịch vụ Backend trung tâm của hệ thống **AI-Powered Adaptive Learning & Student Performance Prediction**, xây dựng trên nền tảng **Java 21, Spring Boot 3.3, Maven, Spring Data JPA, Spring Security và PostgreSQL**.

---

## 1. Kiến Trúc Dữ Liệu & 8 Bảng Nghiệp Vụ Cốt Lõi

1. **`users`**: Quản lý tài khoản đăng nhập và phân quyền (`STUDENT`, `ADVISOR`, `ADMIN`).
2. **`students`**: Hồ sơ sinh viên (Mã sinh viên, họ tên, email, lớp).
3. **`subjects`**: Danh mục môn học (Mã môn, tên môn).
4. **`grades`**: Bảng điểm học phần theo từng học kỳ.
5. **`learning_activities`**: Chỉ số học tập (Số giờ tự học, chuyên cần, bài tập LMS, số lần đăng nhập).
6. **`predictions`**: Lịch sử dự đoán từ mô hình AI (Mức học lực, độ tin cậy, tên mô hình).
7. **`learning_paths`**: Lộ trình học tập thích ứng được sinh tự động bởi Rule-based Engine.
8. **`recommendations`**: Các hành động khuyến nghị chi tiết trong từng lộ trình theo mức độ ưu tiên (`HIGH`, `MEDIUM`, `LOW`).

---

## 2. Tài Khoản Demo (Seed Data Mặc Định)

Khi khởi động lần đầu với cơ sở dữ liệu trống, hệ thống tự động nạp dữ liệu mẫu (được đánh dấu rõ ràng `[SEED/DEMO DATA]` trong log):

| Tài khoản (Username) | Mật khẩu (Password) | Phân quyền (Role) | Mô tả |
| :--- | :--- | :--- | :--- |
| `student` | `student123` | `ROLE_STUDENT` | Sinh viên: xem lộ trình học tập, xem kết quả dự đoán cá nhân |
| `advisor` | `advisor123` | `ROLE_ADVISOR` | Giảng viên/Cố vấn: xem dashboard thống kê, quản lý điểm và sinh viên |
| `admin` | `admin123` | `ROLE_ADMIN` | Quản trị viên: toàn quyền quản lý hệ thống |

---

## 3. Cấu Hình Môi Trường (Environment Variables)

| Tên biến | Mặc định | Mô tả |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8080` | Cổng HTTP của Backend |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/adaptive_learning_db` | URL kết nối PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `adaptive_user` | Tài khoản cơ sở dữ liệu |
| `SPRING_DATASOURCE_PASSWORD` | `adaptive_password_dev` | Mật khẩu cơ sở dữ liệu |
| `ML_SERVICE_URL` | `http://localhost:8000` | Địa chỉ HTTP của ML Service FastAPI |
| `SPRING_PROFILES_ACTIVE` | `dev` | Profile hoạt động (`dev`, `test`, `prod`) |

---

## 4. Hướng Dẫn Biên Dịch và Khởi Chạy

### Yêu cầu tiên quyết:
- JDK 21 (Eclipse Adoptium / OpenJDK 21)
- ML Service FastAPI chạy tại `http://localhost:8000` (để thực hiện suy luận AI)

### A. Chạy Kiểm Thử Tự Động (Unit & Integration Tests)
Hệ thống sử dụng H2 In-Memory Database cho profile `test`, không yêu cầu kết nối PostgreSQL bên ngoài:
```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

### B. Khởi Chạy Backend Server
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
Server sẽ chạy tại: [http://localhost:8080](http://localhost:8080)

---

## 5. Tổng Hợp REST API Endpoints

### A. Giám sát & Xác thực
- `GET /api/health`: Health check (Công khai)
- `GET /api/auth/me`: Lấy thông tin tài khoản đang đăng nhập

### B. Dashboard Thống Kê (`ADVISOR`, `ADMIN`)
- `GET /api/dashboard/stats`: Thống kê tổng số sinh viên, tỷ lệ At-Risk, điểm trung bình toàn hệ thống

### C. Quản lý Sinh viên (`students`)
- `GET /api/students`: Lấy danh sách toàn bộ sinh viên (kèm điểm TB và nhãn dự đoán mới nhất)
- `GET /api/students/{id}`: Xem chi tiết sinh viên
- `POST /api/students`: Tạo sinh viên mới (`ADVISOR`, `ADMIN`)
- `PUT /api/students/{id}`: Cập nhật thông tin sinh viên (`ADVISOR`, `ADMIN`)
- `DELETE /api/students/{id}`: Xóa sinh viên (`ADMIN`)

### D. Môn học & Điểm số (`subjects`, `grades`)
- `GET /api/subjects`, `POST /api/subjects`, `PUT /api/subjects/{id}`, `DELETE /api/subjects/{id}`
- `GET /api/grades`, `GET /api/grades/student/{studentId}`, `POST /api/grades`, `DELETE /api/grades/{id}`

### E. Dự đoán Trí tuệ Nhân tạo (`predictions`)
- `POST /api/predictions/predict/{studentId}`: Kích hoạt gọi sang ML Service FastAPI `/predict`, lưu lịch sử dự đoán và tự động sinh lộ trình thích ứng
- `GET /api/predictions/student/{studentId}`: Xem lịch sử dự đoán của sinh viên
- `GET /api/predictions`: Xem toàn bộ lịch sử dự đoán hệ thống

### F. Lộ trình Học tập Thích ứng (`learning_paths`)
- `GET /api/learning-paths/student/{studentId}`: Lấy danh sách lộ trình của sinh viên
- `GET /api/learning-paths/student/{studentId}/latest`: Lấy lộ trình mới nhất kèm khuyến nghị chi tiết

