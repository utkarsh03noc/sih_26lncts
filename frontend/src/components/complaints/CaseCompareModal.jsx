import React, { useState, useEffect } from 'react';
import { complaintService } from '../../services/complaintService';
import { useToast } from '../../hooks/useToast';
import StatusBadge from '../common/StatusBadge';
import PriorityBadge from '../common/PriorityBadge';
import LoadingSpinner from '../common/LoadingSpinner';
import { formatDate } from '../../utils/formatters';
import {
  X,
  GitMerge,
  Link2,
  CheckCircle2,
  AlertTriangle,
  Building2,
  Info,
  ShieldCheck,
  Calendar,
  User,
  ArrowRight,
  Layers,
} from 'lucide-react';

const CaseCompareModal = ({ isOpen, onClose, caseAId, caseBId, onMergeSuccess, onLinkSuccess }) => {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState(null);
  const [selectedPrimaryId, setSelectedPrimaryId] = useState(null);
  const [mode, setMode] = useState('merge'); // 'merge' or 'link'
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const toast = useToast();

  useEffect(() => {
    if (isOpen && caseAId && caseBId) {
      loadComparison();
    }
  }, [isOpen, caseAId, caseBId]);

  const loadComparison = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await complaintService.compareComplaints(caseAId, caseBId);
      if (res.success && res.data) {
        setData(res.data);
        setSelectedPrimaryId(caseAId);
        setReason('');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to compare complaints');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const handleMerge = async (e) => {
    e.preventDefault();
    if (!reason.trim() || reason.trim().length < 10) {
      setError('Please provide a merge justification of at least 10 characters.');
      return;
    }

    const primaryId = selectedPrimaryId;
    const secondaryId = selectedPrimaryId === data.caseA.id ? data.caseB.id : data.caseA.id;

    try {
      setSubmitting(true);
      setError('');
      const res = await complaintService.mergeComplaints({
        primaryComplaintId: primaryId,
        secondaryComplaintId: secondaryId,
        mergeReason: reason.trim(),
        similarityScore: data.similarityScore,
      });

      if (res.success) {
        toast.success(`Complaints merged successfully into Unified Case File!`);
        if (onMergeSuccess) {
          onMergeSuccess(res.data);
        }
        onClose();
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to merge complaints');
    } finally {
      setSubmitting(false);
    }
  };

  const handleLinkRelated = async (e) => {
    e.preventDefault();
    if (!reason.trim() || reason.trim().length < 5) {
      setError('Please provide a relation note of at least 5 characters.');
      return;
    }

    try {
      setSubmitting(true);
      setError('');
      const res = await complaintService.linkRelatedComplaints({
        sourceComplaintId: data.caseA.id,
        targetComplaintId: data.caseB.id,
        linkReason: reason.trim(),
      });

      if (res.success) {
        toast.success('Complaints successfully linked as related!');
        if (onLinkSuccess) {
          onLinkSuccess(res.data);
        }
        onClose();
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to link complaints');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs overflow-y-auto">
      <div className="relative w-full max-w-5xl bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden my-6">
        {/* Header */}
        <div className="px-6 py-4 bg-slate-900 text-white flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center shadow">
              <GitMerge className="w-4 h-4 text-white" />
            </div>
            <div>
              <h3 className="text-sm font-bold tracking-tight">Review & Compare Grievances</h3>
              <p className="text-[11px] text-slate-400">
                Officer duplicate assessment, unified case consolidation, and related case linking
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {loading ? (
          <div className="p-12">
            <LoadingSpinner text="Generating cross-case comparison and similarity dossier..." />
          </div>
        ) : error && !data ? (
          <div className="p-8 text-center space-y-4">
            <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs">
              {error}
            </div>
            <button
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg"
            >
              Close
            </button>
          </div>
        ) : data && (
          <div className="p-6 space-y-5 max-h-[80vh] overflow-y-auto">
            {/* Top Similarity & PID Badge Bar */}
            <div className="p-4 rounded-xl bg-gradient-to-r from-indigo-50/80 via-purple-50/60 to-white border border-indigo-200 space-y-2.5">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center space-x-2">
                  <span className={`text-[11px] font-bold px-2 py-0.5 rounded border ${
                    data.similarityScore >= 50
                      ? 'bg-purple-100 text-purple-800 border-purple-300'
                      : 'bg-amber-100 text-amber-800 border-amber-300'
                  }`}>
                    {data.matchType === 'SEMANTIC_SIMILARITY' ? '🧠 Semantic Match' :
                     data.matchType === 'SEMANTIC_AND_KEYWORD' ? '🧠 Semantic & Keywords' :
                     data.matchType === 'EXACT_MATCH' ? '⚠️ Exact Match' : '🔍 Keyword Overlap'} ({Math.round(data.similarityScore)}%)
                  </span>

                  {data.sameInstitution ? (
                    <span className="inline-flex items-center text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200 px-2 py-0.5 rounded">
                      <CheckCircle2 className="w-3 h-3 mr-1 text-emerald-600" />
                      Same Institution PID ({data.institutionPidA})
                    </span>
                  ) : (
                    <span className="inline-flex items-center text-[10px] font-semibold bg-rose-50 text-rose-700 border border-rose-200 px-2 py-0.5 rounded">
                      <AlertTriangle className="w-3 h-3 mr-1 text-rose-600" />
                      Different Institution PIDs ({data.institutionPidA} vs {data.institutionPidB})
                    </span>
                  )}
                </div>

                <div className="text-[11px] text-slate-500 font-medium">
                  Institution: <strong>{data.institutionName}</strong>
                </div>
              </div>

              {/* Matched Concepts */}
              {data.matchedConcepts && data.matchedConcepts.length > 0 && (
                <div className="flex flex-wrap items-center gap-1.5 pt-1">
                  <span className="text-[10px] uppercase font-bold text-slate-400">Concepts:</span>
                  {data.matchedConcepts.map((c, i) => (
                    <span key={i} className="text-[10px] font-medium bg-purple-100 text-purple-700 px-2 py-0.5 rounded-full border border-purple-200">
                      #{c}
                    </span>
                  ))}
                </div>
              )}

              {data.matchExplanation && (
                <p className="text-[11px] text-slate-600 italic leading-relaxed">
                  {data.matchExplanation}
                </p>
              )}
            </div>

            {error && (
              <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-rose-800 text-xs">
                {error}
              </div>
            )}

            {/* Side-by-Side Comparison Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Case A */}
              <div className={`p-4 rounded-xl border transition-all ${
                selectedPrimaryId === data.caseA.id
                  ? 'border-blue-500 bg-blue-50/20 ring-2 ring-blue-500/20'
                  : 'border-slate-200 bg-white'
              }`}>
                <div className="flex items-center justify-between pb-2 border-b border-slate-200">
                  <div className="flex items-center space-x-2">
                    <span className="font-mono text-xs font-bold text-blue-700">
                      {data.caseA.complaintNumber}
                    </span>
                    <StatusBadge status={data.caseA.status} />
                  </div>
                  <PriorityBadge priority={data.caseA.priority} />
                </div>

                <div className="space-y-2 mt-3 text-xs">
                  <div>
                    <span className="text-[10px] font-bold uppercase text-slate-400 block">Subject</span>
                    <p className="font-semibold text-slate-900 leading-snug">{data.caseA.title}</p>
                  </div>

                  <div>
                    <span className="text-[10px] font-bold uppercase text-slate-400 block">Description</span>
                    <p className="text-slate-600 line-clamp-4 leading-relaxed bg-slate-50 p-2 rounded border border-slate-100 text-[11px]">
                      {data.caseA.description}
                    </p>
                  </div>

                  <div className="grid grid-cols-2 gap-2 pt-1 text-[11px]">
                    <div>
                      <span className="text-[10px] text-slate-400 block">Bureau & Cell</span>
                      <span className="font-semibold text-slate-700">
                        {data.caseA.bureauCode || 'RB'} - {data.caseA.cellCode || 'PGRC'}
                      </span>
                    </div>
                    <div>
                      <span className="text-[10px] text-slate-400 block">Assigned Officer</span>
                      <span className="font-medium text-slate-700">
                        {data.caseA.assignedOfficerName || 'Unassigned'}
                      </span>
                    </div>
                    <div>
                      <span className="text-[10px] text-slate-400 block">Complainant</span>
                      <span className="font-medium text-slate-700">
                        {data.caseA.complainantName || 'Anonymous'}
                      </span>
                    </div>
                    <div>
                      <span className="text-[10px] text-slate-400 block">Filed On</span>
                      <span className="text-slate-600">
                        {formatDate(data.caseA.createdAt)}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Primary Selector Toggle */}
                {mode === 'merge' && (
                  <div className="mt-4 pt-3 border-t border-slate-200">
                    <label className="flex items-center space-x-2 cursor-pointer">
                      <input
                        type="radio"
                        name="primarySelection"
                        checked={selectedPrimaryId === data.caseA.id}
                        onChange={() => setSelectedPrimaryId(data.caseA.id)}
                        disabled={data.caseA.status === 'MERGED'}
                        className="text-blue-600 focus:ring-blue-500"
                      />
                      <span className="text-xs font-bold text-slate-800">
                        Designate as Master Primary Case
                      </span>
                    </label>
                  </div>
                )}
              </div>

              {/* Case B */}
              <div className={`p-4 rounded-xl border transition-all ${
                selectedPrimaryId === data.caseB.id
                  ? 'border-blue-500 bg-blue-50/20 ring-2 ring-blue-500/20'
                  : 'border-slate-200 bg-white'
              }`}>
                <div className="flex items-center justify-between pb-2 border-b border-slate-200">
                  <div className="flex items-center space-x-2">
                    <span className="font-mono text-xs font-bold text-blue-700">
                      {data.caseB.complaintNumber}
                    </span>
                    <StatusBadge status={data.caseB.status} />
                  </div>
                  <PriorityBadge priority={data.caseB.priority} />
                </div>

                <div className="space-y-2 mt-3 text-xs">
                  <div>
                    <span className="text-[10px] font-bold uppercase text-slate-400 block">Subject</span>
                    <p className="font-semibold text-slate-900 leading-snug">{data.caseB.title}</p>
                  </div>

                  <div>
                    <span className="text-[10px] font-bold uppercase text-slate-400 block">Description</span>
                    <p className="text-slate-600 line-clamp-4 leading-relaxed bg-slate-50 p-2 rounded border border-slate-100 text-[11px]">
                      {data.caseB.description}
                    </p>
                  </div>

                  <div className="grid grid-cols-2 gap-2 pt-1 text-[11px]">
                    <div>
                      <span className="text-[10px] text-slate-400 block">Bureau & Cell</span>
                      <span className="font-semibold text-slate-700">
                        {data.caseB.bureauCode || 'RB'} - {data.caseB.cellCode || 'PGRC'}
                      </span>
                    </div>
                    <div>
                      <span className="text-[10px] text-slate-400 block">Assigned Officer</span>
                      <span className="font-medium text-slate-700">
                        {data.caseB.assignedOfficerName || 'Unassigned'}
                      </span>
                    </div>
                    <div>
                      <span className="text-[10px] text-slate-400 block">Complainant</span>
                      <span className="font-medium text-slate-700">
                        {data.caseB.complainantName || 'Anonymous'}
                      </span>
                    </div>
                    <div>
                      <span className="text-[10px] text-slate-400 block">Filed On</span>
                      <span className="text-slate-600">
                        {formatDate(data.caseB.createdAt)}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Primary Selector Toggle */}
                {mode === 'merge' && (
                  <div className="mt-4 pt-3 border-t border-slate-200">
                    <label className="flex items-center space-x-2 cursor-pointer">
                      <input
                        type="radio"
                        name="primarySelection"
                        checked={selectedPrimaryId === data.caseB.id}
                        onChange={() => setSelectedPrimaryId(data.caseB.id)}
                        disabled={data.caseB.status === 'MERGED'}
                        className="text-blue-600 focus:ring-blue-500"
                      />
                      <span className="text-xs font-bold text-slate-800">
                        Designate as Master Primary Case
                      </span>
                    </label>
                  </div>
                )}
              </div>
            </div>

            {/* Mode Selector: Merge vs Link as Related */}
            <div className="pt-2 border-t border-slate-200">
              <div className="flex border-b border-slate-200 mb-4">
                <button
                  type="button"
                  onClick={() => setMode('merge')}
                  className={`pb-2 px-4 text-xs font-bold border-b-2 transition-colors flex items-center ${
                    mode === 'merge'
                      ? 'border-blue-600 text-blue-600'
                      : 'border-transparent text-slate-500 hover:text-slate-800'
                  }`}
                >
                  <GitMerge className="w-3.5 h-3.5 mr-1.5" />
                  Approve Merge into Unified Case File
                </button>
                <button
                  type="button"
                  onClick={() => setMode('link')}
                  className={`pb-2 px-4 text-xs font-bold border-b-2 transition-colors flex items-center ${
                    mode === 'link'
                      ? 'border-indigo-600 text-indigo-600'
                      : 'border-transparent text-slate-500 hover:text-slate-800'
                  }`}
                >
                  <Link2 className="w-3.5 h-3.5 mr-1.5" />
                  Link as Related Cases (Keep Independent)
                </button>
              </div>

              {mode === 'merge' ? (
                <form onSubmit={handleMerge} className="space-y-3">
                  <div className="p-3 bg-amber-50 rounded-lg border border-amber-200 text-xs text-amber-900 space-y-1">
                    <p className="font-bold flex items-center">
                      <ShieldCheck className="w-4 h-4 mr-1 text-amber-700" />
                      Officer-Approved Unified Case Consolidation
                    </p>
                    <p className="text-[11px] leading-relaxed text-amber-800">
                      The chosen secondary complaint will be linked into the Master Case. The secondary status changes to <strong>MERGED</strong>, and a consolidated timeline and cross-bureau coordination view are established. All original statements, evidence, and attachments remain strictly preserved.
                    </p>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">
                      Merge Approval Justification / Reason <span className="text-rose-500">*</span>
                    </label>
                    <textarea
                      rows="3"
                      required
                      minLength={10}
                      placeholder="Detail the factual findings confirming duplicate reporting (e.g., identical hostel block water disruption reported independently by two roommates)..."
                      value={reason}
                      onChange={(e) => setReason(e.target.value)}
                      className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 leading-relaxed"
                    />
                  </div>

                  <div className="flex items-center justify-end space-x-3 pt-2">
                    <button
                      type="button"
                      onClick={onClose}
                      className="px-4 py-2 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={submitting || !data.canMerge}
                      className="inline-flex items-center px-4 py-2 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm disabled:opacity-50"
                    >
                      <GitMerge className="w-3.5 h-3.5 mr-1.5" />
                      {submitting ? 'Executing Merge...' : 'Approve Merge & Generate Unified Dossier'}
                    </button>
                  </div>
                </form>
              ) : (
                <form onSubmit={handleLinkRelated} className="space-y-3">
                  <div className="p-3 bg-indigo-50 rounded-lg border border-indigo-200 text-xs text-indigo-900 space-y-1">
                    <p className="font-bold flex items-center">
                      <Link2 className="w-4 h-4 mr-1 text-indigo-700" />
                      Link Related-but-Distinct Cases
                    </p>
                    <p className="text-[11px] leading-relaxed text-indigo-800">
                      Both complaints will remain active and open under their respective workflows. Cross-referencing links will be displayed on each complaint dossier for coordination.
                    </p>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">
                      Relationship Context / Coordination Note <span className="text-rose-500">*</span>
                    </label>
                    <textarea
                      rows="3"
                      required
                      minLength={5}
                      placeholder="e.g., Related procedural grievances regarding admission counseling cutoff; cross-reference files for joint committee review..."
                      value={reason}
                      onChange={(e) => setReason(e.target.value)}
                      className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 leading-relaxed"
                    />
                  </div>

                  <div className="flex items-center justify-end space-x-3 pt-2">
                    <button
                      type="button"
                      onClick={onClose}
                      className="px-4 py-2 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={submitting}
                      className="inline-flex items-center px-4 py-2 text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow-sm disabled:opacity-50"
                    >
                      <Link2 className="w-3.5 h-3.5 mr-1.5" />
                      {submitting ? 'Linking Cases...' : 'Save Related Case Link'}
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CaseCompareModal;
