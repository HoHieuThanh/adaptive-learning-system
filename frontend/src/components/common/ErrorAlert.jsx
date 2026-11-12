import React from 'react';
import { AlertCircle, X } from 'lucide-react';

export const ErrorAlert = ({ message, onClose }) => {
  if (!message) return null;

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0.85rem 1.25rem',
      backgroundColor: 'var(--danger-light)',
      border: '1px solid var(--danger-border)',
      borderRadius: 'var(--radius-md)',
      color: 'var(--danger-text)',
      marginBottom: '1.25rem',
      fontSize: '0.875rem'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <AlertCircle size={18} />
        <span>{message}</span>
      </div>
      {onClose && (
        <button
          onClick={onClose}
          style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'inherit' }}
        >
          <X size={16} />
        </button>
      )}
    </div>
  );
};

