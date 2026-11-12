"""
Unit Tests cho module predict.py và các router endpoints trong src/main.py
"""

import pytest
from src.main import (
    predict,
    predict_batch,
    get_model_metrics,
    SinglePredictionRequest,
    BatchPredictionRequest,
    StudentFeatures
)
from src.predict import engine

SAMPLE_STUDENT = {
    "student_id": "SV_TEST_001",
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
    "G1": 15.0,
    "G2": 16.0
}


def test_prediction_engine_loaded():
    """Kiểm tra PredictionEngine đã nạp đủ models và artifacts"""
    assert engine.is_loaded is True
    assert engine.dt_model is not None
    assert engine.xgb_model is not None
    assert engine.hybrid_model is not None
    assert engine.kmeans is not None


def test_predict_single_xgboost():
    """Kiểm tra dự đoán sinh viên với XGBoost"""
    res = engine.predict(SAMPLE_STUDENT, model_name="xgboost")
    assert res["model_name"] == "XGBoost"
    assert res["predicted_level"] in ["Low", "Medium", "High"]
    assert 0.0 <= res["confidence"] <= 1.0
    assert "probabilities" in res
    assert res["cluster_id"] in [0, 1, 2]


def test_predict_single_decision_tree():
    """Kiểm tra dự đoán sinh viên với Decision Tree"""
    res = engine.predict(SAMPLE_STUDENT, model_name="decision_tree")
    assert res["model_name"] == "Decision Tree"
    assert res["predicted_level"] in ["Low", "Medium", "High"]
    assert 0.0 <= res["confidence"] <= 1.0


def test_predict_single_kmeans_hybrid():
    """Kiểm tra dự đoán sinh viên với K-Means + Classifier Hybrid"""
    res = engine.predict(SAMPLE_STUDENT, model_name="kmeans_hybrid")
    assert res["model_name"] == "K-Means + Classifier"
    assert res["predicted_level"] in ["Low", "Medium", "High"]
    assert res["cluster_id"] in [0, 1, 2]


def test_predict_batch():
    """Kiểm tra dự đoán danh sách sinh viên"""
    batch_input = [
        SAMPLE_STUDENT,
        {**SAMPLE_STUDENT, "student_id": "SV_TEST_002", "G1": 6.0, "G2": 5.0}
    ]
    results = engine.predict_batch(batch_input, model_name="xgboost")
    assert len(results) == 2
    assert results[0]["student_id"] == "SV_TEST_001"
    assert results[1]["student_id"] == "SV_TEST_002"
    # Học sinh 2 có điểm G1=6, G2=5 thường sẽ rơi vào Low
    assert results[1]["predicted_level"] == "Low"


def test_api_predict_endpoint():
    """Kiểm tra hàm xử lý của endpoint POST /predict"""
    student_obj = StudentFeatures(**SAMPLE_STUDENT)
    req = SinglePredictionRequest(student=student_obj, model_name="xgboost")
    data = predict(req)
    assert data["predicted_level"] in ["Low", "Medium", "High"]
    assert data["model_name"] == "XGBoost"
    assert data["student_id"] == "SV_TEST_001"
    assert 0.0 <= data["confidence"] <= 1.0


def test_api_predict_batch_endpoint():
    """Kiểm tra hàm xử lý của endpoint POST /predict/batch"""
    student_obj = StudentFeatures(**SAMPLE_STUDENT)
    req = BatchPredictionRequest(students=[student_obj], model_name="kmeans_hybrid")
    data = predict_batch(req)
    assert isinstance(data, list)
    assert len(data) == 1
    assert data[0]["model_name"] == "K-Means + Classifier"
    assert data[0]["cluster_id"] in [0, 1, 2]


def test_api_metrics_endpoint():
    """Kiểm tra endpoint GET /metrics đọc đúng kết quả thực tế"""
    data = get_model_metrics()
    assert "Decision Tree" in data
    assert "XGBoost" in data
    assert "K-Means + Classifier" in data
    assert data["XGBoost"]["accuracy"] > 0.8
    assert "confusion_matrix" in data["XGBoost"]
