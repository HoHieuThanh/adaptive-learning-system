# BẢNG KIỂM TRA & HƯỚNG DẪN DEMO BẢO VỆ ĐỒ ÁN
## ADAPTIVE LEARNING & STUDENT PERFORMANCE PREDICTION SYSTEM
**Dành riêng cho sinh viên thực hiện trên máy tính cá nhân (Hệ điều hành Windows)**

---

## I. CHUẨN BỊ TRƯỚC GIỜ BẢO VỆ (15 PHÚT TRƯỚC KHI VÀO PHÒNG)

### 1. Khởi động Docker Desktop
1. Mở ứng dụng **Docker Desktop** trên Windows.
2. Chờ biểu tượng Docker ở khay hệ thống chuyển sang màu xanh lá cây (*Docker Engine running*).
3. Mở **PowerShell** với quyền Administrator (hoặc quyền thông thường) và di chuyển vào thư mục dự án:
   ```powershell
   cd C:\Users\ACER\Documents\PTHTTM\adaptive-learning-system
   ```

### 2. Khởi động Toàn bộ Hệ thống 5 Dịch vụ
Chạy lệnh khởi động trọn bộ container bằng Docker Compose:
```powershell
cd deployment/docker
docker-compose up -d
```

Kiểm tra trạng thái các container:
```powershell
docker ps
```
**Yêu cầu:** Cả 5 container sau phải hiển thị trạng thái `Up (healthy)` hoặc `Up`:
- `adaptive-frontend` (Cổng `5173`)
- `adaptive-backend` (Cổng `8080`)
- `adaptive-ml-service` (Cổng `8000`)
- `adaptive-postgres` (Cổng `5432`)
- `adaptive-mlflow` (Cổng `5000`)

### 3. Mở sẵn các Tab Trình duyệt theo Thứ tự
Chuẩn bị sẵn một cửa sổ trình duyệt (Chrome/Edge) gồm 4 tab:
- **Tab 1 (Giao diện chính):** `http://localhost:5173`
- **Tab 2 (MLflow Tracking UI):** `http://localhost:5000`
- **Tab 3 (FastAPI Swagger Docs):** `http://localhost:8000/docs`
- **Tab 4 (Backend Health Check):** `http://localhost:8080/api/health`

---

## II. KỊCH BẢN THUYẾT MINH & DEMO TRỰC TIẾP (5 - 7 PHÚT)

### 🎯 PHẦN 1: TỔNG QUAN HỆ THỐNG & ĐĂNG NHẬP (1 phút)
1. Trên **Tab 1 (`http://localhost:5173`)**, chọn đăng nhập nhanh bằng tài khoản **Cố vấn học tập (Advisor)**:
   - Tài khoản: `advisor`
   - Mật khẩu: `advisor123`
2. Giới thiệu với Hội đồng:
   - *"Hệ thống Adaptive Learning giải quyết 2 bài toán chính: (1) Cảnh báo sớm sinh viên có nguy cơ rớt môn bằng Machine Learning, và (2) Tự động sinh lộ trình học tập thích ứng cá nhân hóa cho từng sinh viên."*
3. Chỉ vào màn hình **Dashboard**:
   - Biểu đồ phân bổ 20 sinh viên theo 3 nhóm: **High (5 SV)**, **Medium (9 SV)**, **At-Risk/Low (6 SV)**.
   - Nhấn vào nút đỏ **"Xem Danh sách Nguy cơ"** để chuyển sang trang theo dõi các sinh viên cần hỗ trợ khẩn cấp.

---

### 🎯 PHẦN 2: MINH HỌA SUY LUẬN AI THỜI GIAN THỰC (AI STUDIO) (2 phút)
1. Chọn mục **AI Prediction** trên thanh menu bên trái.
2. **Kịch bản 1 - Sinh viên có nguy cơ rớt môn:**
   - Chọn sinh viên: `SV003 - Lê Hoàng Cường` (có điểm trung bình 3.8, vắng học nhiều).
   - Chọn mô hình: **XGBoost Classifier (SOTA)**.
   - Nhấn **"Bắt đầu Dự đoán Ngay"**:
     - Giải thích luồng dữ liệu: React gửi request $\rightarrow$ Spring Boot lấy điểm môn học $\rightarrow$ gọi FastAPI `/predict` $\rightarrow$ XGBoost suy luận $\rightarrow$ trả về kết quả.
     - Kết quả hiển thị: **Trình độ: Low (Nguy cơ rớt môn)**, Độ tin cậy: **97.3%**.
3. **Kịch bản 2 - So sánh với Mô hình K-Means Hybrid:**
   - Vẫn chọn sinh viên `SV003`, đổi sang mô hình **K-Means + Classifier (Hybrid)**.
   - Nhấn **"Bắt đầu Dự đoán Ngay"**:
     - Kết quả hiển thị: **Trình độ: Low**, Độ tin cậy: **97.4%**.
     - **Điểm nhấn giải thích cho Giảng viên:** *"Mô hình Hybrid sử dụng phương pháp Feature Augmentation, trích xuất thêm 3 đặc trưng cụm từ K-Means không giám sát (chiếm tới 9% trọng số). Sự đóng góp của cụm làm thay đổi xác suất mềm từ 97.33% sang 97.36%, nhưng hàm argmax vẫn nhất quán xếp sinh viên vào nhóm Low."*
4. **Kịch bản 3 - Sinh viên Xuất sắc:**
   - Chọn sinh viên `SV001 - Nguyễn Văn An` (GPA 9.0) $\rightarrow$ Nhấn dự đoán $\rightarrow$ Trả về **Trình độ: High (97.3%)**.
5. Cuộn xuống bảng **Lịch sử Dự đoán & Audit Trail**: Chỉ cho Hội đồng thấy mọi lần dự đoán đều được lưu vết thời gian thực vào PostgreSQL.

---

### 🎯 PHẦN 3: LỘ TRÌNH HỌC TẬP THÍCH ỨNG (ADAPTIVE LEARNING PATH) (2 phút)
1. Nhấn vào mục **Lộ trình học tập (Learning Path)** trên menu.
2. Chọn sinh viên `SV003 - Lê Hoàng Cường`:
   - Chỉ cho Hội đồng thấy hệ thống đã tự động kích hoạt Rule Engine:
     - Vì sinh viên thuộc mức `Low` và có 3 môn bị điểm dưới 5.0 gồm: `CS201 (3.5)`, `CS301 (3.8)` và `CS101 (4.1)`.
     - Hệ thống tự động ưu tiên đề xuất 3 học phần nền tảng này lên đầu danh sách với mức độ ưu tiên **HIGH**.
     - Kèm theo 2 đề xuất kỹ năng mềm: *Thiết lập thời khóa biểu tự học Pomodoro* và *Đặt lịch tư vấn cùng Cố vấn học tập*.
3. Mở chi tiết một đề xuất:
   - Nêu rõ 4 thông tin: **Mục tiêu học tập cụ thể**, **Thời lượng dự kiến**, **Mức ưu tiên**, và **Lý do giải thích minh bạch (Explanation)**.
4. Thao tác cập nhật trạng thái:
   - Nhấp vào nút trạng thái: `Chưa bắt đầu` $\rightarrow$ `Đang học` $\rightarrow$ `Đã hoàn thành`.
   - Quan sát thanh tiến độ phần trăm tự động nhảy từ `0%` lên `20%`.

---

### 🎯 PHẦN 4: THỰC NGHIỆM MLFLOW & CHỊU LỖI (FAULT TOLERANCE) (1.5 phút)
1. Chuyển sang **Tab 2 (`http://localhost:5000`) - MLflow UI**:
   - Cho Giảng viên xem Experiment `student-performance-prediction`.
   - Mở xem 3 run tương ứng của **Decision Tree**, **XGBoost** và **K-Means Hybrid**.
   - Chỉ vào bảng metrics so sánh: Accuracy (93.67%), F1-Score (94.63%) và các siêu tham số (hyperparameters).
2. **Minh họa khả năng Chịu lỗi (Fault Tolerance) - Ghi điểm thực tế:**
   - Mở cửa sổ PowerShell và gõ lệnh tạm dừng ML Service:
     ```powershell
     docker stop adaptive-ml-service
     ```
   - Quay lại Tab 1 Web Portal, bấm nút dự đoán AI:
     - Web Portal **không bị crash hoặc màn hình trắng**.
     - Giao diện lập tức hiển thị Banner màu vàng cảnh báo:  
       `[CHẾ ĐỘ DEMO / DỮ LIỆU MẪU MÔ PHỎNG]: Không thể kết nối ML Service (FastAPI: 8000)...`
     - Backend log ghi nhận mã lỗi sạch sẽ: `HTTP 503 Service Unavailable`.
   - Bật lại ML Service ngay trên terminal:
     ```powershell
     docker start adaptive-ml-service
     ```
   - Bấm dự đoán lại $\rightarrow$ Kết quả AI thật lập tức phục hồi bình thường.

---

## III. NẾU GIẢNG VIÊN YÊU CẦU XEM DEVOPS (K8S & CI/CD)

### 1. Hướng dẫn Kích hoạt Kubernetes trên Docker Desktop
*(Chỉ thực hiện nếu Giảng viên hỏi sâu về Kubernetes/HPA)*
1. Trên giao diện Docker Desktop, bấm vào biểu tượng bánh răng **Settings** (góc trên bên phải).
2. Chọn menu **Kubernetes** ở thanh bên trái.
3. Tích chọn vào ô **"Enable Kubernetes"** $\rightarrow$ Bấm nút **"Apply & restart"**.
4. Chờ 2 - 3 phút để Docker Desktop tải và khởi động cụm Kubernetes local (khi biểu tượng Kubernetes ở góc dưới chuyển màu xanh).
5. Mở PowerShell gõ lệnh triển khai:
   ```powershell
   cd C:\Users\ACER\Documents\PTHTTM\adaptive-learning-system\deployment\k8s
   # Áp dụng cấu hình secrets và manifests
   kubectl apply -f configmap.yaml
   kubectl apply -f secret.yaml.example
   kubectl apply -f postgres.yaml
   kubectl apply -f ml-service.yaml
   kubectl apply -f backend.yaml
   kubectl apply -f frontend.yaml
   kubectl apply -f metrics-server.yaml
   kubectl apply -f hpa.yaml
   ```
6. Kiểm tra HPA:
   ```powershell
   kubectl get pods
   kubectl get hpa
   ```
7. Mô phỏng tải để xem Auto-scaling:
   ```powershell
   kubectl apply -f load-generator.yaml
   # Theo dõi HPA co giãn pod từ 1 lên 3:
   kubectl get hpa -w
   ```

---

### 2. Hướng dẫn Kích hoạt GitHub Actions Remote
*(Nếu Giảng viên yêu cầu xem quy trình CI/CD trên cloud GitHub)*
Mở PowerShell tại thư mục gốc dự án:
```powershell
# Tạo một branch kiểm thử để không ảnh hưởng nhánh main
git checkout -b test/ci-verification
git push origin test/ci-verification
```
Sau đó mở trình duyệt truy cập:
`https://github.com/HoHieuThanh/adaptive-learning-system/actions`
Cho Giảng viên xem 4 Jobs đang chạy tự động song song:
1. `Frontend (React + Vite)`: chạy `npm test` và `npm run build`.
2. `Backend (Spring Boot 3 + Java 21)`: chạy `mvn clean test package`.
3. `ML Service (FastAPI + XGBoost)`: chạy `pytest` và kiểm tra ngưỡng Accuracy >= 80%.
4. `Docker Build Verification`: kiểm tra build container images.

---

## IV. PHƯƠNG ÁN XỬ LÝ SỰ CỐ KHẨN CẤP (EMERGENCY BACKUP)

| Tình huống | Cách xử lý nhanh |
| :--- | :--- |
| **Phòng bảo vệ không có mạng Internet** | Hệ thống chạy **100% Offline** trên Docker nội bộ máy tính (`localhost`), hoàn toàn không phụ thuộc vào internet trường học. |
| **Cổng 8080 hoặc 5173 bị xung đột** | Mở PowerShell gõ `netstat -ano \| findstr :8080`, tìm PID và kill bằng `taskkill /F /PID <pid>`, sau đó chạy lại `docker-compose up -d`. |
| **Container bị lỗi không khởi động được** | Chạy lệnh reset sạch sẽ: `docker-compose down`, sau đó `docker-compose up -d --build`. Dữ liệu Database được bảo toàn trên volume. |
| **Giảng viên hỏi về số liệu XGBoost và Hybrid** | Mở file `ml-service/scripts/verify_ml_deep.py` và chạy `python ml-service/scripts/verify_ml_deep.py` để in trực tiếp bảng 15 mẫu có xác suất khác nhau ngay trên console. |

---
*Chúc bạn có một buổi bảo vệ đồ án thành công xuất sắc!*

