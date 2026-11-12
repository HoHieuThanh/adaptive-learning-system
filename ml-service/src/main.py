"""
=============================================================================
MODULE: main.py
FastAPI Server cung cấp API dự đoán và giám sát cho ML Service.
=============================================================================
"""

import os
from typing import List, Dict, Any, Optional
from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import json

from src.predict import engine

app = FastAPI(
    title="AI Adaptive Learning - ML Microservice",
    description="Dịch vụ Học máy Phân loại Trình độ Sinh viên và Phân cụm Hành vi Học tập",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ==========================================
# PYDANTIC SCHEMAS (Validation & Docs)
# ==========================================
class StudentFeatures(BaseModel):
    student_id: Optional[str] = Field(default="SV001", description="Mã sinh viên hoặc ID")
    school: Optional[str] = Field(default="GP", description="Trường học (GP hoặc MS)")
    sex: Optional[str] = Field(default="F", description="Giới tính (F hoặc M)")
    age: Optional[int] = Field(default=17, ge=15, le=25, description="Tuổi")
    address: Optional[str] = Field(default="U", description="Khu vực sinh sống (U: Urban, R: Rural)")
    famsize: Optional[str] = Field(default="GT3", description="Quy mô gia đình (LE3: <=3, GT3: >3)")
    Pstatus: Optional[str] = Field(default="T", description="Tình trạng bố mẹ sống chung (T) hay ly thân (A)")
    Medu: Optional[int] = Field(default=3, ge=0, le=4, description="Trình độ học vấn của mẹ (0-4)")
    Fedu: Optional[int] = Field(default=3, ge=0, le=4, description="Trình độ học vấn của bố (0-4)")
    Mjob: Optional[str] = Field(default="other", description="Nghề nghiệp của mẹ")
    Fjob: Optional[str] = Field(default="other", description="Nghề nghiệp của bố")
    reason: Optional[str] = Field(default="course", description="Lý do chọn trường")
    guardian: Optional[str] = Field(default="mother", description="Người giám hộ")
    traveltime: Optional[int] = Field(default=1, ge=1, le=4, description="Thời gian di chuyển (1-4)")
    studytime: Optional[int] = Field(default=2, ge=1, le=4, description="Thời gian tự học hàng tuần (1: <2h, 2: 2-5h, 3: 5-10h, 4: >10h)")
    failures: Optional[int] = Field(default=0, ge=0, le=4, description="Số lần trượt môn trong quá khứ")
    schoolsup: Optional[str] = Field(default="no", description="Hỗ trợ giáo dục từ trường (yes/no)")
    famsup: Optional[str] = Field(default="yes", description="Hỗ trợ giáo dục từ gia đình (yes/no)")
    paid: Optional[str] = Field(default="no", description="Học thêm ngoài giờ (yes/no)")
    activities: Optional[str] = Field(default="yes", description="Tham gia hoạt động ngoại khóa (yes/no)")
    nursery: Optional[str] = Field(default="yes", description="Từng học mẫu giáo (yes/no)")
    higher: Optional[str] = Field(default="yes", description="Có ý định học lên đại học/cao học (yes/no)")
    internet: Optional[str] = Field(default="yes", description="Có kết nối Internet tại nhà (yes/no)")
    romantic: Optional[str] = Field(default="no", description="Đang trong mối quan hệ tình cảm (yes/no)")
    famrel: Optional[int] = Field(default=4, ge=1, le=5, description="Chất lượng quan hệ gia đình (1-5)")
    freetime: Optional[int] = Field(default=3, ge=1, le=5, description="Thời gian rảnh rỗi (1-5)")
    goout: Optional[int] = Field(default=3, ge=1, le=5, description="Tần suất đi chơi với bạn bè (1-5)")
    Dalc: Optional[int] = Field(default=1, ge=1, le=5, description="Mức tiêu thụ đồ uống có cồn ngày thường (1-5)")
    Walc: Optional[int] = Field(default=1, ge=1, le=5, description="Mức tiêu thụ đồ uống có cồn cuối tuần (1-5)")
    health: Optional[int] = Field(default=4, ge=1, le=5, description="Tình trạng sức khỏe hiện tại (1-5)")
    absences: Optional[int] = Field(default=4, ge=0, description="Số buổi vắng học")
    G1: Optional[float] = Field(default=11.0, ge=0, le=20, description="Điểm kỳ 1 (thang 0-20)")
    G2: Optional[float] = Field(default=12.0, ge=0, le=20, description="Điểm kỳ 2 (thang 0-20)")


class SinglePredictionRequest(BaseModel):
    student: StudentFeatures
    model_name: Optional[str] = Field(default="xgboost", description="Tùy chọn: 'xgboost', 'decision_tree', hoặc 'kmeans_hybrid'")


class BatchPredictionRequest(BaseModel):
    students: List[StudentFeatures]
    model_name: Optional[str] = Field(default="xgboost", description="Tùy chọn: 'xgboost', 'decision_tree', hoặc 'kmeans_hybrid'")


class PredictionResponse(BaseModel):
    student_id: str
    predicted_level: str
    model_name: str
    confidence: float
    cluster_id: int
    probabilities: Dict[str, float]


# ==========================================
# REST API ENDPOINTS
# ==========================================
@app.get("/health", tags=["Monitoring"])
def health_check():
    """
    Endpoint kiểm tra trạng thái hoạt động của ML Service.
    """
    return {
        "status": "UP",
        "service": "ml-service",
        "model_loaded": engine.is_loaded,
        "available_models": ["xgboost", "decision_tree", "kmeans_hybrid"]
    }


@app.post("/predict", response_model=PredictionResponse, tags=["Inference"])
def predict(request: SinglePredictionRequest):
    """
    Dự đoán trình độ học tập cho 01 sinh viên.
    Trả về: predicted_level (Low, Medium, High), model_name, confidence, cluster_id.
    """
    if not engine.is_loaded:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="ML Models chưa sẵn sàng. Hãy kích hoạt huấn luyện trước."
        )
    try:
        result = engine.predict(
            student_data=request.student.model_dump(),
            model_name=request.model_name
        )
        return result
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Lỗi khi thực hiện suy luận: {str(e)}"
        )


@app.post("/predict/batch", response_model=List[PredictionResponse], tags=["Inference"])
def predict_batch(request: BatchPredictionRequest):
    """
    Dự đoán trình độ học tập cho một danh sách sinh viên (Batch Processing).
    """
    if not engine.is_loaded:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="ML Models chưa sẵn sàng. Hãy kích hoạt huấn luyện trước."
        )
    try:
        raw_list = [s.model_dump() for s in request.students]
        results = engine.predict_batch(
            students_data=raw_list,
            model_name=request.model_name
        )
        return results
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Lỗi khi thực hiện dự đoán batch: {str(e)}"
        )


@app.get("/metrics", tags=["Evaluation"])
def get_model_metrics():
    """
    Trả về kết quả đánh giá thực tế của 3 mô hình từ file JSON đã lưu.
    """
    results_path = os.path.join(engine.models_dir, "evaluation_results.json")
    if not os.path.exists(results_path):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Chưa có kết quả đánh giá. Hãy chạy huấn luyện trước."
        )
    with open(results_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    return data


@app.get("/", tags=["General"])
def root():
    return {
        "message": "AI Adaptive Learning ML Service đang hoạt động",
        "docs_url": "/docs",
        "health_check": "/health"
    }

