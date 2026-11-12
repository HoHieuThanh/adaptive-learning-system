# Tài Liệu Kỹ Thuật: Hệ Thống Gợi Ý Lộ Trình Học Tập Thích Ứng (Adaptive Learning Path)
## Rule-Based Recommendation Engine & Explainability Guide

---

### 1. Mục Tiêu Nghiệp Vụ

Hệ thống kết hợp giữa **mô hình Trí tuệ Nhân tạo (XGBoost/K-Means)** và **Động cơ Đề xuất Dựa trên Luật (Rule-based Recommendation Engine)** để:
1. Nhận diện sớm trình độ tổng thể của sinh viên (`Low`, `Medium`, `High`) kèm độ tin cậy và phân cụm hành vi.
2. Tự động rà soát bảng điểm từng môn học để phát hiện các lỗ hổng kiến thức cụ thể (môn có điểm $< 5.0$ hoặc $< 6.5$).
3. Sinh ra lộ trình học tập cá nhân hóa với:
   - **Mục tiêu học tập cốt lõi** (`learningGoal`).
   - **Thời gian dự kiến hoàn thành** (`estimatedWeeks`).
   - **Thứ tự ưu tiên** (`HIGH`, `MEDIUM`, `LOW`).
   - **Thời lượng thực hiện từng nhiệm vụ** (`estimatedDuration`).
   - **Giải thích minh bạch lý do đề xuất** (`explanation` - Khái niệm Explainable AI/Rule-based System).
   - **Theo dõi tiến độ 3 trạng thái**: `Chưa bắt đầu` (`NOT_STARTED`), `Đang học` (`IN_PROGRESS`), `Đã hoàn thành` (`COMPLETED`).

---

### 2. Ma Trận Quy Tắc Đề Xuất (Rule-Based Matrix)

| Trình độ AI | Điểm Môn Học | Mức Ưu Tiên | Tiêu Đề Đề Xuất (Demo) | Mục Tiêu & Thời Lượng | Giải Thích Lý Do Đề Xuất (Explainability) |
| :--- | :--- | :---: | :--- | :--- | :--- |
| **Low** *(At-Risk)* | **Điểm < 5.0** *(Yếu/Rớt)* | **HIGH** | `[Demo] Ôn tập kiến thức nền tảng: [Tên môn]` | Nắm vững lý thuyết cốt lõi, giải 80% bài tập cơ bản.<br>⏱️ *10 giờ (2 tuần)* | *Điểm môn [Tên môn] đạt [Điểm]/10 (< 5.0). Trình độ AI phân loại là Low. Cần gấp rút củng cố kiến thức nền tảng để đủ điều kiện thi kết thúc học phần.* |
| **Low** *(At-Risk)* | **5.0 ≤ Điểm < 7.0** *(TB)* | **MEDIUM** | `[Demo] Luyện tập bài tập thực hành cơ bản: [Tên môn]` | Hoàn thành bài lab và bài tập tuần trên LMS.<br>⏱️ *6 giờ (1 tuần)* | *Điểm đạt mức trung bình ([Điểm]/10). Cần duy trì làm bài tập thực hành để tránh bị tụt điểm vào cuối kỳ.* |
| **Low** *(At-Risk)* | *Thói quen tự học* | **HIGH** | `[Demo] Thiết lập thời khóa biểu tự học ≥ 8h/tuần` | Xây dựng thói quen tự học cố định với Pomodoro.<br>⏱️ *Duy trì 6 tuần* | *Số giờ tự học thấp và tỷ lệ vắng mặt cao. Kỷ luật tự học là chìa khóa quan trọng nhất để cải thiện điểm số.* |
| **Low** *(At-Risk)* | *Học vụ* | **HIGH** | `[Demo] Đặt lịch tư vấn cùng Cố vấn học tập (Advisor)` | Trao đổi tháo gỡ áp lực, điều chỉnh thời khóa biểu.<br>⏱️ *1 buổi (45 phút)* | *Sinh viên thuộc nhóm cảnh báo học vụ. Cố vấn học tập sẽ hỗ trợ lên chiến lược thi cử hợp lý.* |
| **Medium** *(Khá)* | **Điểm < 6.5** *(Yếu hơn mức TB)* | **HIGH** | `[Demo] Luyện tập chuyên đề còn yếu: [Tên môn]` | Lấp lỗ hổng chuyên đề hay sai, làm quen đề kiểm tra.<br>⏱️ *8 giờ (1.5 tuần)* | *Môn [Tên môn] hiện có điểm [Điểm]/10, dưới mức trung bình của bạn. Bạn đủ năng lực nhưng cần luyện thêm bài tập vận dụng.* |
| **Medium** *(Khá)* | **Điểm ≥ 6.5** *(Khá)* | **MEDIUM** | `[Demo] Bài tập vận dụng thực tế: [Tên môn]` | Áp dụng lý thuyết vào đồ án mini hoặc case study.<br>⏱️ *6 giờ (1 tuần)* | *Điểm môn đạt [Điểm]/10 (Khá). Thực hành vận dụng thực tế giúp tối ưu hóa điểm số bài tập lớn.* |
| **Medium** *(Khá)* | *Kỹ năng nhóm* | **MEDIUM** | `[Demo] Tham gia nhóm học tập chuyên đề & Thảo luận` | Học hỏi kinh nghiệm giải đề từ bạn bè.<br>⏱️ *2 giờ/tuần* | *Học nhóm giúp giải quyết nhanh các bài toán khó và giữ vững động lực suốt học kỳ.* |
| **High** *(Giỏi)* | **Tất cả các môn** | **MEDIUM / LOW** | `[Demo] Thử thách chuyên sâu & Đồ án thực tế: [Tên môn]` | Tối ưu hóa thuật toán hoặc xây dựng sản phẩm hoàn chỉnh.<br>⏱️ *12 giờ (2 tuần)* | *Bạn đạt điểm xuất sắc [Điểm]/10 môn [Tên môn]. Hệ thống khuyến khích thử sức bài toán mở rộng để xây dựng portfolio.* |
| **High** *(Giỏi)* | *Nghiên cứu* | **LOW** | `[Demo] Tham gia Đề tài NCKH Sinh viên` | Tham gia phòng lab nghiên cứu AI/Công nghệ phần mềm.<br>⏱️ *Toàn học kỳ* | *Học lực xuất sắc là nền tảng vững chắc để bạn tham gia NCKH và viết bài báo gửi các hội nghị.* |
| **High** *(Giỏi)* | *Đồng đẳng* | **LOW** | `[Demo] Đảm nhiệm vai trò Hỗ trợ học tập đồng đẳng (Peer Tutor)` | Phụ đạo bài tập cho các bạn sinh viên cần giúp đỡ.<br>⏱️ *2 giờ/tuần* | *Dạy lại cho người khác là phương pháp học tập đỉnh cao giúp khắc sâu kiến thức của bản thân.* |

---

### 3. Thiết Kế Cơ Sở Dữ Liệu & Entity Mapping

```sql
-- Bảng lộ trình học tập tổng thể
CREATE TABLE learning_paths (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    learning_goal TEXT,
    estimated_weeks VARCHAR(50) DEFAULT '4 tuần',
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS', -- IN_PROGRESS, COMPLETED, ARCHIVED
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Bảng chi tiết các khuyến nghị học tập thích ứng
CREATE TABLE recommendations (
    id BIGSERIAL PRIMARY KEY,
    learning_path_id BIGINT NOT NULL REFERENCES learning_paths(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',   -- HIGH, MEDIUM, LOW
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED', -- NOT_STARTED, IN_PROGRESS, COMPLETED
    learning_objective TEXT,                          -- Mục tiêu học tập cụ thể
    estimated_duration VARCHAR(50),                   -- Thời lượng dự kiến
    explanation TEXT,                                 -- Lý do hệ thống đề xuất (Explainability)
    subject_code VARCHAR(50),                         -- Mã môn học liên kết
    resource_type VARCHAR(100)                        -- Loại tài liệu mẫu (Demo Content)
);
```

---

### 4. Danh Sách REST APIs Quản Lý Tiến Độ Lộ Trình

| Method | Endpoint | Quyền (Role) | Chức Năng |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/learning-paths/student/{studentId}` | `STUDENT`, `ADVISOR`, `ADMIN` | Lấy lịch sử tất cả các lộ trình của sinh viên |
| `GET` | `/api/learning-paths/student/{studentId}/latest` | `STUDENT`, `ADVISOR`, `ADMIN` | Lấy lộ trình thích ứng mới nhất kèm tiến độ % |
| `POST` | `/api/learning-paths/generate/{studentId}` | `ADVISOR`, `ADMIN` | Sinh lại / cập nhật lộ trình mới dựa trên AI & điểm mới nhất |
| `PATCH` | `/api/learning-paths/recommendations/{id}/status` | `STUDENT`, `ADVISOR`, `ADMIN` | Cập nhật trạng thái đề xuất (`NOT_STARTED` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `COMPLETED`) |
| `PATCH` | `/api/learning-paths/{id}/status` | `ADVISOR`, `ADMIN` | Cập nhật trạng thái toàn bộ lộ trình (`IN_PROGRESS`, `COMPLETED`, `ARCHIVED`) |

---

### 5. Hướng Dẫn Trải Nghiệm Trên Giao Diện Web React

1. Truy cập Web Portal tại **[http://localhost:5173](http://localhost:5173)**.
2. Đăng nhập bằng tài khoản **Cố vấn (`advisor` / `advisor123`)** hoặc **Sinh viên (`student` / `student123`)**.
3. Chọn menu **"Lộ trình Cá nhân" (Learning Path)** trên Sidebar.
4. **Trải nghiệm các tính năng chính:**
   - **Đổi sinh viên theo dõi:** Chọn **SV003 (Lê Hoàng Cường - At-risk)** để xem lộ trình can thiệp khẩn cấp, hoặc **SV001 (Nguyễn Văn An - High)** để xem lộ trình phát triển nghiên cứu.
   - **Xem lý do đề xuất:** Mỗi thẻ khuyến nghị có khung màu vàng *"Tại sao có đề xuất này?"* giải thích trực quan lý do phân loại từ điểm số môn học và kết quả AI.
   - **Cập nhật tiến độ 1 chạm:** Click vào biểu tượng trạng thái hoặc badge bên cạnh đề xuất để chuyển đổi trạng thái: `○ Chưa bắt đầu` $\rightarrow$ `▶ Đang học` $\rightarrow$ `✓ Hoàn thành`.
   - **Thanh tiến độ thời gian thực:** Thanh phần trăm (`%`) trên banner tự động tính toán lại tỷ lệ hoàn thành ngay tức thì.
   - **Bộ lọc:** Sử dụng dropdown lọc theo mức ưu tiên (`HIGH`, `MEDIUM`, `LOW`) và trạng thái (`Chưa bắt đầu`, `Đang học`, `Hoàn thành`).

