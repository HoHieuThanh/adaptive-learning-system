import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { studentService } from '../api/studentService';
import { predictionService } from '../api/predictionService';
import { LevelBadge } from '../components/common/LevelBadge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { Brain, Sparkles, CheckCircle2, History, Info, Play, Layers } from 'lucide-react';

export const AIPrediction = () => {
  const [searchParams] = useSearchParams();
  const preselectedStudentId = searchParams.get('studentId') || '';

  const [students, setStudents] = useState([]);
  const [selectedStudentId, setSelectedStudentId] = useState(preselectedStudentId);
  const [selectedModel, setSelectedModel] = useState('xgboost');
  const [predicting, setPredicting] = useState(false);
  const [predictionResult, setPredictionResult] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        const [stRes, histRes] = await Promise.all([
          studentService.getAll(),
          predictionService.getAll()
        ]);
        setStudents(stRes.data || []);
        setHistory(histRes.data || []);

        if (!preselectedStudentId && stRes.data?.length > 0) {
          setSelectedStudentId(String(stRes.data[0].id));
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, [preselectedStudentId]);

  const handleRunPrediction = async () => {
    if (!selectedStudentId) return;
    try {
      setPredicting(true);
      const res = await predictionService.predict(selectedStudentId, selectedModel);
      setPredictionResult(res.data);

      // Refresh audit history
      const histRes = await predictionService.getAll();
      setHistory(histRes.data || []);
    } catch (err) {
      alert('Lỗi dự đoán: ' + err.message);
    } finally {
      setPredicting(false);
    }
  };

  if (loading) return <LoadingSpinner message="Đang nạp mô hình AI và dữ liệu..." />;

  const currentStudent = students.find((s) => s.id === Number(selectedStudentId));

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-main)' }}>
          Phân Tích & Dự Đoán Trình Độ Học Tập (AI Studio)
        </h1>
        <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
          Thực hiện suy luận trực tiếp từ FastAPI Service bằng 3 mô hình học máy: XGBoost, Decision Tree và K-Means Hybrid
        </p>
      </div>

      {/* Control Card */}
      <div className="card" style={{
        background: 'linear-gradient(135deg, #ffffff 0%, #f0fdf4 100%)',
        border: '1px solid #bbf7d0'
      }}>
        <h3 className="card-title" style={{ marginBottom: '1.25rem', color: '#166534' }}>
          <Brain size={20} /> Cấu Hình Suy Luận Mô Hình AI
        </h3>

        <div className="grid-3" style={{ alignItems: 'flex-end' }}>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Chọn sinh viên cần phân tích:</label>
            <select
              className="form-select"
              value={selectedStudentId}
              onChange={(e) => setSelectedStudentId(e.target.value)}
            >
              {students.map((st) => (
                <option key={st.id} value={st.id}>
                  {st.studentCode} - {st.fullName} ({st.className})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Thuật toán học máy (Model):</label>
            <select
              className="form-select"
              value={selectedModel}
              onChange={(e) => setSelectedModel(e.target.value)}
            >
              <option value="xgboost">XGBoost Classifier (SOTA - Khuyến nghị)</option>
              <option value="decision_tree">Decision Tree Classifier (Baseline)</option>
              <option value="kmeans_hybrid">K-Means + Classifier (Feature Augmentation)</option>
            </select>
          </div>

          <div>
            <button
              className="btn btn-primary"
              onClick={handleRunPrediction}
              disabled={predicting}
              style={{ width: '100%', padding: '0.65rem 1rem' }}
            >
              {predicting ? (
                <>Đang chạy suy luận qua FastAPI...</>
              ) : (
                <><Play size={16} /> Bắt đầu Dự đoán Ngay</>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Real-time Prediction Output */}
      {predictionResult && (
        <div className="card" style={{
          border: '2px solid var(--primary)',
          backgroundColor: '#ffffff',
          boxShadow: 'var(--shadow-md)'
        }}>
          <div className="card-header">
            <h3 className="card-title" style={{ color: 'var(--primary)' }}>
              <CheckCircle2 size={20} /> Kết Quả Suy Luận Thời Gian Thực Từ AI
            </h3>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Thời gian: {new Date(predictionResult.createdAt).toLocaleString('vi-VN')}
            </span>
          </div>

          <div className="grid-3" style={{ marginTop: '0.5rem' }}>
            <div style={{ padding: '1rem', backgroundColor: '#f8fafc', borderRadius: 'var(--radius-md)' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>SINH VIÊN ĐƯỢC ĐÁNH GIÁ</span>
              <h4 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-main)', marginTop: '0.25rem' }}>
                {predictionResult.studentName || currentStudent?.fullName}
              </h4>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                Mã SV: {predictionResult.studentCode || currentStudent?.studentCode}
              </span>
            </div>

            <div style={{ padding: '1rem', backgroundColor: '#f8fafc', borderRadius: 'var(--radius-md)' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>TRÌNH ĐỘ DỰ ĐOÁN</span>
              <div style={{ marginTop: '0.35rem' }}>
                <LevelBadge level={predictionResult.predictedLevel} />
              </div>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
                Mô hình sử dụng: <strong>{predictionResult.modelName}</strong>
              </p>
            </div>

            <div style={{ padding: '1rem', backgroundColor: '#f8fafc', borderRadius: 'var(--radius-md)' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>ĐỘ TIN CẬY (CONFIDENCE SCORE)</span>
              <h3 style={{ fontSize: '1.75rem', fontWeight: 800, color: '#16a34a', marginTop: '0.2rem' }}>
                {(Number(predictionResult.confidence) * 100).toFixed(1)}%
              </h3>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                Đạt độ tin cậy cao theo xác suất Softmax
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Prediction History / Audit Trail */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">
            <History size={18} /> Lịch Sử Dự Đoán & Audit Trail (Ghi Nhận Trong DB)
          </h3>
        </div>

        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Thời gian</th>
                <th>Mã SV</th>
                <th>Họ và Tên</th>
                <th>Mô hình AI</th>
                <th>Trình độ dự đoán</th>
                <th>Độ tin cậy</th>
              </tr>
            </thead>
            <tbody>
              {history.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                    Chưa có lịch sử dự đoán nào.
                  </td>
                </tr>
              ) : (
                history.map((h) => (
                  <tr key={h.id}>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {new Date(h.createdAt).toLocaleString('vi-VN')}
                    </td>
                    <td style={{ fontWeight: 700, color: 'var(--primary)' }}>{h.studentCode}</td>
                    <td style={{ fontWeight: 600 }}>{h.studentName}</td>
                    <td><span className="badge badge-info">{h.modelName}</span></td>
                    <td><LevelBadge level={h.predictedLevel} /></td>
                    <td style={{ fontWeight: 700 }}>{(Number(h.confidence) * 100).toFixed(1)}%</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

