"""
=============================================================================
MODULE: preprocessing.py
Mục đích:
- Đọc dữ liệu học tập của sinh viên từ Student Performance Dataset (UCI).
- Chuyển đổi điểm G3 (thang 0-20) thành 3 lớp phân loại:
    + Low (< 10): Học lực yếu / Nguy cơ rớt môn
    + Medium (10 - 14): Học lực trung bình - khá
    + High (>= 15): Học lực giỏi / xuất sắc
- Đảm bảo KHÔNG CÓ DATA LEAKAGE:
    + Tách tập Train/Test (Stratified 80/20) TRƯỚC KHI fit bất kỳ bộ biến đổi nào.
    + Fit StandardScaler và OneHotEncoder DUY NHẤT trên tập Train, sau đó áp
      dụng (transform) lên cả tập Train và Test.
=============================================================================
"""

import pandas as pd
import numpy as np
from typing import Tuple, List, Dict
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.compose import ColumnTransformer


# Danh sách các đặc trưng Categorical (dạng chữ/chuỗi)
CATEGORICAL_FEATURES = [
    'school', 'sex', 'address', 'famsize', 'Pstatus',
    'Mjob', 'Fjob', 'reason', 'guardian', 'schoolsup',
    'famsup', 'paid', 'activities', 'nursery', 'higher',
    'internet', 'romantic'
]

# Danh sách các đặc trưng Numerical (dạng số)
# G1 (điểm đợt 1) và G2 (điểm đợt 2) có sẵn tại thời điểm giữa kỳ/trước khi kết thúc học kỳ.
NUMERICAL_FEATURES_WITH_G1_G2 = [
    'age', 'Medu', 'Fedu', 'traveltime', 'studytime',
    'failures', 'famrel', 'freetime', 'goout', 'Dalc',
    'Walc', 'health', 'absences', 'G1', 'G2'
]

NUMERICAL_FEATURES_WITHOUT_G1_G2 = [
    'age', 'Medu', 'Fedu', 'traveltime', 'studytime',
    'failures', 'famrel', 'freetime', 'goout', 'Dalc',
    'Walc', 'health', 'absences'
]

# Ánh xạ nhãn sang số nguyên để huấn luyện (đặc biệt cần cho XGBoost)
LABEL_MAPPING = {
    'Low': 0,
    'Medium': 1,
    'High': 2
}

REVERSE_LABEL_MAPPING = {v: k for k, v in LABEL_MAPPING.items()}


def convert_g3_to_class(g3_score: float) -> str:
    """
    Phân loại điểm kết thúc học phần G3 (thang 0-20) thành 3 mức độ:
    - Low: < 10 (Không đạt / Cần can thiệp sớm)
    - Medium: 10 đến 14 (Trung bình - Khá)
    - High: >= 15 (Giỏi - Xuất sắc)
    """
    if g3_score < 10:
        return 'Low'
    elif g3_score <= 14:
        return 'Medium'
    else:
        return 'High'


def build_preprocessor(include_g1_g2: bool = True) -> Tuple[ColumnTransformer, List[str], List[str]]:
    """
    Tạo ColumnTransformer để tiền xử lý:
    - Thuộc tính số: StandardScaler (chuẩn hóa về mean=0, std=1)
    - Thuộc tính phân loại: OneHotEncoder (mã hóa One-Hot, bỏ qua giá trị chưa từng gặp với handle_unknown='ignore')
    """
    num_features = NUMERICAL_FEATURES_WITH_G1_G2 if include_g1_g2 else NUMERICAL_FEATURES_WITHOUT_G1_G2
    cat_features = CATEGORICAL_FEATURES

    preprocessor = ColumnTransformer(
        transformers=[
            ('num', StandardScaler(), num_features),
            ('cat', OneHotEncoder(handle_unknown='ignore', sparse_output=False), cat_features)
        ],
        remainder='drop'  # Loại bỏ các cột không được định nghĩa (ví dụ: G3)
    )
    return preprocessor, num_features, cat_features


def load_and_preprocess_dataset(
    csv_path: str,
    test_size: float = 0.2,
    random_state: int = 42,
    include_g1_g2: bool = True
) -> Tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray, ColumnTransformer, List[str], pd.DataFrame]:
    """
    Hàm đọc dữ liệu, phân chia Train/Test và tiền xử lý chuẩn mực học máy:
    
    1. Đọc file CSV với dấu phân cách ';'
    2. Sinh cột nhãn mục tiêu 'target' dựa trên G3
    3. Loại bỏ G3 khỏi ma trận đặc trưng X (để tránh rò rỉ dữ liệu)
    4. Chia tập Train/Test theo Stratified Split để giữ nguyên tỷ lệ các lớp
    5. Fit bộ tiền xử lý (StandardScaler + OneHotEncoder) CHỈ TRÊN TẬP TRAIN
    6. Transform trên cả tập Train và tập Test
    """
    # 1. Đọc dữ liệu
    df = pd.read_csv(csv_path, sep=';')
    
    if 'G3' not in df.columns:
        raise ValueError("Dataset bắt buộc phải có cột 'G3' làm nhãn mục tiêu.")

    # 2. Tạo nhãn mục tiêu (Target)
    y_str = df['G3'].apply(convert_g3_to_class)
    y_encoded = y_str.map(LABEL_MAPPING).values

    # 3. Loại bỏ G3 khỏi tập đặc trưng X
    X = df.drop(columns=['G3'])

    # Nếu không muốn dùng G1, G2 thì loại bỏ
    if not include_g1_g2:
        cols_to_drop = [col for col in ['G1', 'G2'] if col in X.columns]
        X = X.drop(columns=cols_to_drop)

    # 4. Stratified Train/Test Split (TRƯỚC KHI FIT TIỀN XỬ LÝ)
    X_train_df, X_test_df, y_train, y_test = train_test_split(
        X,
        y_encoded,
        test_size=test_size,
        random_state=random_state,
        stratify=y_encoded
    )

    # 5. Khởi tạo và Fit preprocessor TRÊN TRAIN SET
    preprocessor, num_features, cat_features = build_preprocessor(include_g1_g2=include_g1_g2)
    X_train_proc = preprocessor.fit_transform(X_train_df)

    # 6. Transform TRÊN TEST SET (không gọi fit lại!)
    X_test_proc = preprocessor.transform(X_test_df)

    # Trích xuất tên đặc trưng sau khi One-Hot Encoding để giải thích mô hình
    cat_encoder = preprocessor.named_transformers_['cat']
    encoded_cat_names = cat_encoder.get_feature_names_out(cat_features).tolist()
    feature_names = num_features + encoded_cat_names

    return (
        X_train_proc,
        X_test_proc,
        y_train,
        y_test,
        preprocessor,
        feature_names,
        df
    )

