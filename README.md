# AI-Powered Adaptive Learning & Student Performance Prediction System

> **Đồ Án Tốt Nghiệp / Capstone Project**  
> Hệ thống Gợi ý Lộ trình Học tập và Phân loại Trình độ Sinh viên ứng dụng Trí tuệ Nhân tạo.

---

## 📌 Tổng Quan Hệ Thống

Dự án hướng đến mục tiêu hỗ trợ cá nhân hóa việc học cho sinh viên và cung cấp công cụ cảnh báo sớm cho Cố vấn học tập (Advisor):
1. **Phân loại trình độ sinh viên (3 mức):** `At-Risk` (Cần hỗ trợ sớm), `Moderate` (Trung bình - Khá), `High-Performing` (Xuất sắc).
2. **So sánh 3 mô hình học máy:** `Decision Tree`, `XGBoost`, và `K-Means kết hợp Classification` (Cluster-as-Feature).
3. **Đánh giá minh bạch:** Sử dụng số liệu thực (Accuracy, Precision, Recall, F1-score, Confusion Matrix), tuyệt đối không dùng số liệu giả lập.
4. **Gợi ý lộ trình học tập thích ứng:** Kết hợp nhãn dự đoán của AI với Rule Engine dựa trên các lỗ hổng kiến thức thực tế của sinh viên.
5. **Lưu vết dự đoán (Audit Trail):** Toàn bộ lịch sử dự đoán được lưu trữ trong PostgreSQL.

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

| Tầng | Công nghệ |
| :--- | :--- |
| **Frontend** | React 18, Vite, JavaScript, CSS3 |
| **Backend Core** | Java 21, Spring Boot 3.3, Maven, Spring Data JPA |
| **ML Service** | Python 3.11+, FastAPI, Scikit-Learn, XGBoost, Pandas, NumPy |
| **Database** | PostgreSQL 16 |
| **DevOps / Container** | Docker, Docker Compose, Kubernetes, HPA |
| **CI/CD** | GitHub Actions |

---

## 📂 Cấu Trúc Thư Mục Dự Án

```text
adaptive-learning-system/
├── .github/workflows/          # CI/CD Pipelines (GitHub Actions)
├── backend/                    # Spring Boot 3 Core Backend (Java 21)
├── ml-service/                 # FastAPI ML Service (Python 3.11+)
├── frontend/                   # React + Vite Web Portal
├── database/                   # PostgreSQL schema và script khởi tạo
├── deployment/                 # Docker Compose & Kubernetes Manifests + HPA
├── docs/                       # Tài liệu kiến trúc và hướng dẫn
├── .env.example                # File mẫu biến môi trường (không chứa mật khẩu thật)
├── .gitignore                  # Gitignore chuẩn hóa cho Java, Python, Node.js
└── README.md                   # Tài liệu hướng dẫn cài đặt & vận hành
```

---

## 🚀 Hướng Dẫn Cài Đặt và Khởi Chạy

### 1. Chuẩn bị Môi Trường
- **Java:** JDK 21 trở lên (Eclipse Adoptium / OpenJDK)
- **Node.js:** Node.js v18+ và npm
- **Python:** Python 3.10+
- **Docker & Docker Compose** (Tùy chọn nếu chạy container)

Sao chép file cấu hình môi trường:
```bash
cp .env.example .env
```

---

### 2. Khởi Chạy Cơ Sở Dữ Liệu (PostgreSQL)

Cách nhanh nhất là dùng Docker để chạy cơ sở dữ liệu:
```bash
docker run --name adaptive-postgres \
  -e POSTGRES_DB=adaptive_learning_db \
  -e POSTGRES_USER=adaptive_user \
  -e POSTGRES_PASSWORD=adaptive_password_dev \
  -p 5432:5432 \
  -v $(pwd)/database/init.sql:/docker-entrypoint-initdb.d/init.sql:ro \
  -d postgres:16-alpine
```

---

### 3. Khởi Chạy ML Service (Python + FastAPI)

1. Di chuyển vào thư mục ML Service:
   ```bash
   cd ml-service
   ```
2. Tạo và kích hoạt môi trường ảo (Virtualenv):
   ```bash
   # Windows
   python -m venv venv
   .\venv\Scripts\activate

   # Linux / macOS
   python3 -m venv venv
   source venv/bin/activate
   ```
3. Cài đặt các thư viện cần thiết:
   ```bash
   pip install -r requirements.txt
   ```
4. Kiểm thử pipeline:
   ```bash
   pytest
   ```
5. Khởi chạy server FastAPI:
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
   ```
   - Swagger UI tài liệu API: [http://localhost:8000/docs](http://localhost:8000/docs)
   - Healthcheck: [http://localhost:8000/health](http://localhost:8000/health)

---

### 4. Khởi Chạy Backend (Spring Boot 3 + Java 21)

1. Di chuyển vào thư mục Backend:
   ```bash
   cd backend
   ```
2. Biên dịch và kiểm thử:
   ```bash
   # Windows
   .\mvnw.cmd test

   # Linux / macOS
   ./mvnw test
   ```
3. Khởi chạy Spring Boot server:
   ```bash
   # Windows
   .\mvnw.cmd spring-boot:run

   # Linux / macOS
   ./mvnw spring-boot:run
   ```
   - Server chạy tại: [http://localhost:8080](http://localhost:8080)
   - Healthcheck: [http://localhost:8080/api/health](http://localhost:8080/api/health)

---

### 5. Khởi Chạy Frontend (React + Vite)

1. Di chuyển vào thư mục Frontend:
   ```bash
   cd frontend
   ```
2. Cài đặt dependencies:
   ```bash
   npm install
   ```
3. Khởi chạy dev server:
   ```bash
   npm run dev
   ```
   - Mở trình duyệt tại: [http://localhost:5173](http://localhost:5173)

---

### 6. Khởi Chạy Toàn Bộ Hệ Thống Với Docker Compose

Chỉ với một lệnh duy nhất từ thư mục gốc dự án:
```bash
docker compose up --build -d
```
*(hoặc trỏ file: `docker compose -f deployment/docker/docker-compose.yml up --build -d`)*

Hệ thống sẽ đồng thời khởi động và cấu hình sẵn sàng 5 containers với healthcheck:
- **Frontend Portal (Nginx SPA):** [http://localhost:5173](http://localhost:5173)
- **Spring Boot Backend Core:** [http://localhost:8080](http://localhost:8080) (Health: `http://localhost:8080/api/health`)
- **FastAPI ML Microservice:** [http://localhost:8000](http://localhost:8000) (Swagger: `http://localhost:8000/docs`, Health: `http://localhost:8000/health`)
- **MLflow Tracking Server:** [http://localhost:5000](http://localhost:5000)
- **PostgreSQL 16 Alpine Database:** `localhost:5432` (Persistent volume: `adaptive_postgres_data`)

Kiểm tra trạng thái các container:
```bash
docker compose ps
```

Chạy kịch bản kiểm thử nghiệm thu tự động toàn bộ 5 container:
```powershell
powershell -ExecutionPolicy Bypass -File .\test_docker_deployment.ps1
```

### 7. Tự Động Hóa CI/CD Với GitHub Actions

Hệ thống được trang bị 2 pipeline tự động hóa hoàn chỉnh:
1. **Continuous Integration (`.github/workflows/ci.yml`):**
   - Kích hoạt tự động khi `push` hoặc tạo `pull_request` về nhánh `main` và `develop`.
   - **Frontend Job:** Cài đặt Node 20, kiểm tra Unit Tests (`npm test`), build bundle tối ưu (`npm run build`), lưu trữ artifact `frontend-build`.
   - **Backend Job:** Cài đặt JDK 21 Temurin, chạy 14 bài kiểm thử tích hợp (`mvn clean test`), đóng gói JAR thực thi (`mvn package`), lưu trữ test reports và file JAR.
   - **ML Service Job:** Cài đặt Python 3.11, chạy 19 bài kiểm thử `pytest` và **Model Governance Quality Gate** (xác thực schema Pydantic, kiểm tra độ chính xác trên held-out test set: Accuracy $\ge$ 80%, F1 $\ge$ 80%), lưu trữ test reports và model weights artifacts.
   - **Docker Build Verification:** Kiểm tra tính hợp lệ của toàn bộ Dockerfile qua Docker Buildx.
2. **Container Image Publishing (`.github/workflows/docker-publish.yml`):**
   - Tự động đóng gói và phát hành multi-arch Docker images lên GitHub Container Registry (`ghcr.io`) khi tạo Git Tag (`v*.*.*`) hoặc kích hoạt thủ công (`workflow_dispatch`).

Chạy kiểm thử toàn diện Local CI trước khi push lên GitHub:
```powershell
powershell -ExecutionPolicy Bypass -File .\test_local_ci.ps1
```

---

### 8. Triển Khai Lên Kubernetes & Auto-scaling (HPA)

Hệ thống đã sẵn sàng các manifest chuẩn hóa tại thư mục `deployment/k8s/`:
- **Frontend:** Deployment + NodePort Service (`port 30080:80`).
- **Backend:** Deployment (CPU 250m-1000m, RAM 512Mi-1Gi) + ClusterIP Service (`port 8080`).
- **ML Service:** Deployment (CPU 200m-800m, RAM 256Mi-1Gi) + ClusterIP Service (`port 8000`).
- **PostgreSQL:** StatefulSet + PersistentVolumeClaim (`2Gi`) + ClusterIP Service (`port 5432`).
- **Config & Secret:** ConfigMap `adaptive-config`, Secret template `secret.yaml.example`.
- **Healthchecks:** `livenessProbe` và `readinessProbe` cho tất cả pods.
- **Horizontal Pod Autoscaler (HPA):** Tự động co giãn từ **1 đến 3 Pods** khi mức sử dụng CPU vượt ngưỡng **60%**.
- **Metrics Server:** Manifest `metrics-server.yaml` kèm cấu hình `--kubelet-insecure-tls` cho local cluster (Docker Desktop / Minikube).

Lệnh triển khai nhanh bằng script:
```powershell
# 1. Cài đặt Metrics Server
powershell -ExecutionPolicy Bypass -File .\deployment\k8s\deploy_k8s.ps1 -Action metrics-server

# 2. Triển khai toàn bộ ứng dụng
powershell -ExecutionPolicy Bypass -File .\deployment\k8s\deploy_k8s.ps1 -Action apply

# 3. Chạy kịch bản tạo tải minh họa HPA Auto-scaling (1 -> 3 pods)
powershell -ExecutionPolicy Bypass -File .\deployment\k8s\deploy_k8s.ps1 -Action load-test
```

---

## 📑 Tài Liệu Kỹ Thuật Bổ Sung
- [Hướng dẫn Triển khai Kubernetes & HPA (docs/kubernetes_guide.md)](docs/kubernetes_guide.md)
- [Hướng dẫn CI/CD Pipeline & GitHub Actions (docs/ci_cd_guide.md)](docs/ci_cd_guide.md)
- [Hướng dẫn Vận hành Docker & Khắc phục sự cố (docs/docker_guide.md)](docs/docker_guide.md)
- [Tài liệu Kiến trúc & Luồng giao tiếp (docs/architecture.md)](docs/architecture.md)
- [Quy tắc Gợi ý Lộ trình Thích ứng & Explainability (docs/learning_path_rules.md)](docs/learning_path_rules.md)
- [Cơ sở dữ liệu & Schema (database/README.md)](database/README.md)
- [Script Vận hành Kubernetes (deployment/k8s/deploy_k8s.ps1)](deployment/k8s/deploy_k8s.ps1)
- [Kịch bản Kiểm thử Local CI (test_local_ci.ps1)](test_local_ci.ps1)
- [Kịch bản Kiểm thử Nghiệm thu Docker (test_docker_deployment.ps1)](test_docker_deployment.ps1)
- [Kịch bản Kiểm thử End-to-End Tự động (test_e2e_integration.ps1)](test_e2e_integration.ps1)
- [Kịch bản Kiểm thử Lộ trình Học tập (test_learning_path_api.ps1)](test_learning_path_api.ps1)

