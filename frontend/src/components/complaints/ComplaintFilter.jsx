import React from 'react';
import { Search, RotateCcw } from 'lucide-react';
import { CATEGORIES, COMPLAINT_STATUS, PRIORITIES } from '../../utils/constants';

const ComplaintFilter = ({ filters, onFilterChange, onReset, bureaus = [] }) => {
  return (
    <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm mb-6">
      <div className={`grid grid-cols-1 sm:grid-cols-2 ${bureaus.length > 0 ? 'lg:grid-cols-6' : 'lg:grid-cols-5'} gap-3`}>
        {/* Search */}
        <div className="relative">
          <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
          <input
            type="text"
            placeholder="Search ID, title, description..."
            value={filters.search || ''}
            onChange={(e) => onFilterChange('search', e.target.value)}
            className="w-full pl-9 pr-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-brand-500"
          />
        </div>

        {/* Bureau (if bureaus provided) */}
        {bureaus.length > 0 && (
          <div>
            <select
              value={filters.bureauId || ''}
              onChange={(e) => onFilterChange('bureauId', e.target.value)}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-brand-500 bg-white"
            >
              <option value="">All AICTE Bureaus</option>
              {bureaus.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.name} ({b.code})
                </option>
              ))}
            </select>
          </div>
        )}

        {/* Category */}
        <div>
          <select
            value={filters.category || ''}
            onChange={(e) => onFilterChange('category', e.target.value)}
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-brand-500 bg-white"
          >
            <option value="">All Categories</option>
            {CATEGORIES.map((cat) => (
              <option key={cat.value} value={cat.value}>
                {cat.label}
              </option>
            ))}
          </select>
        </div>

        {/* Status */}
        <div>
          <select
            value={filters.status || ''}
            onChange={(e) => onFilterChange('status', e.target.value)}
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-brand-500 bg-white"
          >
            <option value="">All Statuses</option>
            {Object.entries(COMPLAINT_STATUS).map(([key, item]) => (
              <option key={key} value={key}>
                {item.label}
              </option>
            ))}
          </select>
        </div>

        {/* Priority */}
        <div>
          <select
            value={filters.priority || ''}
            onChange={(e) => onFilterChange('priority', e.target.value)}
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-brand-500 bg-white"
          >
            <option value="">All Priorities</option>
            {Object.entries(PRIORITIES).map(([key, item]) => (
              <option key={key} value={key}>
                {item.label}
              </option>
            ))}
          </select>
        </div>

        {/* Reset */}
        <div>
          <button
            type="button"
            onClick={onReset}
            className="w-full inline-flex items-center justify-center px-4 py-2 text-xs font-semibold text-slate-700 bg-slate-100 hover:bg-slate-200 border border-slate-300 rounded-lg transition-colors"
          >
            <RotateCcw className="w-3.5 h-3.5 mr-1.5" />
            Reset Filters
          </button>
        </div>
      </div>
    </div>
  );
};

export default ComplaintFilter;
