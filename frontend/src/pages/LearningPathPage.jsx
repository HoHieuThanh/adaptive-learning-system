import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { studentService } from '../api/studentService';
import { learningPathService } from '../api/learningPathService';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import {
  Compass,
  CheckCircle2,
  Clock,
  AlertTriangle,
  ArrowRight,
  BookOpen,
  Calendar,
  Target,
  RefreshCw,
  Lightbulb,
  Award,
  Layers,
  Sparkles,
  Check,
  PlayCircle,
  HelpCircle,
  Filter
} from 'lucide-react';

export const LearningPathPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const paramStudentId = searchParams.get('studentId') || '';

  const [students, setStudents] = useState([]);
  const [selectedStudentId, setSelectedStudentId] = useState(paramStudentId);
  const [pathway, setPathway] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [filterPriority, setFilterPriority] = useState('ALL');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [actionMessage, setActionMessage] = useState(null);

  // Nạp danh sách sinh viên
  useEffect(() => {
    async function loadStudents() {
      try {
        const res = await studentService.getAll();
        const list = res.data || [];
        setStudents(list);
        if (!paramStudentId && list.length > 0) {
          // Mặc định chọn SV003 (At-risk) hoặc sinh viên đầu tiên
          const defaultStudent = list.find((s) => s.studentCode === 'SV003') || list[0];
          setSelectedStudentId(String(defaultStudent.id));
        }
      } catch (err) {
        console.error('Lỗi nạp danh sách sinh viên:', err);
      }
    }
    loadStudents();
  }, [paramStudentId]);

  // Nạp lộ trình học tập khi đổi sinh viên
  useEffect(() => {
    async function loadPathway() {
      if (!selectedStudentId) return;
      try {
        setLoading(true);
        const res = await learningPathService.getLatestByStudent(selectedStudentId);
        setPathway(res.data);
      } catch (err) {
        console.error('Lỗi nạp lộ trình:', err);
      } finally {
        setLoading(false);
      }
    }
    loadPathway();
  }, [selectedStudentId]);

  // Xử lý đổi sinh viên
  const handleStudentChange = (id) => {
    setSelectedStudentId(id);
    setSearchParams({ studentId: id });
  };

  // Làm mới / Sinh lại lộ trình từ Backend
  const handleRefreshPathway = async () => {
    if (!selectedStudentId) return;
    try {
      setRefreshing(true);
      const res = await learningPathService.generateOrRefresh(selectedStudentId);
      setPathway(res.data);
      showNotification('Đã cập nhật và làm mới lộ trình thích ứng thành công!');
    } catch (err) {
      console.error('Lỗi làm mới lộ trình:', err);
    } finally {
      setRefreshing(false);
    }
  };

  // Cập nhật trạng thái của đề xuất (NOT_STARTED -> IN_PROGRESS -> COMPLETED)
  const handleUpdateStatus = async (recId, currentStatus) => {
    let nextStatus = 'IN_PROGRESS';
    if (currentStatus === 'NOT_STARTED' || currentStatus === 'PENDING') {
      nextStatus = 'IN_PROGRESS';
    } else if (currentStatus === 'IN_PROGRESS') {
      nextStatus = 'COMPLETED';
    } else {
      nextStatus = 'NOT_STARTED';
    }

    try {
      const res = await learningPathService.updateRecommendationStatus(recId, nextStatus);
      if (res.success) {
        // Cập nhật state cục bộ ngay lập tức
        setPathway((prev) => {
          if (!prev) return prev;
          const updatedRecs = (prev.recommendations || []).map((r) =>
            r.id === recId ? { ...r, status: nextStatus } : r
          );
          const completedCount = updatedRecs.filter((r) => r.status === 'COMPLETED').length;
          const totalCount = updatedRecs.length;
          const newProgress = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) : 0;

          return {
            ...prev,
            recommendations: updatedRecs,
            completedRecommendations: completedCount,
            progressPercentage: newProgress,
            status: newProgress === 100 ? 'COMPLETED' : 'IN_PROGRESS'
          };
        });

        const statusLabel =
          nextStatus === 'COMPLETED' ? 'Hoàn thành' : (nextStatus === 'IN_PROGRESS' ? 'Đang học' : 'Chưa bắt đầu');
        showNotification(`Đã chuyển trạng thái sang: "${statusLabel}"`);
      }
    } catch (err) {
      console.error('Lỗi cập nhật trạng thái:', err);
    }
  };

  const showNotification = (msg) => {
    setActionMessage(msg);
    setTimeout(() => setActionMessage(null), 3000);
  };

  const currentStudent = students.find((s) => s.id === Number(selectedStudentId));

  // Lọc đề xuất
  const filteredRecommendations = (pathway?.recommendations || []).filter((rec) => {
    const matchesPriority = filterPriority === 'ALL' || rec.priority === filterPriority;
    const matchesStatus =
      filterStatus === 'ALL' ||
      rec.status === filterStatus ||
      (filterStatus === 'NOT_STARTED' && rec.status === 'PENDING');
    return matchesPriority && matchesStatus;
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.75rem' }}>
      {/* Header & Mô tả */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.25rem' }}>
            <Compass size={28} color="var(--primary)" />
            <h1 style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-main)', margin: 0 }}>
              Gợi Ý Lộ Trình Học Tập Thích Ứng (Adaptive Learning Path)
            </h1>
          </div>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', margin: 0 }}>
            Hệ thống Rule-based Engine tự động phân tích điểm số từng môn và trình độ AI để thiết kế lộ trình cải thiện cá nhân hóa.
          </p>
        </div>

        {/* Nút hành động */}
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button
            className="btn btn-primary"
            onClick={handleRefreshPathway}
            disabled={refreshing || !selectedStudentId}
            style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
          >
            <RefreshCw size={16} className={refreshing ? 'animate-spin' : ''} />
            {refreshing ? 'Đang tái tạo...' : 'Làm Mới Lộ Trình'}
          </button>
        </div>
      </div>

      {/* Thông báo thao tác */}
      {actionMessage && (
        <div
          style={{
            backgroundColor: '#ecfdf5',
            border: '1px solid #6ee7b7',
            color: '#065f46',
            padding: '0.75rem 1.25rem',
            borderRadius: 'var(--radius-md)',
            fontSize: '0.875rem',
            fontWeight: 600,
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          <Check size={18} />
          {actionMessage}
        </div>
      )}

      {/* Bộ Chọn Sinh Viên Dành Cho Cố Vấn & Sinh Viên */}
      <div className="card" style={{ padding: '1rem 1.25rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flex: 1, minWidth: '280px' }}>
            <span style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--text-main)', whiteSpace: 'nowrap' }}>
              Sinh viên theo dõi:
            </span>
            <select
              className="form-select"
              value={selectedStudentId}
              onChange={(e) => handleStudentChange(e.target.value)}
              style={{ width: '100%', maxWidth: '380px', fontWeight: 600 }}
            >
              {students.map((st) => (
                <option key={st.id} value={st.id}>
                  {st.studentCode} - {st.fullName} ({st.className}) • {st.latestPredictedLevel || 'Chưa dự đoán'}
                </option>
              ))}
            </select>
          </div>

          {currentStudent && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                Email: <strong style={{ color: 'var(--text-main)' }}>{currentStudent.email}</strong>
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                GPA Tích lũy: <strong style={{ color: 'var(--primary)' }}>{currentStudent.gpa || 'N/A'}</strong>
              </div>
              <span
                style={{
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  padding: '0.2rem 0.6rem',
                  borderRadius: '20px',
                  backgroundColor:
                    currentStudent.latestPredictedLevel === 'High'
                      ? '#dcfce7'
                      : currentStudent.latestPredictedLevel === 'Medium'
                      ? '#fef3c7'
                      : '#fee2e2',
                  color:
                    currentStudent.latestPredictedLevel === 'High'
                      ? '#166534'
                      : currentStudent.latestPredictedLevel === 'Medium'
                      ? '#92400e'
                      : '#991b1b'
                }}
              >
                Trình độ AI: {currentStudent.latestPredictedLevel || 'Low'}
              </span>
            </div>
          )}
        </div>
      </div>

      {loading ? (
        <LoadingSpinner message="Đang nạp lộ trình thích ứng và phân tích mục tiêu..." />
      ) : !pathway ? (
        <div className="card" style={{ textAlign: 'center', padding: '3.5rem 1rem' }}>
          <AlertTriangle size={48} color="var(--warning)" style={{ margin: '0 auto 1rem' }} />
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-main)' }}>
            Chưa có lộ trình học tập cho sinh viên này
          </h3>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', maxWidth: '480px', margin: '0.5rem auto 1.5rem' }}>
            Vui lòng nhấn nút bên dưới để Rule-based Recommendation Engine tự động phân tích điểm số môn học và tạo lộ trình thích ứng.
          </p>
          <button className="btn btn-primary" onClick={handleRefreshPathway}>
            Tạo Lộ Trình Thích Ứng Ngay
          </button>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.75rem' }}>
          {/* Banner Tổng Quan Lộ Trình */}
          <div
            className="card"
            style={{
              background: 'linear-gradient(135deg, #1e1b4b 0%, #312e81 60%, #4338ca 100%)',
              color: '#ffffff',
              padding: '1.75rem 2rem',
              boxShadow: '0 10px 25px -5px rgba(49, 46, 129, 0.3)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1.5rem' }}>
              <div style={{ flex: 1, minWidth: '300px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.75rem' }}>
                  <span
                    style={{
                      fontSize: '0.75rem',
                      fontWeight: 800,
                      letterSpacing: '0.05em',
                      backgroundColor: 'rgba(255, 255, 255, 0.2)',
                      padding: '0.25rem 0.75rem',
                      borderRadius: '4px',
                      textTransform: 'uppercase'
                    }}
                  >
                    TRẠNG THÁI: {pathway.status === 'COMPLETED' ? 'ĐÃ HOÀN THÀNH' : 'ĐANG THỰC HIỆN'}
                  </span>
                  <span style={{ fontSize: '0.8rem', color: '#c7d2fe', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                    <Calendar size={14} /> Thời gian dự kiến: <strong>{pathway.estimatedWeeks || '4 tuần'}</strong>
                  </span>
                </div>

                <h2 style={{ fontSize: '1.5rem', fontWeight: 800, lineHeight: 1.3, marginBottom: '0.5rem' }}>
                  {pathway.title}
                </h2>
                <p style={{ color: '#e0e7ff', fontSize: '0.9rem', lineHeight: 1.5, margin: 0 }}>
                  {pathway.description}
                </p>

                {/* Mục tiêu học tập cốt lõi */}
                {pathway.learningGoal && (
                  <div
                    style={{
                      marginTop: '1rem',
                      padding: '0.75rem 1rem',
                      backgroundColor: 'rgba(255, 255, 255, 0.08)',
                      borderRadius: 'var(--radius-md)',
                      borderLeft: '4px solid #38bdf8'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#7dd3fc', fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', marginBottom: '0.25rem' }}>
                      <Target size={14} /> Mục Tiêu Học Tập Cốt Lõi:
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#f8fafc', lineHeight: 1.4 }}>
                      {pathway.learningGoal}
                    </div>
                  </div>
                )}
              </div>

              {/* Tiến độ hoàn thành */}
              <div
                style={{
                  backgroundColor: 'rgba(255, 255, 255, 0.1)',
                  backdropFilter: 'blur(8px)',
                  padding: '1.25rem 1.5rem',
                  borderRadius: 'var(--radius-lg)',
                  minWidth: '220px',
                  textAlign: 'center'
                }}
              >
                <div style={{ fontSize: '0.8rem', color: '#c7d2fe', fontWeight: 600, marginBottom: '0.25rem' }}>
                  Tiến Độ Lộ Trình
                </div>
                <div style={{ fontSize: '2.25rem', fontWeight: 900, color: '#38bdf8', lineHeight: 1 }}>
                  {pathway.progressPercentage || 0}%
                </div>
                <div style={{ fontSize: '0.75rem', color: '#e0e7ff', marginTop: '0.5rem' }}>
                  Đã hoàn thành <strong>{pathway.completedRecommendations || 0}</strong> /{' '}
                  <strong>{pathway.totalRecommendations || (pathway.recommendations?.length || 0)}</strong> đề xuất
                </div>

                {/* Thanh tiến độ */}
                <div
                  style={{
                    height: '8px',
                    backgroundColor: 'rgba(255, 255, 255, 0.2)',
                    borderRadius: '4px',
                    overflow: 'hidden',
                    marginTop: '0.75rem'
                  }}
                >
                  <div
                    style={{
                      width: `${pathway.progressPercentage || 0}%`,
                      height: '100%',
                      backgroundColor: '#38bdf8',
                      transition: 'width 0.3s ease'
                    }}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Thanh Bộ Lọc Khuyến Nghị */}
          <div
            className="card"
            style={{
              padding: '0.75rem 1.25rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              flexWrap: 'wrap',
              gap: '1rem'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem', fontWeight: 700, color: 'var(--text-main)' }}>
              <Filter size={16} color="var(--primary)" />
              Bộ Lọc Đề Xuất:
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Ưu tiên:</span>
                <select
                  className="form-select"
                  value={filterPriority}
                  onChange={(e) => setFilterPriority(e.target.value)}
                  style={{ padding: '0.25rem 0.5rem', fontSize: '0.8rem', minWidth: '110px' }}
                >
                  <option value="ALL">Tất cả</option>
                  <option value="HIGH">Ưu tiên Cao (HIGH)</option>
                  <option value="MEDIUM">Trung bình (MEDIUM)</option>
                  <option value="LOW">Thấp (LOW)</option>
                </select>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Trạng thái:</span>
                <select
                  className="form-select"
                  value={filterStatus}
                  onChange={(e) => setFilterStatus(e.target.value)}
                  style={{ padding: '0.25rem 0.5rem', fontSize: '0.8rem', minWidth: '130px' }}
                >
                  <option value="ALL">Tất cả</option>
                  <option value="NOT_STARTED">Chưa bắt đầu</option>
                  <option value="IN_PROGRESS">Đang học</option>
                  <option value="COMPLETED">Đã hoàn thành</option>
                </select>
              </div>
            </div>
          </div>

          {/* Danh Sách Khuyến Nghị & Nhiệm Vụ Học Tập */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {filteredRecommendations.length === 0 ? (
              <div className="card" style={{ textAlign: 'center', padding: '2.5rem 1rem' }}>
                <p style={{ color: 'var(--text-muted)', margin: 0 }}>
                  Không có đề xuất học tập nào phù hợp với bộ lọc hiện tại.
                </p>
              </div>
            ) : (
              filteredRecommendations.map((rec, index) => {
                const isCompleted = rec.status === 'COMPLETED';
                const isInProgress = rec.status === 'IN_PROGRESS';
                const isHigh = rec.priority === 'HIGH';
                const isMedium = rec.priority === 'MEDIUM';

                return (
                  <div
                    key={rec.id || index}
                    className="card"
                    style={{
                      padding: '1.5rem',
                      borderLeft: `5px solid ${
                        isCompleted
                          ? '#10b981'
                          : isHigh
                          ? '#ef4444'
                          : isMedium
                          ? '#f59e0b'
                          : '#6366f1'
                      }`,
                      backgroundColor: isCompleted ? '#fafdfc' : '#ffffff',
                      transition: 'all 0.2s ease',
                      boxShadow: 'var(--shadow-sm)'
                    }}
                  >
                    {/* Hàng Tiêu Đề & Huy Hiệu */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.75rem', marginBottom: '0.75rem' }}>
                      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem', flex: 1 }}>
                        {/* Nút bấm chuyển trạng thái nhanh */}
                        <button
                          onClick={() => handleUpdateStatus(rec.id, rec.status)}
                          title="Click để chuyển trạng thái: Chưa học -> Đang học -> Hoàn thành"
                          style={{
                            border: 'none',
                            background: 'none',
                            cursor: 'pointer',
                            padding: '2px',
                            color: isCompleted ? '#10b981' : isInProgress ? '#3b82f6' : '#cbd5e1',
                            transition: 'color 0.2s ease'
                          }}
                        >
                          {isCompleted ? (
                            <CheckCircle2 size={24} />
                          ) : isInProgress ? (
                            <PlayCircle size={24} />
                          ) : (
                            <Clock size={24} />
                          )}
                        </button>

                        <div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                            <h3
                              style={{
                                fontSize: '1.05rem',
                                fontWeight: 700,
                                color: isCompleted ? '#059669' : 'var(--text-main)',
                                textDecoration: isCompleted ? 'line-through' : 'none',
                                margin: 0
                              }}
                            >
                              {rec.title}
                            </h3>

                            {rec.subjectCode && (
                              <span
                                style={{
                                  fontSize: '0.7rem',
                                  fontWeight: 700,
                                  backgroundColor: '#e0e7ff',
                                  color: '#3730a3',
                                  padding: '0.15rem 0.5rem',
                                  borderRadius: '4px'
                                }}
                              >
                                {rec.subjectCode}
                              </span>
                            )}
                          </div>

                          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', margin: '0.35rem 0 0 0', lineHeight: 1.4 }}>
                            {rec.description}
                          </p>
                        </div>
                      </div>

                      {/* Huy hiệu ưu tiên & Trạng thái */}
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span
                          style={{
                            fontSize: '0.7rem',
                            fontWeight: 800,
                            padding: '0.2rem 0.6rem',
                            borderRadius: '4px',
                            backgroundColor: isHigh ? '#fee2e2' : isMedium ? '#fef3c7' : '#e0e7ff',
                            color: isHigh ? '#dc2626' : isMedium ? '#b45309' : '#4338ca',
                            textTransform: 'uppercase'
                          }}
                        >
                          Ưu Tiên: {rec.priority}
                        </span>

                        <span
                          onClick={() => handleUpdateStatus(rec.id, rec.status)}
                          style={{
                            fontSize: '0.7rem',
                            fontWeight: 700,
                            padding: '0.2rem 0.6rem',
                            borderRadius: '20px',
                            cursor: 'pointer',
                            backgroundColor: isCompleted ? '#dcfce7' : isInProgress ? '#dbeafe' : '#f1f5f9',
                            color: isCompleted ? '#15803d' : isInProgress ? '#1d4ed8' : '#64748b',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '0.3rem'
                          }}
                        >
                          {isCompleted ? '✓ Hoàn thành' : isInProgress ? '▶ Đang học' : '○ Chưa bắt đầu'}
                        </span>
                      </div>
                    </div>

                    {/* Khối Chi Tiết: Mục Tiêu & Thời Lượng */}
                    <div
                      style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
                        gap: '0.75rem',
                        marginTop: '0.75rem',
                        padding: '0.75rem 1rem',
                        backgroundColor: isCompleted ? '#f0fdf4' : '#f8fafc',
                        borderRadius: 'var(--radius-md)',
                        fontSize: '0.8rem'
                      }}
                    >
                      {rec.learningObjective && (
                        <div>
                          <span style={{ fontWeight: 700, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                            <Target size={13} color="var(--primary)" /> Mục tiêu học tập:
                          </span>
                          <span style={{ color: 'var(--text-muted)' }}>{rec.learningObjective}</span>
                        </div>
                      )}

                      {rec.estimatedDuration && (
                        <div>
                          <span style={{ fontWeight: 700, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                            <Clock size={13} color="#f59e0b" /> Thời lượng dự kiến:
                          </span>
                          <span style={{ color: 'var(--text-muted)' }}>{rec.estimatedDuration}</span>
                        </div>
                      )}

                      {rec.resourceType && (
                        <div>
                          <span style={{ fontWeight: 700, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                            <BookOpen size={13} color="#10b981" /> Tài liệu học tập:
                          </span>
                          <span style={{ color: '#047857', fontWeight: 600 }}>{rec.resourceType}</span>
                        </div>
                      )}
                    </div>

                    {/* Khối Explainability: Tại sao hệ thống đề xuất điều này? */}
                    {rec.explanation && (
                      <div
                        style={{
                          marginTop: '0.75rem',
                          padding: '0.6rem 0.85rem',
                          backgroundColor: '#fefce8',
                          border: '1px dashed #fde047',
                          borderRadius: 'var(--radius-md)',
                          fontSize: '0.775rem',
                          color: '#713f12',
                          display: 'flex',
                          alignItems: 'flex-start',
                          gap: '0.5rem'
                        }}
                      >
                        <Lightbulb size={16} color="#ca8a04" style={{ flexShrink: 0, marginTop: '2px' }} />
                        <div>
                          <strong>Tại sao có đề xuất này? (Lý do từ Rule Engine):</strong>{' '}
                          <span>{rec.explanation}</span>
                        </div>
                      </div>
                    )}
                  </div>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
};
