import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { institutionService } from '../../services/institutionService';
import { complaintService } from '../../services/complaintService';
import { userService } from '../../services/userService';
import StatusBadge from '../../components/common/StatusBadge';
import PriorityBadge from '../../components/common/PriorityBadge';
import CategoryBadge from '../../components/common/CategoryBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import Pagination from '../../components/common/Pagination';
import EmptyState from '../../components/common/EmptyState';
import { formatDate, formatDateTime } from '../../utils/formatters';
import {
  ArrowLeft,
  Building2,
  Mail,
  Phone,
  MapPin,
  Shield,
  FileText,
  UserCheck,
  AlertTriangle,
  History,
  Layers,
  Network,
  ExternalLink,
} from 'lucide-react';

const InstitutionDetailPage = () => {
  const { id } = useParams();

  const [institution, setInstitution] = useState(null);
  const [complaints, setComplaints] = useState([]);
  const [officers, setOfficers] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('complaints'); // 'complaints', 'officers', 'audit'

  const [pageData, setPageData] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
  });

  useEffect(() => {
    loadInstitutionDetails();
  }, [id, pageData.pageNumber]);

  useEffect(() => {
    if (activeTab === 'audit') {
      loadAuditLogs();
    }
  }, [activeTab, id]);

  const loadInstitutionDetails = async () => {
    try {
      setLoading(true);
      const [instRes, compRes, offRes] = await Promise.all([
        institutionService.getInstitutionById(id),
        complaintService.getComplaints({
          institutionId: id,
          page: pageData.pageNumber,
          size: pageData.pageSize,
        }),
        userService.getOfficersByInstitution(id),
      ]);
      setInstitution(instRes.data);
      setComplaints(compRes.data?.content || []);
      setPageData((prev) => ({
        ...prev,
        totalElements: compRes.data?.totalElements || 0,
        totalPages: compRes.data?.totalPages || 0,
      }));
      setOfficers(offRes.data || []);
    } catch (err) {
      console.error('Failed to load institution details:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadAuditLogs = async () => {
    try {
      const res = await institutionService.getInstitutionAuditLogs(id, { page: 0, size: 20 });
      if (res.success && res.data) {
        setAuditLogs(res.data.content || []);
      }
    } catch (err) {
      console.error('Failed to load institution audit trail:', err);
    }
  };

  if (loading && !institution) {
    return <LoadingSpinner text="Retrieving institution dossier..." size="lg" />;
  }

  return (
    <div className="space-y-6">
      <Link
        to="/admin/institutions"
        className="inline-flex items-center text-xs font-semibold text-slate-600 hover:text-slate-900 transition-colors"
      >
        <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to Institutions Monitoring
      </Link>

      {/* Overview Dossier Card */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-slate-200">
          <div>
            <div className="flex items-center space-x-2">
              <span className="font-mono text-xs font-bold text-blue-700 bg-blue-50 px-2 py-0.5 rounded border border-blue-200">
                {institution.code}
              </span>
              {institution.aicteReferenceCode && (
                <span className="font-mono text-xs font-semibold text-slate-600 bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
                  AICTE Ref: {institution.aicteReferenceCode}
                </span>
              )}
            </div>
            <h1 className="text-xl font-bold text-slate-900 mt-2">{institution.name}</h1>
            <p className="text-xs text-slate-500">{institution.type}</p>
          </div>

          <div className="flex items-center space-x-3 text-xs text-slate-600">
            <div className="bg-slate-50 px-4 py-2 rounded-lg border border-slate-200 text-center">
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Total Cases</span>
              <span className="text-lg font-bold text-slate-900">{institution.totalComplaints}</span>
            </div>
            <div className="bg-amber-50 px-4 py-2 rounded-lg border border-amber-200 text-center">
              <span className="text-amber-700 block text-[10px] uppercase font-bold">Open</span>
              <span className="text-lg font-bold text-amber-700">{institution.openComplaints || 0}</span>
            </div>
            <div className="bg-emerald-50 px-4 py-2 rounded-lg border border-emerald-200 text-center">
              <span className="text-emerald-700 block text-[10px] uppercase font-bold">Resolved</span>
              <span className="text-lg font-bold text-emerald-700">{institution.resolvedComplaints}</span>
            </div>
            {institution.overdueComplaints > 0 && (
              <div className="bg-rose-50 px-4 py-2 rounded-lg border border-rose-200 text-center">
                <span className="text-rose-700 block text-[10px] uppercase font-bold">Overdue SLA</span>
                <span className="text-lg font-bold text-rose-700">{institution.overdueComplaints}</span>
              </div>
            )}
          </div>
        </div>

        {/* Institution Metadata Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 pt-4 text-xs">
          <div className="flex items-start space-x-2 text-slate-600">
            <MapPin className="w-4 h-4 text-slate-400 mt-0.5 flex-shrink-0" />
            <div>
              <span className="font-semibold text-slate-700 block">Location & Address</span>
              <span>{institution.location}</span>
              {(institution.state || institution.district) && (
                <span className="text-slate-400 block text-[11px]">
                  {institution.district ? `${institution.district}, ` : ''}{institution.state}
                </span>
              )}
              {institution.address && (
                <span className="text-slate-400 block text-[10px] mt-0.5">{institution.address}</span>
              )}
            </div>
          </div>

          <div className="flex items-start space-x-2 text-slate-600">
            <Mail className="w-4 h-4 text-slate-400 mt-0.5 flex-shrink-0" />
            <div>
              <span className="font-semibold text-slate-700 block">Contact Email</span>
              <a href={`mailto:${institution.contactEmail}`} className="text-blue-600 hover:underline">
                {institution.contactEmail}
              </a>
            </div>
          </div>

          <div className="flex items-start space-x-2 text-slate-600">
            <Phone className="w-4 h-4 text-slate-400 mt-0.5 flex-shrink-0" />
            <div>
              <span className="font-semibold text-slate-700 block">Contact Phone</span>
              <span>{institution.contactPhone}</span>
            </div>
          </div>

          <div className="flex items-start space-x-2 text-slate-600">
            <Shield className="w-4 h-4 text-slate-400 mt-0.5 flex-shrink-0" />
            <div>
              <span className="font-semibold text-slate-700 block">Monitoring Scope</span>
              <span>
                {institution.involvedBureausCount || 0} AICTE Bureaus involved
              </span>
              <span className="text-slate-400 block text-[10px]">
                {institution.involvedCellsCount || 0} specialized cells active
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200 text-xs font-semibold">
        <button
          onClick={() => setActiveTab('complaints')}
          className={`pb-3 px-4 flex items-center border-b-2 transition-colors ${
            activeTab === 'complaints'
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          <FileText className="w-4 h-4 mr-2" />
          Cross-Bureau Grievances ({institution.totalComplaints})
        </button>
        <button
          onClick={() => setActiveTab('officers')}
          className={`pb-3 px-4 flex items-center border-b-2 transition-colors ${
            activeTab === 'officers'
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          <UserCheck className="w-4 h-4 mr-2" />
          Affiliated Nodal Officers ({officers.length})
        </button>
        <button
          onClick={() => setActiveTab('audit')}
          className={`pb-3 px-4 flex items-center border-b-2 transition-colors ${
            activeTab === 'audit'
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          <History className="w-4 h-4 mr-2" />
          Institutional Audit Trail
        </button>
      </div>

      {/* Tab: Cross-Bureau Complaints */}
      {activeTab === 'complaints' && (
        complaints.length === 0 ? (
          <EmptyState
            icon={FileText}
            title="No Grievances On File"
            description="This institution currently has no complaints registered across any AICTE Bureau."
          />
        ) : (
          <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[10px]">
                  <tr>
                    <th className="px-4 py-3">Case ID</th>
                    <th className="px-4 py-3">Subject</th>
                    <th className="px-4 py-3">Responsible Bureau / Cell</th>
                    <th className="px-4 py-3">Assigned Officer</th>
                    <th className="px-4 py-3">Category</th>
                    <th className="px-4 py-3">Priority</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3">Due Date</th>
                    <th className="px-4 py-3 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200">
                  {complaints.map((c) => (
                    <tr key={c.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="px-4 py-3 font-mono font-bold text-brand-600">
                        <Link to={`/admin/complaints/${c.complaintNumber || c.id}`} className="hover:underline">
                          {c.complaintNumber}
                        </Link>
                      </td>

                      <td className="px-4 py-3 font-semibold text-slate-900 max-w-xs">
                        <div className="line-clamp-1">{c.title}</div>
                        <div className="text-[10px] text-slate-400 font-normal">
                          Filed: {formatDate(c.createdAt)}
                        </div>
                      </td>

                      <td className="px-4 py-3">
                        <div className="flex flex-col gap-0.5">
                          <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-bold bg-blue-50 text-blue-700 border border-blue-200 w-max">
                            {c.bureauCode || 'RB'}
                          </span>
                          {c.cellCode && (
                            <span className="text-[10px] text-slate-500 font-medium">
                              Cell: {c.cellCode}
                            </span>
                          )}
                        </div>
                      </td>

                      <td className="px-4 py-3 text-slate-600">
                        {c.assignedOfficerName ? (
                          <span className="font-medium text-slate-800">{c.assignedOfficerName}</span>
                        ) : (
                          <span className="text-slate-400 italic">Unassigned</span>
                        )}
                      </td>

                      <td className="px-4 py-3">
                        <CategoryBadge category={c.category} />
                      </td>

                      <td className="px-4 py-3">
                        <PriorityBadge priority={c.priority} />
                      </td>

                      <td className="px-4 py-3">
                        <StatusBadge status={c.status} />
                      </td>

                      <td className="px-4 py-3 font-mono text-[11px]">
                        {c.dueDate ? (
                          <span className={c.overdue ? 'text-rose-600 font-bold' : 'text-slate-600'}>
                            {formatDate(c.dueDate)}
                          </span>
                        ) : (
                          <span className="text-slate-400">N/A</span>
                        )}
                      </td>

                      <td className="px-4 py-3 text-right">
                        <Link
                          to={`/admin/complaints/${c.complaintNumber || c.id}`}
                          className="inline-flex items-center text-xs font-semibold text-blue-600 hover:text-blue-800"
                        >
                          View Dossier
                          <ExternalLink className="w-3 h-3 ml-1" />
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <Pagination
              pageNumber={pageData.pageNumber}
              totalPages={pageData.totalPages}
              totalElements={pageData.totalElements}
              pageSize={pageData.pageSize}
              onPageChange={(page) => setPageData((prev) => ({ ...prev, pageNumber: page }))}
            />
          </div>
        )
      )}

      {/* Tab: Nodal Officers */}
      {activeTab === 'officers' && (
        officers.length === 0 ? (
          <EmptyState
            icon={UserCheck}
            title="No Assigned Officers"
            description="No nodal grievance officers are registered for this institution yet."
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {officers.map((off) => (
              <div key={off.id} className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-sm">
                    {off.fullName?.charAt(0) || 'O'}
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-slate-900">{off.fullName}</h4>
                    <p className="text-[11px] text-slate-500 font-mono">{off.email}</p>
                    {off.phone && <p className="text-[10px] text-slate-400 mt-0.5">{off.phone}</p>}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )
      )}

      {/* Tab: Institutional Audit Trail */}
      {activeTab === 'audit' && (
        auditLogs.length === 0 ? (
          <EmptyState
            icon={History}
            title="No Institutional Audit Entries"
            description="No activity log entries found for this institution."
          />
        ) : (
          <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm">
            <div className="p-4 border-b border-slate-200 bg-slate-50 text-xs font-semibold text-slate-700">
              Institutional Action & Oversight Trail
            </div>
            <div className="divide-y divide-slate-100">
              {auditLogs.map((log) => (
                <div key={log.id} className="p-4 text-xs flex items-start space-x-3 hover:bg-slate-50/60">
                  <div className="w-2 h-2 rounded-full bg-blue-600 mt-1.5 flex-shrink-0" />
                  <div className="flex-1 space-y-1">
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-slate-900">{log.action}</span>
                      <span className="text-[10px] text-slate-400 font-mono">
                        {formatDateTime(log.timestamp)}
                      </span>
                    </div>
                    <p className="text-slate-600 leading-relaxed">{log.details}</p>
                    <div className="text-[10px] text-slate-400">
                      Operator: <span className="font-medium text-slate-700">{log.performedBy || 'System Admin'}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )
      )}
    </div>
  );
};

export default InstitutionDetailPage;
