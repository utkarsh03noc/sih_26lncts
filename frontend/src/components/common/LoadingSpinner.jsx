import React from 'react';
import { Loader2 } from 'lucide-react';

const LoadingSpinner = ({ text = 'Loading data...', size = 'md' }) => {
  const sizeClass = size === 'sm' ? 'w-4 h-4' : size === 'lg' ? 'w-8 h-8' : 'w-6 h-6';

  return (
    <div className="flex flex-col items-center justify-center p-8 space-y-3">
      <Loader2 className={`${sizeClass} animate-spin text-brand-600`} />
      {text && <p className="text-xs text-slate-500 font-medium">{text}</p>}
    </div>
  );
};

export default LoadingSpinner;
