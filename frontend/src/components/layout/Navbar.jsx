import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { Bell, Sparkles, User, LogOut } from 'lucide-react';
import { Link } from 'react-router-dom';

export const Navbar = () => {
  const { user, loginAsRole, logout } = useAuth();

  return (
    <header style={{
      height: '64px',
      backgroundColor: '#ffffff',
      borderBottom: '1px solid var(--border)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 2rem',
      position: 'sticky',
      top: 0,
      zIndex: 30
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>
          Học kỳ: <strong>HK1 - 2026/2027</strong>
        </span>
        <span style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '0.3rem',
          padding: '0.2rem 0.5rem',
          backgroundColor: '#eff6ff',
          color: '#1d4ed8',
          fontSize: '0.75rem',
          fontWeight: 700,
          borderRadius: '4px'
        }}>
          <Sparkles size={13} /> AI Engine: XGBoost & K-Means
        </span>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
        {/* Quick Role Switcher for demo & grading convenience */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem' }}>
          <span style={{ color: 'var(--text-muted)' }}>Vai trò:</span>
          <button
            onClick={() => loginAsRole('STUDENT')}
            className={`btn btn-sm ${user?.role === 'STUDENT' ? 'btn-primary' : 'btn-secondary'}`}
          >
            Student
          </button>
          <button
            onClick={() => loginAsRole('ADVISOR')}
            className={`btn btn-sm ${user?.role === 'ADVISOR' ? 'btn-primary' : 'btn-secondary'}`}
          >
            Advisor
          </button>
          <button
            onClick={() => loginAsRole('ADMIN')}
            className={`btn btn-sm ${user?.role === 'ADMIN' ? 'btn-primary' : 'btn-secondary'}`}
          >
            Admin
          </button>
        </div>

        {/* User Profile Badge */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', borderLeft: '1px solid var(--border)', paddingLeft: '1rem' }}>
          <div style={{
            width: '36px',
            height: '36px',
            borderRadius: '50%',
            backgroundColor: '#e0e7ff',
            color: '#4338ca',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 700,
            fontSize: '0.85rem'
          }}>
            {user?.fullName?.charAt(0) || 'U'}
          </div>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--text-main)' }}>
              {user?.fullName || 'Người dùng'}
            </span>
            <span style={{ fontSize: '0.7rem', color: 'var(--primary)', fontWeight: 600 }}>
              {user?.role}
            </span>
          </div>
          <button
            onClick={logout}
            title="Đăng xuất"
            style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', marginLeft: '0.5rem' }}
          >
            <LogOut size={18} />
          </button>
        </div>
      </div>
    </header>
  );
};

