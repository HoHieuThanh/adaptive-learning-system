"""
=============================================================================
MODULE: predict.py
Mục đích:
- Nạp các mô hình và bộ tiền xử lý đã huấn luyện từ thư mục `models/`.
- Thực hiện suy luận (Inference) cho một học sinh hoặc một danh sách học sinh (Batch).
- Trả về kết quả gồm:
    + predicted_level: Low, Medium, High
    + model_name: Tên mô hình đã dùng (xgboost, decision_tree, kmeans_hybrid)
    + confidence: Độ tin cậy (xác suất cao nhất từ predict_proba)
    + probabilities: Chi tiết phân phối xác suất cho từng lớp
    + cluster_id: Cụm hành vi học tập tương ứng
=============================================================================
"""

import os
import joblib
import numpy as np
import pandas as pd
from typing import Dict, Any, List, Optional
from src.preprocessing import REVERSE_LABEL_MAPPING, CATEGORICAL_FEATURES, NUMERICAL_FEATURES_WITH_G1_G2


class PredictionEngine:
    def __init__(self, models_dir: Optional[str] = None):
        if models_dir is None:
            models_dir = os.path.join(os.path.dirname(__file__), "..", "models")
        self.models_dir = models_dir
        self.is_loaded = False
        self._load_artifacts()

    def _load_artifacts(self):
        """
        Nạp các file artifacts do quy trình train.py sinh ra.
        """
        try:
            self.preprocessor = joblib.load(os.path.join(self.models_dir, "preprocessor.joblib"))
            self.dt_model = joblib.load(os.path.join(self.models_dir, "decision_tree_model.joblib"))
            self.xgb_model = joblib.load(os.path.join(self.models_dir, "xgboost_model.joblib"))
            self.hybrid_model = joblib.load(os.path.join(self.models_dir, "kmeans_hybrid_model.joblib"))
            self.kmeans = joblib.load(os.path.join(self.models_dir, "kmeans_clustering.joblib"))
            self.cluster_encoder = joblib.load(os.path.join(self.models_dir, "cluster_encoder.joblib"))
            self.metadata = joblib.load(os.path.join(self.models_dir, "metadata.joblib"))
            self.is_loaded = True
            print(f"[OK] Đã tải toàn bộ ML Models & Preprocessor từ: {self.models_dir}")
        except Exception as e:
            print(f"[WARNING] Chưa thể nạp models: {e}. Vui lòng chạy `python -m src.train` trước.")
            self.is_loaded = False

    def _prepare_dataframe(self, raw_data: List[Dict[str, Any]]) -> pd.DataFrame:
        """
        Chuyển đổi dữ liệu thô dạng JSON/dict thành DataFrame chuẩn,
        điền giá trị mặc định cho các trường còn thiếu nếu cần.
        """
        df = pd.DataFrame(raw_data)
        
        # Đảm bảo các cột số có kiểu float/int
        for col in NUMERICAL_FEATURES_WITH_G1_G2:
            if col in df.columns:
                df[col] = pd.to_numeric(df[col], errors='coerce').fillna(0)
            else:
                df[col] = 0

        # Đảm bảo các cột phân loại có kiểu chuỗi
        for col in CATEGORICAL_FEATURES:
            if col in df.columns:
                df[col] = df[col].astype(str).fillna("other")
            else:
                df[col] = "other"

        return df

    def predict(
        self,
        student_data: Dict[str, Any],
        model_name: str = "xgboost"
    ) -> Dict[str, Any]:
        """
        Dự đoán trình độ cho một sinh viên đơn lẻ.
        """
        batch_res = self.predict_batch([student_data], model_name=model_name)
        return batch_res[0]

    def predict_batch(
        self,
        students_data: List[Dict[str, Any]],
        model_name: str = "xgboost"
    ) -> List[Dict[str, Any]]:
        """
        Dự đoán trình độ cho danh sách sinh viên theo lô (Batch).
        """
        if not self.is_loaded:
            self._load_artifacts()
            if not self.is_loaded:
                raise RuntimeError("Mô hình chưa được nạp. Hãy huấn luyện mô hình trước!")

        model_name_clean = model_name.lower().strip()
        df = self._prepare_dataframe(students_data)

        # 1. Tiền xử lý dữ liệu qua ColumnTransformer (đã fit)
        X_proc = self.preprocessor.transform(df)

        # 2. Luôn tính toán cụm hành vi học tập bằng K-Means Unsupervised
        cluster_ids = self.kmeans.predict(X_proc)

        # 3. Lựa chọn mô hình dự đoán
        if model_name_clean in ["decision_tree", "dt"]:
            model = self.dt_model
            X_input = X_proc
            actual_model_name = "Decision Tree"
        elif model_name_clean in ["kmeans_hybrid", "hybrid", "kmeans"]:
            # Tăng cường đặc trưng với nhãn cụm One-Hot
            cluster_ohe = self.cluster_encoder.transform(cluster_ids.reshape(-1, 1))
            X_input = np.hstack([X_proc, cluster_ohe])
            model = self.hybrid_model
            actual_model_name = "K-Means + Classifier"
        else:
            # Mặc định dùng XGBoost
            model = self.xgb_model
            X_input = X_proc
            actual_model_name = "XGBoost"

        # 4. Dự đoán nhãn và xác suất
        preds = model.predict(X_input)
        
        # Lấy xác suất nếu mô hình hỗ trợ
        has_proba = hasattr(model, "predict_proba")
        probas = model.predict_proba(X_input) if has_proba else None

        results = []
        for i in range(len(students_data)):
            pred_idx = int(preds[i])
            predicted_level = REVERSE_LABEL_MAPPING.get(pred_idx, "Unknown")
            
            prob_dict = {}
            confidence = 1.0
            if probas is not None:
                prob_row = probas[i]
                for idx, label in REVERSE_LABEL_MAPPING.items():
                    if idx < len(prob_row):
                        prob_dict[label] = round(float(prob_row[idx]), 4)
                confidence = round(float(np.max(prob_row)), 4)

            results.append({
                "student_id": students_data[i].get("student_id", f"student_{i+1}"),
                "predicted_level": predicted_level,
                "model_name": actual_model_name,
                "confidence": confidence,
                "cluster_id": int(cluster_ids[i]),
                "probabilities": prob_dict
            })

        return results


# Global singleton instance
engine = PredictionEngine()

