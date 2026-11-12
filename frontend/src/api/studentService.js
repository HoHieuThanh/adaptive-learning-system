import apiClient from './client';
import { MOCK_STUDENTS } from './mockData';

export const studentService = {
  async getAll() {
    try {
      const response = await apiClient.get('/students');
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      console.warn('[DEMO MODE] Không thể kết nối Backend, sử dụng dữ liệu mẫu cho danh sách sinh viên.');
      return { success: true, data: MOCK_STUDENTS, isMock: true };
    }
  },

  async getById(id) {
    try {
      const response = await apiClient.get(`/students/${id}`);
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      const found = MOCK_STUDENTS.find(s => s.id === Number(id)) || MOCK_STUDENTS[0];
      return { success: true, data: found, isMock: true };
    }
  },

  async create(studentData) {
    try {
      const response = await apiClient.post('/students', studentData);
      return { success: true, data: response.data.data };
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tạo sinh viên');
    }
  },

  async update(id, studentData) {
    try {
      const response = await apiClient.put(`/students/${id}`, studentData);
      return { success: true, data: response.data.data };
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể cập nhật sinh viên');
    }
  },

  async delete(id) {
    try {
      const response = await apiClient.delete(`/students/${id}`);
      return { success: true, data: response.data };
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể xóa sinh viên');
    }
  }
};

