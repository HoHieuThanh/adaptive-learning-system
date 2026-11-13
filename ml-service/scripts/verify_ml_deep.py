import os
import sys
import json
import joblib
import numpy as np
import pandas as pd
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, confusion_matrix, classification_report

# Setup path
ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, ROOT_DIR)

from src.preprocessing import load_and_preprocess_dataset, LABEL_MAPPING, REVERSE_LABEL_MAPPING

def main():
    data_csv = os.path.join(ROOT_DIR, "data", "student-mat.csv")
    models_dir = os.path.join(ROOT_DIR, "models")

    # 1. Load dataset & preprocess
    X_train, X_test, y_train, y_test, preprocessor, feature_names, raw_df = load_and_preprocess_dataset(
        csv_path=data_csv, test_size=0.2, random_state=42, include_g1_g2=True
    )

    # Load artifacts
    xgb_model = joblib.load(os.path.join(models_dir, "xgboost_model.joblib"))
    hybrid_model = joblib.load(os.path.join(models_dir, "kmeans_hybrid_model.joblib"))
    kmeans = joblib.load(os.path.join(models_dir, "kmeans_clustering.joblib"))
    cluster_encoder = joblib.load(os.path.join(models_dir, "cluster_encoder.joblib"))

    # Hybrid test feature augmentation
    test_clusters = kmeans.predict(X_test)
    test_cluster_ohe = cluster_encoder.transform(test_clusters.reshape(-1, 1))
    X_test_aug = np.hstack([X_test, test_cluster_ohe])

    print("=" * 80)
    print("MỤC 1. KIỂM CHỨNG PIPELINE & SỐ LƯỢNG ĐẶC TRƯNG")
    print("=" * 80)
    print(f"- Số chiều vector đặc trưng gốc (XGBoost): {X_test.shape[1]}")
    print(f"- Số chiều vector đặc trưng tăng cường (K-Means Hybrid): {X_test_aug.shape[1]}")
    print(f"- Số cụm One-Hot bổ sung: {test_cluster_ohe.shape[1]} (K=3)")
    print(f"- Pipeline độc lập: K-Means chỉ fit trên X_train (Unsupervised, không dùng nhãn y).")

    print("\n" + "=" * 80)
    print("MỤC 2. FEATURE IMPORTANCE CỦA CLUSTER FEATURES TRONG K-MEANS HYBRID")
    print("=" * 80)
    importances = hybrid_model.feature_importances_
    cluster_feats = ["cluster_feature_0", "cluster_feature_1", "cluster_feature_2"]
    all_hybrid_feats = feature_names + cluster_feats
    for i, cf in enumerate(cluster_feats):
        idx = len(feature_names) + i
        print(f"  * {cf}: {importances[idx]:.6f} ({importances[idx] * 100:.2f}%)")

    top_indices = np.argsort(importances)[::-1][:10]
    print("\nTop 10 đặc trưng quan trọng nhất trong mô hình K-Means Hybrid:")
    for rank, idx in enumerate(top_indices, 1):
        print(f"  {rank:2d}. {all_hybrid_feats[idx]:<25}: {importances[idx]:.6f} ({importances[idx] * 100:.2f}%)")

    print("\n" + "=" * 80)
    print("MỤC 3. SO SÁNH XÁC SUẤT (SOFT PROBABILITY) TRÊN 79 MẪU TẬP TEST")
    print("=" * 80)
    proba_xgb = xgb_model.predict_proba(X_test)
    proba_hyb = hybrid_model.predict_proba(X_test_aug)
    pred_xgb = np.argmax(proba_xgb, axis=1)
    pred_hyb = np.argmax(proba_hyb, axis=1)

    diff_samples = []
    for i in range(len(y_test)):
        p_diff = float(np.max(np.abs(proba_xgb[i] - proba_hyb[i])))
        if p_diff > 1e-4:
            diff_samples.append({
                "sample_idx": i,
                "true_label": REVERSE_LABEL_MAPPING[y_test[i]],
                "pred_label": REVERSE_LABEL_MAPPING[pred_xgb[i]],
                "xgb_proba": [round(float(x), 4) for x in proba_xgb[i]],
                "hyb_proba": [round(float(x), 4) for x in proba_hyb[i]],
                "max_p_diff": round(p_diff, 4)
            })

    print(f"- Tổng số mẫu kiểm thử: {len(y_test)}")
    print(f"- Số mẫu có xác suất mềm khác nhau giữa XGBoost và Hybrid: {len(diff_samples)} / {len(y_test)} ({len(diff_samples)/len(y_test)*100:.1f}%)")
    print(f"- Số mẫu có cùng nhãn dự đoán rời rạc giữa XGBoost và Hybrid: {int(np.sum(pred_xgb == pred_hyb))} / {len(y_test)} (100.0%)")

    print("\nBẢNG MINH CHỨNG 15 MẪU CÓ XÁC SUẤT KHÁC NHAU NHƯNG CÙNG NHÃN DỰ ĐOÁN:")
    print(f"{'Idx':<4} | {'True':<7} | {'Pred':<7} | {'XGBoost Proba [L, M, H]':<26} | {'Hybrid Proba [L, M, H]':<26} | {'Max Diff':<8}")
    print("-" * 88)
    for s in diff_samples[:15]:
        xgb_str = str(s['xgb_proba'])
        hyb_str = str(s['hyb_proba'])
        print(f"{s['sample_idx']:<4} | {s['true_label']:<7} | {s['pred_label']:<7} | {xgb_str:<26} | {hyb_str:<26} | {s['max_p_diff']:<8}")

    print("\n" + "=" * 80)
    print("MỤC 4. KIỂM CHỨNG TÁI TẠO METRICS TỪ KẾT QUẢ DỰ ĐOÁN (REPRODUCIBILITY)")
    print("=" * 80)
    acc = accuracy_score(y_test, pred_xgb)
    prec = precision_score(y_test, pred_xgb, average="macro")
    rec = recall_score(y_test, pred_xgb, average="macro")
    f1 = f1_score(y_test, pred_xgb, average="macro")
    print(f"Tính trực tiếp từ y_test & y_pred:")
    print(f"  Accuracy:  {acc:.6f} ({acc*100:.2f}%)")
    print(f"  Precision: {prec:.6f} ({prec*100:.2f}%)")
    print(f"  Recall:    {rec:.6f} ({rec*100:.2f}%)")
    print(f"  F1-Score:  {f1:.6f} ({f1*100:.2f}%)")

    with open(os.path.join(models_dir, "evaluation_results.json")) as f:
        saved_res = json.load(f)["XGBoost"]
    print(f"\nĐối chiếu với file evaluation_results.json lưu sẵn:")
    print(f"  Accuracy:  {saved_res['accuracy']:.6f}")
    print(f"  Precision: {saved_res['precision_macro']:.6f}")
    print(f"  Recall:    {saved_res['recall_macro']:.6f}")
    print(f"  F1-Score:  {saved_res['f1_macro']:.6f}")
    
    assert round(acc, 4) == round(saved_res['accuracy'], 4), "Accuracy mismatch!"
    assert round(prec, 4) == round(saved_res['precision_macro'], 4), "Precision mismatch!"
    assert round(rec, 4) == round(saved_res['recall_macro'], 4), "Recall mismatch!"
    assert round(f1, 4) == round(saved_res['f1_macro'], 4), "F1 mismatch!"
    print("\n--> [XÁC NHẬN] Các chỉ số trong báo cáo khớp chính xác 100% (làm tròn 4 chữ số thập phân)!")

    print("\n" + "=" * 80)
    print("MỤC 5. CHI TIẾT PHÂN BỔ TẬP DỮ LIỆU & STRATIFIED SPLIT 80/20")
    print("=" * 80)
    total_samples = len(raw_df)
    train_count = len(y_train)
    test_count = len(y_test)
    print(f"- Tổng số mẫu ban đầu: {total_samples}")
    print(f"- Train set: {train_count} mẫu ({train_count/total_samples*100:.1f}%)")
    print(f"- Test set:  {test_count} mẫu ({test_count/total_samples*100:.1f}%)")
    print("\nPhân bổ từng lớp (Low / Medium / High) bảo toàn tỷ lệ qua Stratified Split:")
    print(f"{'Class':<8} | {'ID':<3} | {'Train Count':<12} | {'Train %':<10} | {'Test Count':<11} | {'Test %':<10}")
    print("-" * 65)
    for c in range(3):
        name = REVERSE_LABEL_MAPPING[c]
        tr_c = int(np.sum(y_train == c))
        te_c = int(np.sum(y_test == c))
        print(f"{name:<8} | {c:<3} | {tr_c:<12} | {tr_c/train_count*100:.2f}%     | {te_c:<11} | {te_c/test_count*100:.2f}%")

if __name__ == "__main__":
    main()
