import React from 'react';

export const LevelDonutChart = ({ atRisk = 1, moderate = 2, high = 2 }) => {
  const total = atRisk + moderate + high || 1;
  const pRisk = (atRisk / total) * 100;
  const pMod = (moderate / total) * 100;
  const pHigh = (high / total) * 100;

  // SVG Donut calculation with stroke-dasharray (circumference = 2 * PI * 40 = 251.32)
  const c = 251.32;
  const dashRisk = (pRisk / 100) * c;
  const dashMod = (pMod / 100) * c;
  const dashHigh = (pHigh / 100) * c;

  const offsetRisk = 0;
  const offsetMod = -dashRisk;
  const offsetHigh = -(dashRisk + dashMod);

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '2rem', flexWrap: 'wrap' }}>
      <div style={{ position: 'relative', width: '150px', height: '150px' }}>
        <svg viewBox="0 0 100 100" width="150" height="150" style={{ transform: 'rotate(-90deg)' }}>
          <circle cx="50" cy="50" r="40" fill="transparent" stroke="#f1f5f9" strokeWidth="16" />
          {/* Low / At-Risk */}
          <circle
            cx="50" cy="50" r="40"
            fill="transparent"
            stroke="#ef4444"
            strokeWidth="16"
            strokeDasharray={`${dashRisk} ${c}`}
            strokeDashoffset={offsetRisk}
          />
          {/* Medium */}
          <circle
            cx="50" cy="50" r="40"
            fill="transparent"
            stroke="#f59e0b"
            strokeWidth="16"
            strokeDasharray={`${dashMod} ${c}`}
            strokeDashoffset={offsetMod}
          />
          {/* High */}
          <circle
            cx="50" cy="50" r="40"
            fill="transparent"
            stroke="#10b981"
            strokeWidth="16"
            strokeDasharray={`${dashHigh} ${c}`}
            strokeDashoffset={offsetHigh}
          />
        </svg>
        <div style={{
          position: 'absolute',
          top: 0, left: 0, right: 0, bottom: 0,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          pointerEvents: 'none'
        }}>
          <span style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--text-main)', lineHeight: 1 }}>{total}</span>
          <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 600 }}>Sinh viên</span>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', flex: 1, minWidth: '160px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.85rem' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ width: '10px', height: '10px', borderRadius: '50%', background: '#ef4444' }}></span>
            Cần hỗ trợ (Low):
          </span>
          <strong>{atRisk} ({Math.round(pRisk)}%)</strong>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.85rem' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ width: '10px', height: '10px', borderRadius: '50%', background: '#f59e0b' }}></span>
            Trung bình (Medium):
          </span>
          <strong>{moderate} ({Math.round(pMod)}%)</strong>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.85rem' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ width: '10px', height: '10px', borderRadius: '50%', background: '#10b981' }}></span>
            Xuất sắc (High):
          </span>
          <strong>{high} ({Math.round(pHigh)}%)</strong>
        </div>
      </div>
    </div>
  );
};

