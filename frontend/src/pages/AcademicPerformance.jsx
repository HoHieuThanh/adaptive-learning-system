import React, { useEffect, useState } from 'react';
import { gradeService } from '../api/gradeService';
import { subjectService } from '../api/subjectService';
import { studentService } from '../api/studentService';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { BarChart3, BookOpen, Users, TrendingUp } from 'lucide-react';

export const AcademicPerformance = () => {
  const [grades, setGrades] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedSubject, setSelectedSubject] = useState('ALL');

  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        const [grRes, subRes, stRes] = await Promise.all([
          gradeService.getAll(),
          subjectService.getAll(),
          studentService.getAll()
        ]);
        setGrades(grRes.data || []);
        setSubjects(subRes.data || []);
        setStudents(stRes.data || []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  if (loading) return <LoadingSpinner message="Đang nạp kết quả học tập..." />;

  const filteredGrades = selectedSubject === 'ALL'
    ? grades
    : grades.filter((g) => g.subjectCode === selectedSubject);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <div>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-main)' }}>
          Kết Quả Học Tập & Điểm Số Học Phần
        </h1>
        <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
          Theo dõi bảng điểm tổng hợp, phân phối điểm thi giữa kỳ/cuối kỳ theo môn học
        </p>
      </div>

      {/* Filter by subject */}
      <div className="card" style={{ padding: '1rem 1.25rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <BookOpen size={18} color="var(--text-muted)" />
        <span style={{ fontSize: '0.875rem', fontWeight: 600 }}>Lọc theo môn học:</span>
        <select
          className="form-select"
          value={selectedSubject}
          onChange={(e) => setSelectedSubject(e.target.value)}
          style={{ maxWidth: '320px' }}
        >
          <option value="ALL">Tất cả môn học ({grades.length} bản ghi điểm)</option>
          {subjects.map((sub) => (
            <option key={sub.id} value={sub.subjectCode}>
              {sub.subjectCode} - {sub.subjectName}
            </option>
          ))}
        </select>
      </div>

      {/* Grade Table */}
      <div className="data-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Sinh viên</th>
              <th>Mã môn</th>
              <th>Tên môn học</th>
              <th>Điểm số (Thang 10)</th>
              <th>Đánh giá</th>
              <th>Học kỳ</th>
            </tr>
          </thead>
          <tbody>
            {filteredGrades.length === 0 ? (
              <tr>
                <td colSpan={6} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                  Không có bản ghi điểm nào cho bộ lọc này.
                </td>
              </tr>
            ) : (
              filteredGrades.map((g) => {
                const score = Number(g.score);
                let badgeClass = 'badge-high';
                let rating = 'Giỏi / Xuất sắc';

                if (score < 5.0) {
                  badgeClass = 'badge-low';
                  rating = 'Chưa đạt (Cần hỗ trợ)';
                } else if (score < 7.0) {
                  badgeClass = 'badge-medium';
                  rating = 'Trung bình';
                } else if (score < 8.5) {
                  badgeClass = 'badge-info';
                  rating = 'Khá';
                }

                return (
                  <tr key={g.id}>
                    <td style={{ fontWeight: 600 }}>{g.studentName || `Sinh viên ID: ${g.studentId}`}</td>
                    <td style={{ fontWeight: 700, color: 'var(--primary)' }}>{g.subjectCode}</td>
                    <td>{g.subjectName}</td>
                    <td style={{ fontWeight: 800, fontSize: '0.95rem' }}>{score.toFixed(1)}</td>
                    <td>
                      <span className={`badge ${badgeClass}`}>{rating}</span>
                    </td>
                    <td style={{ color: 'var(--text-muted)' }}>{g.semester}</td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

