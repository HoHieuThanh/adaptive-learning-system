"""
=============================================================================
MODULE: evaluate.py
Mục đích:
- Tính toán các chỉ số đánh giá chất lượng mô hình:
    + Accuracy (Độ chính xác tổng thể)
    + Precision (Độ chuẩn xác - Macro & Weighted)
    + Recall (Độ thu hồi / nhạy - Macro & Weighted)
    + F1-Score (Trung bình điều hòa giữa Precision và Recall)
    + Confusion Matrix (Ma trận nhầm lẫn 3x3: Low, Medium, High)
- So sánh công bằng 3 mô hình (Decision Tree, XGBoost, K-Means Hybrid)
  trên CÙNG MỘT TẬP KIỂM THỬ (Test Set).
- Xuất biểu đồ so sánh trực quan (PNG) và lưu kết quả JSON cho Backend/Frontend.
=============================================================================
"""

import json
import os
from typing import Dict, Any, List
import numpy as np
import matplotlib
matplotlib.use('Agg')  # Headless non-GUI backend
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    confusion_matrix,
    classification_report
)

CLASS_NAMES = ['Low', 'Medium', 'High']


def evaluate_single_model(y_true: np.ndarray, y_pred: np.ndarray, model_name: str) -> Dict[str, Any]:
    """
    Tính toán đầy đủ các chỉ số đánh giá cho một mô hình cụ thể.
    """
    acc = float(accuracy_score(y_true, y_pred))
    prec_macro = float(precision_score(y_true, y_pred, average='macro', zero_division=0))
    prec_weighted = float(precision_score(y_true, y_pred, average='weighted', zero_division=0))
    rec_macro = float(recall_score(y_true, y_pred, average='macro', zero_division=0))
    rec_weighted = float(recall_score(y_true, y_pred, average='weighted', zero_division=0))
    f1_macro = float(f1_score(y_true, y_pred, average='macro', zero_division=0))
    f1_weighted = float(f1_score(y_true, y_pred, average='weighted', zero_division=0))
    cm = confusion_matrix(y_true, y_pred, labels=[0, 1, 2]).tolist()

    report_dict = classification_report(
        y_true,
        y_pred,
        target_names=CLASS_NAMES,
        output_dict=True,
        zero_division=0
    )

    return {
        "model_name": model_name,
        "accuracy": round(acc, 4),
        "precision_macro": round(prec_macro, 4),
        "precision_weighted": round(prec_weighted, 4),
        "recall_macro": round(rec_macro, 4),
        "recall_weighted": round(rec_weighted, 4),
        "f1_macro": round(f1_macro, 4),
        "f1_weighted": round(f1_weighted, 4),
        "confusion_matrix": cm,
        "classification_report": report_dict
    }


def compare_and_visualize_models(
    results: Dict[str, Dict[str, Any]],
    output_image_path: str = "models/model_comparison.png",
    output_json_path: str = "models/evaluation_results.json"
):
    """
    Vẽ biểu đồ so sánh 3 mô hình và trực quan hóa Confusion Matrix từng mô hình.
    """
    os.makedirs(os.path.dirname(output_image_path), exist_ok=True)
    os.makedirs(os.path.dirname(output_json_path), exist_ok=True)

    # 1. Lưu kết quả ra file JSON
    with open(output_json_path, 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=4, ensure_ascii=False)

    model_names = list(results.keys())
    metrics_to_plot = ['accuracy', 'precision_macro', 'recall_macro', 'f1_macro']
    metric_labels = ['Accuracy', 'Precision (Macro)', 'Recall (Macro)', 'F1-Score (Macro)']

    # 2. Tạo hình vẽ kích thước lớn (gồm biểu đồ cột và 3 heatmap confusion matrices)
    fig = plt.figure(figsize=(16, 10))
    gs = fig.add_gridspec(2, 3, height_ratios=[1.2, 1])

    # Subplot 1: Bảng so sánh chỉ số tổng hợp
    ax_bar = fig.add_subplot(gs[0, :])
    x = np.arange(len(metric_labels))
    width = 0.25

    colors = ['#3b82f6', '#10b981', '#f59e0b']  # Xanh dương, Xanh lá, Cam

    for i, model_name in enumerate(model_names):
        scores = [results[model_name][m] for m in metrics_to_plot]
        rects = ax_bar.bar(x + (i - 1) * width, scores, width, label=model_name, color=colors[i % len(colors)], alpha=0.85)
        # Thêm nhãn giá trị trên mỗi cột
        for rect in rects:
            height = rect.get_height()
            ax_bar.annotate(f'{height:.3f}',
                            xy=(rect.get_x() + rect.get_width() / 2, height),
                            xytext=(0, 3),
                            textcoords="offset points",
                            ha='center', va='bottom', fontsize=9, fontweight='bold')

    ax_bar.set_ylabel('Điểm số (0.0 - 1.0)', fontsize=11)
    ax_bar.set_title('So sánh Hiệu Năng 3 Mô Hình Học Máy trên Test Set Độc Lập', fontsize=14, fontweight='bold', pad=15)
    ax_bar.set_xticks(x)
    ax_bar.set_xticklabels(metric_labels, fontsize=11, fontweight='medium')
    ax_bar.set_ylim(0, 1.1)
    ax_bar.legend(loc='lower right', fontsize=11)
    ax_bar.grid(axis='y', linestyle='--', alpha=0.5)

    # Subplot 2, 3, 4: Confusion Matrix cho từng mô hình
    for i, model_name in enumerate(model_names):
        ax_cm = fig.add_subplot(gs[1, i])
        cm_data = np.array(results[model_name]["confusion_matrix"])
        sns.heatmap(
            cm_data,
            annot=True,
            fmt='d',
            cmap='Blues',
            cbar=False,
            xticklabels=CLASS_NAMES,
            yticklabels=CLASS_NAMES,
            ax=ax_cm,
            annot_kws={"fontsize": 11, "fontweight": "bold"}
        )
        ax_cm.set_title(f'Confusion Matrix\n{model_name}', fontsize=12, fontweight='bold')
        ax_cm.set_xlabel('Dự đoán (Predicted)', fontsize=10)
        ax_cm.set_ylabel('Thực tế (Actual)', fontsize=10)

    plt.tight_layout()
    plt.savefig(output_image_path, dpi=300)
    plt.close()
    print(f"[OK] Đã lưu biểu đồ đánh giá tại: {output_image_path}")
    print(f"[OK] Đã lưu số liệu JSON tại: {output_json_path}")
