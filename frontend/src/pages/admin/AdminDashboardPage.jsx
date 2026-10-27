import React, { useState, useEffect } from 'react';
import { dashboardService } from '../../services/dashboardService';
import { complaintService } from '../../services/complaintService';
import { institutionService } from '../../services/institutionService';
import { bureauService } from '../../services/bureauService';
import MetricsGrid from '../../components/dashboard/MetricsGrid';
import CategoryChart from '../../components/dashboard/CategoryChart';
import StatusChart from '../../components/dashboard/StatusChart';
import MonthlyTrendChart from '../../components/dashboard/MonthlyTrendChart';
import BureauDistributionChart from '../../components/dashboard/BureauDistributionChart';
import InstitutionPerformanceTable from '../../components/dashboard/InstitutionPerformanceTable';
import RecentComplaintsTable from '../../components/dashboard/RecentComplaintsTable';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { STATUS_OPTIONS, PRIORITY_OPTIONS } from '../../utils/constants';
import {
  ShieldCheck,
  RefreshCw,
  Filter,
  X,
  Layers,
  Building2,
  AlertTriangle,
} from 'lucide-react';

const AdminDashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [recentComplaints, setRecentComplaints] = useState([]);
  const [institutions, setInstitutions] = useState([]);
  const [bureaus, setBureaus] = useState([]);
  const [cells, setCells] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  // Multi-dimensional filter states
  const [filters, setFilters] = useState({
    institutionId: '',
    bureauId: '',
    cellId: '',
    status: '',
    priority: '',
  });

  useEffect(() => {
    loadFilterMetadata();
  }, []);

  useEffect(() => {
    fetchDashboardData();
  }, [filters]);

  const loadFilterMetadata = async () => {
    try {
      const [instRes, burRes, cellRes] = await Promise.all([
        institutionService.getActiveInstitutions(),
        bureauService.getActiveBureaus(),
        bureauService.getActiveCells(),
      ]);
      setInstitutions(instRes.data || []);
      setBureaus(burRes.data || []);
      setCells(cellRes.data || []);
    } catch (err) {
      console.warn('Failed to load filter metadata:', err);
    }
  };

  const fetchDashboardData = async () => {
    try {
      setRefreshing(true);
      const params = {};
      if (filters.institutionId) params.institutionId = Number(filters.institutionId);
      if (filters.bureauId) params.bureauId = Number(filters.bureauId);
      if (filters.cellId) params.cellId = Number(filters.cellId);
      if (filters.status) params.status = filters.status;
      if (filters.priority) params.priority = filters.priority;

      const [statsRes, complaintsRes] = await Promise.all([
        dashboardService.getOverallStats(params),
        complaintService.getComplaints({
          ...params,
          page: 0,
          size: 6,
          sortBy: 'createdAt',
          sortDir: 'desc',
        }),
      ]);
      setStats(statsRes.data);
      setRecentComplaints(complaintsRes.data?.content || []);
    } catch (err) {
      console.error('Failed to load dashboard metrics:', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  };

  const handleClearFilters = () => {
    setFilters({
      institutionId: '',
      bureauId: '',
      cellId: '',
      status: '',
      priority: '',
    });
  };

  const hasActiveFilters = Object.values(filters).some((v) => v !== '');

  if (loading && !stats) {
    return <LoadingSpinner text="Compiling centralized grievance metrics..." size="lg" />;
  }

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">
              AICTE Grievance Command Center
            </h1>
            <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-blue-100 text-blue-800 border border-blue-200">
              <ShieldCheck className="w-3 h-3 mr-1" /> Apex Monitoring Console
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Consolidated oversight of institution complaints, bureau allocations, and resolution performance
          </p>
        </div>

        <button
          onClick={fetchDashboardData}
          disabled={refreshing}
          className="inline-flex items-center px-3 py-1.5 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-lg shadow-sm hover:bg-slate-50 transition-colors disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${refreshing ? 'animate-spin' : ''}`} />
          {refreshing ? 'Refreshing...' : 'Refresh Metrics'}
        </button>
      </div>

      {/* Multi-Dimensional Filter Bar */}
      <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-1.5 text-xs font-bold text-slate-800">
            <Filter className="w-3.5 h-3.5 text-blue-600" />
            <span>Multi-Dimensional Dimension Filters</span>
          </div>
          {hasActiveFilters && (
            <button
              onClick={handleClearFilters}
              className="inline-flex items-center text-[11px] font-semibold text-rose-600 hover:text-rose-800"
            >
              <X className="w-3 h-3 mr-1" /> Reset All Filters
            </button>
          )}
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-3">
          {/* Institution Filter */}
          <div>
            <label className="block text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1">
              Institution
            </label>
            <select
              value={filters.institutionId}
              onChange={(e) => handleFilterChange('institutionId', e.target.value)}
              className="w-full px-2.5 py-1.5 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
            >
              <option value="">All Institutions</option>
              {institutions.map((inst) => (
                <option key={inst.id} value={inst.id}>
                  {inst.code} - {inst.name}
                </option>
              ))}
            </select>
          </div>

          {/* Bureau Filter */}
          <div>
            <label className="block text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1">
              AICTE Bureau
            </label>
            <select
              value={filters.bureauId}
              onChange={(e) => handleFilterChange('bureauId', e.target.value)}
              className="w-full px-2.5 py-1.5 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
            >
              <option value="">All Bureaus</option>
              {bureaus.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.name} ({b.code})
                </option>
              ))}
            </select>
          </div>

          {/* Cell Filter */}
          <div>
            <label className="block text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1">
              Operational Cell
            </label>
            <select
              value={filters.cellId}
              onChange={(e) => handleFilterChange('cellId', e.target.value)}
              className="w-full px-2.5 py-1.5 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
            >
              <option value="">All Cells</option>
              {cells.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.code} - {c.name}
                </option>
              ))}
            </select>
          </div>

          {/* Status Filter */}
          <div>
            <label className="block text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1">
              Status
            </label>
            <select
              value={filters.status}
              onChange={(e) => handleFilterChange('status', e.target.value)}
              className="w-full px-2.5 py-1.5 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
            >
              <option value="">All Statuses</option>
              {STATUS_OPTIONS.map((st) => (
                <option key={st.value} value={st.value}>
                  {st.label}
                </option>
              ))}
            </select>
          </div>

          {/* Priority Filter */}
          <div>
            <label className="block text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1">
              Priority
            </label>
            <select
              value={filters.priority}
              onChange={(e) => handleFilterChange('priority', e.target.value)}
              className="w-full px-2.5 py-1.5 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
            >
              <option value="">All Priorities</option>
              {PRIORITY_OPTIONS.map((pr) => (
                <option key={pr.value} value={pr.value}>
                  {pr.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Top Dynamic Metrics Cards */}
      <MetricsGrid stats={stats} />

      {/* Bureau & Cell Distribution Breakdown */}
      <BureauDistributionChart
        bureauStats={stats?.bureauStats || []}
        cellStats={stats?.cellStats || []}
      />

      {/* Analytics Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <CategoryChart data={stats?.categoryStats || []} />
        <StatusChart data={stats?.statusStats || []} />
      </div>

      {/* Monthly Trend Area Chart */}
      <MonthlyTrendChart data={stats?.monthlyTrend || []} />

      {/* Split Grid: Institution Performance & Recent Complaints */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        <InstitutionPerformanceTable institutions={stats?.institutionStats || []} />
        <RecentComplaintsTable complaints={recentComplaints} viewBasePath="/admin/complaints" />
      </div>
    </div>
  );
};

export default AdminDashboardPage;
