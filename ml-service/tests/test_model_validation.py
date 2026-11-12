"""
=============================================================================
TEST SUITE: test_model_validation.py
Mục đích:
- Kiểm tra tính toàn vẹn của ML Model Artifacts (joblib loading).
- Kiểm tra Input Schema Validation (Pydantic & Preprocessor dimensions).
- Dự đoán thử nghiệm trên tập dữ liệu Test (Held-out Test Set).
- Kiểm tra các chỉ số chất lượng (Metrics: Accuracy, F1-Score) không thấp hơn
  ngưỡng chất lượng tối thiểu đã cấu hình (Quality Gate / Model Governance).
=============================================================================
"""

import os
import json
import joblib
import pytest
import numpy as np
import pandas as pd
from pydantic import ValidationError
from sklearn.metrics import accuracy_score, f1_score, recall_score, precision_score

from src.main import StudentFeatures
from src.preprocessing import (
    load_and_preprocess_dataset,
    LABEL_MAPPING,
    REVERSE_LABEL_MAPPING
)

# Thư mục gốc chứa model artifacts
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODELS_DIR = os.path.join(BASE_DIR, "models")
DATA_DIR = os.path.join(BASE_DIR, "data")
CSV_PATH = os.path.join(DATA_DIR, "student-mat.csv")

# Ngưỡng chất lượng tối thiểu cho mô hình sản phẩm (Quality Gates)
MIN_ACCURACY_THRESHOLD = 0.80      # Tối thiểu 80% độ chính xác
MIN_F1_MACRO_THRESHOLD = 0.80      # Tối thiểu 0.80 F1-score bình quân 3 lớp
MIN_RECALL_MACRO_THRESHOLD = 0.80  # Tối thiểu 80% độ nhạy nhận diện học sinh At-Risk/Medium/High


# Dữ liệu sinh viên mẫu hợp lệ
VALID_STUDENT_DICT = {
    "student_id": "SV_VALID_001",
    "school": "GP",
    "sex": "F",
    "age": 17,
    "address": "U",
    "famsize": "GT3",
    "Pstatus": "T",
    "Medu": 4,
    "Fedu": 4,
    "Mjob": "health",
    "Fjob": "teacher",
    "reason": "course",
    "guardian": "mother",
    "traveltime": 1,
    "studytime": 3,
    "failures": 0,
    "schoolsup": "no",
    "famsup": "yes",
    "paid": "yes",
    "activities": "yes",
    "nursery": "yes",
    "higher": "yes",
    "internet": "yes",
    "romantic": "no",
    "famrel": 4,
    "freetime": 3,
    "goout": 2,
    "Dalc": 1,
    "Walc": 1,
    "health": 5,
    "absences": 2,
    "G1": 14.0,
    "G2": 15.0
}


def test_model_artifacts_exist_and_loadable():
    """1. Kiểm tra toàn bộ file model artifacts tồn tại và load được bằng joblib"""
    required_artifacts = [
        "preprocessor.joblib",
        "decision_tree_model.joblib",
        "xgboost_model.joblib",
        "kmeans_clustering.joblib",
        "cluster_encoder.joblib",
        "kmeans_hybrid_model.joblib",
        "metadata.joblib"
    ]
    for filename in required_artifacts:
        path = os.path.join(MODELS_DIR, filename)
        assert os.path.exists(path), f"Không tìm thấy artifact: {path}"
        loaded_obj = joblib.load(path)
        assert loaded_obj is not None, f"Không thể load artifact: {path}"


def test_input_schema_validation_success():
    """2. Kiểm tra input schema hợp lệ được Pydantic xác thực thành công"""
    student = StudentFeatures(**VALID_STUDENT_DICT)
    assert student.age == 17
    assert student.studytime == 3
    assert student.G1 == 14.0
    assert student.G2 == 15.0


def test_input_schema_validation_failure_on_invalid_data():
    """3. Kiểm tra input schema ném lỗi validation khi sai dải giá trị hoặc sai kiểu dữ liệu"""
    # G1 ngoài thang điểm 0-20
    invalid_g1 = {**VALID_STUDENT_DICT, "G1": 25.0}
    with pytest.raises(ValidationError):
        StudentFeatures(**invalid_g1)

    # age ngoài khoảng 15-25
    invalid_age = {**VALID_STUDENT_DICT, "age": 10}
    with pytest.raises(ValidationError):
        StudentFeatures(**invalid_age)

    # studytime ngoài dải 1-4
    invalid_studytime = {**VALID_STUDENT_DICT, "studytime": 9}
    with pytest.raises(ValidationError):
        StudentFeatures(**invalid_studytime)

    # Sai kiểu dữ liệu cho studytime (string không thể convert sang int)
    invalid_type = {**VALID_STUDENT_DICT, "studytime": "khong_phai_so"}
    with pytest.raises(ValidationError):
        StudentFeatures(**invalid_type)


def test_preprocessor_transformation_dimension():
    """4. Kiểm tra pipeline tiền xử lý biến đổi đúng số lượng features mong đợi"""
    preprocessor = joblib.load(os.path.join(MODELS_DIR, "preprocessor.joblib"))
    df_single = pd.DataFrame([VALID_STUDENT_DICT])
    transformed = preprocessor.transform(df_single)
    
    # Dataset student-mat có 15 numerical + one-hot encoded categorical = ~41 features
    assert transformed.shape[0] == 1
    assert transformed.shape[1] >= 35, f"Số chiều sau transform ({transformed.shape[1]}) không hợp lệ"


def test_models_inference_on_held_out_test_set():
    """
    5. Dự đoán thử trên tập Test Set độc lập (Held-out Test Set)
       và kiểm tra các metrics không thấp hơn ngưỡng cho phép (Quality Gates).
    """
    if not os.path.exists(CSV_PATH):
        pytest.skip(f"Dataset {CSV_PATH} không tồn tại để kiểm tra test set")

    # Tách dữ liệu Train/Test theo đúng seed chuẩn (không rò rỉ dữ liệu)
    X_train, X_test, y_train, y_test, preprocessor, feature_names, _ = load_and_preprocess_dataset(
        csv_path=CSV_PATH,
        test_size=0.2,
        random_state=42,
        include_g1_g2=True
    )

    assert len(X_test) > 0, "Tập test không được rỗng"
    assert len(y_test) == len(X_test)

    # Nạp models
    dt_model = joblib.load(os.path.join(MODELS_DIR, "decision_tree_model.joblib"))
    xgb_model = joblib.load(os.path.join(MODELS_DIR, "xgboost_model.joblib"))
    kmeans = joblib.load(os.path.join(MODELS_DIR, "kmeans_clustering.joblib"))
    cluster_enc = joblib.load(os.path.join(MODELS_DIR, "cluster_encoder.joblib"))
    hybrid_model = joblib.load(os.path.join(MODELS_DIR, "kmeans_hybrid_model.joblib"))

    # --- A. Đánh giá Decision Tree ---
    y_pred_dt = dt_model.predict(X_test)
    dt_acc = accuracy_score(y_test, y_pred_dt)
    dt_f1_macro = f1_score(y_test, y_pred_dt, average="macro")
    print(f"\n[Quality Gate] Decision Tree -> Accuracy: {dt_acc:.4f}, F1-Macro: {dt_f1_macro:.4f}")
    assert dt_acc >= MIN_ACCURACY_THRESHOLD, f"Decision Tree Accuracy ({dt_acc}) thấp hơn ngưỡng {MIN_ACCURACY_THRESHOLD}"
    assert dt_f1_macro >= MIN_F1_MACRO_THRESHOLD, f"Decision Tree F1-Macro ({dt_f1_macro}) thấp hơn ngưỡng {MIN_F1_MACRO_THRESHOLD}"

    # --- B. Đánh giá XGBoost ---
    y_pred_xgb = xgb_model.predict(X_test)
    xgb_acc = accuracy_score(y_test, y_pred_xgb)
    xgb_f1_macro = f1_score(y_test, y_pred_xgb, average="macro")
    print(f"[Quality Gate] XGBoost -> Accuracy: {xgb_acc:.4f}, F1-Macro: {xgb_f1_macro:.4f}")
    assert xgb_acc >= MIN_ACCURACY_THRESHOLD, f"XGBoost Accuracy ({xgb_acc}) thấp hơn ngưỡng {MIN_ACCURACY_THRESHOLD}"
    assert xgb_f1_macro >= MIN_F1_MACRO_THRESHOLD, f"XGBoost F1-Macro ({xgb_f1_macro}) thấp hơn ngưỡng {MIN_F1_MACRO_THRESHOLD}"

    # --- C. Đánh giá K-Means Hybrid ---
    test_clusters = kmeans.predict(X_test).reshape(-1, 1)
    test_cluster_ohe = cluster_enc.transform(test_clusters)
    X_test_hybrid = np.hstack([X_test, test_cluster_ohe])
    y_pred_hybrid = hybrid_model.predict(X_test_hybrid)
    hybrid_acc = accuracy_score(y_test, y_pred_hybrid)
    hybrid_f1_macro = f1_score(y_test, y_pred_hybrid, average="macro")
    print(f"[Quality Gate] K-Means Hybrid -> Accuracy: {hybrid_acc:.4f}, F1-Macro: {hybrid_f1_macro:.4f}")
    assert hybrid_acc >= MIN_ACCURACY_THRESHOLD, f"Hybrid Accuracy ({hybrid_acc}) thấp hơn ngưỡng {MIN_ACCURACY_THRESHOLD}"
    assert hybrid_f1_macro >= MIN_F1_MACRO_THRESHOLD, f"Hybrid F1-Macro ({hybrid_f1_macro}) thấp hơn ngưỡng {MIN_F1_MACRO_THRESHOLD}"


def test_saved_evaluation_results_json():
    """6. Kiểm tra file evaluation_results.json lưu đầy đủ báo cáo 3 models"""
    eval_json_path = os.path.join(MODELS_DIR, "evaluation_results.json")
    assert os.path.exists(eval_json_path), f"Không tìm thấy file: {eval_json_path}"
    
    with open(eval_json_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    for model_name in ["Decision Tree", "XGBoost", "K-Means + Classifier"]:
        assert model_name in data, f"Thiếu đánh giá của model: {model_name}"
        res = data[model_name]
        assert res["accuracy"] >= MIN_ACCURACY_THRESHOLD
        assert res["f1_macro"] >= MIN_F1_MACRO_THRESHOLD
        assert len(res["confusion_matrix"]) == 3  # 3x3 matrix (Low, Medium, High)
