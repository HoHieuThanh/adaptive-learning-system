# Database Module (PostgreSQL 16)

Mô-đun quản lý cơ sở dữ liệu PostgreSQL cho hệ thống Adaptive Learning.

## Cấu trúc bảng
1. **users**: Lưu thông tin đăng nhập và phân quyền (`STUDENT`, `ADVISOR`, `ADMIN`).
2. **students**: Hồ sơ học tập, chỉ số hành vi (chuyên cần, giờ tự học, điểm số).
3. **prediction_history**: Lịch sử các lần dự đoán từ AI model (audit trail).
4. **learning_paths**: Kế hoạch lộ trình học tập thích ứng được sinh ra từ Rule Engine.
5. **ml_model_metrics**: Lưu trữ kết quả đánh giá (Accuracy, F1, Confusion Matrix) của các phiên bản model.

## Khởi chạy cục bộ với Docker
```bash
docker run --name adaptive-postgres -e POSTGRES_DB=adaptive_learning_db -e POSTGRES_USER=adaptive_user -e POSTGRES_PASSWORD=secret -p 5432:5432 -v $(pwd)/init.sql:/docker-entrypoint-initdb.d/init.sql -d postgres:16-alpine
```

