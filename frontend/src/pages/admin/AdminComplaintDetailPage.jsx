import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import { useToast } from '../../hooks/useToast';
import StatusBadge from '../../components/common/StatusBadge';
import PriorityBadge from '../../components/common/PriorityBadge';
import CategoryBadge from '../../components/common/CategoryBadge';
import ComplaintTimeline from '../../components/complaints/ComplaintTimeline';
import StatusUpdateModal from '../../components/complaints/StatusUpdateModal';
import AssignOfficerModal from '../../components/complaints/AssignOfficerModal';
import TransferCaseModal from '../../components/complaints/TransferCaseModal';
import CaseCompareModal from '../../components/complaints/CaseCompareModal';
import UnifiedCaseDossier from '../../components/complaints/UnifiedCaseDossier';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { formatDate, formatDateTime, formatFileSize } from '../../utils/formatters';
import {
  ArrowLeft,
  Calendar,
  Building,
  User,
  ShieldCheck,
  Star,
  Edit3,
  UserCheck,
  AlertTriangle,
  Flame,
  Paperclip,
  FileText,
  Download,
  ArrowRightLeft,
  Network,
  CopyCheck,
  Layers,
  History,
  GitMerge,
} from 'lucide-react';

const AdminComplaintDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();

  const [complaint, setComplaint] = useState(null);
  const [updates, setUpdates] = useState([]);
  const [feedback, setFeedback] = useState(null);
  const [attachments, setAttachments] = useState([]);
  const [transfers, setTransfers] = useState([]);
  const [loading, setLoading] = useState(true);

  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [transferModalOpen, setTransferModalOpen] = useState(false);
  const [compareModalOpen, setCompareModalOpen] = useState(false);
  const [compareTargetId, setCompareTargetId] = useState(null);

  const handleOpenCompare = async () => {
    try {
      const targetId = complaint?.id || complaint?.complaintNumber || id;
      const res = await complaintService.getMergeCandidates(targetId);
      const candidates = res?.data || (Array.isArray(res) ? res : []);
      if (candidates && candidates.length > 0) {
        setCompareTargetId(candidates[0].complaintId || candidates[0].id || candidates[0].complaintNumber);
        setCompareModalOpen(true);
      } else {
        toast.info('No overlapping duplicate candidates detected for this case.');
      }
    } catch (err) {
      console.error('Failed to load merge candidates:', err);
      const msg = err.response?.data?.message || err.message || 'Could not load merge candidates.';
      toast.error(msg);
    }
  };

  useEffect(() => {
    fetchComplaintData();
  }, [id]);

  const fetchComplaintData = async () => {
    try {
      setLoading(true);
      const [compRes, updatesRes, fbRes, attachRes, transferRes] = await Promise.all([
        complaintService.getComplaintById(id),
        complaintService.getUpdates(id),
        complaintService.getFeedback(id).catch(() => ({ data: null })),
        complaintService.getAttachments(id).catch(() => ({ data: [] })),
        complaintService.getTransfers(id).catch(() => ({ data: [] })),
      ]);
      setComplaint(compRes.data);
      setUpdates(updatesRes.data || []);
      setFeedback(fbRes?.data || null);
      setAttachments(attachRes?.data || []);
      setTransfers(transferRes?.data || []);
    } catch (err) {
      toast.error('Failed to load complaint details');
      navigate('/admin/complaints');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusSubmit = async (statusData) => {
    await complaintService.updateStatus(id, statusData);
    toast.success('Complaint status updated successfully');
    fetchComplaintData();
  };

  const handleAssignSubmit = async (assignData) => {
    await complaintService.assignComplaint(id, assignData);
    toast.success('Complaint assigned successfully');
    fetchComplaintData();
  };

  const handleTransferSubmit = async (transferData) => {
    await complaintService.transferComplaint(id, transferData);
    toast.success('Complaint transferred successfully across Bureau/Cell');
    fetchComplaintData();
  };

  if (loading || !complaint) {
    return <LoadingSpinner text="Retrieving grievance dossier..." size="lg" />;
  }

  return (
    <div className="space-y-6">
      {/* Back button & Actions Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <Link
          to="/admin/complaints"
          className="inline-flex items-center text-xs font-semibold text-slate-600 hover:text-slate-900 transition-colors"
        >
          <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to Complaints
        </Link>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={handleOpenCompare}
            className="inline-flex items-center px-3 py-1.5 text-xs font-semibold text-purple-700 bg-purple-50 border border-purple-200 rounded-lg hover:bg-purple-100 transition-colors shadow-sm"
          >
            <GitMerge className="w-3.5 h-3.5 mr-1.5" />
            Review Overlap & Merge
          </button>
          <button
            onClick={() => setTransferModalOpen(true)}
            className="inline-flex items-center px-3 py-1.5 text-xs font-semibold text-blue-700 bg-blue-50 border border-blue-200 rounded-lg hover:bg-blue-100 transition-colors shadow-sm"
          >
            <ArrowRightLeft className="w-3.5 h-3.5 mr-1.5" />
            Transfer Case
          </button>
          <button
            onClick={() => setAssignModalOpen(true)}
            className="inline-flex items-center px-3 py-1.5 text-xs font-semibold text-indigo-700 bg-indigo-50 border border-indigo-200 rounded-lg hover:bg-indigo-100 transition-colors"
          >
            <UserCheck className="w-3.5 h-3.5 mr-1.5" />
            Assign Officer
          </button>
          <button
            onClick={() => setStatusModalOpen(true)}
            className="inline-flex items-center px-3 py-1.5 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors"
          >
            <Edit3 className="w-3.5 h-3.5 mr-1.5" />
            Update Status
          </button>
        </div>
      </div>

      {/* Main Dossier Header Card */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-3 pb-4 border-b border-slate-200">
          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <span className="font-mono text-xs font-bold text-blue-700 bg-blue-50 px-2.5 py-1 rounded border border-blue-200">
                {complaint.complaintNumber}
              </span>
              {complaint.bureauCode && (
                <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold bg-indigo-50 text-indigo-700 border border-indigo-200">
                  <Network className="w-3 h-3 mr-1" />
                  Bureau: {complaint.bureauCode}
                </span>
              )}
              {complaint.cellCode && (
                <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-slate-100 text-slate-700 border border-slate-200">
                  Cell: {complaint.cellCode}
                </span>
              )}
            </div>
            <h1 className="text-lg font-bold text-slate-900 mt-2">{complaint.title}</h1>
          </div>
          <div className="flex items-center space-x-2">
            <PriorityBadge priority={complaint.priority} />
            <StatusBadge status={complaint.status} />
          </div>
        </div>

        {/* Potential Duplicate Banner */}
        {complaint.potentialDuplicate && (
          <div className="mt-4 p-4 rounded-xl bg-gradient-to-r from-amber-50 via-purple-50/30 to-amber-50/50 border border-amber-300 flex items-start space-x-3 text-amber-900 text-xs font-medium">
            <CopyCheck className="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" />
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <p className="font-bold text-amber-950 text-sm">Potential Duplicate / Overlapping Grievance Flag</p>
                <span className="text-[10px] font-semibold text-purple-700 bg-purple-100 px-1.5 py-0.5 rounded border border-purple-200">
                  Semantic & Topic Match
                </span>
              </div>
              <p className="text-amber-800 mt-0.5 leading-relaxed">
                The AICTE multi-factor similarity engine detected substantial concept and topical overlap with another registered complaint at this institution.
              </p>
              {complaint.duplicateAcknowledged ? (
                <div className="mt-2 p-2 bg-white/90 rounded border border-emerald-200 text-[11px] font-semibold text-emerald-800">
                  ✓ Complainant verified and confirmed unique incident during submission:
                  <div className="font-normal italic text-slate-700 mt-0.5">"{complaint.duplicateRemarks}"</div>
                </div>
              ) : (
                <div className="mt-1 text-[11px] font-semibold text-amber-800">
                  ⚠️ Complainant registered this case without prior acknowledgment. Independent verification recommended.
                </div>
              )}

              <div className="pt-2">
                <button
                  type="button"
                  onClick={handleOpenCompare}
                  className="inline-flex items-center px-2.5 py-1 text-xs font-semibold text-purple-800 bg-white border border-purple-300 rounded shadow-xs hover:bg-purple-100 transition-colors"
                >
                  <GitMerge className="w-3.5 h-3.5 mr-1.5 text-purple-700" />
                  Compare & Review Overlapping Case
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Escalation Alert banner if status is ESCALATED */}
        {complaint.status === 'ESCALATED' && (
          <div className="mt-4 p-3.5 rounded-lg bg-amber-50 border border-amber-300 flex items-start space-x-3 text-amber-900 text-xs font-medium">
            <Flame className="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" />
            <div>
              <p className="font-bold text-amber-950 text-sm">Case Formally Escalated</p>
              <p className="text-amber-800 mt-0.5 leading-relaxed">
                This grievance case has exceeded standard procedural thresholds or was escalated for expedited oversight by the central AICTE grievance redressal cell.
              </p>
            </div>
          </div>
        )}

        {/* Overdue Alert banner if SLA breached */}
        {complaint.overdue && (
          <div className="mt-4 p-3.5 rounded-lg bg-rose-50 border border-rose-300 flex items-start space-x-3 text-rose-900 text-xs font-medium">
            <AlertTriangle className="w-5 h-5 text-rose-600 flex-shrink-0 mt-0.5" />
            <div>
              <p className="font-bold text-rose-950 text-sm">SLA Statutory Deadline Breached</p>
              <p className="text-rose-800 mt-0.5 leading-relaxed">
                The target resolution date for this grievance was <strong>{formatDate(complaint.dueDate)}</strong>. Immediate nodal intervention or institutional escalation is required.
              </p>
            </div>
          </div>
        )}

        {/* Grievance Statement */}
        <div className="mt-4">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
            Grievance Description
          </h3>
          <p className="text-xs text-slate-700 leading-relaxed bg-slate-50 p-4 rounded-lg border border-slate-100 whitespace-pre-line">
            {complaint.description}
          </p>
        </div>

        {/* Meta details grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-6 pt-4 border-t border-slate-100 text-xs">
          <div>
            <span className="text-slate-400 block font-medium">Category</span>
            <div className="mt-1">
              <CategoryBadge category={complaint.category} displayName={complaint.categoryDisplayName} />
            </div>
          </div>
          <div>
            <span className="text-slate-400 block font-medium">Lodged Date</span>
            <span className="font-semibold text-slate-800 mt-1 block">
              {formatDateTime(complaint.createdAt)}
            </span>
          </div>
          <div>
            <span className="text-slate-400 block font-medium">SLA Due Date</span>
            <span className={`font-semibold mt-1 block ${complaint.overdue ? 'text-rose-600' : 'text-slate-800'}`}>
              {formatDate(complaint.dueDate)}
            </span>
          </div>
          <div>
            <span className="text-slate-400 block font-medium">Resolved At</span>
            <span className="font-semibold text-slate-800 mt-1 block">
              {complaint.resolvedAt ? formatDateTime(complaint.resolvedAt) : 'Pending Resolution'}
            </span>
          </div>
        </div>
      </div>

      {/* Unified Case Dossier for Primary Master or Merged Secondary Cases */}
      <UnifiedCaseDossier complaint={complaint} onReload={fetchComplaintData} />

      {/* 2-Column Info & Timeline Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left column: Organization, Complainant & Institution & Officer Cards */}
        <div className="space-y-6 lg:col-span-1">
          {/* Responsible Bureau & Cell Ownership Card */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center justify-between">
              <span className="flex items-center">
                <Network className="w-3.5 h-3.5 mr-1.5 text-blue-600" />
                Case Bureau & Cell Ownership
              </span>
              {transfers.length > 0 && (
                <span className="text-[10px] font-bold bg-blue-100 text-blue-800 px-1.5 py-0.5 rounded">
                  {transfers.length} Transfers
                </span>
              )}
            </h3>
            <div className="space-y-2 text-xs">
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Assigned Bureau</span>
                <span className="font-semibold text-slate-900 block mt-0.5">
                  {complaint.bureauName ? `${complaint.bureauName} (${complaint.bureauCode})` : 'Central Regulation Bureau'}
                </span>
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Operational Cell</span>
                <span className="font-semibold text-slate-900 block mt-0.5">
                  {complaint.cellName ? `${complaint.cellName} (${complaint.cellCode})` : 'Public Grievance Redressal Cell'}
                </span>
              </div>
              {complaint.assignmentRemarks && (
                <div className="pt-2 border-t border-slate-100">
                  <span className="text-slate-400 block text-[10px] uppercase font-bold">Ownership Remarks</span>
                  <p className="text-slate-600 text-[11px] italic mt-0.5">
                    {complaint.assignmentRemarks}
                  </p>
                </div>
              )}
              <button
                onClick={() => setTransferModalOpen(true)}
                className="mt-2 text-blue-600 hover:text-blue-800 font-semibold inline-flex items-center text-xs"
              >
                <ArrowRightLeft className="w-3 h-3 mr-1" /> Reassign / Transfer Bureau →
              </button>
            </div>
          </div>

          {/* Accused / Target Institution Card */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center">
              <Building className="w-3.5 h-3.5 mr-1.5 text-blue-600" />
              Target Institution
            </h3>
            <div className="space-y-1.5 text-xs">
              <p className="font-semibold text-slate-900">{complaint.institutionName}</p>
              <p className="text-slate-500 font-mono text-[11px]">Code: {complaint.institutionCode}</p>
              <Link
                to={`/admin/institutions/${complaint.institutionId}`}
                className="inline-block mt-2 text-blue-600 hover:text-blue-700 font-semibold"
              >
                View Full Institution Profile →
              </Link>
            </div>
          </div>

          {/* Complainant Card */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center">
              <User className="w-3.5 h-3.5 mr-1.5 text-blue-600" />
              Complainant Information
            </h3>
            <div className="space-y-1 text-xs">
              <p className="font-semibold text-slate-900">{complaint.complainantName}</p>
              <p className="text-slate-600">{complaint.complainantEmail}</p>
              {complaint.complainantPhone && (
                <p className="text-slate-500">Phone: {complaint.complainantPhone}</p>
              )}
            </div>
          </div>

          {/* Assigned Nodal Officer */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center">
              <ShieldCheck className="w-3.5 h-3.5 mr-1.5 text-indigo-600" />
              Assigned Nodal Officer
            </h3>
            <div className="text-xs">
              {complaint.assignedOfficerName ? (
                <div>
                  <p className="font-semibold text-slate-900">{complaint.assignedOfficerName}</p>
                  <p className="text-slate-500 text-[11px] mt-0.5">Primary redressal officer</p>
                </div>
              ) : (
                <div className="text-slate-500 italic">
                  Not currently assigned to a specific officer.
                  <button
                    onClick={() => setAssignModalOpen(true)}
                    className="block mt-2 text-blue-600 hover:text-blue-700 font-semibold not-italic"
                  >
                    Assign Now →
                  </button>
                </div>
              )}
            </div>
          </div>

          {/* Supporting Attachments Card */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center justify-between">
              <span className="flex items-center">
                <Paperclip className="w-3.5 h-3.5 mr-1.5 text-blue-600" />
                Supporting Documents & Evidence
              </span>
              <span className="font-semibold text-slate-500 bg-slate-100 px-2 py-0.5 rounded text-[11px]">
                {attachments.length}
              </span>
            </h3>

            {attachments.length === 0 ? (
              <div className="text-xs text-slate-400 italic bg-slate-50 p-3 rounded-lg border border-slate-100 text-center">
                No supporting documents were attached to this complaint.
              </div>
            ) : (
              <div className="space-y-2.5">
                {attachments.map((att) => (
                  <div
                    key={att.id}
                    className="flex items-center justify-between p-2.5 rounded-lg border border-slate-200 bg-slate-50 hover:bg-slate-100 transition-colors text-xs"
                  >
                    <div className="flex items-center space-x-2.5 min-w-0">
                      <div className="p-1.5 bg-blue-100 text-blue-700 rounded flex-shrink-0">
                        <FileText className="w-4 h-4" />
                      </div>
                      <div className="min-w-0">
                        <p className="font-semibold text-slate-800 truncate" title={att.fileName}>
                          {att.fileName}
                        </p>
                        <p className="text-[10px] text-slate-400">
                          {formatFileSize(att.fileSize)} • Uploaded {formatDate(att.uploadedAt)}
                        </p>
                      </div>
                    </div>
                    <span className="inline-flex items-center text-[11px] font-semibold text-blue-600 hover:text-blue-800 ml-2 flex-shrink-0">
                      <Download className="w-3.5 h-3.5 mr-0.5" />
                      View
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Feedback */}
          {feedback && (
            <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center">
                <Star className="w-3.5 h-3.5 mr-1.5 text-amber-500" />
                Complainant Feedback
              </h3>
              <div className="space-y-2 text-xs">
                <div className="flex items-center space-x-1">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <Star
                      key={star}
                      className={`w-4 h-4 ${
                        star <= feedback.rating ? 'text-amber-400 fill-amber-400' : 'text-slate-300'
                      }`}
                    />
                  ))}
                  <span className="font-semibold text-slate-700 ml-1.5">{feedback.rating}/5</span>
                </div>
                {feedback.comments && (
                  <p className="text-slate-600 italic bg-amber-50/50 p-2.5 rounded border border-amber-100">
                    "{feedback.comments}"
                  </p>
                )}
                <p className="text-[10px] text-slate-400">
                  Submitted on {formatDate(feedback.createdAt)}
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Right column: Chronological Timeline & Transfer History */}
        <div className="space-y-6 lg:col-span-2">
          {/* Transfer History Card (if any transfers exist) */}
          {transfers.length > 0 && (
            <div className="bg-white rounded-xl border border-blue-200 p-5 shadow-sm bg-gradient-to-br from-blue-50/30 to-white">
              <div className="flex items-center justify-between pb-3 mb-3 border-b border-blue-100">
                <div className="flex items-center space-x-2">
                  <ArrowRightLeft className="w-4 h-4 text-blue-600" />
                  <h3 className="text-xs font-bold uppercase tracking-wider text-blue-900">
                    Cross-Bureau Transfer & Ownership Trail ({transfers.length})
                  </h3>
                </div>
                <span className="text-[10px] text-slate-400 font-medium">Audit Preserved</span>
              </div>

              <div className="space-y-3">
                {transfers.map((t, idx) => (
                  <div key={t.id || idx} className="p-3 bg-white rounded-lg border border-slate-200 text-xs shadow-2xs">
                    <div className="flex flex-wrap items-center justify-between gap-1 text-[11px] text-slate-500">
                      <span className="font-semibold text-slate-800">
                        Transfer Event #{transfers.length - idx}
                      </span>
                      <span className="font-mono text-[10px]">{formatDateTime(t.transferredAt)}</span>
                    </div>

                    <div className="mt-1.5 flex flex-wrap items-center gap-2 text-slate-700">
                      <span className="px-2 py-0.5 rounded bg-slate-100 border border-slate-200 font-medium">
                        From: {t.fromBureauName || 'N/A'} {t.fromCellName ? `(${t.fromCellName})` : ''}
                      </span>
                      <span className="text-blue-600 font-bold">➔</span>
                      <span className="px-2 py-0.5 rounded bg-blue-50 text-blue-800 border border-blue-200 font-bold">
                        To: {t.toBureauName || 'N/A'} {t.toCellName ? `(${t.toCellName})` : ''}
                      </span>
                    </div>

                    <p className="mt-2 text-slate-800 text-xs leading-relaxed">
                      <strong>Reason:</strong> {t.transferReason}
                    </p>

                    <div className="mt-1 text-[10px] text-slate-400">
                      Transferred by: <span className="font-medium text-slate-600">{t.transferredByName || 'Admin'}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Chronological Timeline */}
          <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
            <div className="flex items-center justify-between mb-6 pb-3 border-b border-slate-200">
              <div>
                <h2 className="text-sm font-bold text-slate-900">Official Case History & Audit Timeline</h2>
                <p className="text-xs text-slate-500">Immutable audit log of all updates, transfers, findings, and status changes</p>
              </div>
              <button
                onClick={() => setStatusModalOpen(true)}
                className="text-xs font-semibold text-blue-600 hover:text-blue-700"
              >
                + Add Progress Entry
              </button>
            </div>

            <ComplaintTimeline updates={updates} />
          </div>
        </div>
      </div>

      {/* Modals */}
      <StatusUpdateModal
        isOpen={statusModalOpen}
        onClose={() => setStatusModalOpen(false)}
        currentStatus={complaint.status}
        onSubmit={handleStatusSubmit}
      />

      <AssignOfficerModal
        isOpen={assignModalOpen}
        onClose={() => setAssignModalOpen(false)}
        complaint={complaint}
        onSubmit={handleAssignSubmit}
        isAdmin={true}
      />

      <TransferCaseModal
        isOpen={transferModalOpen}
        onClose={() => setTransferModalOpen(false)}
        complaint={complaint}
        onSubmit={handleTransferSubmit}
      />

      {compareModalOpen && (
        <CaseCompareModal
          isOpen={compareModalOpen}
          onClose={() => setCompareModalOpen(false)}
          caseAId={complaint?.id}
          caseBId={compareTargetId}
          onMergeSuccess={fetchComplaintData}
          onLinkSuccess={fetchComplaintData}
        />
      )}
    </div>
  );
};

export default AdminComplaintDetailPage;
