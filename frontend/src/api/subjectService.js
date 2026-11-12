import apiClient from './client';
import { MOCK_SUBJECTS } from './mockData';

export const subjectService = {
  async getAll() {
    try {
      const response = await apiClient.get('/subjects');
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      return { success: true, data: MOCK_SUBJECTS, isMock: true };
    }
  },

  async create(subjectData) {
    try {
      const response = await apiClient.post('/subjects', subjectData);
      return { success: true, data: response.data.data };
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tạo môn học');
    }
  },

  async delete(id) {
    try {
      const response = await apiClient.delete(`/subjects/${id}`);
      return { success: true, data: response.data };
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể xóa môn học');
    }
  }
};

