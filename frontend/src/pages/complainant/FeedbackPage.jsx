import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import { formatDate } from '../../utils/formatters';
import { Star, MessageSquare } from 'lucide-react';

const FeedbackPage = () => {
  const [resolvedComplaints, setResolvedComplaints] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchResolved();
  }, []);

  const fetchResolved = async () => {
    try {
      setLoading(true);
      const res = await complaintService.getComplaints({
        status: 'RESOLVED',
        page: 0,
        size: 20,
      });
      setResolvedComplaints(res.data?.content || []);
    } catch (err) {
      console.error('Failed to load resolved cases for feedback:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">
          Resolution Feedback & Citizen Rating
        </h1>
        <p className="text-xs text-slate-500 mt-1">
          Review your resolved complaints and submit citizen satisfaction ratings for institutional performance evaluation
        </p>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <LoadingSpinner text="Checking resolved cases..." />
        ) : resolvedComplaints.length === 0 ? (
          <EmptyState
            icon={MessageSquare}
            title="No resolved cases pending feedback"
            description="When your grievances are resolved by the institutions, they will appear here for you to submit satisfaction ratings."
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-200">
                <tr>
                  <th className="px-5 py-3">Case ID</th>
                  <th className="px-5 py-3">Subject</th>
                  <th className="px-5 py-3">Institution</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3">Resolved Date</th>
                  <th className="px-5 py-3 text-right">Feedback</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {resolvedComplaints.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="px-5 py-3.5 font-mono font-semibold text-brand-700">
                      {c.complaintNumber}
                    </td>
                    <td className="px-5 py-3.5 font-medium text-slate-900 max-w-xs truncate" title={c.title}>
                      {c.title}
                    </td>
                    <td className="px-5 py-3.5 text-slate-600 max-w-[180px] truncate">
                      {c.institutionName}
                    </td>
                    <td className="px-5 py-3.5">
                      <StatusBadge status={c.status} />
                    </td>
                    <td className="px-5 py-3.5 text-slate-500 whitespace-nowrap">
                      {formatDate(c.resolvedAt || c.updatedAt)}
                    </td>
                    <td className="px-5 py-3.5 text-right whitespace-nowrap">
                      <Link
                        to={`/complainant/complaints/${c.id}`}
                        className="inline-flex items-center px-3 py-1 text-xs font-semibold text-amber-800 bg-amber-50 hover:bg-amber-100 border border-amber-200 rounded-md transition-colors"
                      >
                        <Star className="w-3.5 h-3.5 mr-1 text-amber-500 fill-amber-500" />
                        Rate / Review →
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default FeedbackPage;
