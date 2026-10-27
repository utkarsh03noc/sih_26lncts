import React from 'react';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { COMPLAINT_STATUS } from '../../utils/constants';

const StatusChart = ({ data = [] }) => {
  const statusColors = {
    SUBMITTED: '#3b82f6',
    ASSIGNED: '#6366f1',
    UNDER_REVIEW: '#f59e0b',
    IN_PROGRESS: '#0ea5e9',
    RESOLVED: '#10b981',
    CLOSED: '#64748b',
    REOPENED: '#f97316',
    ESCALATED: '#ef4444',
  };

  const chartData = data
    .filter((item) => item.count > 0)
    .map((item) => ({
      name: COMPLAINT_STATUS[item.status]?.label || item.status,
      statusKey: item.status,
      value: item.count,
    }));

  return (
    <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
      <div className="mb-4">
        <h3 className="text-sm font-semibold text-slate-900">Complaints by Status</h3>
        <p className="text-xs text-slate-500">Current workflow breakdown</p>
      </div>

      <div className="h-64 w-full">
        {chartData.length === 0 ? (
          <div className="h-full flex items-center justify-center text-xs text-slate-400">
            No status data available
          </div>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={chartData}
                innerRadius={55}
                outerRadius={80}
                paddingAngle={3}
                dataKey="value"
              >
                {chartData.map((entry, index) => (
                  <Cell
                    key={`cell-${index}`}
                    fill={statusColors[entry.statusKey] || '#94a3b8'}
                  />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: '#0f172a',
                  borderColor: '#1e293b',
                  borderRadius: '0.5rem',
                  color: '#fff',
                  fontSize: '12px',
                }}
              />
              <Legend
                verticalAlign="bottom"
                height={36}
                wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }}
              />
            </PieChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
};

export default StatusChart;
