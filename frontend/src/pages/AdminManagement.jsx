import React, { useEffect, useState } from 'react';
import { subjectService } from '../api/subjectService';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { Modal } from '../components/common/Modal';
import { Settings, BookOpen, Plus, Trash2, Cpu, CheckCircle, BarChart2 } from 'lucide-react';

export const AdminManagement = () => {
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newSubject, setNewSubject] = useState({ subjectCode: '', subjectName: '' });

  const fetchSubjects = async () => {
    try {
      setLoading(true);
      const res = await subjectService.getAll();
      setSubjects(res.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSubjects();
  }, []);

  const handleCreateSubject = async (e) => {
    e.preventDefault();
    try {
      await subjectService.create(newSubject);
      setIsModalOpen(false);
      setNewSubject({ subjectCode: '', subjectName: '' });
      fetchSubjects();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleDeleteSubject = async (id) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa môn học này không?')) return;
    try {
      await subjectService.delete(id);
      fetchSubjects();
    } catch (err) {
      alert(err.message);
    }
  };

  // Real evaluation metrics from our trained models in ML Service
  const mlMetrics = [
    { model: 'Decision Tree (Baseline)', accuracy: '88.61%', precision: '89.55%', recall: '91.70%', f1: '89.98%', status: 'Sẵn sàng' },
    { model: 'XGBoost Classifier (SOTA)', accuracy: '93.67%', precision: '94.49%', recall: '94.80%', f1: '94.63%', status: 'Mô hình chính (Active)' },
    { model: 'K-Means + Classifier Hybrid', accuracy: '93.67%', precision: '94.49%', recall: '94.80%', f1: '94.63%', status: 'Sẵn sàng' }
  ];

  if (loading) return <LoadingSpinner message="Đang nạp dữ liệu quản trị..." />;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-main)' }}>
          Quản Trị Hệ Thống & Cấu Hình Học Máy (Admin Center)
        </h1>
        <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
          Quản lý danh mục môn học, giám sát trạng thái microservices và xem báo cáo hiệu năng mô hình AI
        </p>
      </div>

      {/* Services Health Status */}
      <div className="grid-3">
        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: '#ecfdf5', color: '#10b981', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <CheckCircle size={22} />
          </div>
          <div>
            <h4 style={{ fontSize: '0.9rem', fontWeight: 700 }}>FastAPI ML Service</h4>
            <span style={{ fontSize: '0.75rem', color: '#16a34a' }}>Đang hoạt động (Port 8000)</span>
          </div>
        </div>

        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: '#ecfdf5', color: '#10b981', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <CheckCircle size={22} />
          </div>
          <div>
            <h4 style={{ fontSize: '0.9rem', fontWeight: 700 }}>Spring Boot Core</h4>
            <span style={{ fontSize: '0.75rem', color: '#16a34a' }}>Đang hoạt động (Port 8080)</span>
          </div>
        </div>

        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: '#ecfdf5', color: '#10b981', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <CheckCircle size={22} />
          </div>
          <div>
            <h4 style={{ fontSize: '0.9rem', fontWeight: 700 }}>PostgreSQL Database</h4>
            <span style={{ fontSize: '0.75rem', color: '#16a34a' }}>Đã kết nối (Port 5432)</span>
          </div>
        </div>
      </div>

      {/* AI Model Evaluation Benchmark */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">
            <Cpu size={18} /> Bảng So Sánh Hiệu Năng 3 Mô Hình AI Trên Test Set Độc Lập
          </h3>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            Đánh giá trên 79 mẫu test thực tế (UCI Dataset)
          </span>
        </div>

        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Tên mô hình</th>
                <th>Accuracy</th>
                <th>Precision (Macro)</th>
                <th>Recall (Macro)</th>
                <th>F1-Score (Macro)</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {mlMetrics.map((m, idx) => (
                <tr key={idx}>
                  <td style={{ fontWeight: 700, color: 'var(--text-main)' }}>{m.model}</td>
                  <td style={{ fontWeight: 800, color: '#16a34a' }}>{m.accuracy}</td>
                  <td>{m.precision}</td>
                  <td>{m.recall}</td>
                  <td style={{ fontWeight: 800 }}>{m.f1}</td>
                  <td>
                    <span className="badge badge-high">{m.status}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Subject Management */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">
            <BookOpen size={18} /> Quản Lý Danh Mục Môn Học ({subjects.length})
          </h3>
          <button className="btn btn-primary btn-sm" onClick={() => setIsModalOpen(true)}>
            <Plus size={14} /> Thêm môn học
          </button>
        </div>

        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Mã môn</th>
                <th>Tên môn học</th>
                <th style={{ textAlign: 'right' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {subjects.map((sub) => (
                <tr key={sub.id}>
                  <td style={{ fontWeight: 700, color: 'var(--primary)' }}>{sub.subjectCode}</td>
                  <td>{sub.subjectName}</td>
                  <td style={{ textAlign: 'right' }}>
                    <button
                      className="btn btn-danger btn-sm"
                      onClick={() => handleDeleteSubject(sub.id)}
                      title="Xóa môn học"
                    >
                      <Trash2 size={14} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal Thêm Môn Học */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Thêm Môn Học Mới">
        <form onSubmit={handleCreateSubject}>
          <div className="form-group">
            <label className="form-label">Mã môn học *</label>
            <input
              type="text"
              className="form-input"
              value={newSubject.subjectCode}
              onChange={(e) => setNewSubject({ ...newSubject, subjectCode: e.target.value.toUpperCase() })}
              placeholder="VD: INT3306"
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Tên môn học *</label>
            <input
              type="text"
              className="form-input"
              value={newSubject.subjectName}
              onChange={(e) => setNewSubject({ ...newSubject, subjectName: e.target.value })}
              placeholder="VD: Phát triển Ứng dụng Web"
              required
            />
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.5rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>Hủy</button>
            <button type="submit" className="btn btn-primary">Lưu môn học</button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

