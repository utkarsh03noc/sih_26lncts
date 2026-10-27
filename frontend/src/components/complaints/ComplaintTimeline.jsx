import React from 'react';
import { CheckCircle2, Clock, ArrowRight, User } from 'lucide-react';
import StatusBadge from '../common/StatusBadge';
import { formatDateTime } from '../../utils/formatters';

const ComplaintTimeline = ({ updates = [] }) => {
  if (!updates || updates.length === 0) {
    return (
      <div className="p-6 text-center text-xs text-slate-400 bg-slate-50 rounded-lg border border-slate-200">
        No progress updates recorded yet.
      </div>
    );
  }

  return (
    <div className="relative pl-6 space-y-6 before:absolute before:left-2 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-200">
      {updates.map((update, index) => (
        <div key={update.id || index} className="relative">
          {/* Node Dot */}
          <div className="absolute -left-6 top-1 w-4 h-4 rounded-full bg-white border-2 border-brand-600 flex items-center justify-center">
            <div className="w-1.5 h-1.5 rounded-full bg-brand-600" />
          </div>

          <div className="bg-white p-4 rounded-lg border border-slate-200 shadow-sm space-y-2">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div className="flex items-center space-x-2">
                {update.previousStatus && (
                  <>
                    <StatusBadge status={update.previousStatus} />
                    <ArrowRight className="w-3.5 h-3.5 text-slate-400" />
                  </>
                )}
                <StatusBadge status={update.newStatus} />
              </div>
              <span className="text-xs text-slate-400 font-medium">
                {formatDateTime(update.createdAt)}
              </span>
            </div>

            <p className="text-xs text-slate-700 leading-relaxed bg-slate-50 p-2.5 rounded border border-slate-100">
              {update.remarks}
            </p>

            <div className="flex items-center text-[11px] text-slate-500 pt-1">
              <User className="w-3.5 h-3.5 mr-1 text-slate-400" />
              <span>
                Updated by <strong className="text-slate-700">{update.updatedByName || 'System'}</strong>{' '}
                ({update.updatedByRole || 'SYSTEM'})
              </span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default ComplaintTimeline;
