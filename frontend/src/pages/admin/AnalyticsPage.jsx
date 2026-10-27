import React, { useState, useEffect } from 'react';
import { dashboardService } from '../../services/dashboardService';
import CategoryChart from '../../components/dashboard/CategoryChart';
import StatusChart from '../../components/dashboard/StatusChart';
import MonthlyTrendChart from '../../components/dashboard/MonthlyTrendChart';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { BarChart3, TrendingUp, CheckCircle2, Clock } from 'lucide-react';

const AnalyticsPage = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAnalytics();
  }, []);

  const loadAnalytics = async () => {
    try {
      setLoading(true);
      const res = await dashboardService.getOverallStats();
      setStats(res.data);
    } catch (err) {
      console.error('Failed to load analytics:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading || !stats) {
    return <LoadingSpinner text="Computing grievance intelligence..." size="lg" />;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">
          Centralized Grievance Analytics & Intelligence
        </h1>
        <p className="text-xs text-slate-500 mt-1">
          Deep-dive institutional trend analysis, SLA compliance metrics, and systemic grievance hot-spots
        </p>
      </div>

      {/* KPI highlight row */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center space-x-4">
          <div className="p-3 bg-blue-50 text-blue-600 rounded-lg">
            <TrendingUp className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-400 uppercase">Total Intake</p>
            <p className="text-2xl font-bold text-slate-900">{stats.totalComplaints}</p>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center space-x-4">
          <div className="p-3 bg-emerald-50 text-emerald-600 rounded-lg">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-400 uppercase">Resolution Efficiency</p>
            <p className="text-2xl font-bold text-emerald-600">{stats.resolutionRate}%</p>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center space-x-4">
          <div className="p-3 bg-rose-50 text-rose-600 rounded-lg">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-400 uppercase">Overdue Cases</p>
            <p className="text-2xl font-bold text-rose-600">{stats.overdueComplaints}</p>
          </div>
        </div>
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <CategoryChart data={stats.categoryStats || []} />
        <StatusChart data={stats.statusStats || []} />
      </div>

      {/* Monthly Area Chart */}
      <MonthlyTrendChart data={stats.monthlyTrend || []} />
    </div>
  );
};

export default AnalyticsPage;
