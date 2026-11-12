# ==============================================================================
# DOCKERFILE CHO FASTAPI ML SERVICE (PYTHON 3.11)
# ==============================================================================

FROM python:3.11-slim

WORKDIR /app

# Cài đặt thư viện C/C++ cần thiết cho XGBoost (libgomp1) và curl cho healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# Cài đặt Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy source code, data và model artifacts đã huấn luyện
COPY . .

EXPOSE 8000

HEALTHCHECK --interval=15s --timeout=5s --start-period=15s --retries=3 \
  CMD curl -f http://localhost:8000/health || exit 1

CMD ["uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "8000"]
