import React from 'react';

export const PerformanceBarChart = ({ grades = [] }) => {
  if (!grades || grades.length === 0) {
    return <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Chưa có dữ liệu điểm học phần.</p>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', width: '100%' }}>
      {grades.map((grade) => {
        const score = Number(grade.score);
        const percentage = Math.min(100, Math.max(0, (score / 10) * 100));

        let color = '#10b981'; // Green for >= 8.0
        if (score < 5.0) {
          color = '#ef4444'; // Red for < 5.0
        } else if (score < 7.0) {
          color = '#f59e0b'; // Amber for < 7.0
        } else if (score < 8.0) {
          color = '#0ea5e9'; // Blue for 7.0 - 8.0
        }

        return (
          <div key={grade.id || grade.subjectCode} style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
              <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                {grade.subjectCode} - {grade.subjectName}
              </span>
              <span style={{ fontWeight: 700, color: color }}>
                {score.toFixed(1)} / 10.0
              </span>
            </div>
            <div style={{
              width: '100%',
              height: '10px',
              backgroundColor: '#f1f5f9',
              borderRadius: '9999px',
              overflow: 'hidden'
            }}>
              <div style={{
                width: `${percentage}%`,
                height: '100%',
                backgroundColor: color,
                borderRadius: '9999px',
                transition: 'width 0.4s ease'
              }} />
            </div>
          </div>
        );
      })}
    </div>
  );
};

