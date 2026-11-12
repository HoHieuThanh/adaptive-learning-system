import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../api/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('adaptive_current_user');
    return saved ? JSON.parse(saved) : {
      username: 'advisor',
      fullName: 'TS. Trần Thu Hà',
      role: 'ADVISOR'
    };
  });

  const [isDemoMode, setIsDemoMode] = useState(false);

  useEffect(() => {
    localStorage.setItem('adaptive_current_user', JSON.stringify(user));
  }, [user]);

  const loginAsRole = (roleType) => {
    let mockAccount = {
      username: 'student',
      fullName: 'Nguyễn Văn An (Sinh viên)',
      role: 'STUDENT'
    };
    if (roleType === 'ADVISOR') {
      mockAccount = {
        username: 'advisor',
        fullName: 'TS. Trần Thu Hà (Cố vấn học tập)',
        role: 'ADVISOR'
      };
    } else if (roleType === 'ADMIN') {
      mockAccount = {
        username: 'admin',
        fullName: 'Quản trị viên hệ thống',
        role: 'ADMIN'
      };
    }
    setUser(mockAccount);
    authService.login(mockAccount.username, `${mockAccount.username}123`);
  };

  const logout = () => {
    setUser({
      username: 'guest',
      fullName: 'Khách',
      role: 'GUEST'
    });
    authService.logout();
  };

  return (
    <AuthContext.Provider value={{ user, setUser, loginAsRole, logout, isDemoMode, setIsDemoMode }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

