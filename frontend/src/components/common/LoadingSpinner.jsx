import React from 'react';
import { Loader2 } from 'lucide-react';

export const LoadingSpinner = ({ message = 'Đang tải dữ liệu...' }) => {
  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '3rem',
      gap: '0.75rem',
      color: 'var(--text-muted)'
    }}>
      <Loader2 size={32} style={{ animation: 'spin 1s linear infinite', color: 'var(--primary)' }} />
      <p style={{ fontSize: '0.9rem', fontWeight: 500 }}>{message}</p>
      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

