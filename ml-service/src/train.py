"""
=============================================================================
MODULE: train.py
Mục đích:
- Huấn luyện 3 mô hình học máy:
    1. Decision Tree Classifier (Baseline model, cây quyết định trực quan)
    2. XGBoost Classifier (Gradient Boosting SOTA)
    3. K-Means + Classifier Hybrid (Phương pháp kết hợp phân cụm và phân loại)
- Tuân thủ quy tắc chống Data Leakage:
    + Train/Test Split trước khi tiền xử lý.
    + Fit Scaler và K-Means CHỈ TRÊN TẬP TRAIN.
    + Không sử dụng nhãn target y khi huấn luyện K-Means (Unsupervised hoàn toàn).
- Đánh giá trên cùng một tập Test set độc lập bằng các số liệu thực.
- Lưu trữ tất cả artifacts (Pipeline, Models, Metadata) bằng `joblib`.
=============================================================================
"""

import os
import joblib
import numpy as np
import pandas as pd
from typing import Dict, Any, Tuple
from sklearn.tree import DecisionTreeClassifier
from sklearn.cluster import KMeans
from sklearn.preprocessing import OneHotEncoder
from xgboost import XGBClassifier

from src.preprocessing import (
    load_and_preprocess_dataset,
    LABEL_MAPPING,
    REVERSE_LABEL_MAPPING
)
from src.evaluate import evaluate_single_model, compare_and_visualize_models


def log_model_to_mlflow(
    model_name: str,
    params: Dict[str, Any],
    metrics: Dict[str, Any],
    tags: Dict[str, str] = None
) -> bool:
    """
    Ghi nhận metrics, params và tags vào MLflow Tracking Server nếu MLflow sẵn sàng.
    Graceful fallback: Không làm gián đoạn pipeline huấn luyện nếu MLflow server chưa bật.
    """
    try:
        import mlflow
        tracking_uri = os.getenv("MLFLOW_TRACKING_URI", "http://localhost:5000")
        mlflow.set_tracking_uri(tracking_uri)
        mlflow.set_experiment("student-performance-prediction")

        with mlflow.start_run(run_name=model_name):
            mlflow.log_params(params)
            mlflow.log_metrics({
                "accuracy": metrics["accuracy"],
                "f1_macro": metrics["f1_macro"],
                "precision_macro": metrics["precision_macro"],
                "recall_macro": metrics["recall_macro"],
            })
            if tags:
                mlflow.set_tags(tags)
            print(f"[MLflow] Ghi nhận run '{model_name}' thành công tới {tracking_uri}")
        return True
    except Exception as e:
        print(f"[MLflow Info] Không thể kết nối MLflow ({e}). Đã lưu artifact cục bộ an toàn.")
        return False



def train_decision_tree(
    X_train: np.ndarray,
    y_train: np.ndarray,
    random_state: int = 42
) -> DecisionTreeClassifier:
    """
    Huấn luyện mô hình Decision Tree Classifier.
    - Giới hạn max_depth=5 và min_samples_split=6 để tránh Overfitting trên dataset nhỏ (~395 mẫu).
    """
    print("\n--> [1/3] Đang huấn luyện Decision Tree Classifier...")
    model = DecisionTreeClassifier(
        criterion='gini',
        max_depth=5,
        min_samples_split=6,
        min_samples_leaf=3,
        random_state=random_state
    )
    model.fit(X_train, y_train)
    return model


def train_xgboost(
    X_train: np.ndarray,
    y_train: np.ndarray,
    random_state: int = 42
) -> XGBClassifier:
    """
    Huấn luyện mô hình XGBoost Classifier.
    - n_estimators=100, max_depth=3, learning_rate=0.05
    - objective='multi:softprob' cho phân loại 3 lớp (Low, Medium, High).
    """
    print("\n--> [2/3] Đang huấn luyện XGBoost Classifier...")
    model = XGBClassifier(
        n_estimators=100,
        max_depth=3,
        learning_rate=0.05,
        subsample=0.8,
        colsample_bytree=0.8,
        random_state=random_state,
        eval_metric='mlogloss'
    )
    model.fit(X_train, y_train)
    return model


def train_kmeans_hybrid(
    X_train: np.ndarray,
    y_train: np.ndarray,
    X_test: np.ndarray,
    n_clusters: int = 3,
    random_state: int = 42
) -> Tuple[Any, KMeans, OneHotEncoder, np.ndarray, np.ndarray]:
    """
    Phương pháp K-Means kết hợp Phân loại (Cluster-Augmented Classification):
    
    NGUYÊN LÝ HOẠT ĐỘNG:
    1. K-Means được fit HOÀN TOÀN TRÊN X_train (không có nhãn y, thuần túy Unsupervised).
    2. Dự đoán cluster_id cho từng mẫu trong Train set và Test set.
    3. Mã hóa One-Hot các cluster_id (ví dụ 3 cụm -> 3 cột nhị phân: is_cluster_0, is_cluster_1, is_cluster_2).
    4. Ghép nối các đặc trưng cụm này vào ma trận đặc trưng ban đầu:
       X_augmented = [ X_features | OneHot(Cluster_ID) ]
    5. Huấn luyện Classifier (XGBoost) trên X_augmented.
    """
    print(f"\n--> [3/3] Đang huấn luyện K-Means Clustering (K={n_clusters}) + Classifier...")

    # 1. Fit K-Means trên X_train (chuẩn hóa đã được thực hiện trước đó)
    kmeans = KMeans(n_clusters=n_clusters, random_state=random_state, n_init=10)
    train_clusters = kmeans.fit_predict(X_train)
    test_clusters = kmeans.predict(X_test)

    # 2. One-hot encoding nhãn cụm
    cluster_encoder = OneHotEncoder(sparse_output=False, categories=[list(range(n_clusters))])
    train_cluster_ohe = cluster_encoder.fit_transform(train_clusters.reshape(-1, 1))
    test_cluster_ohe = cluster_encoder.transform(test_clusters.reshape(-1, 1))

    # 3. Tăng cường đặc trưng (Feature Augmentation)
    X_train_augmented = np.hstack([X_train, train_cluster_ohe])
    X_test_augmented = np.hstack([X_test, test_cluster_ohe])

    # 4. Huấn luyện Classifier trên tập đặc trưng tăng cường
    hybrid_classifier = XGBClassifier(
        n_estimators=100,
        max_depth=3,
        learning_rate=0.05,
        subsample=0.8,
        colsample_bytree=0.8,
        random_state=random_state,
        eval_metric='mlogloss'
    )
    hybrid_classifier.fit(X_train_augmented, y_train)

    return hybrid_classifier, kmeans, cluster_encoder, X_train_augmented, X_test_augmented


def analyze_clusters(kmeans: KMeans, feature_names: list, n_top_features: int = 5):
    """
    Phân tích tâm cụm (Cluster Centroids) để giải thích ý nghĩa hành vi từng cụm sinh viên.
    (Chỉ phục vụ mục đích phân tích học thuật, không dùng target).
    """
    print("\n--- PHÂN TÍCH Ý NGHĨA CÁC CỤM K-MEANS (STUDENT PROFILING) ---")
    centroids = kmeans.cluster_centers_
    for cluster_id in range(kmeans.n_clusters):
        centroid = centroids[cluster_id]
        # Tìm các thuộc tính có giá trị độ lệch lớn nhất (ảnh hưởng mạnh nhất trong cụm)
        top_indices = np.argsort(np.abs(centroid))[-n_top_features:][::-1]
        print(f"\n[Cụm {cluster_id}] Đặc trưng nổi bật:")
        for idx in top_indices:
            feat_name = feature_names[idx] if idx < len(feature_names) else f"Feature_{idx}"
            print(f"   - {feat_name}: {centroid[idx]:.3f} (giá trị chuẩn hóa)")


def run_training_pipeline(
    dataset_path: str = "data/student-mat.csv",
    models_dir: str = "models",
    include_g1_g2: bool = True
) -> Dict[str, Any]:
    """
    Quy trình toàn diện: Tải dữ liệu -> Tiền xử lý -> Huấn luyện 3 mô hình -> Đánh giá -> Lưu trữ
    """
    os.makedirs(models_dir, exist_ok=True)

    print("==================================================================")
    print("BẮT ĐẦU QUY TRÌNH HUẤN LUYỆN MACHINE LEARNING CHUẨN MỰC")
    print(f"Dataset: {dataset_path} | Bao gồm điểm G1, G2: {include_g1_g2}")
    print("==================================================================")

    # 1. Đọc và Tiền xử lý dữ liệu (Có Stratified Split, Không Data Leakage)
    (
        X_train,
        X_test,
        y_train,
        y_test,
        preprocessor,
        feature_names,
        raw_df
    ) = load_and_preprocess_dataset(
        csv_path=dataset_path,
        test_size=0.2,
        random_state=42,
        include_g1_g2=include_g1_g2
    )

    print(f"[OK] Kích thước Train set: {X_train.shape[0]} dòng, {X_train.shape[1]} đặc trưng")
    print(f"[OK] Kích thước Test set:  {X_test.shape[0]} dòng, {X_test.shape[1]} đặc trưng")

    # 2. Huấn luyện Mô hình 1: Decision Tree
    dt_model = train_decision_tree(X_train, y_train)

    # 3. Huấn luyện Mô hình 2: XGBoost
    xgb_model = train_xgboost(X_train, y_train)

    # 4. Huấn luyện Mô hình 3: K-Means Hybrid
    hybrid_model, kmeans, cluster_encoder, X_train_aug, X_test_aug = train_kmeans_hybrid(
        X_train, y_train, X_test, n_clusters=3
    )

    # Phân tích cụm K-Means
    analyze_clusters(kmeans, feature_names)

    # 5. Đánh giá trên CÙNG MỘT TẬP TEST SET ĐỘC LẬP
    print("\n==================================================================")
    print("KẾT QUẢ ĐÁNH GIÁ TRÊN TEST SET ĐỘC LẬP (KHÔNG DÙNG SỐ LIỆU GIẢ)")
    print("==================================================================")

    # Dự đoán trên test set
    y_pred_dt = dt_model.predict(X_test)
    y_pred_xgb = xgb_model.predict(X_test)
    y_pred_hybrid = hybrid_model.predict(X_test_aug)

    results = {
        "Decision Tree": evaluate_single_model(y_test, y_pred_dt, "Decision Tree"),
        "XGBoost": evaluate_single_model(y_test, y_pred_xgb, "XGBoost"),
        "K-Means + Classifier": evaluate_single_model(y_test, y_pred_hybrid, "K-Means + Classifier")
    }

    # In kết quả so sánh dạng bảng console
    print(f"{'Mô hình':<25} | {'Accuracy':<10} | {'Precision (M)':<15} | {'Recall (M)':<12} | {'F1-Score (M)':<12}")
    print("-" * 80)
    for name, metrics in results.items():
        print(f"{name:<25} | {metrics['accuracy']:<10.4f} | {metrics['precision_macro']:<15.4f} | {metrics['recall_macro']:<12.4f} | {metrics['f1_macro']:<12.4f}")

    # 6. Ghi nhận Experiment Tracking qua MLflow (nếu có server/thư viện)
    log_model_to_mlflow(
        "Decision_Tree_Classifier",
        {"criterion": "gini", "max_depth": 5, "min_samples_split": 6, "min_samples_leaf": 3},
        results["Decision Tree"],
        {"model_family": "decision_tree", "baseline": "true"}
    )
    log_model_to_mlflow(
        "XGBoost_Classifier",
        {"n_estimators": 100, "max_depth": 3, "learning_rate": 0.05, "subsample": 0.8, "colsample_bytree": 0.8},
        results["XGBoost"],
        {"model_family": "xgboost", "sota": "true"}
    )
    log_model_to_mlflow(
        "KMeans_Hybrid_Classifier",
        {"n_clusters": 3, "n_estimators": 100, "max_depth": 3, "learning_rate": 0.05},
        results["K-Means + Classifier"],
        {"model_family": "hybrid_kmeans_xgboost", "augmented": "true"}
    )

    # 7. Trực quan hóa và lưu file ảnh so sánh
    chart_path = os.path.join(models_dir, "model_comparison.png")
    json_path = os.path.join(models_dir, "evaluation_results.json")
    compare_and_visualize_models(results, output_image_path=chart_path, output_json_path=json_path)

    # 7. Lưu trữ toàn bộ mô hình và bộ tiền xử lý bằng Joblib
    metadata = {
        "feature_names": feature_names,
        "label_mapping": LABEL_MAPPING,
        "reverse_label_mapping": REVERSE_LABEL_MAPPING,
        "include_g1_g2": include_g1_g2,
        "n_clusters": 3,
        "train_samples": len(X_train),
        "test_samples": len(X_test),
        "results": results
    }

    joblib.dump(preprocessor, os.path.join(models_dir, "preprocessor.joblib"))
    joblib.dump(dt_model, os.path.join(models_dir, "decision_tree_model.joblib"))
    joblib.dump(xgb_model, os.path.join(models_dir, "xgboost_model.joblib"))
    joblib.dump(hybrid_model, os.path.join(models_dir, "kmeans_hybrid_model.joblib"))
    joblib.dump(kmeans, os.path.join(models_dir, "kmeans_clustering.joblib"))
    joblib.dump(cluster_encoder, os.path.join(models_dir, "cluster_encoder.joblib"))
    joblib.dump(metadata, os.path.join(models_dir, "metadata.joblib"))

    print("\n[THÀNH CÔNG] Toàn bộ Artifacts đã được lưu vào thư mục:", os.path.abspath(models_dir))
    return results


if __name__ == "__main__":
    # Đường dẫn file dữ liệu thực tế
    data_csv = os.path.join(os.path.dirname(__file__), "..", "data", "student-mat.csv")
    models_dir = os.path.join(os.path.dirname(__file__), "..", "models")
    run_training_pipeline(dataset_path=data_csv, models_dir=models_dir, include_g1_g2=True)

