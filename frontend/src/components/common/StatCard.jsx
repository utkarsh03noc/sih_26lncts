import React from 'react';

const StatCard = ({ title, value, subtitle, icon: Icon, color = 'blue', onClick }) => {
  const colorStyles = {
    blue: 'bg-blue-50 text-blue-600 border-blue-100',
    amber: 'bg-amber-50 text-amber-600 border-amber-100',
    sky: 'bg-sky-50 text-sky-600 border-sky-100',
    emerald: 'bg-emerald-50 text-emerald-600 border-emerald-100',
    rose: 'bg-rose-50 text-rose-600 border-rose-100',
    purple: 'bg-purple-50 text-purple-600 border-purple-100',
    slate: 'bg-slate-50 text-slate-600 border-slate-200',
  };

  const style = colorStyles[color] || colorStyles.blue;

  return (
    <div
      onClick={onClick}
      className={`bg-white rounded-xl border border-slate-200 p-5 shadow-sm transition-all hover:shadow-md ${
        onClick ? 'cursor-pointer hover:border-slate-300' : ''
      }`}
    >
      <div className="flex items-center justify-between">
        <div className="space-y-1">
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">{title}</p>
          <p className="text-2xl font-bold tracking-tight text-slate-900">{value}</p>
          {subtitle && <p className="text-xs text-slate-500">{subtitle}</p>}
        </div>
        {Icon && (
          <div className={`p-3 rounded-xl border ${style}`}>
            <Icon className="w-6 h-6" />
          </div>
        )}
      </div>
    </div>
  );
};

export default StatCard;
