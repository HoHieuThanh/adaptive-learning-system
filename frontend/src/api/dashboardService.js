import apiClient from './client';
import { MOCK_DASHBOARD_STATS } from './mockData';

export const dashboardService = {
  async getStats() {
    try {
      const response = await apiClient.get('/dashboard/stats');
      return { success: true, data: response.data.data, isMock: false };
    } catch (error) {
      console.warn('[DEMO MODE] Sử dụng Mock Dashboard Stats.');
      return { success: true, data: MOCK_DASHBOARD_STATS, isMock: true };
    }
  }
};

