import React from 'react';
import { Inbox } from 'lucide-react';

export const EmptyState = ({ title = 'Không có dữ liệu', message = 'Chưa có bản ghi nào để hiển thị.', actionText, onAction }) => {
  return (
    <div style={{
      padding: '3.5rem 1.5rem',
      textAlign: 'center',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '0.75rem'
    }}>
      <div style={{
        backgroundColor: '#f1f5f9',
        color: '#94a3b8',
        width: '56px',
        height: '56px',
        borderRadius: '50%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}>
        <Inbox size={28} />
      </div>
      <h4 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-main)' }}>{title}</h4>
      <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', maxWidth: '400px' }}>{message}</p>
      {actionText && onAction && (
        <button className="btn btn-primary btn-sm" onClick={onAction} style={{ marginTop: '0.5rem' }}>
          {actionText}
        </button>
      )}
    </div>
  );
};

