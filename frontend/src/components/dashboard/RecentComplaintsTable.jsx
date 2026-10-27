import React from 'react';
import { Link } from 'react-router-dom';
import { ExternalLink, Clock } from 'lucide-react';
import StatusBadge from '../common/StatusBadge';
import PriorityBadge from '../common/PriorityBadge';
import { formatDate } from '../../utils/formatters';

const RecentComplaintsTable = ({ complaints = [], viewBasePath = '/admin/complaints' }) => {
  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="p-5 border-b border-slate-200 flex items-center justify-between">
        <div>
          <h3 className="text-sm font-semibold text-slate-900">Recent Complaints</h3>
          <p className="text-xs text-slate-500">Latest cases recorded across institutions</p>
        </div>
        <Link
          to={viewBasePath}
          className="text-xs font-semibold text-brand-600 hover:text-brand-700 inline-flex items-center"
        >
          View All <ExternalLink className="w-3.5 h-3.5 ml-1" />
        </Link>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-200">
            <tr>
              <th className="px-5 py-3">Case ID</th>
              <th className="px-5 py-3">Subject</th>
              <th className="px-5 py-3">Institution</th>
              <th className="px-5 py-3">Priority</th>
              <th className="px-5 py-3">Status</th>
              <th className="px-5 py-3">Date</th>
              <th className="px-5 py-3 text-right">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {complaints.length === 0 ? (
              <tr>
                <td colSpan="7" className="px-5 py-8 text-center text-slate-400">
                  No complaints found.
                </td>
              </tr>
            ) : (
              complaints.slice(0, 6).map((c) => (
                <tr key={c.id} className="hover:bg-slate-50/70 transition-colors">
                  <td className="px-5 py-3.5 font-mono font-semibold text-brand-700">
                    <Link
                      to={`${viewBasePath}/${c.complaintNumber}`}
                      className="hover:text-brand-900 hover:underline cursor-pointer"
                      title={`Open complaint dossier ${c.complaintNumber}`}
                    >
                      {c.complaintNumber}
                    </Link>
                  </td>
                  <td className="px-5 py-3.5">
                    <Link
                      to={`${viewBasePath}/${c.complaintNumber}`}
                      className="font-medium text-slate-800 hover:text-brand-600 transition-colors max-w-xs truncate block"
                      title={c.title}
                    >
                      {c.title}
                    </Link>
                  </td>
                  <td className="px-5 py-3.5 text-slate-600 max-w-[180px] truncate" title={c.institutionName}>
                    {c.institutionName}
                  </td>
                  <td className="px-5 py-3.5">
                    <PriorityBadge priority={c.priority} />
                  </td>
                  <td className="px-5 py-3.5">
                    <StatusBadge status={c.status} />
                  </td>
                  <td className="px-5 py-3.5 text-slate-500 whitespace-nowrap">
                    {formatDate(c.createdAt)}
                  </td>
                  <td className="px-5 py-3.5 text-right whitespace-nowrap">
                    <Link
                      to={`${viewBasePath}/${c.complaintNumber}`}
                      className="px-2.5 py-1 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-200 rounded-md transition-colors"
                    >
                      Details
                    </Link>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default RecentComplaintsTable;
