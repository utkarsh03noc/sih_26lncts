import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useParams } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import { useAuth } from './hooks/useAuth';

// Layouts
import DashboardLayout from './layouts/DashboardLayout';
import AuthLayout from './layouts/AuthLayout';

// Public Pages
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import TrackComplaintPage from './pages/complainant/TrackComplaintPage';

// Admin Pages
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AdminComplaintsPage from './pages/admin/AdminComplaintsPage';
import AdminComplaintDetailPage from './pages/admin/AdminComplaintDetailPage';
import InstitutionsPage from './pages/admin/InstitutionsPage';
import InstitutionDetailPage from './pages/admin/InstitutionDetailPage';
import BureausPage from './pages/admin/BureausPage';
import DuplicateAlertsPage from './pages/admin/DuplicateAlertsPage';
import EscalationsPage from './pages/admin/EscalationsPage';
import AnalyticsPage from './pages/admin/AnalyticsPage';
import ReportsPage from './pages/admin/ReportsPage';
import AuditLogsPage from './pages/admin/AuditLogsPage';

// Officer Pages
import OfficerDashboardPage from './pages/officer/OfficerDashboardPage';
import OfficerAssignedComplaintsPage from './pages/officer/OfficerAssignedComplaintsPage';
import OfficerComplaintDetailPage from './pages/officer/OfficerComplaintDetailPage';
import OfficerAnalyticsPage from './pages/officer/OfficerAnalyticsPage';

// Complainant Pages
import ComplainantDashboardPage from './pages/complainant/ComplainantDashboardPage';
import MyComplaintsPage from './pages/complainant/MyComplaintsPage';
import RegisterComplaintPage from './pages/complainant/RegisterComplaintPage';
import ComplainantComplaintDetailPage from './pages/complainant/ComplainantComplaintDetailPage';
import FeedbackPage from './pages/complainant/FeedbackPage';

// Shared Pages
import UnauthorizedPage from './pages/shared/UnauthorizedPage';
import NotFoundPage from './pages/shared/NotFoundPage';
import LoadingSpinner from './components/common/LoadingSpinner';

// Protected Route Guard
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, isAuthenticated, loading } = useAuth();

  if (loading) {
    return <LoadingSpinner text="Authenticating user session..." size="lg" />;
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

// Root index redirector based on authenticated user's role
const RootRedirect = () => {
  const { user, isAuthenticated, loading } = useAuth();

  if (loading) {
    return <LoadingSpinner text="Initializing session..." size="lg" />;
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  if (user.role === 'ADMIN') {
    return <Navigate to="/admin/dashboard" replace />;
  } else if (user.role === 'INSTITUTION_OFFICER') {
    return <Navigate to="/officer/dashboard" replace />;
  } else {
    return <Navigate to="/complainant/dashboard" replace />;
  }
};

// Universal /complaints/:id route handler resolving directly by role
const ComplaintRedirectDispatcher = () => {
  const { id } = useParams();
  const { user, isAuthenticated, loading } = useAuth();

  if (loading) {
    return <LoadingSpinner text="Opening grievance dossier..." size="lg" />;
  }

  if (!isAuthenticated || !user) {
    return <Navigate to={`/login?redirect=/complaints/${id}`} replace />;
  }

  if (user.role === 'ADMIN') {
    return <Navigate to={`/admin/complaints/${id}`} replace />;
  } else if (user.role === 'INSTITUTION_OFFICER') {
    return <Navigate to={`/officer/complaints/${id}`} replace />;
  } else {
    return <Navigate to={`/complainant/complaints/${id}`} replace />;
  }
};

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            {/* Public Auth Routes */}
            <Route element={<AuthLayout />}>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
            </Route>

            {/* Authenticated Dashboard Routes */}
            <Route element={<DashboardLayout />}>
              {/* Root redirect */}
              <Route path="/" element={<RootRedirect />} />

              {/* Public Track Grievance inside Dashboard Layout */}
              <Route path="/complainant/track" element={<TrackComplaintPage />} />
              <Route path="/track" element={<TrackComplaintPage />} />

              {/* Universal complaint routing */}
              <Route path="/complaints" element={<RootRedirect />} />
              <Route
                path="/complaints/:id"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'INSTITUTION_OFFICER', 'COMPLAINANT']}>
                    <ComplaintRedirectDispatcher />
                  </ProtectedRoute>
                }
              />

              {/* Admin Routes */}
              <Route
                path="/admin/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <AdminDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/complaints"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <AdminComplaintsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/complaints/:id"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <AdminComplaintDetailPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/institutions"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <InstitutionsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/institutions/:id"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <InstitutionDetailPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/bureaus"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <BureausPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/duplicate-alerts"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <DuplicateAlertsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/duplicates"
                element={<Navigate to="/admin/duplicate-alerts" replace />}
              />
              <Route
                path="/admin/escalations"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <EscalationsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/analytics"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <AnalyticsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/reports"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <ReportsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/audit-logs"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <AuditLogsPage />
                  </ProtectedRoute>
                }
              />

              {/* Institution Officer Routes */}
              <Route
                path="/officer/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'INSTITUTION_OFFICER']}>
                    <OfficerDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/officer/complaints"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'INSTITUTION_OFFICER']}>
                    <OfficerAssignedComplaintsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/officer/complaints/:id"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'INSTITUTION_OFFICER']}>
                    <OfficerComplaintDetailPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/officer/analytics"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'INSTITUTION_OFFICER']}>
                    <OfficerAnalyticsPage />
                  </ProtectedRoute>
                }
              />

              {/* Complainant Routes */}
              <Route
                path="/complainant/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'COMPLAINANT']}>
                    <ComplainantDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/complainant/complaints"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'COMPLAINANT']}>
                    <MyComplaintsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/complainant/complaints/:id"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'COMPLAINANT']}>
                    <ComplainantComplaintDetailPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/complainant/register-complaint"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'COMPLAINANT']}>
                    <RegisterComplaintPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/complainant/complaints/register"
                element={<Navigate to="/complainant/register-complaint" replace />}
              />
              <Route
                path="/complainant/feedback"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'COMPLAINANT']}>
                    <FeedbackPage />
                  </ProtectedRoute>
                }
              />

              {/* Shared Pages */}
              <Route path="/unauthorized" element={<UnauthorizedPage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Route>
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
