import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { GraduationCap, ArrowRight, ShieldCheck, UserCheck } from 'lucide-react';

export const Login = () => {
  const [username, setUsername] = useState('advisor');
  const [password, setPassword] = useState('advisor123');
  const { loginAsRole } = useAuth();
  const navigate = useNavigate();

  const handleLogin = (e) => {
    e.preventDefault();
    if (username.includes('student')) {
      loginAsRole('STUDENT');
    } else if (username.includes('admin')) {
      loginAsRole('ADMIN');
    } else {
      loginAsRole('ADVISOR');
    }
    navigate('/');
  };

  const handleQuickRole = (role) => {
    loginAsRole(role);
    navigate('/');
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: '#f8fafc',
      padding: '1.5rem'
    }}>
      <div className="card" style={{ maxWidth: '440px', width: '100%', padding: '2.5rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{
            width: '54px',
            height: '54px',
            borderRadius: '14px',
            background: 'linear-gradient(135deg, #4f46e5 0%, #06b6d4 100%)',
            color: '#ffffff',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: '1rem'
          }}>
            <GraduationCap size={32} />
          </div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-main)' }}>
            AI Adaptive Learning
          </h2>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Hệ thống Gợi ý Lộ trình Học tập & Phân loại Trình độ
          </p>
        </div>

        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label className="form-label">Tài khoản (Username)</label>
            <input
              type="text"
              className="form-input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="student / advisor / admin"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Mật khẩu (Password)</label>
            <input
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '0.5rem' }}>
            Đăng nhập vào Hệ thống <ArrowRight size={16} />
          </button>
        </form>

        <div style={{ marginTop: '2rem', borderTop: '1px solid var(--border)', paddingTop: '1.25rem' }}>
          <p style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textAlign: 'center', marginBottom: '0.75rem' }}>
            HOẶC ĐĂNG NHẬP NHANH BẰNG TÀI KHOẢN MẪU (DEMO)
          </p>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.5rem' }}>
            <button
              onClick={() => handleQuickRole('STUDENT')}
              className="btn btn-secondary btn-sm"
            >
              Sinh viên
            </button>
            <button
              onClick={() => handleQuickRole('ADVISOR')}
              className="btn btn-secondary btn-sm"
              style={{ borderColor: 'var(--primary)', color: 'var(--primary)' }}
            >
              Cố vấn (Advisor)
            </button>
            <button
              onClick={() => handleQuickRole('ADMIN')}
              className="btn btn-secondary btn-sm"
            >
              Quản trị (Admin)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

