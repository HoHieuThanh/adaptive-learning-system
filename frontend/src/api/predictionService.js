import apiClient from './client';

export const predictionService = {
  async predict(studentId, modelName = 'xgboost') {
    try {
      const response = await apiClient.post(`/predictions/predict/${studentId}`, { modelName });
      return { success: true, data: { ...response.data.data, isMock: false }, isMock: false };
    } catch (error) {
      console.warn('[DEMO MODE] Không thể kết nối Backend/ML Service, sử dụng dữ liệu mẫu mô phỏng:', error.message);
      const isRisk = Number(studentId) === 3;
      return {
        success: true,
        data: {
          id: Date.now(),
          studentId: Number(studentId),
          studentName: isRisk ? "Lê Hoàng Cường" : "Nguyễn Văn An",
          studentCode: isRisk ? "SV003" : "SV001",
          predictedLevel: isRisk ? "Low" : "High",
          confidence: isRisk ? 0.9410 : 0.9520,
          modelName: `${modelName === 'decision_tree' ? 'Decision Tree' : (modelName === 'kmeans_hybrid' ? 'K-Means + Classifier' : 'XGBoost')} (Demo Mock)`,
          createdAt: new Date().toISOString(),
          isMock: true,
          mockWarning: "DỮ LIỆU DEMO MÔ PHỎNG - Chưa kết nối Backend (8080) hoặc ML Service (8000)"
        },
        isMock: true,
        error: error.message
      };
    }
  },

  async getHistoryByStudent(studentId) {
    try {
      const response = await apiClient.get(`/predictions/student/${studentId}`);
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      return {
        success: true,
        data: [
          {
            id: 1,
            studentId: Number(studentId),
            predictedLevel: Number(studentId) === 3 ? "Low" : "High",
            confidence: 0.9410,
            modelName: "XGBoost",
            createdAt: "2026-11-12T08:00:00"
          }
        ],
        isMock: true
      };
    }
  },

  async getAll() {
    try {
      const response = await apiClient.get('/predictions');
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      return {
        success: true,
        data: [
          { id: 1, studentId: 1, studentName: "Nguyễn Văn An", studentCode: "SV001", predictedLevel: "High", confidence: 0.9520, modelName: "XGBoost", createdAt: "2026-11-12T09:00:00" },
          { id: 2, studentId: 2, studentName: "Trần Thị Bình", studentCode: "SV002", predictedLevel: "Medium", confidence: 0.8850, modelName: "XGBoost", createdAt: "2026-11-12T09:05:00" },
          { id: 3, studentId: 3, studentName: "Lê Hoàng Cường", studentCode: "SV003", predictedLevel: "Low", confidence: 0.9410, modelName: "XGBoost", createdAt: "2026-11-12T09:10:00" }
        ],
        isMock: true
      };
    }
  }
};

