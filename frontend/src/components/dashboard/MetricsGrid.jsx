import React from 'react';
import {
  FileText,
  Clock,
  Activity,
  CheckCircle2,
  AlertTriangle,
  Flame,
  Award,
  Building2,
  CopyCheck,
} from 'lucide-react';
import StatCard from '../common/StatCard';

const MetricsGrid = ({ stats }) => {
  if (!stats) return null;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {stats.totalInstitutions !== undefined && (
        <StatCard
          title="Monitored Institutions"
          value={stats.totalInstitutions || 0}
          subtitle="AICTE approved technical campuses"
          icon={Building2}
          color="indigo"
        />
      )}
      <StatCard
        title="Total Complaints"
        value={stats.totalComplaints || 0}
        subtitle="Registered cases across all bureaus"
        icon={FileText}
        color="blue"
      />
      <StatCard
        title="Pending / Under Review"
        value={stats.pendingComplaints || 0}
        subtitle="Awaiting nodal verification"
        icon={Clock}
        color="amber"
      />
      <StatCard
        title="In Progress"
        value={stats.inProgressComplaints || 0}
        subtitle="Active inquiry & remediation"
        icon={Activity}
        color="sky"
      />
      <StatCard
        title="Resolved Cases"
        value={stats.resolvedComplaints || 0}
        subtitle={`Closed: ${stats.closedComplaints || 0}`}
        icon={CheckCircle2}
        color="emerald"
      />
      <StatCard
        title="Escalated Cases"
        value={stats.escalatedComplaints || 0}
        subtitle="Requires central oversight"
        icon={Flame}
        color="rose"
      />
      <StatCard
        title="Overdue (SLA Breached)"
        value={stats.overdueComplaints || 0}
        subtitle="Target resolution date exceeded"
        icon={AlertTriangle}
        color="rose"
      />
      <StatCard
        title="Duplicate Alerts"
        value={stats.potentialDuplicateCount || 0}
        subtitle="Flagged by similarity engine"
        icon={CopyCheck}
        color="purple"
      />
    </div>
  );
};

export default MetricsGrid;
