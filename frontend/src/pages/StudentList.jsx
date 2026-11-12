import React, { useEffect, useState } from 'react';
import { studentService } from '../api/studentService';
import { LevelBadge } from '../components/common/LevelBadge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { Modal } from '../components/common/Modal';
import { Link } from 'react-router-dom';
import { Search, Plus, Filter, Eye, Sparkles } from 'lucide-react';

export const StudentList = () => {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [levelFilter, setLevelFilter] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newStudent, setNewStudent] = useState({
    studentCode: '',
    fullName: '',
    email: '',
    className: 'CNTT-K65'
  });
  const [formError, setFormError] = useState('');

  const fetchStudents = async () => {
    try {
      setLoading(true);
      const res = await studentService.getAll();
      setStudents(res.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStudents();
  }, []);

  const handleCreateStudent = async (e) => {
    e.preventDefault();
    setFormError('');
    try {
      await studentService.create(newStudent);
      setIsModalOpen(false);
      setNewStudent({ studentCode: '', fullName: '', email: '', className: 'CNTT-K65' });
      fetchStudents();
    } catch (err) {
      setFormError(err.message);
    }
  };

  const filteredStudents = students.filter((s) => {
    const matchSearch =
      s.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.studentCode.toLowerCase().includes(searchTerm.toLowerCase());

    if (levelFilter === 'ALL') return matchSearch;
    return matchSearch && s.latestPredictedLevel?.toUpperCase() === levelFilter;
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* Page Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-main)' }}>
            Danh Sách Sinh Viên
          </h1>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
            Quản lý hồ sơ học tập và trạng thái phân loại trình độ của sinh viên
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          <Plus size={16} /> Thêm Sinh Viên Mới
        </button>
      </div>

      {/* Controls: Search and Filter */}
      <div className="card" style={{ padding: '1rem 1.25rem', display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flex: 1, minWidth: '240px' }}>
          <Search size={18} color="var(--text-muted)" />
          <input
            type="text"
            className="form-input"
            placeholder="Tìm theo tên hoặc mã sinh viên (VD: An, SV001)..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Filter size={18} color="var(--text-muted)" />
          <select
            className="form-select"
            value={levelFilter}
            onChange={(e) => setLevelFilter(e.target.value)}
            style={{ minWidth: '180px' }}
          >
            <option value="ALL">Tất cả trình độ</option>
            <option value="LOW">Cần can thiệp (Low / At-Risk)</option>
            <option value="MEDIUM">Trung bình (Medium)</option>
            <option value="HIGH">Xuất sắc (High)</option>
          </select>
        </div>
      </div>

      {/* Students Data Table */}
      {loading ? (
        <LoadingSpinner message="Đang tải danh sách sinh viên..." />
      ) : (
        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Mã SV</th>
                <th>Họ và Tên</th>
                <th>Email</th>
                <th>Lớp</th>
                <th>Điểm TB</th>
                <th>Trình độ (AI Predicted)</th>
                <th>Độ tin cậy</th>
                <th style={{ textAlign: 'right' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {filteredStudents.length === 0 ? (
                <tr>
                  <td colSpan={8} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                    Không tìm thấy sinh viên nào phù hợp với bộ lọc.
                  </td>
                </tr>
              ) : (
                filteredStudents.map((st) => (
                  <tr key={st.id}>
                    <td style={{ fontWeight: 700, color: 'var(--primary)' }}>{st.studentCode}</td>
                    <td style={{ fontWeight: 600 }}>{st.fullName}</td>
                    <td style={{ color: 'var(--text-muted)' }}>{st.email}</td>
                    <td>{st.className}</td>
                    <td style={{ fontWeight: 700 }}>{st.averageScore ? Number(st.averageScore).toFixed(1) : '—'}</td>
                    <td><LevelBadge level={st.latestPredictedLevel} /></td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {st.latestConfidence ? `${(st.latestConfidence * 100).toFixed(1)}%` : '—'}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.4rem' }}>
                        <Link to={`/students/${st.id}`} className="btn btn-secondary btn-sm" title="Xem chi tiết">
                          <Eye size={14} /> Chi tiết
                        </Link>
                        <Link to={`/predictions?studentId=${st.id}`} className="btn btn-primary btn-sm" title="Chạy dự đoán">
                          <Sparkles size={14} /> Dự đoán
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal Thêm Sinh Viên */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Thêm Sinh Viên Mới">
        {formError && (
          <div style={{ padding: '0.6rem 0.8rem', backgroundColor: '#fef2f2', border: '1px solid #fecaca', borderRadius: '6px', color: '#dc2626', fontSize: '0.85rem', marginBottom: '1rem' }}>
            {formError}
          </div>
        )}
        <form onSubmit={handleCreateStudent}>
          <div className="form-group">
            <label className="form-label">Mã sinh viên *</label>
            <input
              type="text"
              className="form-input"
              value={newStudent.studentCode}
              onChange={(e) => setNewStudent({ ...newStudent, studentCode: e.target.value })}
              placeholder="VD: SV006"
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Họ và tên *</label>
            <input
              type="text"
              className="form-input"
              value={newStudent.fullName}
              onChange={(e) => setNewStudent({ ...newStudent, fullName: e.target.value })}
              placeholder="VD: Hoàng Văn Nam"
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Email trường *</label>
            <input
              type="email"
              className="form-input"
              value={newStudent.email}
              onChange={(e) => setNewStudent({ ...newStudent, email: e.target.value })}
              placeholder="VD: nam.hv@university.edu.vn"
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Lớp sinh hoạt *</label>
            <input
              type="text"
              className="form-input"
              value={newStudent.className}
              onChange={(e) => setNewStudent({ ...newStudent, className: e.target.value })}
              placeholder="VD: CNTT-K65"
              required
            />
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.5rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>Hủy</button>
            <button type="submit" className="btn btn-primary">Lưu Sinh Viên</button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

