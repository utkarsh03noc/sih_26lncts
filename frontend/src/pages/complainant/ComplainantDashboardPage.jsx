import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { dashboardService } from '../../services/dashboardService';
import { complaintService } from '../../services/complaintService';
import RecentComplaintsTable from '../../components/dashboard/RecentComplaintsTable';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import StatCard from '../../components/common/StatCard';
import {
  FileText,
  Clock,
  Activity,
  CheckCircle2,
  PlusCircle,
  Search,
  MessageSquare,
} from 'lucide-react';

const ComplainantDashboardPage = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [recentComplaints, setRecentComplaints] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [statsRes, compRes] = await Promise.all([
        dashboardService.getMyStats(),
        complaintService.getComplaints({ page: 0, size: 5, sortBy: 'createdAt', sortDir: 'desc' }),
      ]);
      setStats(statsRes.data);
      setRecentComplaints(compRes.data?.content || []);
    } catch (err) {
      console.error('Failed to load complainant dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner text="Loading your grievance dashboard..." size="lg" />;
  }

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-blue-900 via-blue-800 to-indigo-900 rounded-2xl p-6 text-white shadow-md flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-blue-200 text-xs font-semibold uppercase tracking-wider">
            Student & Citizen Grievance Portal
          </span>
          <h1 className="text-xl font-bold tracking-tight mt-1">
            Welcome back, {user?.fullName || user?.username}!
          </h1>
          <p className="text-xs text-blue-100 max-w-xl mt-1">
            Register new complaints, monitor administrative enquiry status, track SLA deadlines, and submit resolution feedback.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2 flex-shrink-0">
          <Link
            to="/complainant/register-complaint"
            className="inline-flex items-center px-4 py-2 text-xs font-semibold bg-white text-blue-900 hover:bg-blue-50 rounded-lg shadow-sm transition-colors"
          >
            <PlusCircle className="w-4 h-4 mr-1.5" /> Lodge Grievance
          </Link>
          <Link
            to="/complainant/track"
            className="inline-flex items-center px-4 py-2 text-xs font-semibold bg-blue-700/80 hover:bg-blue-700 text-white border border-blue-500 rounded-lg transition-colors"
          >
            <Search className="w-4 h-4 mr-1.5" /> Track Status
          </Link>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="My Total Cases"
          value={stats?.totalComplaints || 0}
          subtitle="All grievances registered"
          icon={FileText}
          color="blue"
        />
        <StatCard
          title="Pending Review"
          value={stats?.pendingComplaints || 0}
          subtitle="Submitted / Assigned"
          icon={Clock}
          color="amber"
        />
        <StatCard
          title="In Progress"
          value={stats?.inProgressComplaints || 0}
          subtitle="Active enquiry underway"
          icon={Activity}
          color="sky"
        />
        <StatCard
          title="Resolved Cases"
          value={stats?.resolvedComplaints || 0}
          subtitle="Successfully concluded"
          icon={CheckCircle2}
          color="emerald"
        />
      </div>

      {/* Recent Cases */}
      <RecentComplaintsTable
        complaints={recentComplaints}
        viewBasePath="/complainant/complaints"
      />
    </div>
  );
};

export default ComplainantDashboardPage;
