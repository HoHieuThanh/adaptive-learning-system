import apiClient from './client';
import { MOCK_LEARNING_PATHS } from './mockData';

export const learningPathService = {
  async getLatestByStudent(studentId) {
    try {
      const response = await apiClient.get(`/learning-paths/student/${studentId}/latest`);
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      const mock = MOCK_LEARNING_PATHS[Number(studentId)] || MOCK_LEARNING_PATHS[3];
      return { success: true, data: mock, isMock: true };
    }
  },

  async getAllByStudent(studentId) {
    try {
      const response = await apiClient.get(`/learning-paths/student/${studentId}`);
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      const mock = MOCK_LEARNING_PATHS[Number(studentId)] || MOCK_LEARNING_PATHS[3];
      return { success: true, data: [mock], isMock: true };
    }
  },

  async generateOrRefresh(studentId) {
    try {
      const response = await apiClient.post(`/learning-paths/generate/${studentId}`);
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      console.warn('[DEMO MODE] Fallback refresh lộ trình:', error);
      const mock = MOCK_LEARNING_PATHS[Number(studentId)] || MOCK_LEARNING_PATHS[3];
      return { success: true, data: mock, isMock: true };
    }
  },

  async updateRecommendationStatus(recommendationId, status) {
    try {
      const response = await apiClient.patch(`/learning-paths/recommendations/${recommendationId}/status`, { status });
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      console.warn('[DEMO MODE] Fallback update status recommendation:', error);
      return { success: true, data: { id: recommendationId, status }, isMock: true };
    }
  },

  async updatePathStatus(pathId, status) {
    try {
      const response = await apiClient.patch(`/learning-paths/${pathId}/status`, { status });
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      console.warn('[DEMO MODE] Fallback update path status:', error);
      return { success: true, data: { id: pathId, status }, isMock: true };
    }
  }
};
