import React from 'react';

export const MetricCard = ({ title, value, subtitle, icon: Icon, color = 'primary' }) => {
  const colorMap = {
    primary: { bg: '#eef2ff', text: '#4f46e5' },
    danger: { bg: '#fef2f2', text: '#ef4444' },
    warning: { bg: '#fffbeb', text: '#f59e0b' },
    success: { bg: '#ecfdf5', text: '#10b981' }
  };

  const scheme = colorMap[color] || colorMap.primary;

  return (
    <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
      {Icon && (
        <div style={{
          backgroundColor: scheme.bg,
          color: scheme.text,
          width: '54px',
          height: '54px',
          borderRadius: '12px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0
        }}>
          <Icon size={26} />
        </div>
      )}
      <div style={{ flex: 1, minWidth: 0 }}>
        <p style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.2rem' }}>
          {title}
        </p>
        <h3 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-main)', lineHeight: 1.1 }}>
          {value}
        </h3>
        {subtitle && (
          <p style={{ fontSize: '0.75rem', color: 'var(--text-light)', marginTop: '0.35rem' }}>
            {subtitle}
          </p>
        )}
      </div>
    </div>
  );
};

