import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor nạp Basic Auth nếu người dùng đã đăng nhập
apiClient.interceptors.request.use(
  (config) => {
    const authData = localStorage.getItem('adaptive_auth');
    if (authData) {
      try {
        const { username, password } = JSON.parse(authData);
        if (username && password) {
          const token = btoa(`${username}:${password}`);
          config.headers.Authorization = `Basic ${token}`;
        }
      } catch (err) {
        console.error('Lỗi phân tích auth header:', err);
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default apiClient;

