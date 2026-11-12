# Architecture & Communication Flow Documentation

Hệ thống **AI-Powered Adaptive Learning & Student Performance Prediction** được thiết kế theo kiến trúc Microservices phân tầng rõ ràng (Multi-tier Decoupled Architecture).

---

## 1. Sơ đồ Kiến trúc Tổng thể (High-Level Architecture)

```
+-------------------------------------------------------------------------+
|                               FRONTEND                                  |
|                         (React 18 + Vite SPA)                           |
|         [Student Portal]      [Advisor Dashboard]     [Admin View]      |
+------------------------------------+------------------------------------+
                                     |
                                     | HTTPS / JSON REST API + JWT
                                     v
+-------------------------------------------------------------------------+
|                              BACKEND CORE                               |
|                         (Spring Boot 3 + Java 21)                       |
|   - Security & Authentication (JWT RBAC)                                |
|   - Student Management & Profile Services                               |
|   - Rule-Based Adaptive Recommendation Engine                           |
|   - Prediction History Logger (Audit Trail)                             |
+-------------------+---------------------------------+-------------------+
                    |                                 |
     SQL Query /    |                  Internal REST  | (HTTP JSON)
     Spring Data JPA|                  (RestClient)   |
                    v                                 v
+-----------------------+         +---------------------------------------+
|       DATABASE        |         |              ML SERVICE               |
|    (PostgreSQL 16)    |         |        (FastAPI + Python 3.11)        |
| - Users / Roles       |         | - Preprocessing (StandardScaler)      |
| - Students Profile    |         | - Model Inference (DT, XGBoost)       |
| - Prediction History  |         | - K-Means Clustering & Profiling      |
| - Learning Paths      |         | - Model Training & Metrics Tracking   |
+-----------------------+         +---------------------------------------+
```

---

## 2. Luồng Giao tiếp Chi tiết (Sequence Flow)

### Luồng 1: Dự đoán & Sinh Lộ trình Thích ứng (Prediction & Adaptive Pathway Generation)

1. **Client Request:** 
   - Sinh viên hoặc Cố vấn học tập (Advisor) bấm nút *"Phân tích Trình độ & Gợi ý Lộ trình"* trên giao diện React.
   - Frontend gửi HTTP POST tới Backend:
     ```http
     POST /api/students/{studentId}/predict
     Authorization: Bearer <jwt_token>
     ```
2. **Backend Orchestration:**
   - Spring Boot đón nhận request, kiểm tra quyền truy cập (RBAC).
   - Truy vấn thông tin học tập và hành vi gần nhất của sinh viên từ PostgreSQL (`students` table).
   - Chuẩn bị JSON payload chuyển tiếp sang ML Service:
     ```json
     {
       "student_id": "123e4567-e89b-12d3-a456-426614174000",
       "attendance_rate": 82.5,
       "study_hours_per_week": 6.5,
       "assignments_completed_ratio": 75.0,
       "lms_interactions": 45,
       "previous_gpa": 2.8,
       "midterm_score": 5.5
     }
     ```
3. **ML Service Inference:**
   - FastAPI nhận request tại endpoint `POST /internal/v1/predict`.
   - Áp dụng `preprocessor.transform()` (sử dụng `StandardScaler` đã fit trên tập Train, **tuyệt đối không refit để tránh Data Leakage**).
   - Chạy mô hình (Decision Tree / XGBoost / K-Means Augmented Classifier).
   - Trả về nhãn dự đoán, xác suất tin cậy (confidence) và cụm hành vi (cluster profile):
     ```json
     {
       "predicted_level": "AT_RISK",
       "confidence_score": 0.875,
       "cluster_id": 1,
       "cluster_name": "Chủ quan / Giờ học ít",
       "feature_importance": {
         "study_hours_per_week": -0.42,
         "midterm_score": -0.38
       }
     }
     ```
4. **Rule Engine & Persistence:**
   - Spring Boot nhận kết quả từ AI, nạp vào `RuleBasedRecommendationService`.
   - Đối chiếu các chỉ số bị yếu (`study_hours < 10`, `midterm_score < 6.0`) để sinh danh sách hành động cải thiện cụ thể (`recommended_actions`).
   - Ghi nhận lịch sử vào bảng `prediction_history` và cập nhật lộ trình vào `learning_paths`.
5. **Client Response:**
   - Trả dữ liệu tổng hợp về React Frontend để hiển thị Dashboard đồ họa cho người dùng.

---

## 3. Các Endpoint Nội bộ & Công khai (API Contracts)

| Thành phần | Method | Endpoint | Mục đích |
| :--- | :--- | :--- | :--- |
| **Backend** | `GET` | `/api/health` | Kiểm tra trạng thái Backend & kết nối |
| **Backend** | `GET` | `/api/students/{id}` | Lấy hồ sơ học tập sinh viên |
| **Backend** | `POST`| `/api/students/{id}/predict` | Kích hoạt AI phân tích + Rule Engine |
| **Backend** | `GET` | `/api/students/{id}/history` | Xem lịch sử các lần dự đoán trước đó |
| **ML Service**| `GET` | `/health` | Healthcheck liveness probe |
| **ML Service**| `POST`| `/internal/v1/predict` | Chạy suy luận mô hình AI |
| **ML Service**| `POST`| `/internal/v1/train` | Kích hoạt huấn luyện và sinh metrics thật |
| **ML Service**| `GET` | `/internal/v1/metrics` | Lấy bảng so sánh DT vs XGBoost vs K-Means |

---

## 4. Cơ chế Bảo đảm Chất lượng AI & An toàn Dữ liệu
- **Không có Data Leakage:** Tiền xử lý dữ liệu được đóng gói thành Pipeline. Scaler chỉ học tham số $\mu, \sigma$ từ tập Huấn luyện (Train set).
- **Audit Trail:** Mọi lượt dự đoán đều được chụp ảnh nhanh (`input_features_snapshot`) lưu vào DB kèm timestamp và người thực hiện, đảm bảo tính minh bạch khi sinh viên khiếu nại hoặc giảng viên xem lại.
- **Microservice Fault Tolerance:** Nếu ML Service tạm thời quá tải hoặc mất kết nối, Spring Boot sẽ xử lý lỗi có kiểm soát (Graceful Error Handling), thông báo rõ ràng cho người dùng thay vì làm sập toàn bộ hệ thống.

