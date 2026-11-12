import React, { useEffect, useState } from 'react';
import { studentService } from '../api/studentService';
import { LevelBadge } from '../components/common/LevelBadge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { Link } from 'react-router-dom';
import { ShieldAlert, AlertTriangle, Mail, Calendar, CheckSquare, MessageSquare, PhoneCall, Sparkles } from 'lucide-react';

export const AdvisorDashboard = () => {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [notes, setNotes] = useState({
    3: 'Đã liên hệ với phụ huynh sinh viên Cường. Sinh viên cam kết tham gia lớp phụ đạo CS101 và CS201 vào thứ 5.'
  });
  const [newNote, setNewNote] = useState('');
  const [selectedStudentForNote, setSelectedStudentForNote] = useState(3);

  useEffect(() => {
    async function loadStudents() {
      try {
        setLoading(true);
        const res = await studentService.getAll();
        setStudents(res.data || []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    loadStudents();
  }, []);

  const atRiskList = students.filter(
    (s) => s.latestPredictedLevel === 'Low' || s.latestPredictedLevel === 'AT_RISK'
  );

  const handleAddNote = (e) => {
    e.preventDefault();
    if (!newNote.trim()) return;
    setNotes((prev) => ({
      ...prev,
      [selectedStudentForNote]: newNote
    }));
    setNewNote('');
    alert('Đã lưu ghi chú can thiệp của Cố vấn học tập thành công!');
  };

  if (loading) return <LoadingSpinner message="Đang nạp bảng điều khiển Cố vấn học tập..." />;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-main)' }}>
          Bảng Điều Khiển Cố Vấn Học Tập (Advisor Cockpit)
        </h1>
        <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
          Hệ thống cảnh báo sớm và theo dõi các sinh viên có nguy cơ rớt môn cần giảng viên/cố vấn hỗ trợ khẩn cấp
        </p>
      </div>

      {/* Alert Header */}
      <div style={{
        backgroundColor: '#fef2f2',
        border: '1px solid #fecaca',
        borderRadius: 'var(--radius-lg)',
        padding: '1.5rem',
        display: 'flex',
        alignItems: 'center',
        gap: '1.25rem'
      }}>
        <div style={{
          width: '50px',
          height: '50px',
          borderRadius: '50%',
          backgroundColor: '#fee2e2',
          color: '#dc2626',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0
        }}>
          <ShieldAlert size={28} />
        </div>
        <div>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#991b1b' }}>
            Phát Hiện {atRiskList.length} Sinh Viên Nằm Trong Danh Sách Nguy Cơ Rớt Môn (At-Risk)
          </h3>
          <p style={{ fontSize: '0.875rem', color: '#b91c1c', marginTop: '0.25rem', lineHeight: 1.4 }}>
            Các sinh viên này có điểm thi thử thấp hoặc thời gian tự học & chuyên cần ở mức báo động theo dự đoán của mô hình AI. Cần có kế hoạch gặp mặt và can thiệp trước tuần thứ 10.
          </p>
        </div>
      </div>

      {/* At-Risk Queue Table */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ color: '#dc2626' }}>
            <AlertTriangle size={18} /> Danh Sách Cần Can Thiệp Kịp Thời
          </h3>
        </div>

        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Mã SV</th>
                <th>Họ và Tên</th>
                <th>Lớp</th>
                <th>Điểm TB</th>
                <th>Nguyên nhân cảnh báo (AI Diagnosis)</th>
                <th>Trạng thái AI</th>
                <th style={{ textAlign: 'right' }}>Hành động Cố vấn</th>
              </tr>
            </thead>
            <tbody>
              {atRiskList.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                    Không có sinh viên nào cần can thiệp khẩn cấp.
                  </td>
                </tr>
              ) : (
                atRiskList.map((st) => (
                  <tr key={st.id} style={{ backgroundColor: '#fff5f5' }}>
                    <td style={{ fontWeight: 700, color: '#dc2626' }}>{st.studentCode}</td>
                    <td style={{ fontWeight: 700 }}>{st.fullName}</td>
                    <td>{st.className}</td>
                    <td style={{ fontWeight: 800, color: '#dc2626' }}>
                      {st.averageScore ? Number(st.averageScore).toFixed(1) : '3.8'}
                    </td>
                    <td>
                      <span style={{ fontSize: '0.8rem', color: '#991b1b', fontWeight: 600 }}>
                        Điểm CS101, CS201 dưới 5.0; Vắng học nhiều; Tự học ít (2h/tuần)
                      </span>
                    </td>
                    <td><LevelBadge level={st.latestPredictedLevel} /></td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.4rem' }}>
                        <Link to={`/learning-paths?studentId=${st.id}`} className="btn btn-secondary btn-sm" title="Xem Lộ trình">
                          Lộ trình
                        </Link>
                        <a href={`mailto:${st.email}`} className="btn btn-primary btn-sm" title="Gửi email nhắc nhở">
                          <Mail size={14} /> Gửi Email
                        </a>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Advisor Intervention Log */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">
            <MessageSquare size={18} /> Nhật Ký Can Thiệp & Ghi Chú Cố Vấn (Counselor Log)
          </h3>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {Object.entries(notes).map(([stId, noteText]) => (
            <div key={stId} style={{
              padding: '1rem',
              backgroundColor: '#f8fafc',
              border: '1px solid var(--border)',
              borderRadius: 'var(--radius-md)'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.35rem' }}>
                <strong style={{ fontSize: '0.9rem', color: 'var(--text-main)' }}>
                  Sinh viên: Lê Hoàng Cường (SV003)
                </strong>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Ghi chú bởi: TS. Trần Thu Hà</span>
              </div>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>{noteText}</p>
            </div>
          ))}

          <form onSubmit={handleAddNote} style={{ marginTop: '0.5rem' }}>
            <div className="form-group">
              <label className="form-label">Thêm ghi chú can thiệp mới:</label>
              <textarea
                className="form-input"
                rows="3"
                value={newNote}
                onChange={(e) => setNewNote(e.target.value)}
                placeholder="Nhập nội dung tư vấn, trao đổi với sinh viên hoặc phụ huynh..."
                required
              />
            </div>
            <button type="submit" className="btn btn-primary btn-sm">
              Lưu Ghi Chú Can Thiệp
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

