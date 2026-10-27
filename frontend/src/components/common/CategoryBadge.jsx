import React from 'react';

const CategoryBadge = ({ category, displayName }) => {
  return (
    <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-slate-100 text-slate-700 border border-slate-200">
      {displayName || category || 'General'}
    </span>
  );
};

export default CategoryBadge;
