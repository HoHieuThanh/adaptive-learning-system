import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Users,
  Brain,
  Compass,
  ShieldAlert,
  BarChart3,
  Settings,
  GraduationCap
} from 'lucide-react';

export const Sidebar = () => {
  const { user } = useAuth();

  const navItems = [
    { label: 'Tổng quan (Dashboard)', path: '/', icon: LayoutDashboard },
    { label: 'Danh sách Sinh viên', path: '/students', icon: Users },
    { label: 'Kết quả Học tập', path: '/performance', icon: BarChart3 },
    { label: 'Dự đoán AI (Inference)', path: '/predictions', icon: Brain },
    { label: 'Lộ trình Thích ứng', path: '/learning-paths', icon: Compass },
    { label: 'Cố vấn Học tập', path: '/advisor', icon: ShieldAlert, highlight: true },
    { label: 'Quản trị Hệ thống', path: '/admin', icon: Settings, adminOnly: true }
  ];

  return (
    <aside className="sidebar">
      {/* Brand logo header */}
      <div style={{
        padding: '1.25rem 1.5rem',
        borderBottom: '1px solid var(--border)',
        display: 'flex',
        alignItems: 'center',
        gap: '0.75rem'
      }}>
        <div style={{
          width: '38px',
          height: '38px',
          borderRadius: '10px',
          background: 'linear-gradient(135deg, #4f46e5 0%, #06b6d4 100%)',
          color: '#ffffff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <GraduationCap size={22} />
        </div>
        <div>
          <h2 style={{ fontSize: '1rem', fontWeight: 800, color: 'var(--text-main)', lineHeight: 1.1 }}>AdaptiveLearn</h2>
          <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 600 }}>AI Portal v1.0</span>
        </div>
      </div>

      {/* Navigation Links */}
      <nav style={{ padding: '1.25rem 0.75rem', display: 'flex', flexDirection: 'column', gap: '0.35rem', flex: 1 }}>
        {navItems.map((item) => {
          if (item.adminOnly && user?.role !== 'ADMIN') return null;

          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              style={({ isActive }) => ({
                display: 'flex',
                alignItems: 'center',
                gap: '0.75rem',
                padding: '0.65rem 0.85rem',
                borderRadius: 'var(--radius-md)',
                fontSize: '0.875rem',
                fontWeight: isActive ? 700 : 500,
                color: isActive ? 'var(--primary)' : 'var(--text-main)',
                backgroundColor: isActive ? 'var(--primary-light)' : 'transparent',
                transition: 'all 0.15s ease',
                textDecoration: 'none'
              })}
            >
              <Icon size={19} />
              <span>{item.label}</span>
              {item.highlight && (
                <span style={{
                  marginLeft: 'auto',
                  fontSize: '0.65rem',
                  fontWeight: 800,
                  backgroundColor: '#fee2e2',
                  color: '#dc2626',
                  padding: '0.1rem 0.4rem',
                  borderRadius: '9999px'
                }}>
                  Cảnh báo
                </span>
              )}
            </NavLink>
          );
        })}
      </nav>

      {/* Footer Info */}
      <div style={{ padding: '1rem 1.25rem', borderTop: '1px solid var(--border)', fontSize: '0.75rem', color: 'var(--text-light)' }}>
        <p>Đồ án Tốt nghiệp</p>
        <p style={{ fontWeight: 600, color: 'var(--text-muted)' }}>Adaptive Learning & ML</p>
      </div>
    </aside>
  );
};

