import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import MainLayout from './components/layout/MainLayout';

// Public Pages
import LandingPage from './pages/public/LandingPage';
import AboutPage from './pages/public/AboutPage';
import LoginPage from './pages/public/LoginPage';
import RegisterPage from './pages/public/RegisterPage';

// Applicant Pages
import ApplicantDashboard from './pages/applicant/ApplicantDashboard';
import NewApplicationPage from './pages/applicant/NewApplicationPage';
import MyApplicationsPage from './pages/applicant/MyApplicationsPage';
import ApplicationDetailsPage from './pages/applicant/ApplicationDetailsPage';
import RevisionsPage from './pages/applicant/RevisionsPage';
import PaymentPage from './pages/applicant/PaymentPage';
import ApprovedPermitsPage from './pages/applicant/ApprovedPermitsPage';
import ProfilePage from './pages/applicant/ProfilePage';

// Officer Pages
import OfficerDashboard from './pages/officer/OfficerDashboard';
import TaskInboxPage from './pages/officer/TaskInboxPage';
import ReviewApplicationPage from './pages/officer/ReviewApplicationPage';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import ApplicationMonitoringPage from './pages/admin/ApplicationMonitoringPage';
import DepartmentManagementPage from './pages/admin/DepartmentManagementPage';
import UserManagementPage from './pages/admin/UserManagementPage';
import WorkflowMonitoringPage from './pages/admin/WorkflowMonitoringPage';
import AuditHistoryPage from './pages/admin/AuditHistoryPage';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, user, loading } = useAuth();

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center text-xs text-slate-500">Authenticating...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Routes with Top Navbar only */}
          <Route element={<MainLayout />}>
            <Route path="/" element={<LandingPage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Applicant Routes */}
            <Route
              path="/applicant/dashboard"
              element={
                <ProtectedRoute allowedRoles={['ROLE_APPLICANT', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <ApplicantDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/applicant/new-application"
              element={
                <ProtectedRoute allowedRoles={['ROLE_APPLICANT', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <NewApplicationPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/applicant/my-applications"
              element={
                <ProtectedRoute allowedRoles={['ROLE_APPLICANT', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <MyApplicationsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/applicant/applications/:id"
              element={
                <ProtectedRoute>
                  <ApplicationDetailsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/applicant/revisions"
              element={
                <ProtectedRoute allowedRoles={['ROLE_APPLICANT', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <RevisionsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/applicant/payments"
              element={
                <ProtectedRoute allowedRoles={['ROLE_APPLICANT', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <PaymentPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/applicant/payments/:id"
              element={
                <ProtectedRoute allowedRoles={['ROLE_APPLICANT', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <PaymentPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/applicant/approved-permits"
              element={
                <ProtectedRoute allowedRoles={['ROLE_APPLICANT', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <ApprovedPermitsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/applicant/profile"
              element={
                <ProtectedRoute>
                  <ProfilePage />
                </ProtectedRoute>
              }
            />

            {/* Officer Routes */}
            <Route
              path="/officer/dashboard"
              element={
                <ProtectedRoute allowedRoles={['ROLE_OFFICER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <OfficerDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/officer/inbox"
              element={
                <ProtectedRoute allowedRoles={['ROLE_OFFICER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <TaskInboxPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/officer/review/:id"
              element={
                <ProtectedRoute allowedRoles={['ROLE_OFFICER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <ReviewApplicationPage />
                </ProtectedRoute>
              }
            />

            {/* Admin Routes */}
            <Route
              path="/admin/dashboard"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <AdminDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/applications"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <ApplicationMonitoringPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/departments"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <DepartmentManagementPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/users"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <UserManagementPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/workflows"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <WorkflowMonitoringPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/audit-logs"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <AuditHistoryPage />
                </ProtectedRoute>
              }
            />

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
