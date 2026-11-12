import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { DashboardLayout } from './components/layout/DashboardLayout';

import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
import { StudentList } from './pages/StudentList';
import { StudentDetail } from './pages/StudentDetail';
import { AcademicPerformance } from './pages/AcademicPerformance';
import { AIPrediction } from './pages/AIPrediction';
import { LearningPathPage } from './pages/LearningPathPage';
import { AdvisorDashboard } from './pages/AdvisorDashboard';
import { AdminManagement } from './pages/AdminManagement';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public login route */}
          <Route path="/login" element={<Login />} />

          {/* Protected dashboard routes with shared Layout */}
          <Route path="/" element={<DashboardLayout><Dashboard /></DashboardLayout>} />
          <Route path="/students" element={<DashboardLayout><StudentList /></DashboardLayout>} />
          <Route path="/students/:id" element={<DashboardLayout><StudentDetail /></DashboardLayout>} />
          <Route path="/performance" element={<DashboardLayout><AcademicPerformance /></DashboardLayout>} />
          <Route path="/predictions" element={<DashboardLayout><AIPrediction /></DashboardLayout>} />
          <Route path="/learning-paths" element={<DashboardLayout><LearningPathPage /></DashboardLayout>} />
          <Route path="/advisor" element={<DashboardLayout><AdvisorDashboard /></DashboardLayout>} />
          <Route path="/admin" element={<DashboardLayout><AdminManagement /></DashboardLayout>} />

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
