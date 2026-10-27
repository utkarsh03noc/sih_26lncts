import React from 'react';
import { Network, Layers, Building2, CheckCircle2, Clock } from 'lucide-react';

const BureauDistributionChart = ({ bureauStats = [], cellStats = [] }) => {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Bureau Distribution Card */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
        <div className="flex items-center justify-between pb-3 mb-4 border-b border-slate-200">
          <div className="flex items-center space-x-2">
            <Layers className="w-4 h-4 text-blue-600" />
            <h3 className="text-sm font-bold text-slate-900">
              Bureau-Wise Case Distribution
            </h3>
          </div>
          <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
            Central Bureaus
          </span>
        </div>

        {bureauStats.length === 0 ? (
          <div className="text-xs text-slate-400 italic py-6 text-center">
            No bureau case statistics available.
          </div>
        ) : (
          <div className="space-y-4">
            {bureauStats.map((b) => {
              const total = b.totalCount || 1;
              const resPercent = Math.round(((b.resolvedCount || 0) / total) * 100);
              return (
                <div key={b.bureauId} className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs">
                    <div className="flex items-center space-x-1.5">
                      <span className="font-bold text-blue-700 font-mono bg-blue-50 px-1.5 py-0.2 rounded border border-blue-200 text-[10px]">
                        {b.bureauCode}
                      </span>
                      <span className="font-semibold text-slate-800">{b.bureauName}</span>
                    </div>
                    <span className="font-bold text-slate-900">{b.totalCount} cases</span>
                  </div>

                  {/* Dual progress bar */}
                  <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden flex">
                    <div
                      className="bg-emerald-500 h-2"
                      style={{ width: `${resPercent}%` }}
                      title={`Resolved: ${b.resolvedCount}`}
                    />
                    <div
                      className="bg-amber-400 h-2"
                      style={{ width: `${100 - resPercent}%` }}
                      title={`Pending: ${b.pendingCount}`}
                    />
                  </div>

                  <div className="flex items-center justify-between text-[10px] text-slate-500">
                    <span className="text-emerald-700 font-medium">
                      ✓ {b.resolvedCount} Resolved ({resPercent}%)
                    </span>
                    <span className="text-amber-700 font-medium">
                      ⏳ {b.pendingCount} Pending Action
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Cell Distribution Card */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
        <div className="flex items-center justify-between pb-3 mb-4 border-b border-slate-200">
          <div className="flex items-center space-x-2">
            <Network className="w-4 h-4 text-indigo-600" />
            <h3 className="text-sm font-bold text-slate-900">
              Specialized Grievance Cell Load
            </h3>
          </div>
          <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
            Operational Cells
          </span>
        </div>

        {cellStats.length === 0 ? (
          <div className="text-xs text-slate-400 italic py-6 text-center">
            No cell case distribution available.
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {cellStats.map((c) => (
              <div
                key={c.cellId}
                className="p-3 bg-slate-50 rounded-lg border border-slate-200 flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-xs font-bold text-indigo-700 bg-indigo-50 px-1.5 py-0.2 rounded border border-indigo-200">
                      {c.cellCode}
                    </span>
                    <span className="text-sm font-bold text-slate-900">
                      {c.totalCount}
                    </span>
                  </div>
                  <div className="text-xs font-semibold text-slate-800 mt-1 line-clamp-1">
                    {c.cellName}
                  </div>
                </div>
                <div className="text-[10px] text-slate-400 mt-2">
                  Complaints assigned for resolution
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default BureauDistributionChart;
