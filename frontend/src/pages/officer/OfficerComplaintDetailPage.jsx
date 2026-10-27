import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import { useToast } from '../../hooks/useToast';
import StatusBadge from '../../components/common/StatusBadge';
import PriorityBadge from '../../components/common/PriorityBadge';
import CategoryBadge from '../../components/common/CategoryBadge';
import ComplaintTimeline from '../../components/complaints/ComplaintTimeline';
import StatusUpdateModal from '../../components/complaints/StatusUpdateModal';
import CaseCompareModal from '../../components/complaints/CaseCompareModal';
import UnifiedCaseDossier from '../../components/complaints/UnifiedCaseDossier';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { formatDate, formatDateTime, formatFileSize } from '../../utils/formatters';
import {
  ArrowLeft,
  Building,
  User,
  ShieldCheck,
  Star,
  Edit3,
  AlertTriangle,
  Flame,
  Paperclip,
  FileText,
  Download,
  GitMerge,
  CopyCheck,
} from 'lucide-react';

const OfficerComplaintDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();

  const [complaint, setComplaint] = useState(null);
  const [updates, setUpdates] = useState([]);
  const [feedback, setFeedback] = useState(null);
  const [attachments, setAttachments] = useState([]);
  const [loading, setLoading] = useState(true);

  const [statusModalOpen, setStatusModalOpen] = useState(false);
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
      const [compRes, updatesRes, fbRes, attachRes] = await Promise.all([
        complaintService.getComplaintById(id),
        complaintService.getUpdates(id),
        complaintService.getFeedback(id).catch(() => ({ data: null })),
        complaintService.getAttachments(id).catch(() => ({ data: [] })),
      ]);
      setComplaint(compRes.data);
      setUpdates(updatesRes.data || []);
      setFeedback(fbRes?.data || null);
      setAttachments(attachRes?.data || []);
    } catch (err) {
      toast.error('Failed to load complaint details');
      navigate('/officer/complaints');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusSubmit = async (statusData) => {
    await complaintService.updateStatus(id, statusData);
    toast.success('Complaint status updated successfully');
    fetchComplaintData();
  };

  if (loading || !complaint) {
    return <LoadingSpinner text="Retrieving case details..." size="lg" />;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <Link
          to="/officer/complaints"
          className="inline-flex items-center text-xs font-semibold text-slate-600 hover:text-slate-900 transition-colors"
        >
          <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to Assigned Complaints
        </Link>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={handleOpenCompare}
            className="inline-flex items-center px-3 py-2 text-xs font-semibold text-purple-700 bg-purple-50 border border-purple-200 rounded-lg hover:bg-purple-100 transition-colors shadow-sm"
          >
            <GitMerge className="w-3.5 h-3.5 mr-1.5" />
            Review Overlap & Merge
          </button>
          <button
            onClick={() => setStatusModalOpen(true)}
            className="inline-flex items-center px-4 py-2 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-lg shadow-sm transition-colors"
          >
            <Edit3 className="w-4 h-4 mr-1.5" />
            Update Status & Record Actions
          </button>
        </div>
      </div>

      {/* Case Header Card */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-3 pb-4 border-b border-slate-200">
          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <span className="font-mono text-xs font-bold text-brand-600 bg-brand-50 px-2.5 py-1 rounded border border-brand-100">
                {complaint.complaintNumber}
              </span>
              {complaint.bureauCode && (
                <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold bg-blue-50 text-blue-700 border border-blue-200">
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
                  Campus Concept Match
                </span>
              </div>
              <p className="text-amber-800 mt-0.5 leading-relaxed">
                The AICTE multi-factor similarity engine detected substantial concept and topical overlap with another registered complaint at your institution.
              </p>
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

        {complaint.status === 'ESCALATED' && (
          <div className="mt-4 p-3.5 rounded-lg bg-amber-50 border border-amber-300 flex items-start space-x-3 text-amber-900 text-xs font-medium">
            <Flame className="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" />
            <div>
              <p className="font-bold text-amber-950 text-sm">Escalated Priority Case</p>
              <p className="text-amber-800 mt-0.5 leading-relaxed">
                This grievance case has been formally escalated for executive monitoring by the central AICTE grievance board.
              </p>
            </div>
          </div>
        )}

        {complaint.overdue && (
          <div className="mt-4 p-3.5 rounded-lg bg-rose-50 border border-rose-300 flex items-start space-x-3 text-rose-900 text-xs font-medium">
            <AlertTriangle className="w-5 h-5 text-rose-600 flex-shrink-0 mt-0.5" />
            <div>
              <p className="font-bold text-rose-950 text-sm">Attention Nodal Officer: SLA Deadline Elapsed</p>
              <p className="text-rose-800 mt-0.5 leading-relaxed">
                The SLA deadline elapsed on <strong>{formatDate(complaint.dueDate)}</strong>. Please conclude enquiry and submit resolution remarks urgently.
              </p>
            </div>
          </div>
        )}

        <div className="mt-4">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
            Grievance Particulars
          </h3>
          <p className="text-xs text-slate-700 leading-relaxed bg-slate-50 p-4 rounded-lg border border-slate-100 whitespace-pre-line">
            {complaint.description}
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-6 pt-4 border-t border-slate-100 text-xs">
          <div>
            <span className="text-slate-400 block font-medium">Category</span>
            <div className="mt-1">
              <CategoryBadge category={complaint.category} displayName={complaint.categoryDisplayName} />
            </div>
          </div>
          <div>
            <span className="text-slate-400 block font-medium">Submission Date</span>
            <span className="font-semibold text-slate-800 mt-1 block">
              {formatDateTime(complaint.createdAt)}
            </span>
          </div>
          <div>
            <span className="text-slate-400 block font-medium">Target Redressal Date</span>
            <span className={`font-semibold mt-1 block ${complaint.overdue ? 'text-rose-600' : 'text-slate-800'}`}>
              {formatDate(complaint.dueDate)}
            </span>
          </div>
          <div>
            <span className="text-slate-400 block font-medium">Status</span>
            <span className="font-semibold text-slate-800 mt-1 block">{complaint.status}</span>
          </div>
        </div>
      </div>

      {/* Unified Case Dossier for Primary Master or Merged Secondary Cases */}
      <UnifiedCaseDossier complaint={complaint} onReload={fetchComplaintData} />

      {/* Grid: Complainant details & Timeline */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="space-y-6 lg:col-span-1">
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

          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center">
              <Building className="w-3.5 h-3.5 mr-1.5 text-indigo-600" />
              Your Campus Jurisdiction
            </h3>
            <div className="space-y-1 text-xs">
              <p className="font-semibold text-slate-900">{complaint.institutionName}</p>
              <p className="text-slate-500 font-mono text-[11px]">Code: {complaint.institutionCode}</p>
            </div>
          </div>

          {/* Attachments Card */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center justify-between">
              <span className="flex items-center">
                <Paperclip className="w-3.5 h-3.5 mr-1.5 text-blue-600" />
                Submitted Evidence & Files
              </span>
              <span className="font-semibold text-slate-500 bg-slate-100 px-2 py-0.5 rounded text-[11px]">
                {attachments.length}
              </span>
            </h3>

            {attachments.length === 0 ? (
              <div className="text-xs text-slate-400 italic bg-slate-50 p-3 rounded-lg border border-slate-100 text-center">
                No attachments submitted with this case.
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
              </div>
            </div>
          )}
        </div>

        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm lg:col-span-2">
          <div className="flex items-center justify-between mb-6 pb-3 border-b border-slate-200">
            <div>
              <h2 className="text-sm font-bold text-slate-900">Case Investigation & Action Timeline</h2>
              <p className="text-xs text-slate-500">Every status update and remark entered by your office is recorded here</p>
            </div>
            <button
              onClick={() => setStatusModalOpen(true)}
              className="text-xs font-semibold text-brand-600 hover:text-brand-700"
            >
              + Update Status
            </button>
          </div>

          <ComplaintTimeline updates={updates} />
        </div>
      </div>

      <StatusUpdateModal
        isOpen={statusModalOpen}
        onClose={() => setStatusModalOpen(false)}
        currentStatus={complaint.status}
        onSubmit={handleStatusSubmit}
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

export default OfficerComplaintDetailPage;
