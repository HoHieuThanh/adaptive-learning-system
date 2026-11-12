import React from 'react';
import { AlertTriangle, CheckCircle2, Clock } from 'lucide-react';

export const LevelBadge = ({ level }) => {
  if (!level) return <span className="badge badge-info">Chưa dự đoán</span>;

  const normalized = level.toLowerCase();

  if (normalized === 'low' || normalized === 'at_risk') {
    return (
      <span className="badge badge-low">
        <AlertTriangle size={12} /> Cần can thiệp (Low)
      </span>
    );
  } else if (normalized === 'medium' || normalized === 'moderate') {
    return (
      <span className="badge badge-medium">
        <Clock size={12} /> Trung bình (Medium)
      </span>
    );
  } else if (normalized === 'high') {
    return (
      <span className="badge badge-high">
        <CheckCircle2 size={12} /> Xuất sắc (High)
      </span>
    );
  }

  return <span className="badge badge-info">{level}</span>;
};

