import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import { useToast } from '../../hooks/useToast';
import StatusBadge from '../common/StatusBadge';
import PriorityBadge from '../common/PriorityBadge';
import { formatDate, formatDateTime, formatFileSize } from '../../utils/formatters';
import {
  GitMerge,
  Layers,
  Network,
  Clock,
  Paperclip,
  ArrowRight,
  RotateCcw,
  AlertTriangle,
  CheckCircle2,
  ExternalLink,
  ShieldAlert,
  FileText,
  User,
  Building,
} from 'lucide-react';

const UnifiedCaseDossier = ({ complaint, onReload }) => {
  const [unifiedFile, setUnifiedFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('cases'); // 'cases', 'timeline', 'bureaus', 'attachments'
  
  // Unmerge Modal state
  const [unmergeModalOpen, setUnmergeModalOpen] = useState(false);
  const [targetSecondaryId, setTargetSecondaryId] = useState(null);
  const [targetSecondaryNumber, setTargetSecondaryNumber] = useState('');
  const [unmergeReason, setUnmergeReason] = useState('');
  const [unmerging, setUnmerging] = useState(false);
  const [unmergeError, setUnmergeError] = useState('');

  const toast = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    if (complaint && (complaint.merged || complaint.mergedCasesCount > 0 || complaint.status === 'MERGED')) {
      loadUnifiedFile();
    }
  }, [complaint?.id, complaint?.status]);

  const loadUnifiedFile = async () => {
    try {
      setLoading(true);
      const res = await complaintService.getUnifiedCaseFile(complaint.id);
      if (res.success && res.data) {
        setUnifiedFile(res.data);
      }
    } catch (err) {
      console.warn('Could not load unified file:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenUnmerge = (secId, secNumber) => {
    setTargetSecondaryId(secId);
    setTargetSecondaryNumber(secNumber);
    setUnmergeReason('');
    setUnmergeError('');
    setUnmergeModalOpen(true);
  };

  const handleExecuteUnmerge = async (e) => {
    e.preventDefault();
    if (!unmergeReason.trim() || unmergeReason.trim().length < 10) {
      setUnmergeError('Please provide a justification of at least 10 characters for regulatory reversal.');
      return;
    }

    try {
      setUnmerging(true);
      setUnmergeError('');
      const primaryId = unifiedFile?.primaryComplaintId || complaint.mergedIntoComplaintId || complaint.id;

      const res = await complaintService.unmergeComplaints({
        primaryComplaintId: primaryId,
        secondaryComplaintId: targetSecondaryId,
        unmergeReason: unmergeReason.trim(),
      });

      if (res.success) {
        toast.success(`Complaint ${targetSecondaryNumber} successfully unmerged and restored.`);
        setUnmergeModalOpen(false);
        if (onReload) onReload();
        loadUnifiedFile();
      }
    } catch (err) {
      setUnmergeError(err.response?.data?.message || err.message || 'Failed to unmerge complaint');
    } finally {
      setUnmerging(false);
    }
  };

  // 1. Render Secondary Merged Notice if this case is merged into another
  if (complaint.status === 'MERGED' || complaint.merged) {
    return (
      <div className="p-4 rounded-xl bg-gradient-to-r from-purple-50 via-indigo-50/50 to-white border border-purple-300 shadow-sm space-y-3">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-start space-x-3">
            <div className="w-8 h-8 rounded-lg bg-purple-600 text-white flex items-center justify-center flex-shrink-0 shadow">
              <GitMerge className="w-4 h-4" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h4 className="text-xs font-bold text-purple-950 uppercase tracking-wider">
                  Case Merged into Master Dossier
                </h4>
                <span className="text-[10px] font-bold bg-purple-100 text-purple-800 px-2 py-0.5 rounded border border-purple-200">
                  Status: MERGED
                </span>
              </div>
              <p className="text-xs text-purple-900 mt-1 leading-relaxed">
                This grievance was officially merged into Master Case{' '}
                <strong className="font-mono text-purple-700">
                  {complaint.mergedIntoComplaintNumber || 'Master Case'}
                </strong>
                . All subsequent investigations, hearing notices, and redressal actions are consolidated into the master file.
              </p>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row items-end sm:items-center gap-2 flex-shrink-0">
            {complaint.mergedIntoComplaintNumber && (
              <Link
                to={`/admin/complaints/${complaint.mergedIntoComplaintNumber}`}
                className="inline-flex items-center px-3 py-1.5 text-xs font-bold text-white bg-purple-600 hover:bg-purple-700 rounded-lg shadow-sm transition-colors"
              >
                View Master Case Dossier
                <ArrowRight className="w-3.5 h-3.5 ml-1.5" />
              </Link>
            )}

            <button
              type="button"
              onClick={() => handleOpenUnmerge(complaint.id, complaint.complaintNumber)}
              className="inline-flex items-center px-2.5 py-1.5 text-xs font-semibold text-rose-700 bg-rose-50 hover:bg-rose-100 border border-rose-200 rounded-lg transition-colors"
            >
              <RotateCcw className="w-3.5 h-3.5 mr-1 text-rose-600" />
              Unmerge Case
            </button>
          </div>
        </div>

        {/* Unmerge Modal */}
        {renderUnmergeModal()}
      </div>
    );
  }

  // 2. Render Unified Case Dossier if this complaint is a Primary Master Case with merged cases
  if (!unifiedFile || unifiedFile.totalMergedCases === 0) {
    return null;
  }

  function renderUnmergeModal() {
    if (!unmergeModalOpen) return null;
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
        <div className="w-full max-w-md bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
          <div className="px-5 py-4 bg-slate-900 text-white flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <RotateCcw className="w-4 h-4 text-rose-400" />
              <h3 className="text-xs font-bold tracking-tight">
                Regulatory Reversal: Unmerge Complaint {targetSecondaryNumber}
              </h3>
            </div>
            <button
              onClick={() => setUnmergeModalOpen(false)}
              className="text-slate-400 hover:text-white text-xs font-bold"
            >
              ✕
            </button>
          </div>

          <form onSubmit={handleExecuteUnmerge} className="p-5 space-y-4">
            <div className="p-3 rounded-lg bg-amber-50 border border-amber-200 text-xs text-amber-900">
              <p className="font-bold flex items-center">
                <AlertTriangle className="w-3.5 h-3.5 mr-1.5 text-amber-600" />
                Unmerge & Status Restoration Notice
              </p>
              <p className="text-[11px] text-amber-800 mt-0.5 leading-relaxed">
                Unmerging will detach complaint <strong>{targetSecondaryNumber}</strong> from this master case and restore its prior open investigation status. Both merge and unmerge actions will remain permanently in the regulatory audit log.
              </p>
            </div>

            {unmergeError && (
              <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-800 text-xs">
                {unmergeError}
              </div>
            )}

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Unmerge Justification / Reversal Reason <span className="text-rose-500">*</span>
              </label>
              <textarea
                rows="3"
                required
                minLength={10}
                placeholder="Detail the procedural basis for unmerging (e.g., subsequent discovery of distinct departmental liability or new independent evidence)..."
                value={unmergeReason}
                onChange={(e) => setUnmergeReason(e.target.value)}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-rose-500"
              />
            </div>

            <div className="flex items-center justify-end space-x-2 pt-2">
              <button
                type="button"
                onClick={() => setUnmergeModalOpen(false)}
                className="px-3 py-1.5 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={unmerging}
                className="px-4 py-1.5 text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 rounded-lg shadow-sm disabled:opacity-50"
              >
                {unmerging ? 'Unmerging...' : 'Confirm Regulatory Unmerge'}
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl border border-indigo-200 shadow-sm overflow-hidden space-y-0">
      {/* Dossier Banner Header */}
      <div className="p-5 bg-gradient-to-r from-indigo-900 via-slate-900 to-blue-950 text-white flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center shadow">
            <Layers className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <span className="font-mono text-sm font-bold text-blue-300">
                {unifiedFile.unifiedCaseNumber}
              </span>
              <span className="text-[10px] font-bold bg-blue-500/30 text-blue-200 border border-blue-400/30 px-2 py-0.5 rounded-full">
                Unified Case File
              </span>
            </div>
            <p className="text-xs text-slate-300 mt-0.5">
              Consolidated master tracking dossier coordinating {unifiedFile.totalMergedCases} merged grievance{unifiedFile.totalMergedCases > 1 ? 's' : ''} across AICTE Bureaus
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-2">
          <span className="px-2.5 py-1 bg-white/10 rounded-lg text-xs font-semibold text-slate-200 border border-white/10">
            {unifiedFile.crossBureauCoordination.length} Bureau Desks Coordinating
          </span>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200 bg-slate-50 px-5 text-xs font-semibold">
        <button
          type="button"
          onClick={() => setActiveTab('cases')}
          className={`py-3 px-3 border-b-2 transition-colors flex items-center ${
            activeTab === 'cases'
              ? 'border-blue-600 text-blue-600 font-bold bg-white'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <GitMerge className="w-3.5 h-3.5 mr-1.5" />
          Merged Complaints ({unifiedFile.totalMergedCases})
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('bureaus')}
          className={`py-3 px-3 border-b-2 transition-colors flex items-center ${
            activeTab === 'bureaus'
              ? 'border-blue-600 text-blue-600 font-bold bg-white'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <Network className="w-3.5 h-3.5 mr-1.5" />
          Cross-Bureau Desks ({unifiedFile.crossBureauCoordination.length})
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('timeline')}
          className={`py-3 px-3 border-b-2 transition-colors flex items-center ${
            activeTab === 'timeline'
              ? 'border-blue-600 text-blue-600 font-bold bg-white'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <Clock className="w-3.5 h-3.5 mr-1.5" />
          Consolidated Timeline ({unifiedFile.consolidatedTimeline.length})
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('attachments')}
          className={`py-3 px-3 border-b-2 transition-colors flex items-center ${
            activeTab === 'attachments'
              ? 'border-blue-600 text-blue-600 font-bold bg-white'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <Paperclip className="w-3.5 h-3.5 mr-1.5" />
          Consolidated Attachments ({unifiedFile.consolidatedAttachments.length})
        </button>
      </div>

      {/* Tab Contents */}
      <div className="p-5">
        {/* Tab 1: Merged Child Complaints */}
        {activeTab === 'cases' && (
          <div className="space-y-3">
            <div className="grid grid-cols-1 gap-3">
              {unifiedFile.mergedComplaints.map((c) => (
                <div
                  key={c.id}
                  className="p-4 rounded-xl border border-slate-200 bg-slate-50/60 hover:bg-slate-50 transition-colors flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs"
                >
                  <div className="space-y-1 max-w-xl">
                    <div className="flex items-center space-x-2">
                      <span className="font-mono font-bold text-blue-700">
                        {c.complaintNumber}
                      </span>
                      <StatusBadge status={c.status} />
                      <PriorityBadge priority={c.priority} />
                      <span className="text-[10px] text-slate-400">
                        Filed {formatDate(c.createdAt)}
                      </span>
                    </div>
                    <div className="font-semibold text-slate-900 text-xs">
                      {c.title}
                    </div>
                    <p className="text-[11px] text-slate-500 line-clamp-2 leading-relaxed">
                      {c.description}
                    </p>
                    <div className="text-[10px] text-slate-400 pt-1 flex items-center space-x-3">
                      <span>Complainant: <strong>{c.complainantName || 'Anonymous'}</strong></span>
                      <span>Bureau: <strong>{c.bureauCode || 'RB'} - {c.cellCode || 'PGRC'}</strong></span>
                      <span>Officer: <strong>{c.assignedOfficerName || 'Unassigned'}</strong></span>
                    </div>
                  </div>

                  <div className="flex items-center space-x-2 flex-shrink-0 self-end sm:self-center">
                    <Link
                      to={`/admin/complaints/${c.complaintNumber || c.id}`}
                      className="px-2.5 py-1 text-xs font-semibold text-blue-700 bg-white border border-blue-200 rounded-lg hover:bg-blue-50 transition-colors"
                    >
                      Inspect
                    </Link>
                    <button
                      type="button"
                      onClick={() => handleOpenUnmerge(c.id, c.complaintNumber)}
                      className="px-2.5 py-1 text-xs font-semibold text-rose-700 bg-white border border-rose-200 rounded-lg hover:bg-rose-50 transition-colors"
                    >
                      Unmerge
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Tab 2: Cross-Bureau Coordination */}
        {activeTab === 'bureaus' && (
          <div className="space-y-3">
            <p className="text-xs text-slate-500">
              Active Bureau & Cell desks coordinating investigation and compliance enforcement across the consolidated dossier:
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {unifiedFile.crossBureauCoordination.map((b, idx) => (
                <div key={idx} className="p-3.5 rounded-xl border border-slate-200 bg-white shadow-2xs text-xs space-y-1.5">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-slate-900">
                      {b.bureauName} ({b.bureauCode})
                    </span>
                    <span className="text-[10px] font-bold bg-blue-50 text-blue-700 border border-blue-200 px-2 py-0.5 rounded">
                      {b.roleInCase}
                    </span>
                  </div>
                  <div className="text-[11px] text-slate-600">
                    Operational Cell: <strong>{b.cellName} ({b.cellCode})</strong>
                  </div>
                  <div className="text-[11px] text-slate-500 pt-1 border-t border-slate-100 flex items-center justify-between">
                    <span>Officer: <strong>{b.assignedOfficerName}</strong></span>
                    <span className="font-mono text-[10px] text-slate-400">{b.complaintNumber}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Tab 3: Consolidated Unified Timeline */}
        {activeTab === 'timeline' && (
          <div className="space-y-3">
            <p className="text-xs text-slate-500">
              Complete chronologically sorted event log merging procedural actions, bureau transfers, and officer updates from all connected cases:
            </p>
            <div className="space-y-2.5">
              {unifiedFile.consolidatedTimeline.map((item, idx) => (
                <div
                  key={idx}
                  className={`p-3 rounded-lg border text-xs space-y-1 ${
                    item.isPrimaryCase
                      ? 'bg-blue-50/40 border-blue-200'
                      : 'bg-purple-50/40 border-purple-200'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-2">
                      <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded border ${
                        item.isPrimaryCase
                          ? 'bg-blue-100 text-blue-800 border-blue-300'
                          : 'bg-purple-100 text-purple-800 border-purple-300'
                      }`}>
                        {item.isPrimaryCase ? 'Master Case' : `Merged: ${item.originatingComplaintNumber}`}
                      </span>
                      <span className="font-bold text-slate-900">{item.title}</span>
                    </div>
                    <span className="text-[10px] text-slate-400">
                      {formatDateTime(item.timestamp)}
                    </span>
                  </div>
                  <p className="text-slate-600 text-[11px] leading-relaxed">
                    {item.description}
                  </p>
                  <div className="text-[10px] text-slate-400 pt-0.5">
                    Logged by <strong>{item.actorName}</strong> ({item.actorRole})
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Tab 4: Consolidated Attachments */}
        {activeTab === 'attachments' && (
          <div className="space-y-3">
            {unifiedFile.consolidatedAttachments.length === 0 ? (
              <div className="p-6 text-center text-xs text-slate-400 italic bg-slate-50 rounded-xl">
                No attachments uploaded across these complaints.
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                {unifiedFile.consolidatedAttachments.map((att) => (
                  <div
                    key={att.id}
                    className="p-3 bg-white rounded-lg border border-slate-200 flex items-center justify-between text-xs hover:bg-slate-50 transition-colors"
                  >
                    <div className="flex items-center space-x-2.5 min-w-0">
                      <FileText className="w-4 h-4 text-blue-600 flex-shrink-0" />
                      <div className="truncate">
                        <p className="font-semibold text-slate-800 truncate">{att.fileName}</p>
                        <p className="text-[10px] text-slate-400">
                          {formatFileSize(att.fileSize)} • {formatDate(att.uploadedAt)}
                        </p>
                      </div>
                    </div>
                    <span className="text-blue-600 font-semibold text-[11px] hover:underline cursor-pointer">
                      Download
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Unmerge Modal */}
      {renderUnmergeModal()}
    </div>
  );
};

export default UnifiedCaseDossier;
