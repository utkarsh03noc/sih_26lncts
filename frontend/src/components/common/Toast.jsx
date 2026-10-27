import React from 'react';
import { CheckCircle2, AlertTriangle, AlertCircle, Info, X } from 'lucide-react';

const Toast = ({ message, type = 'info', onClose }) => {
  const configs = {
    success: {
      icon: CheckCircle2,
      bg: 'bg-emerald-50 border-emerald-200 text-emerald-800',
      iconColor: 'text-emerald-500',
    },
    error: {
      icon: AlertCircle,
      bg: 'bg-rose-50 border-rose-200 text-rose-800',
      iconColor: 'text-rose-500',
    },
    warning: {
      icon: AlertTriangle,
      bg: 'bg-amber-50 border-amber-200 text-amber-800',
      iconColor: 'text-amber-500',
    },
    info: {
      icon: Info,
      bg: 'bg-blue-50 border-blue-200 text-blue-800',
      iconColor: 'text-blue-500',
    },
  };

  const config = configs[type] || configs.info;
  const IconComponent = config.icon;

  return (
    <div className={`flex items-start gap-3 p-3.5 rounded-lg border shadow-lg transition-all transform duration-200 ${config.bg}`}>
      <IconComponent className={`w-5 h-5 flex-shrink-0 mt-0.5 ${config.iconColor}`} />
      <div className="flex-1 text-sm font-medium leading-tight">
        {message}
      </div>
      <button
        onClick={onClose}
        className="text-slate-400 hover:text-slate-600 transition-colors p-0.5"
      >
        <X className="w-4 h-4" />
      </button>
    </div>
  );
};

export default Toast;
