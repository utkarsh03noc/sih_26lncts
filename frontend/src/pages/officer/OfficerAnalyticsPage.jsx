import React, { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { dashboardService } from '../../services/dashboardService';
import CategoryChart from '../../components/dashboard/CategoryChart';
import StatusChart from '../../components/dashboard/StatusChart';
import MonthlyTrendChart from '../../components/dashboard/MonthlyTrendChart';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const OfficerAnalyticsPage = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAnalytics();
  }, [user]);

  const loadAnalytics = async () => {
    try {
      setLoading(true);
      const res = await dashboardService.getInstitutionStats(user?.institutionId);
      setStats(res.data);
    } catch (err) {
      console.error('Failed to load institution analytics:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading || !stats) {
    return <LoadingSpinner text="Computing institution grievance metrics..." size="lg" />;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">
          Institution Grievance Analytics
        </h1>
        <p className="text-xs text-slate-500 mt-1">
          Performance metrics, category hotspots, and resolution efficiency for {user?.institutionName}
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <CategoryChart data={stats.categoryStats || []} />
        <StatusChart data={stats.statusStats || []} />
      </div>

      <MonthlyTrendChart data={stats.monthlyTrend || []} />
    </div>
  );
};

export default OfficerAnalyticsPage;
