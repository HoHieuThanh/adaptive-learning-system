import apiClient from './client';
import { MOCK_GRADES } from './mockData';

export const gradeService = {
  async getAll() {
    try {
      const response = await apiClient.get('/grades');
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      return { success: true, data: MOCK_GRADES, isMock: true };
    }
  },

  async getByStudentId(studentId) {
    try {
      const response = await apiClient.get(`/grades/student/${studentId}`);
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      const filtered = MOCK_GRADES.filter(g => g.studentId === Number(studentId));
      return { success: true, data: filtered, isMock: true };
    }
  },

  async create(gradeData) {
    try {
      const response = await apiClient.post('/grades', gradeData);
      return { success: true, data: response.data.data };
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tạo bản ghi điểm');
    }
  }
};

