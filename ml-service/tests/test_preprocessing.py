"""
Unit Tests cho module preprocessing.py
"""

import os
import pytest
import numpy as np
import pandas as pd

from src.preprocessing import (
    convert_g3_to_class,
    build_preprocessor,
    load_and_preprocess_dataset,
    LABEL_MAPPING,
    CATEGORICAL_FEATURES,
    NUMERICAL_FEATURES_WITH_G1_G2
)


def test_convert_g3_to_class():
    """Kiểm tra phân loại đúng 3 khoảng điểm của G3"""
    # Low (< 10)
    assert convert_g3_to_class(0) == 'Low'
    assert convert_g3_to_class(5.5) == 'Low'
    assert convert_g3_to_class(9.9) == 'Low'

    # Medium (10 - 14)
    assert convert_g3_to_class(10) == 'Medium'
    assert convert_g3_to_class(12.5) == 'Medium'
    assert convert_g3_to_class(14) == 'Medium'

    # High (>= 15)
    assert convert_g3_to_class(15) == 'High'
    assert convert_g3_to_class(18) == 'High'
    assert convert_g3_to_class(20) == 'High'


def test_build_preprocessor():
    """Kiểm tra khởi tạo ColumnTransformer với số lượng features đúng"""
    preprocessor, num_cols, cat_cols = build_preprocessor(include_g1_g2=True)
    assert len(num_cols) == len(NUMERICAL_FEATURES_WITH_G1_G2)
    assert len(cat_cols) == len(CATEGORICAL_FEATURES)
    assert preprocessor is not None


def test_load_and_preprocess_dataset():
    """Kiểm tra quy trình nạp dataset, phân tách train/test và không rò rỉ dữ liệu"""
    csv_path = os.path.join(os.path.dirname(__file__), "..", "data", "student-mat.csv")
    if not os.path.exists(csv_path):
        pytest.skip("Dataset file chưa tồn tại")

    X_train, X_test, y_train, y_test, preprocessor, feature_names, raw_df = load_and_preprocess_dataset(
        csv_path=csv_path,
        test_size=0.2,
        random_state=42,
        include_g1_g2=True
    )

    # Tổng số dòng ban đầu là 395
    assert len(X_train) + len(X_test) == 395
    assert len(y_train) + len(y_test) == 395

    # Đảm bảo tỷ lệ 80/20
    assert len(X_train) == 316
    assert len(X_test) == 79

    # Kiểm tra số đặc trưng giống nhau giữa train và test
    assert X_train.shape[1] == X_test.shape[1]

    # Kiểm tra không có giá trị NaN sau khi tiền xử lý
    assert not np.isnan(X_train).any()
    assert not np.isnan(X_test).any()

    # Nhãn y phải là các số nguyên 0, 1, 2
    assert set(np.unique(y_train)).issubset({0, 1, 2})
    assert set(np.unique(y_test)).issubset({0, 1, 2})

