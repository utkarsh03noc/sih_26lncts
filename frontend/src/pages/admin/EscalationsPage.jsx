import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import StatusBadge from '../../components/common/StatusBadge';
import PriorityBadge from '../../components/common/PriorityBadge';
import CategoryBadge from '../../components/common/CategoryBadge';
import Pagination from '../../components/common/Pagination';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import { formatDate } from '../../utils/formatters';
import { AlertTriangle, Flame, ShieldAlert } from 'lucide-react';

const EscalationsPage = () => {
  const [escalations, setEscalations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pageData, setPageData] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
  });

  useEffect(() => {
    fetchEscalations();
  }, [pageData.pageNumber]);

  const fetchEscalations = async () => {
    try {
      setLoading(true);
      const res = await complaintService.getEscalations({
        page: pageData.pageNumber,
        size: pageData.pageSize,
      });
      if (res.success && res.data) {
        setEscalations(res.data.content || []);
        setPageData((prev) => ({
          ...prev,
          totalElements: res.data.totalElements,
          totalPages: res.data.totalPages,
        }));
      }
    } catch (err) {
      console.error('Error fetching escalations:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="bg-rose-50 border border-rose-200 rounded-xl p-5 shadow-sm">
        <div className="flex items-start space-x-3">
          <div className="p-2.5 bg-rose-100 text-rose-700 rounded-lg flex-shrink-0">
            <Flame className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-lg font-bold text-rose-950 tracking-tight">
              Escalation Desk & SLA Violation Monitoring
            </h1>
            <p className="text-xs text-rose-700 mt-1">
              Active grievances that have either breached the statutory redressal deadline or were formally escalated by oversight committees.
            </p>
          </div>
        </div>
      </div>

      {/* Escalated Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="p-4 border-b border-slate-200 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-slate-900">
            Critical Cases Requiring Urgent Intervention ({pageData.totalElements})
          </h3>
        </div>

        {loading ? (
          <LoadingSpinner text="Scanning escalations..." />
        ) : escalations.length === 0 ? (
          <EmptyState
            icon={ShieldAlert}
            title="No Active Escalations"
            description="All complaints are progressing within their allocated SLA deadlines."
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-200">
                <tr>
                  <th className="px-5 py-3">Case ID</th>
                  <th className="px-5 py-3">Subject</th>
                  <th className="px-5 py-3">Institution</th>
                  <th className="px-5 py-3">Priority</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-rose-700">SLA Due Date</th>
                  <th className="px-5 py-3 text-right">Intervention</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {escalations.map((c) => (
                  <tr key={c.id} className="hover:bg-rose-50/40 transition-colors">
                    <td className="px-5 py-3.5 font-mono font-semibold text-brand-700 whitespace-nowrap">
                      <Link
                        to={`/admin/complaints/${c.complaintNumber}`}
                        className="hover:text-brand-900 hover:underline cursor-pointer"
                        title={`Open escalation dossier ${c.complaintNumber}`}
                      >
                        {c.complaintNumber}
                      </Link>
                    </td>
                    <td className="px-5 py-3.5">
                      <Link
                        to={`/admin/complaints/${c.complaintNumber}`}
                        className="font-semibold text-slate-900 hover:text-brand-600 transition-colors max-w-xs truncate block"
                        title={c.title}
                      >
                        {c.title}
                      </Link>
                      <div className="text-[11px] text-slate-500 mt-0.5">Lodged by: {c.complainantName}</div>
                    </td>
                    <td className="px-5 py-3.5 text-slate-700 max-w-[180px] truncate" title={c.institutionName}>
                      {c.institutionName}
                    </td>
                    <td className="px-5 py-3.5">
                      <PriorityBadge priority={c.priority} />
                    </td>
                    <td className="px-5 py-3.5">
                      <StatusBadge status={c.status} />
                    </td>
                    <td className="px-5 py-3.5 font-semibold text-rose-600 whitespace-nowrap">
                      <div className="flex items-center space-x-1">
                        <AlertTriangle className="w-3.5 h-3.5 text-rose-600" />
                        <span>{formatDate(c.dueDate)}</span>
                      </div>
                    </td>
                    <td className="px-5 py-3.5 text-right whitespace-nowrap">
                      <Link
                        to={`/admin/complaints/${c.complaintNumber}`}
                        className="px-3 py-1 text-xs font-semibold text-rose-700 bg-rose-50 hover:bg-rose-100 border border-rose-200 rounded-md transition-colors"
                      >
                        Intervene →
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination
          currentPage={pageData.pageNumber}
          totalPages={pageData.totalPages}
          totalElements={pageData.totalElements}
          pageSize={pageData.pageSize}
          onPageChange={(newPage) => setPageData((prev) => ({ ...prev, pageNumber: newPage }))}
        />
      </div>
    </div>
  );
};

export default EscalationsPage;
