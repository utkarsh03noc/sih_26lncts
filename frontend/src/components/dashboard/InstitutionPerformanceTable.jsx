import React from 'react';
import { Link } from 'react-router-dom';
import { ExternalLink } from 'lucide-react';

const InstitutionPerformanceTable = ({ institutions = [] }) => {
  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="p-5 border-b border-slate-200 flex items-center justify-between">
        <div>
          <h3 className="text-sm font-semibold text-slate-900">Institution Performance & Compliance</h3>
          <p className="text-xs text-slate-500">Case volume, resolution ratios, and active escalations</p>
        </div>
        <Link
          to="/admin/institutions"
          className="text-xs font-semibold text-brand-600 hover:text-brand-700 inline-flex items-center"
        >
          View All <ExternalLink className="w-3.5 h-3.5 ml-1" />
        </Link>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-200">
            <tr>
              <th className="px-5 py-3">Institution Name</th>
              <th className="px-5 py-3 text-center">Total Complaints</th>
              <th className="px-5 py-3 text-center">Resolved</th>
              <th className="px-5 py-3 text-center">Escalated</th>
              <th className="px-5 py-3">Resolution Rate</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {institutions.slice(0, 6).map((inst) => {
              const rate = inst.resolutionRate || 0;
              const barColor =
                rate >= 70 ? 'bg-emerald-500' : rate >= 40 ? 'bg-amber-500' : 'bg-rose-500';

              return (
                <tr key={inst.institutionId} className="hover:bg-slate-50/70 transition-colors">
                  <td className="px-5 py-3.5 font-medium text-slate-800">
                    <Link
                      to={`/admin/institutions/${inst.institutionId}`}
                      className="hover:text-brand-600 transition-colors"
                    >
                      {inst.institutionName}
                    </Link>
                  </td>
                  <td className="px-5 py-3.5 text-center font-semibold text-slate-700">
                    {inst.totalComplaints}
                  </td>
                  <td className="px-5 py-3.5 text-center font-semibold text-emerald-600">
                    {inst.resolvedComplaints}
                  </td>
                  <td className="px-5 py-3.5 text-center font-semibold text-rose-600">
                    {inst.escalatedComplaints}
                  </td>
                  <td className="px-5 py-3.5">
                    <div className="flex items-center space-x-2">
                      <div className="flex-1 bg-slate-100 rounded-full h-2 max-w-[100px] overflow-hidden">
                        <div
                          className={`h-full rounded-full ${barColor}`}
                          style={{ width: `${Math.min(100, rate)}%` }}
                        />
                      </div>
                      <span className="font-semibold text-slate-700 w-10 text-right">{rate}%</span>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default InstitutionPerformanceTable;
