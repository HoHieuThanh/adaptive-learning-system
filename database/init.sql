-- ==============================================================================
-- DATABASE SCHEMA: Adaptive Learning System (PostgreSQL)
-- 8 Bảng nghiệp vụ cốt lõi theo thiết kế
-- ==============================================================================

-- 1. Bảng Users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'ADVISOR', 'ADMIN'))
);

-- 2. Bảng Students
CREATE TABLE IF NOT EXISTS students (
    id BIGSERIAL PRIMARY KEY,
    student_code VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    class_name VARCHAR(50) NOT NULL
);

-- 3. Bảng Subjects
CREATE TABLE IF NOT EXISTS subjects (
    id BIGSERIAL PRIMARY KEY,
    subject_code VARCHAR(20) UNIQUE NOT NULL,
    subject_name VARCHAR(100) NOT NULL
);

-- 4. Bảng Grades
CREATE TABLE IF NOT EXISTS grades (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    score NUMERIC(4, 2) NOT NULL,
    semester VARCHAR(20) NOT NULL
);

-- 5. Bảng Learning Activities
CREATE TABLE IF NOT EXISTS learning_activities (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    study_hours NUMERIC(4, 1) NOT NULL DEFAULT 0.0,
    attendance_rate NUMERIC(5, 2) NOT NULL DEFAULT 100.00,
    assignment_completion NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    lms_logins INT NOT NULL DEFAULT 0,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. Bảng Predictions
CREATE TABLE IF NOT EXISTS predictions (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    predicted_level VARCHAR(20) NOT NULL,
    confidence NUMERIC(5, 4) NOT NULL,
    model_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Bảng Learning Paths
CREATE TABLE IF NOT EXISTS learning_paths (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    learning_goal TEXT,
    estimated_weeks VARCHAR(50) DEFAULT '4 tuần',
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. Bảng Recommendations
CREATE TABLE IF NOT EXISTS recommendations (
    id BIGSERIAL PRIMARY KEY,
    learning_path_id BIGINT NOT NULL REFERENCES learning_paths(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    learning_objective TEXT,
    estimated_duration VARCHAR(50),
    explanation TEXT,
    subject_code VARCHAR(50),
    resource_type VARCHAR(100)
);

-- Chỉ mục tối ưu hóa truy vấn
CREATE INDEX IF NOT EXISTS idx_grades_student_id ON grades(student_id);
CREATE INDEX IF NOT EXISTS idx_activities_student_id ON learning_activities(student_id);
CREATE INDEX IF NOT EXISTS idx_predictions_student_id ON predictions(student_id);
CREATE INDEX IF NOT EXISTS idx_learning_paths_student_id ON learning_paths(student_id);
CREATE INDEX IF NOT EXISTS idx_recommendations_path_id ON recommendations(learning_path_id);
