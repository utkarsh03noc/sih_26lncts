import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const Pagination = ({
  currentPage = 0,
  totalPages = 1,
  totalElements = 0,
  pageSize = 10,
  onPageChange,
}) => {
  if (totalPages <= 1) return null;

  const startIdx = currentPage * pageSize + 1;
  const endIdx = Math.min((currentPage + 1) * pageSize, totalElements);

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-4 px-4 py-3 bg-white border-t border-slate-200">
      <div className="text-xs text-slate-500">
        Showing <span className="font-semibold text-slate-700">{startIdx}</span> to{' '}
        <span className="font-semibold text-slate-700">{endIdx}</span> of{' '}
        <span className="font-semibold text-slate-700">{totalElements}</span> results
      </div>
      <div className="flex items-center space-x-1">
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 0}
          className="inline-flex items-center px-3 py-1.5 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-md hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          <ChevronLeft className="w-4 h-4 mr-1" /> Previous
        </button>
        <span className="px-3 py-1 text-xs text-slate-600 font-medium">
          Page {currentPage + 1} of {totalPages}
        </span>
        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage >= totalPages - 1}
          className="inline-flex items-center px-3 py-1.5 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-md hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          Next <ChevronRight className="w-4 h-4 ml-1" />
        </button>
      </div>
    </div>
  );
};

export default Pagination;
