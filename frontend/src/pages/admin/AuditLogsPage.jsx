import React, { useState, useEffect } from 'react';
import { auditService } from '../../services/auditService';
import Pagination from '../../components/common/Pagination';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import { formatDateTime } from '../../utils/formatters';
import { History, ShieldCheck, User } from 'lucide-react';

const AuditLogsPage = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pageData, setPageData] = useState({
    pageNumber: 0,
    pageSize: 15,
    totalElements: 0,
    totalPages: 0,
  });

  useEffect(() => {
    fetchLogs();
  }, [pageData.pageNumber]);

  const fetchLogs = async () => {
    try {
      setLoading(true);
      const res = await auditService.getAuditLogs({
        page: pageData.pageNumber,
        size: pageData.pageSize,
        sortBy: 'timestamp',
        sortDir: 'desc',
      });
      if (res.success && res.data) {
        setLogs(res.data.content || []);
        setPageData((prev) => ({
          ...prev,
          totalElements: res.data.totalElements,
          totalPages: res.data.totalPages,
        }));
      }
    } catch (err) {
      console.error('Failed to load audit logs:', err);
    } finally {
      setLoading(false);
    }
  };

  const getActionBadgeColor = (action) => {
    if (action.includes('CREATED') || action.includes('REGISTERED')) {
      return 'bg-blue-50 text-blue-700 border-blue-200';
    }
    if (action.includes('STATUS') || action.includes('UPDATED')) {
      return 'bg-amber-50 text-amber-700 border-amber-200';
    }
    if (action.includes('ASSIGNED')) {
      return 'bg-indigo-50 text-indigo-700 border-indigo-200';
    }
    if (action.includes('REOPENED')) {
      return 'bg-orange-50 text-orange-700 border-orange-200';
    }
    if (action.includes('LOGIN')) {
      return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    }
    return 'bg-slate-50 text-slate-700 border-slate-200';
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">
          System Security & Audit Trail
        </h1>
        <p className="text-xs text-slate-500 mt-1">
          Cryptographically immutable audit records of all portal actions, assignments, and resolution updates
        </p>
      </div>

      {/* Logs Table Card */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <LoadingSpinner text="Retrieving audit records..." />
        ) : logs.length === 0 ? (
          <EmptyState
            icon={History}
            title="No audit logs recorded"
            description="Audit events will automatically appear here as operations are performed."
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-200">
                <tr>
                  <th className="px-5 py-3">Timestamp</th>
                  <th className="px-5 py-3">Action</th>
                  <th className="px-5 py-3">Performed By</th>
                  <th className="px-5 py-3">Entity</th>
                  <th className="px-5 py-3">Operation Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="px-5 py-3.5 whitespace-nowrap text-slate-500 font-mono text-[11px]">
                      {formatDateTime(log.timestamp)}
                    </td>
                    <td className="px-5 py-3.5 whitespace-nowrap">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold border ${getActionBadgeColor(
                          log.action
                        )}`}
                      >
                        {log.action}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 whitespace-nowrap">
                      <div className="font-semibold text-slate-800">
                        {log.performedByName || 'System Process'}
                      </div>
                      <div className="text-[10px] text-slate-400">
                        {log.performedByRole || 'SYSTEM'}
                      </div>
                    </td>
                    <td className="px-5 py-3.5 whitespace-nowrap font-medium text-slate-700">
                      {log.entityName} #{log.entityId}
                    </td>
                    <td className="px-5 py-3.5 text-slate-600 max-w-md truncate" title={log.details}>
                      {log.details}
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

export default AuditLogsPage;
