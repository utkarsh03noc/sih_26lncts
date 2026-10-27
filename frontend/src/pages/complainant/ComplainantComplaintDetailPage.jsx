import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import { useToast } from '../../hooks/useToast';
import StatusBadge from '../../components/common/StatusBadge';
import PriorityBadge from '../../components/common/PriorityBadge';
import CategoryBadge from '../../components/common/CategoryBadge';
import ComplaintTimeline from '../../components/complaints/ComplaintTimeline';
import FeedbackModal from '../../components/complaints/FeedbackModal';
import ReopenModal from '../../components/complaints/ReopenModal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { formatDate, formatDateTime, formatFileSize } from '../../utils/formatters';
import {
  ArrowLeft,
  Building,
  RotateCcw,
  Star,
  CheckCircle2,
  AlertTriangle,
  Paperclip,
  FileText,
  Download,
} from 'lucide-react';

const ComplainantComplaintDetailPage = () => {
  const { id } = useParams();
  const toast = useToast();

  const [complaint, setComplaint] = useState(null);
  const [updates, setUpdates] = useState([]);
  const [feedback, setFeedback] = useState(null);
  const [attachments, setAttachments] = useState([]);
  const [loading, setLoading] = useState(true);

  const [feedbackModalOpen, setFeedbackModalOpen] = useState(false);
  const [reopenModalOpen, setReopenModalOpen] = useState(false);

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
      toast.error('Failed to load complaint dossier');
    } finally {
      setLoading(false);
    }
  };

  const handleFeedbackSubmit = async (feedbackData) => {
    await complaintService.submitFeedback(id, feedbackData);
    toast.success('Thank you! Your feedback has been submitted successfully.');
    fetchComplaintData();
  };

  const handleReopenSubmit = async (remarks) => {
    await complaintService.reopenComplaint(id, remarks);
    toast.success('Complaint has been reopened for administrative review.');
    fetchComplaintData();
  };

  if (loading || !complaint) {
    return <LoadingSpinner text="Retrieving grievance status..." size="lg" />;
  }

  const isResolvedOrClosed =
    complaint.status === 'RESOLVED' || complaint.status === 'CLOSED';

  return (
    <div className="space-y-6">
      {/* Navigation Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <Link
          to="/complainant/complaints"
          className="inline-flex items-center text-xs font-semibold text-slate-600 hover:text-slate-900 transition-colors"
        >
          <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to My Grievances
        </Link>

        {/* Action Buttons for Complainant */}
        <div className="flex flex-wrap items-center gap-2">
          {isResolvedOrClosed && !feedback && (
            <button
              onClick={() => setFeedbackModalOpen(true)}
              className="inline-flex items-center px-3.5 py-1.5 text-xs font-semibold text-amber-800 bg-amber-50 hover:bg-amber-100 border border-amber-200 rounded-lg transition-colors"
            >
              <Star className="w-3.5 h-3.5 mr-1.5 text-amber-500 fill-amber-500" />
              Submit Feedback
            </button>
          )}

          {isResolvedOrClosed && (
            <button
              onClick={() => setReopenModalOpen(true)}
              className="inline-flex items-center px-3.5 py-1.5 text-xs font-semibold text-orange-800 bg-orange-50 hover:bg-orange-100 border border-orange-200 rounded-lg transition-colors"
            >
              <RotateCcw className="w-3.5 h-3.5 mr-1.5" />
              Reopen Case
            </button>
          )}
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

        {/* Status notification notice */}
        {isResolvedOrClosed ? (
          <div className="mt-4 p-3 rounded-lg bg-emerald-50 border border-emerald-200 flex items-center space-x-2 text-emerald-800 text-xs font-medium">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
            <span>
              This grievance has been officially resolved by the institution. If the remedial actions are incomplete, you may reopen this case using the button above.
            </span>
          </div>
        ) : (
          <div className="mt-4 p-3 rounded-lg bg-blue-50 border border-blue-200 flex items-center space-x-2 text-blue-800 text-xs font-medium">
            <AlertTriangle className="w-4 h-4 text-blue-600 flex-shrink-0" />
            <span>
              Case currently in progress with target SLA completion date: {formatDate(complaint.dueDate)}.
            </span>
          </div>
        )}

        {/* Description */}
        <div className="mt-4">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
            Your Grievance Statement
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
            <span className="text-slate-400 block font-medium">Target Institution</span>
            <span className="font-semibold text-slate-800 mt-1 block">
              {complaint.institutionName}
            </span>
          </div>
          <div>
            <span className="text-slate-400 block font-medium">Date Filed</span>
            <span className="font-semibold text-slate-800 mt-1 block">
              {formatDateTime(complaint.createdAt)}
            </span>
          </div>
          <div>
            <span className="text-slate-400 block font-medium">Target SLA Date</span>
            <span className="font-semibold text-slate-800 mt-1 block">
              {formatDate(complaint.dueDate)}
            </span>
          </div>
        </div>
      </div>

      {/* Timeline Section & Feedback Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Institution & Feedback status */}
        <div className="space-y-6 lg:col-span-1">
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center">
              <Building className="w-3.5 h-3.5 mr-1.5 text-blue-600" />
              Institution Responding
            </h3>
            <p className="font-semibold text-xs text-slate-900">{complaint.institutionName}</p>
            <p className="text-[11px] text-slate-500 font-mono mt-0.5">Code: {complaint.institutionCode}</p>
          </div>

          {/* Attachments Card */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center justify-between">
              <span className="flex items-center">
                <Paperclip className="w-3.5 h-3.5 mr-1.5 text-blue-600" />
                Submitted Evidence Documents
              </span>
              <span className="font-semibold text-slate-500 bg-slate-100 px-2 py-0.5 rounded text-[11px]">
                {attachments.length}
              </span>
            </h3>

            {attachments.length === 0 ? (
              <div className="text-xs text-slate-400 italic bg-slate-50 p-3 rounded-lg border border-slate-100 text-center">
                No attachments uploaded with this grievance.
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

          {feedback ? (
            <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center">
                <Star className="w-3.5 h-3.5 mr-1.5 text-amber-500 fill-amber-500" />
                Your Submitted Feedback
              </h3>
              <div className="space-y-2 text-xs">
                <div className="flex items-center space-x-1">
                  {[1, 2, 3, 4, 5].map((s) => (
                    <Star
                      key={s}
                      className={`w-4 h-4 ${
                        s <= feedback.rating ? 'text-amber-400 fill-amber-400' : 'text-slate-300'
                      }`}
                    />
                  ))}
                  <span className="font-semibold text-slate-700 ml-1.5">{feedback.rating} out of 5</span>
                </div>
                {feedback.comments && (
                  <p className="text-slate-600 italic bg-amber-50/50 p-2.5 rounded border border-amber-100">
                    "{feedback.comments}"
                  </p>
                )}
                <p className="text-[10px] text-slate-400">
                  Recorded on {formatDate(feedback.createdAt)}
                </p>
              </div>
            </div>
          ) : isResolvedOrClosed ? (
            <div className="bg-amber-50 rounded-xl border border-amber-200 p-5 text-xs text-amber-900">
              <h4 className="font-bold flex items-center mb-1">
                <Star className="w-4 h-4 mr-1 text-amber-600" /> Resolution Feedback
              </h4>
              <p className="text-amber-700 mb-3">
                Please help AICTE evaluate institution responsiveness by rating the resolution provided.
              </p>
              <button
                onClick={() => setFeedbackModalOpen(true)}
                className="w-full py-1.5 px-3 bg-amber-600 hover:bg-amber-700 text-white font-semibold rounded-lg transition-colors text-center"
              >
                Rate Grievance Redressal
              </button>
            </div>
          ) : null}
        </div>

        {/* Right Column: Case Timeline */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm lg:col-span-2">
          <div className="mb-6 pb-3 border-b border-slate-200">
            <h2 className="text-sm font-bold text-slate-900">Official Case Progress Timeline</h2>
            <p className="text-xs text-slate-500">Live tracker of nodal officer actions, hearings, and official remarks</p>
          </div>

          <ComplaintTimeline updates={updates} />
        </div>
      </div>

      <FeedbackModal
        isOpen={feedbackModalOpen}
        onClose={() => setFeedbackModalOpen(false)}
        complaintNumber={complaint.complaintNumber}
        onSubmit={handleFeedbackSubmit}
      />

      <ReopenModal
        isOpen={reopenModalOpen}
        onClose={() => setReopenModalOpen(false)}
        complaintNumber={complaint.complaintNumber}
        onSubmit={handleReopenSubmit}
      />
    </div>
  );
};

export default ComplainantComplaintDetailPage;
