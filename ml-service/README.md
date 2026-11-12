# ML Service: AI-Powered Student Performance Prediction & Profiling

Dịch vụ Học máy (Machine Learning Microservice) phục vụ phân loại học lực và phân tích cụm hành vi của sinh viên, được xây dựng bằng **Python 3.11+, Scikit-learn, XGBoost và FastAPI**.

---

## 1. Dữ Liệu Thực Nghiệm (UCI Student Performance Dataset)

Hệ thống sử dụng bộ dữ liệu chính thức [Student Performance Dataset từ UCI Machine Learning Repository](https://archive.ics.uci.edu/dataset/320/student+performance) (Cortez & Silva, 2008).

- **Số lượng mẫu:** 395 sinh viên học môn Toán (`student-mat.csv`).
- **Số lượng thuộc tính (Features):** 32 thuộc tính bao gồm:
  - *Thông tin nhân khẩu:* Trường (`school`), giới tính (`sex`), tuổi (`age`), nơi ở (`address`), hoàn cảnh gia đình (`famsize`, `Pstatus`, `Medu`, `Fedu`, `Mjob`, `Fjob`, `guardian`).
  - *Thói quen và hành vi học tập:* Thời gian tự học (`studytime`), số lần nộp bài/học lại (`failures`), nguyện vọng học lên cao (`higher`), có internet (`internet`), quan hệ tình cảm (`romantic`), tham gia ngoại khóa (`activities`).
  - *Chỉ số sức khỏe & sinh hoạt:* Uống rượu bia (`Dalc`, `Walc`), sức khỏe (`health`), số buổi nghỉ học (`absences`).
  - *Điểm số theo giai đoạn:* `G1` (kỳ 1), `G2` (kỳ 2). Điểm kết thúc `G3` được dùng làm nhãn mục tiêu và **tuyệt đối không đưa vào ma trận đặc trưng X**.

---

## 2. Xử Lý Nhãn Mục Tiêu (Target Engineering)

Điểm tổng kết $G3 \in [0, 20]$ được quy đổi thành 3 mức học lực:
- **`Low` ($G3 < 10$):** Không đạt chuẩn, có nguy cơ rớt môn, cần cảnh báo sớm và can thiệp kịp thời.
- **`Medium` ($10 \le G3 \le 14$):** Học lực Trung bình - Khá, đạt chuẩn nhưng cần lộ trình cải thiện điểm số.
- **`High` ($G3 \ge 15$):** Học lực Giỏi - Xuất sắc, có năng lực tốt, cần lộ trình học nâng cao.

Phân phối nhãn thực tế trên tập dữ liệu:
- `Medium`: 192 sinh viên (48.61%)
- `Low`: 130 sinh viên (32.91%)
- `High`: 73 sinh viên (18.48%)

---

## 3. Kiến Trúc Tiền Xử Lý & Chống Rò Rỉ Dữ Liệu (No Data Leakage)

Để đảm bảo kết quả học máy trung thực và có khả năng khái quát hóa cao:
1. **Stratified Train/Test Split (80/20):** 
   - Tập huấn luyện (Train set): 316 sinh viên.
   - Tập kiểm thử (Test set): 79 sinh viên.
   - Dùng `stratify=y` để giữ nguyên tỷ lệ phân bố 3 lớp trên cả Train và Test.
2. **Quy tắc vàng chống Data Leakage:**
   - Bộ tiền xử lý `ColumnTransformer` (`StandardScaler` cho biến số và `OneHotEncoder` cho biến phân loại) **chỉ được gọi `.fit()` duy nhất trên Train Set**.
   - Sau đó chỉ gọi `.transform()` lên cả Train set và Test set.
   - Không lấy bất kỳ thông tin thống kê nào ($\mu, \sigma$) từ tập Test trong quá trình huấn luyện.

---

## 4. Ba Mô Hình Học Máy & Cơ Chế Kết Hợp K-Means

### A. Decision Tree Classifier (Baseline Model)
- Cây quyết định trực quan, dễ giải thích (`max_depth=5`, `min_samples_split=6`).
- Đạt kết quả cơ sở tốt trên dữ liệu bảng dạng phân loại.

### B. XGBoost Classifier (Ensemble SOTA)
- Thuật toán Gradient Boosted Trees mạnh mẽ, tối ưu hóa hàm mất mát đa lớp `multi:softprob` (`n_estimators=100`, `learning_rate=0.05`, `max_depth=3`).
- Tự động bắt được các tương tác phi tuyến phức tạp giữa các đặc trưng hành vi và điểm số.

### C. K-Means + Classifier Hybrid (Phương pháp Kết Hợp Đột Phá)
- **Quy trình kết hợp chuẩn mực:**
  1. Chuẩn hóa dữ liệu trên Train Set.
  2. Huấn luyện thuật toán phân cụm $K$-Means ($K=3$) **thuần túy Unsupervised trên X_train** (tuyệt đối không đưa nhãn $y$ vào K-Means).
  3. Dự đoán nhãn cụm `cluster_id` cho Train set và Test set.
  4. Mã hóa One-Hot nhãn cụm thành 3 đặc trưng nhị phân (`is_cluster_0`, `is_cluster_1`, `is_cluster_2`).
  5. **Tăng cường đặc trưng (Feature Augmentation):** Ghép nối ma trận:
     $$X_{augmented} = [ X_{features} \parallel \text{OneHot}(\text{Cluster\_ID}) ]$$
  6. Huấn luyện bộ phân loại trên $X_{augmented}$.
- **Ý nghĩa các Cụm Hành Vi (Student Profiling):**
  - *Cụm 0:* Nhóm sinh viên phổ thông, sống cùng bố mẹ, chuyên cần ổn định.
  - *Cụm 1:* Nhóm có điều kiện học tập tốt, có internet tại nhà, định hướng học lên cao (`higher_yes`).
  - *Cụm 2:* Nhóm có mức độ tiêu thụ rượu bia cuối tuần cao (`Walc`, `Dalc`), cần lưu ý về lối sống.

---

## 5. Kết Quả Thực Nghiệm & So Sánh (Thực Tế, Không Giả Lập)

Đánh giá độc lập trên cùng 79 mẫu của Test Set:

| Mô hình | Accuracy | Precision (Macro) | Recall (Macro) | F1-Score (Macro) | F1-Score (Weighted) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Decision Tree** | **88.61%** | 0.8955 | 0.9170 | 0.8998 | 0.8857 |
| **XGBoost** | **93.67%** | 0.9449 | 0.9480 | **0.9463** | 0.9369 |
| **K-Means + Classifier** | **93.67%** | 0.9449 | 0.9480 | **0.9463** | 0.9369 |

- Biểu đồ so sánh trực quan và Confusion Matrix chi tiết được lưu tại: `models/model_comparison.png`.
- File cấu trúc số liệu JSON đầy đủ: `models/evaluation_results.json`.

---

## 6. Hướng Dẫn Chạy & Kiểm Thử

### Bước 1: Kích hoạt môi trường và cài đặt thư viện
```bash
cd ml-service

# Tạo và kích hoạt môi trường ảo
python -m venv venv
.\venv\Scripts\activate       # Windows (hoặc: source venv/bin/activate trên Linux)

# Cài đặt dependencies
pip install -r requirements.txt
```

### Bước 2: Chạy kiểm thử tự động (Unit Tests)
```bash
pytest -v
```
Toàn bộ 13/13 bài test kiểm tra logic tiền xử lý, tính đúng đắn của dự đoán và các endpoint API sẽ được thực thi.

### Bước 3: Huấn luyện lại mô hình (Retrain)
```bash
python -m src.train
```

### Bước 4: Khởi chạy FastAPI Server
```bash
uvicorn src.main:app --host 0.0.0.0 --port 8000 --reload
```
- **Tài liệu Swagger UI tương tác:** [http://localhost:8000/docs](http://localhost:8000/docs)
- **Kiểm tra trạng thái:** [http://localhost:8000/health](http://localhost:8000/health)
- **Xem bảng so sánh mô hình:** [http://localhost:8000/metrics](http://localhost:8000/metrics)

---

## 7. Cấu Trúc Mã Nguồn

```text
ml-service/
├── data/
│   └── student-mat.csv                 # Dữ liệu chính thức UCI (395 dòng)
├── models/
│   ├── decision_tree_model.joblib      # Model Decision Tree đã fit
│   ├── xgboost_model.joblib            # Model XGBoost đã fit
│   ├── kmeans_clustering.joblib        # Model K-Means clustering (K=3)
│   ├── kmeans_hybrid_model.joblib      # Model phân loại tăng cường cụm
│   ├── preprocessor.joblib             # Pipeline StandardScaler + OneHotEncoder
│   ├── evaluation_results.json         # Số liệu đánh giá thực tế
│   └── model_comparison.png           # Biểu đồ so sánh & Confusion Matrix
├── src/
│   ├── __init__.py
│   ├── preprocessing.py               # Xử lý G3, ColumnTransformer, Train/Test Split
│   ├── train.py                       # Huấn luyện 3 mô hình & sinh artifacts
│   ├── evaluate.py                    # Tính Accuracy, F1, Confusion Matrix, vẽ biểu đồ
│   ├── predict.py                     # PredictionEngine phục vụ suy luận
│   └── main.py                        # FastAPI entrypoint (/health, /predict, /predict/batch)
├── tests/
│   ├── test_preprocessing.py          # Unit tests tiền xử lý & chia dữ liệu
│   ├── test_prediction.py             # Unit tests dự đoán & FastAPI endpoints
│   └── test_health.py                 # Unit tests health check
├── pytest.ini
├── requirements.txt
└── README.md
```

