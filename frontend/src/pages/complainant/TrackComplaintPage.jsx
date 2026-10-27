import React, { useState } from 'react';
import { complaintService } from '../../services/complaintService';
import StatusBadge from '../../components/common/StatusBadge';
import PriorityBadge from '../../components/common/PriorityBadge';
import CategoryBadge from '../../components/common/CategoryBadge';
import ComplaintTimeline from '../../components/complaints/ComplaintTimeline';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { formatDate, formatDateTime } from '../../utils/formatters';
import { Search, Building, ShieldCheck, AlertCircle } from 'lucide-react';

const TrackComplaintPage = () => {
  const [complaintNumber, setComplaintNumber] = useState('');
  const [complaint, setComplaint] = useState(null);
  const [updates, setUpdates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!complaintNumber.trim()) return;

    try {
      setLoading(true);
      setError('');
      setComplaint(null);
      setUpdates([]);

      const cleanNumber = complaintNumber.trim().toUpperCase();
      const compRes = await complaintService.trackPublicComplaint(cleanNumber);
      if (compRes.success && compRes.data) {
        setComplaint(compRes.data);
        const updatesRes = await complaintService.getUpdates(compRes.data.id).catch(() => ({ data: [] }));
        setUpdates(updatesRes.data || []);
      }
    } catch (err) {
      setError(
        err.response?.status === 404
          ? `No complaint record found matching case number "${complaintNumber.toUpperCase()}". Please verify the ID format.`
          : 'Unable to track grievance at this time. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header Banner */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 sm:p-8 shadow-sm text-center">
        <div className="w-12 h-12 bg-blue-50 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-3">
          <Search className="w-6 h-6" />
        </div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">
          Public Grievance Status Tracker
        </h1>
        <p className="text-xs text-slate-500 max-w-lg mx-auto mt-1">
          Enter your unique complaint reference number to inspect live investigation progress and status history
        </p>

        {/* Input Form */}
        <form onSubmit={handleSearch} className="mt-6 max-w-md mx-auto flex gap-2">
          <div className="relative flex-1">
            <input
              type="text"
              value={complaintNumber}
              onChange={(e) => setComplaintNumber(e.target.value)}
              placeholder="e.g. CMP-2026-1001"
              required
              className="w-full px-4 py-2.5 text-xs font-mono uppercase tracking-wider border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="px-5 py-2.5 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors disabled:opacity-50"
          >
            {loading ? 'Searching...' : 'Track Status'}
          </button>
        </form>

        <p className="text-[11px] text-slate-400 mt-2">
          Demo sample cases to test: <code className="text-brand-600 font-bold">CMP-2026-1001</code>,{' '}
          <code className="text-brand-600 font-bold">CMP-2026-1002</code>,{' '}
          <code className="text-brand-600 font-bold">CMP-2026-1003</code>
        </p>
      </div>

      {/* Error state */}
      {error && (
        <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs font-medium flex items-center space-x-2">
          <AlertCircle className="w-4 h-4 text-rose-500 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Loading */}
      {loading && <LoadingSpinner text="Locating case dossier..." />}

      {/* Result Dossier */}
      {complaint && (
        <div className="space-y-6">
          <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
            <div className="flex flex-wrap items-center justify-between gap-3 pb-4 border-b border-slate-200">
              <div className="space-y-1">
                <span className="font-mono text-xs font-bold text-brand-600 bg-brand-50 px-2.5 py-1 rounded border border-brand-100">
                  {complaint.complaintNumber}
                </span>
                <h2 className="text-base font-bold text-slate-900 mt-2">{complaint.title}</h2>
              </div>
              <div className="flex items-center space-x-2">
                <PriorityBadge priority={complaint.priority} />
                <StatusBadge status={complaint.status} />
              </div>
            </div>

            <div className="mt-4">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">
                Grievance Summary
              </h3>
              <p className="text-xs text-slate-700 bg-slate-50 p-3.5 rounded-lg border border-slate-100 leading-relaxed whitespace-pre-line">
                {complaint.description}
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-4 pt-4 border-t border-slate-100 text-xs">
              <div>
                <span className="text-slate-400 block font-medium">Institution</span>
                <span className="font-semibold text-slate-800 mt-0.5 block">{complaint.institutionName}</span>
              </div>
              <div>
                <span className="text-slate-400 block font-medium">Lodged On</span>
                <span className="font-semibold text-slate-800 mt-0.5 block">{formatDate(complaint.createdAt)}</span>
              </div>
              <div>
                <span className="text-slate-400 block font-medium">Target Redressal Date</span>
                <span className={`font-semibold mt-0.5 block ${complaint.overdue ? 'text-rose-600' : 'text-slate-800'}`}>
                  {formatDate(complaint.dueDate)}
                </span>
              </div>
            </div>
          </div>

          {/* Timeline */}
          <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
            <h3 className="text-sm font-bold text-slate-900 mb-4 pb-2 border-b border-slate-200">
              Investigation Progress & Audit Timeline
            </h3>
            <ComplaintTimeline updates={updates} />
          </div>
        </div>
      )}
    </div>
  );
};

export default TrackComplaintPage;
