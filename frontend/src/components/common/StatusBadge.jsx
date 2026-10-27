import React from 'react';
import { COMPLAINT_STATUS } from '../../utils/constants';

const StatusBadge = ({ status }) => {
  const config = COMPLAINT_STATUS[status] || {
    label: status || 'Unknown',
    color: 'bg-slate-100 text-slate-800 border-slate-200',
  };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${config.color}`}>
      <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-70"></span>
      {config.label}
    </span>
  );
};

export default StatusBadge;
