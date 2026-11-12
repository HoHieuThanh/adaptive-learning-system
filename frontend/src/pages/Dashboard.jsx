import React, { useEffect, useState } from 'react';
import { dashboardService } from '../api/dashboardService';
import { studentService } from '../api/studentService';
import { MetricCard } from '../components/common/MetricCard';
import { LevelBadge } from '../components/common/LevelBadge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { LevelDonutChart } from '../components/charts/LevelDonutChart';
import { Link } from 'react-router-dom';
import { Users, AlertTriangle, Award, Brain, ArrowRight, ShieldAlert, Sparkles } from 'lucide-react';

export const Dashboard = () => {
  const [stats, setStats] = useState(null);
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isMock, setIsMock] = useState(false);

  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        const [statsRes, studentsRes] = await Promise.all([
          dashboardService.getStats(),
          studentService.getAll()
        ]);
        setStats(statsRes.data);
        setStudents(studentsRes.data || []);
        setIsMock(statsRes.isMock || studentsRes.isMock);
      } catch (err) {
        console.error('Lỗi nạp dashboard:', err);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  if (loading) return <LoadingSpinner message="Đang tải dữ liệu tổng quan..." />;

  const atRiskStudents = students.filter(
    (s) => s.latestPredictedLevel === 'Low' || s.latestPredictedLevel === 'AT_RISK'
  );

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Header Banner */}
      <div style={{
        background: 'linear-gradient(135deg, #1e1b4b 0%, #312e81 100%)',
        color: '#ffffff',
        borderRadius: 'var(--radius-lg)',
        padding: '2rem',
        boxShadow: 'var(--shadow-md)',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '1.5rem'
      }}>
        <div style={{ maxWidth: '650px' }}>
          <span style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.4rem',
            padding: '0.25rem 0.65rem',
            borderRadius: 'var(--radius-full)',
            backgroundColor: 'rgba(255, 255, 255, 0.15)',
            fontSize: '0.75rem',
            fontWeight: 700,
            marginBottom: '0.75rem'
          }}>
            <Sparkles size={14} /> AI-Powered Adaptive Learning Platform
          </span>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, lineHeight: 1.2 }}>
            Bảng Điều Khiển Học Tập Thích Ứng & Phân Loại Trình Độ
          </h1>
          <p style={{ color: '#c7d2fe', fontSize: '0.9rem', marginTop: '0.5rem', lineHeight: 1.5 }}>
            Dự đoán nguy cơ học tập sớm bằng Machine Learning (XGBoost, Decision Tree & K-Means Clustering) và tự động kiến tạo lộ trình học tập tối ưu cho từng sinh viên.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <Link to="/predictions" className="btn" style={{ backgroundColor: '#ffffff', color: '#1e1b4b' }}>
            <Brain size={16} /> Chạy Dự đoán AI
          </Link>
          <Link to="/advisor" className="btn" style={{ backgroundColor: '#ef4444', color: '#ffffff' }}>
            <ShieldAlert size={16} /> Xem Danh sách Nguy cơ ({atRiskStudents.length})
          </Link>
        </div>
      </div>

      {isMock && (
        <div style={{
          padding: '0.65rem 1rem',
          backgroundColor: '#fffbeb',
          border: '1px solid #fde68a',
          borderRadius: 'var(--radius-md)',
          fontSize: '0.8rem',
          color: '#92400e',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem'
        }}>
          <strong>[CHẾ ĐỘ DEMO]:</strong> Hiện tại hệ thống đang hiển thị dữ liệu mẫu do Backend chưa kết nối hoặc chưa bật. Dữ liệu thực sẽ tự động cập nhật khi bật Spring Boot và PostgreSQL.
        </div>
      )}

      {/* KPI Cards */}
      <div className="grid-4">
        <MetricCard
          title="Tổng số sinh viên"
          value={stats?.totalStudents || students.length}
          subtitle="Đang theo dõi học kỳ này"
          icon={Users}
          color="primary"
        />
        <MetricCard
          title="Cần hỗ trợ (At-Risk)"
          value={stats?.atRiskCount || atRiskStudents.length}
          subtitle="Điểm dự đoán dưới 10 (G3 < 10)"
          icon={AlertTriangle}
          color="danger"
        />
        <MetricCard
          title="Học lực Khá - Giỏi"
          value={(stats?.moderateCount || 0) + (stats?.highCount || 0)}
          subtitle="Đạt chuẩn và xuất sắc"
          icon={Award}
          color="success"
        />
        <MetricCard
          title="Lượt dự đoán AI"
          value={stats?.totalPredictions || 0}
          subtitle="Audit trail ghi nhận trong DB"
          icon={Brain}
          color="primary"
        />
      </div>

      {/* Charts & At-Risk List */}
      <div className="grid-2">
        {/* Phân bố trình độ (Donut Chart) */}
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Phân Bố Trình Độ Sinh Viên (AI Classification)</h3>
          </div>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
            Phân loại dựa trên mô hình XGBoost huấn luyện trên tập UCI Student Performance Dataset.
          </p>
          <LevelDonutChart
            atRisk={stats?.atRiskCount || 1}
            moderate={stats?.moderateCount || 2}
            high={stats?.highCount || 2}
          />
        </div>

        {/* Danh sách cần can thiệp sớm */}
        <div className="card">
          <div className="card-header">
            <h3 className="card-title" style={{ color: 'var(--danger-text)' }}>
              <AlertTriangle size={18} /> Cảnh Báo Sinh Viên Nguy Cơ Rớt Môn
            </h3>
            <Link to="/advisor" className="btn btn-secondary btn-sm">
              Xem tất cả <ArrowRight size={14} />
            </Link>
          </div>

          {atRiskStudents.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
              Hiện tại không có sinh viên nào nằm trong nhóm nguy cơ trượt môn.
            </p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {atRiskStudents.map((st) => (
                <div key={st.id} style={{
                  padding: '0.85rem 1rem',
                  border: '1px solid var(--danger-border)',
                  backgroundColor: 'var(--danger-light)',
                  borderRadius: 'var(--radius-md)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between'
                }}>
                  <div>
                    <h4 style={{ fontSize: '0.9rem', fontWeight: 700, color: 'var(--danger-text)' }}>
                      {st.fullName} ({st.studentCode})
                    </h4>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      Lớp: {st.className} | Điểm TB: {st.averageScore || 'Chưa có'}
                    </span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <LevelBadge level={st.latestPredictedLevel} />
                    <Link to={`/students/${st.id}`} className="btn btn-secondary btn-sm">
                      Hồ sơ
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

