import React, { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { dashboardService } from '../../services/dashboardService';
import { complaintService } from '../../services/complaintService';
import MetricsGrid from '../../components/dashboard/MetricsGrid';
import CategoryChart from '../../components/dashboard/CategoryChart';
import StatusChart from '../../components/dashboard/StatusChart';
import RecentComplaintsTable from '../../components/dashboard/RecentComplaintsTable';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Building2, RefreshCw } from 'lucide-react';

const OfficerDashboardPage = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [recentComplaints, setRecentComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    fetchOfficerData();
  }, [user]);

  const fetchOfficerData = async () => {
    try {
      setRefreshing(true);
      const [statsRes, complaintsRes] = await Promise.all([
        dashboardService.getInstitutionStats(user?.institutionId),
        complaintService.getComplaints({
          institutionId: user?.institutionId,
          page: 0,
          size: 6,
          sortBy: 'createdAt',
          sortDir: 'desc',
        }),
      ]);
      setStats(statsRes.data);
      setRecentComplaints(complaintsRes.data?.content || []);
    } catch (err) {
      console.error('Failed to load officer dashboard:', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  if (loading) {
    return <LoadingSpinner text="Loading institutional grievance dossier..." size="lg" />;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">
              Institutional Grievance Officer Desk
            </h1>
            <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-indigo-100 text-indigo-800 border border-indigo-200">
              <Building2 className="w-3 h-3 mr-1" /> Nodal Desk
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Monitoring, investigation, and time-bound redressal for{' '}
            <strong className="text-slate-700">{user?.institutionName || 'Assigned Institution'}</strong>
          </p>
        </div>

        <button
          onClick={fetchOfficerData}
          disabled={refreshing}
          className="inline-flex items-center px-3 py-1.5 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-lg shadow-sm hover:bg-slate-50 transition-colors disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${refreshing ? 'animate-spin' : ''}`} />
          {refreshing ? 'Refreshing...' : 'Refresh Desk'}
        </button>
      </div>

      {/* Metrics Cards */}
      <MetricsGrid stats={stats} />

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <CategoryChart data={stats?.categoryStats || []} />
        <StatusChart data={stats?.statusStats || []} />
      </div>

      {/* Assigned Complaints Table */}
      <RecentComplaintsTable
        complaints={recentComplaints}
        viewBasePath="/officer/complaints"
      />
    </div>
  );
};

export default OfficerDashboardPage;
