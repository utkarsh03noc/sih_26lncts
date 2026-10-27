import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import { useToast } from '../../hooks/useToast';
import StatusBadge from '../../components/common/StatusBadge';
import PriorityBadge from '../../components/common/PriorityBadge';
import CategoryBadge from '../../components/common/CategoryBadge';
import Pagination from '../../components/common/Pagination';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import CaseCompareModal from '../../components/complaints/CaseCompareModal';
import { formatDate } from '../../utils/formatters';
import {
  CopyCheck,
  AlertTriangle,
  Building2,
  ExternalLink,
  ShieldAlert,
  Info,
  CheckCircle2,
  AlertCircle,
  GitMerge,
} from 'lucide-react';

const DuplicateAlertsPage = () => {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pageData, setPageData] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
  });

  // Compare & Merge Modal State
  const [compareModalOpen, setCompareModalOpen] = useState(false);
  const [selectedCaseA, setSelectedCaseA] = useState(null);
  const [selectedCaseB, setSelectedCaseB] = useState(null);

  const toast = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    fetchDuplicates();
  }, [pageData.pageNumber]);

  const fetchDuplicates = async () => {
    try {
      setLoading(true);
      const res = await complaintService.getDuplicates({
        page: pageData.pageNumber,
        size: pageData.pageSize,
      });
      if (res.success && res.data) {
        setComplaints(res.data.content || []);
        setPageData((prev) => ({
          ...prev,
          totalElements: res.data.totalElements || 0,
          totalPages: res.data.totalPages || 0,
        }));
      }
    } catch (err) {
      console.error('Failed to load duplicate alerts:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleReviewAndMerge = async (complaint) => {
    try {
      const targetId = complaint?.id || complaint?.complaintId || complaint?.complaintNumber;
      if (!targetId) {
        toast.error('Invalid complaint identifier');
        return;
      }
      const res = await complaintService.getMergeCandidates(targetId);
      const candidates = res?.data || (Array.isArray(res) ? res : []);
      if (candidates && candidates.length > 0) {
        setSelectedCaseA(complaint?.id || targetId);
        setSelectedCaseB(candidates[0].complaintId || candidates[0].id || candidates[0].complaintNumber);
        setCompareModalOpen(true);
      } else {
        toast.info('No direct merge candidate found. Opening complaint dossier for manual candidate search.');
        navigate(`/admin/complaints/${complaint?.complaintNumber || targetId}`);
      }
    } catch (err) {
      console.error('Failed to get merge candidates:', err);
      const msg = err.response?.data?.message || err.message || 'Could not retrieve merge candidates for comparison.';
      toast.error(msg);
    }
  };

  if (loading) {
    return <LoadingSpinner text="Scanning cross-institution duplicate grievance alerts..." size="lg" />;
  }

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">
              Duplicate & Overlapping Case Alerts
            </h1>
            <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-amber-100 text-amber-800 border border-amber-200">
              <CopyCheck className="w-3 h-3 mr-1" />
              {pageData.totalElements} Flagged Cases
            </span>
            <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-purple-100 text-purple-800 border border-purple-200">
              🧠 Semantic AI Enabled
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Grievances flagged by the multi-criteria similarity engine for institutional concept overlap, paraphrased topics, and prior open cases
          </p>
        </div>
      </div>

      {/* Info Notice */}
      <div className="p-4 bg-gradient-to-r from-amber-50 via-purple-50/40 to-white border border-amber-200 rounded-xl text-xs text-amber-900 flex items-start space-x-3 shadow-sm">
        <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" />
        <div className="space-y-1">
          <p className="font-bold text-amber-950 flex items-center gap-2">
            Multi-Tier Duplicate & Overlap Intelligence (Semantic Vectors + Lexical Overlap)
          </p>
          <p className="text-amber-800 leading-relaxed">
            These cases share substantial similarity (institution, technical education grievance concept ontology, and subject phrasing) with previously registered complaints. Complainants may have confirmed distinct incidents with written justification during submission.
          </p>
        </div>
      </div>

      {/* Alerts Table */}
      {complaints.length === 0 ? (
        <EmptyState
          icon={CopyCheck}
          title="No Duplicate Alerts"
          description="There are currently no complaints flagged as duplicate or overlapping."
        />
      ) : (
        <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[10px]">
                <tr>
                  <th className="px-4 py-3">Case ID</th>
                  <th className="px-4 py-3">Subject Title</th>
                  <th className="px-4 py-3">Institution</th>
                  <th className="px-4 py-3">Responsible Bureau / Cell</th>
                  <th className="px-4 py-3">Priority / Status</th>
                  <th className="px-4 py-3">Complainant Acknowledgment</th>
                  <th className="px-4 py-3 text-right">Dossier</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {complaints.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="px-4 py-3 font-mono font-bold text-brand-600">
                      <Link
                        to={`/admin/complaints/${c.complaintNumber || c.id}`}
                        className="hover:underline"
                      >
                        {c.complaintNumber}
                      </Link>
                    </td>

                    <td className="px-4 py-3 max-w-xs">
                      <div className="font-semibold text-slate-900 line-clamp-1">{c.title}</div>
                      <div className="text-[11px] text-slate-500 line-clamp-1">{c.description}</div>
                    </td>

                    <td className="px-4 py-3 max-w-[200px]">
                      <div className="font-medium text-slate-800 line-clamp-1">
                        {c.institutionName}
                      </div>
                      <div className="font-mono text-[10px] text-slate-400">{c.institutionCode}</div>
                    </td>

                    <td className="px-4 py-3">
                      <div className="space-y-0.5">
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-blue-50 text-blue-700 border border-blue-200">
                          {c.bureauCode || 'RB'}
                        </span>
                        {c.cellCode && (
                          <span className="ml-1 inline-flex items-center px-2 py-0.5 rounded text-[10px] font-medium bg-indigo-50 text-indigo-700 border border-indigo-200">
                            {c.cellCode}
                          </span>
                        )}
                      </div>
                    </td>

                    <td className="px-4 py-3">
                      <div className="space-y-1">
                        <PriorityBadge priority={c.priority} />
                        <div>
                          <StatusBadge status={c.status} />
                        </div>
                      </div>
                    </td>

                    <td className="px-4 py-3">
                      {c.duplicateAcknowledged ? (
                        <div className="space-y-0.5">
                          <span className="inline-flex items-center text-[11px] font-semibold text-emerald-700">
                            <CheckCircle2 className="w-3.5 h-3.5 mr-1 text-emerald-600" />
                            Confirmed Unique
                          </span>
                          {c.duplicateRemarks && (
                            <p className="text-[10px] text-slate-500 italic max-w-xs line-clamp-2">
                              "{c.duplicateRemarks}"
                            </p>
                          )}
                        </div>
                      ) : (
                        <span className="inline-flex items-center text-[11px] font-semibold text-amber-700">
                          <AlertCircle className="w-3.5 h-3.5 mr-1 text-amber-600" />
                          Unverified Submission
                        </span>
                      )}
                    </td>

                    <td className="px-4 py-3 text-right">
                      <div className="flex items-center justify-end space-x-2">
                        <button
                          type="button"
                          onClick={() => handleReviewAndMerge(c)}
                          className="inline-flex items-center px-2 py-1 text-xs font-semibold text-blue-700 bg-blue-50 hover:bg-blue-100 rounded border border-blue-200 transition-colors"
                        >
                          <GitMerge className="w-3 h-3 mr-1" />
                          Review & Merge
                        </button>
                        <Link
                          to={`/admin/complaints/${c.complaintNumber || c.id}`}
                          className="inline-flex items-center px-2 py-1 text-xs font-semibold text-brand-600 hover:bg-brand-50 rounded transition-colors"
                        >
                          Dossier
                          <ExternalLink className="w-3 h-3 ml-1" />
                        </Link>
                      </div>
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
      )}

      {/* Review & Compare Modal */}
      {compareModalOpen && (
        <CaseCompareModal
          isOpen={compareModalOpen}
          onClose={() => setCompareModalOpen(false)}
          caseAId={selectedCaseA}
          caseBId={selectedCaseB}
          onMergeSuccess={() => {
            fetchDuplicates();
          }}
          onLinkSuccess={() => {
            fetchDuplicates();
          }}
        />
      )}
    </div>
  );
};

export default DuplicateAlertsPage;
