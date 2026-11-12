import apiClient from './client';

export const authService = {
  async getMe() {
    try {
      const response = await apiClient.get('/auth/me');
      return response.data;
    } catch (error) {
      console.warn('Backend chưa sẵn sàng hoặc chưa đăng nhập:', error.message);
      return { success: false, data: null };
    }
  },

  login(username, password) {
    const authPayload = { username, password };
    localStorage.setItem('adaptive_auth', JSON.stringify(authPayload));
  },

  logout() {
    localStorage.removeItem('adaptive_auth');
  }
};

