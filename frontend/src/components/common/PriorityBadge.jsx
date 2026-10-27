import React from 'react';
import { PRIORITIES } from '../../utils/constants';

const PriorityBadge = ({ priority }) => {
  const config = PRIORITIES[priority] || {
    label: priority || 'Normal',
    color: 'bg-slate-100 text-slate-700 border-slate-300',
  };

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${config.color}`}>
      {config.label}
    </span>
  );
};

export default PriorityBadge;
