import React from 'react';
import { Clock, CheckSquare, CalendarCheck, Laptop } from 'lucide-react';

export const HabitRadarBar = ({
  studyHours = 6.5,
  attendanceRate = 90.0,
  assignmentCompletion = 85.0,
  lmsLogins = 28
}) => {
  const habits = [
    {
      label: 'Chuyên cần học tập',
      value: `${attendanceRate}%`,
      percentage: attendanceRate,
      icon: CalendarCheck,
      color: attendanceRate >= 80 ? '#10b981' : '#ef4444'
    },
    {
      label: 'Hoàn thành bài tập',
      value: `${assignmentCompletion}%`,
      percentage: assignmentCompletion,
      icon: CheckSquare,
      color: assignmentCompletion >= 75 ? '#10b981' : '#f59e0b'
    },
    {
      label: 'Tự học hàng tuần',
      value: `${studyHours} giờ/tuần`,
      percentage: Math.min(100, (studyHours / 15) * 100),
      icon: Clock,
      color: studyHours >= 8 ? '#10b981' : (studyHours >= 4 ? '#0ea5e9' : '#ef4444')
    },
    {
      label: 'Tương tác LMS',
      value: `${lmsLogins} lượt`,
      percentage: Math.min(100, (lmsLogins / 50) * 100),
      icon: Laptop,
      color: '#6366f1'
    }
  ];

  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
      {habits.map((item, idx) => {
        const Icon = item.icon;
        return (
          <div key={idx} style={{
            background: '#ffffff',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius-md)',
            padding: '1rem',
            display: 'flex',
            flexDirection: 'column',
            gap: '0.5rem'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 600 }}>
                <Icon size={16} />
                <span>{item.label}</span>
              </div>
              <span style={{ fontSize: '0.9rem', fontWeight: 700, color: item.color }}>{item.value}</span>
            </div>
            <div style={{
              width: '100%',
              height: '6px',
              backgroundColor: '#f1f5f9',
              borderRadius: '9999px',
              overflow: 'hidden'
            }}>
              <div style={{
                width: `${item.percentage}%`,
                height: '100%',
                backgroundColor: item.color,
                borderRadius: '9999px'
              }} />
            </div>
          </div>
        );
      })}
    </div>
  );
};

