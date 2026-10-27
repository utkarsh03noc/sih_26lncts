import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import { institutionService } from '../../services/institutionService';
import { useToast } from '../../hooks/useToast';
import { CATEGORIES, PRIORITY_OPTIONS } from '../../utils/constants';
import {
  ArrowLeft,
  Send,
  AlertCircle,
  Building2,
  Info,
  CopyCheck,
  AlertTriangle,
  CheckCircle2,
  Search,
  Sparkles,
  Bot,
  RefreshCw,
  Check,
} from 'lucide-react';

const RegisterComplaintPage = () => {
  const [institutions, setInstitutions] = useState([]);
  const [loadingInstitutions, setLoadingInstitutions] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    title: '',
    category: 'ACADEMIC',
    priority: 'MEDIUM',
    institutionId: '',
    description: '',
    bureauId: null,
    cellId: null,
  });

  // Duplicate detection state
  const [checkingDuplicates, setCheckingDuplicates] = useState(false);
  const [duplicateMatches, setDuplicateMatches] = useState([]);
  const [duplicateAcknowledged, setDuplicateAcknowledged] = useState(false);
  const [duplicateRemarks, setDuplicateRemarks] = useState('');

  // AI Classification state
  const [analyzingClassification, setAnalyzingClassification] = useState(false);
  const [classificationResult, setClassificationResult] = useState(null);
  const [classificationApplied, setClassificationApplied] = useState(false);

  const toast = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    loadInstitutions();
  }, []);

  const loadInstitutions = async () => {
    try {
      setLoadingInstitutions(true);
      const res = await institutionService.getActiveInstitutions();
      setInstitutions(res.data || []);
      if (res.data && res.data.length > 0) {
        setFormData((prev) => ({ ...prev, institutionId: res.data[0].id }));
      }
    } catch (err) {
      setError('Failed to load institution directory');
    } finally {
      setLoadingInstitutions(false);
    }
  };

  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  // Trigger duplicate check when title or institution or category changes
  const runDuplicateCheck = async () => {
    if (!formData.title.trim() || formData.title.trim().length < 6 || !formData.institutionId) {
      setDuplicateMatches([]);
      return;
    }

    try {
      setCheckingDuplicates(true);
      const res = await complaintService.checkDuplicates({
        title: formData.title.trim(),
        description: formData.description.trim(),
        institutionId: Number(formData.institutionId),
        category: formData.category,
      });
      if (res.success && res.data) {
        setDuplicateMatches(res.data);
      }
    } catch (err) {
      console.warn('Duplicate check could not complete:', err);
    } finally {
      setCheckingDuplicates(false);
    }
  };

  const handleAnalyzeWithAI = async () => {
    if (!formData.title.trim() && !formData.description.trim()) {
      setError('Please provide at least a subject title or description for AI classification.');
      return;
    }
    try {
      setAnalyzingClassification(true);
      setError('');
      const res = await complaintService.classifyComplaint({
        title: formData.title.trim(),
        description: formData.description.trim(),
      });
      if (res.success && res.data) {
        setClassificationResult(res.data);
        setClassificationApplied(false);
      }
    } catch (err) {
      console.warn('AI classification error:', err);
      toast.error('AI assistant could not complete classification. You may proceed manually.');
    } finally {
      setAnalyzingClassification(false);
    }
  };

  const handleApplySuggestions = () => {
    if (!classificationResult) return;
    setFormData((prev) => ({
      ...prev,
      category: classificationResult.suggestedCategory || prev.category,
      priority: classificationResult.suggestedPriority || prev.priority,
      bureauId: classificationResult.suggestedBureauId || prev.bureauId,
      cellId: classificationResult.suggestedCellId || prev.cellId,
    }));
    setClassificationApplied(true);
    toast.success(`Applied Category: ${classificationResult.categoryDisplayName}, Priority: ${classificationResult.suggestedPriority}, and Bureau/Cell routing`);
    setTimeout(runDuplicateCheck, 200);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.title.trim()) {
      setError('Please provide a descriptive subject title');
      return;
    }
    if (!formData.institutionId) {
      setError('Please select the target institution');
      return;
    }
    if (!formData.description.trim()) {
      setError('Please provide complete grievance details');
      return;
    }

    if (duplicateMatches.length > 0 && !duplicateAcknowledged) {
      setError('Similar existing grievances were detected for this institution. Please review the overlapping cases below and check the acknowledgment checkbox to confirm.');
      return;
    }

    try {
      setSubmitting(true);
      setError('');
      const res = await complaintService.createComplaint({
        ...formData,
        institutionId: Number(formData.institutionId),
        duplicateAcknowledged,
        duplicateRemarks: duplicateAcknowledged ? duplicateRemarks.trim() : null,
      });

      if (res.success && res.data) {
        toast.success(`Complaint registered successfully! Case ID: ${res.data.complaintNumber}`);
        navigate(`/complainant/complaints/${res.data.id}`);
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to register grievance');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <Link
        to="/complainant/complaints"
        className="inline-flex items-center text-xs font-semibold text-slate-600 hover:text-slate-900 transition-colors"
      >
        <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to My Complaints
      </Link>

      <div className="bg-white rounded-xl border border-slate-200 p-6 sm:p-8 shadow-sm">
        <div className="border-b border-slate-200 pb-4 mb-6">
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Register New Institution Grievance
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Submit formal grievance dossier to the centralized AICTE monitoring authority across Bureaus & Cells
          </p>
        </div>

        {error && (
          <div className="mb-6 p-4 rounded-lg bg-rose-50 border border-rose-200 text-rose-800 text-xs flex items-start space-x-3">
            <AlertCircle className="w-4 h-4 text-rose-600 mt-0.5 flex-shrink-0" />
            <div className="leading-relaxed">{error}</div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Institution Selector */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Accused / Target Institution <span className="text-rose-500">*</span>
            </label>
            <div className="relative">
              <select
                name="institutionId"
                value={formData.institutionId}
                onChange={(e) => {
                  handleChange(e);
                  setTimeout(runDuplicateCheck, 200);
                }}
                disabled={loadingInstitutions}
                required
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 bg-white"
              >
                {loadingInstitutions ? (
                  <option>Loading institutions directory...</option>
                ) : (
                  institutions.map((inst) => (
                    <option key={inst.id} value={inst.id}>
                      {inst.name} ({inst.code}) - {inst.location}
                    </option>
                  ))
                )}
              </select>
            </div>
            <p className="text-[11px] text-slate-400 mt-1">
              Select the approved university or college against which this grievance is being lodged
            </p>
          </div>

          {/* Grievance Category & Priority */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Grievance Category <span className="text-rose-500">*</span>
              </label>
              <select
                name="category"
                value={formData.category}
                onChange={(e) => {
                  handleChange(e);
                  setTimeout(runDuplicateCheck, 200);
                }}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 bg-white"
              >
                {CATEGORIES.map((cat) => (
                  <option key={cat.value} value={cat.value}>
                    {cat.label}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Urgency Priority <span className="text-rose-500">*</span>
              </label>
              <select
                name="priority"
                value={formData.priority}
                onChange={handleChange}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 bg-white"
              >
                {PRIORITY_OPTIONS.map((pri) => (
                  <option key={pri.value} value={pri.value}>
                    {pri.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Subject Title */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="block text-xs font-semibold text-slate-700">
                Subject / Title <span className="text-rose-500">*</span>
              </label>
              <button
                type="button"
                onClick={runDuplicateCheck}
                disabled={checkingDuplicates || !formData.title.trim()}
                className="text-[11px] font-semibold text-blue-600 hover:text-blue-800 inline-flex items-center"
              >
                <Search className="w-3 h-3 mr-1" />
                {checkingDuplicates ? 'Scanning similarity...' : 'Check Duplicate Cases'}
              </button>
            </div>
            <input
              type="text"
              name="title"
              required
              maxLength={200}
              placeholder="e.g., Water Shortage in Girls Hostel Block 3 or Capitation Fee Demanded for Admission"
              value={formData.title}
              onChange={handleChange}
              onBlur={runDuplicateCheck}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
            <p className="text-[11px] text-slate-400 mt-1">
              Provide a clear, specific subject summarizing the issue
            </p>
          </div>

          {/* Duplicate / Overlap Detection Warning Box */}
          {duplicateMatches.length > 0 && (
            <div className="p-4 bg-amber-50 border border-amber-300 rounded-xl space-y-3">
              <div className="flex items-start space-x-2.5">
                <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" />
                <div>
                  <h4 className="text-xs font-bold text-amber-950">
                    Potential Duplicate / Overlapping Complaints Detected ({duplicateMatches.length})
                  </h4>
                  <p className="text-[11px] text-amber-800 mt-0.5">
                    Our AICTE similarity engine identified existing complaints against this institution with matching concepts or keywords:
                  </p>
                </div>
              </div>

              <div className="space-y-2 mt-2">
                {duplicateMatches.map((m) => (
                  <div key={m.complaintId} className="p-3 bg-white rounded-lg border border-amber-200 text-xs space-y-1.5">
                    <div className="flex flex-wrap items-center justify-between gap-1">
                      <span className="font-mono font-bold text-brand-600">{m.complaintNumber}</span>
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                        m.matchType === 'SEMANTIC_SIMILARITY' || m.matchType === 'SEMANTIC_AND_KEYWORD'
                          ? 'bg-purple-100 text-purple-800 border border-purple-300'
                          : m.matchType === 'EXACT_MATCH'
                          ? 'bg-rose-100 text-rose-800 border border-rose-300'
                          : 'bg-amber-100 text-amber-800 border border-amber-300'
                      }`}>
                        {m.matchType === 'SEMANTIC_SIMILARITY' ? '🧠 Semantic Match' :
                         m.matchType === 'SEMANTIC_AND_KEYWORD' ? '🧠 Semantic & Keywords' :
                         m.matchType === 'EXACT_MATCH' ? '⚠️ Exact Match' : '🔍 Keyword Overlap'} ({Math.round(m.similarityScore)}%)
                      </span>
                    </div>
                    <div className="font-semibold text-slate-900">{m.title}</div>

                    {m.matchedConcepts && m.matchedConcepts.length > 0 && (
                      <div className="flex flex-wrap items-center gap-1 pt-0.5">
                        <span className="text-[10px] text-slate-400 font-semibold">Matched Concepts:</span>
                        {m.matchedConcepts.map((c, i) => (
                          <span key={i} className="text-[9px] font-medium bg-purple-50 text-purple-700 px-1.5 py-0.2 rounded border border-purple-200">
                            {c}
                          </span>
                        ))}
                      </div>
                    )}

                    {m.matchExplanation && (
                      <p className="text-[11px] text-slate-600 italic leading-relaxed">
                        {m.matchExplanation}
                      </p>
                    )}

                    <div className="text-[10px] text-slate-500 pt-1 border-t border-slate-100">
                      Status: <strong className="text-slate-700">{m.status}</strong> • Reasons: {m.matchReasons?.join('; ')}
                    </div>
                  </div>
                ))}
              </div>

              {/* Confirmation Checkbox & Remarks */}
              <div className="pt-2 border-t border-amber-200 space-y-2">
                <label className="flex items-start space-x-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={duplicateAcknowledged}
                    onChange={(e) => setDuplicateAcknowledged(e.target.checked)}
                    className="w-4 h-4 text-blue-600 rounded border-slate-300 mt-0.5"
                  />
                  <span className="text-xs font-medium text-amber-950 leading-tight">
                    I acknowledge that a similar grievance exists, but I confirm this is a distinct incident that requires independent investigation.
                  </span>
                </label>

                {duplicateAcknowledged && (
                  <div className="mt-2">
                    <label className="block text-[11px] font-semibold text-slate-700 mb-1">
                      Reason for Distinct Submission
                    </label>
                    <input
                      type="text"
                      placeholder="e.g., Different department, separate hostel floor, recent recurrence..."
                      value={duplicateRemarks}
                      onChange={(e) => setDuplicateRemarks(e.target.value)}
                      className="w-full px-3 py-1.5 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Description */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Grievance Description & Fact Sheet <span className="text-rose-500">*</span>
            </label>
            <textarea
              name="description"
              required
              rows="6"
              placeholder="Provide a comprehensive chronological statement of facts, dates, affected students, communication with institution authorities, and specific redressal sought..."
              value={formData.description}
              onChange={handleChange}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 leading-relaxed"
            />
          </div>

          {/* AI-Assisted Grievance Classification Card */}
          <div className="p-4 sm:p-5 rounded-xl border border-indigo-200 bg-gradient-to-br from-indigo-50/70 via-purple-50/40 to-white shadow-sm space-y-3">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div className="flex items-center space-x-2.5">
                <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white shadow-sm">
                  <Sparkles className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-indigo-950 flex items-center gap-1.5">
                    AI-Assisted Classification & Routing Advisor
                    <span className="text-[10px] font-semibold text-indigo-700 bg-indigo-100 px-1.5 py-0.5 rounded border border-indigo-200">
                      Local-First Engine
                    </span>
                  </h4>
                  <p className="text-[11px] text-indigo-800">
                    Analyze subject & statement facts to recommend category, urgency priority, and statutory AICTE Bureau/Cell
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={handleAnalyzeWithAI}
                disabled={analyzingClassification || (!formData.title.trim() && !formData.description.trim())}
                className="inline-flex items-center px-3 py-1.5 text-xs font-semibold text-indigo-700 bg-white hover:bg-indigo-50 border border-indigo-300 rounded-lg shadow-xs transition-colors disabled:opacity-50"
              >
                {analyzingClassification ? (
                  <>
                    <RefreshCw className="w-3.5 h-3.5 mr-1.5 animate-spin" />
                    Analyzing Grievance...
                  </>
                ) : (
                  <>
                    <Bot className="w-3.5 h-3.5 mr-1.5 text-indigo-600" />
                    Analyze with AI Assistant
                  </>
                )}
              </button>
            </div>

            {classificationResult && (
              <div className="mt-3 pt-3 border-t border-indigo-100 space-y-3">
                {/* Suggestions Grid */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5">
                  <div className="p-2.5 bg-white rounded-lg border border-indigo-100 shadow-2xs">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">
                      Suggested Category
                    </span>
                    <span className="text-xs font-bold text-slate-900">
                      {classificationResult.categoryDisplayName}
                    </span>
                  </div>

                  <div className="p-2.5 bg-white rounded-lg border border-indigo-100 shadow-2xs">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">
                      Recommended Urgency
                    </span>
                    <span className={`inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-bold ${
                      classificationResult.suggestedPriority === 'CRITICAL'
                        ? 'bg-rose-100 text-rose-800 border border-rose-300'
                        : classificationResult.suggestedPriority === 'HIGH'
                        ? 'bg-amber-100 text-amber-800 border border-amber-300'
                        : 'bg-blue-100 text-blue-800 border border-blue-200'
                    }`}>
                      {classificationResult.suggestedPriority}
                    </span>
                  </div>

                  <div className="p-2.5 bg-white rounded-lg border border-indigo-100 shadow-2xs">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">
                      Recommended Bureau / Cell
                    </span>
                    <span className="text-xs font-semibold text-indigo-900">
                      {classificationResult.suggestedBureauCode} - {classificationResult.suggestedCellCode}
                    </span>
                  </div>
                </div>

                {/* Keywords and Explanation */}
                <div className="p-2.5 bg-white/90 rounded-lg border border-indigo-100 space-y-1.5 text-xs">
                  <div className="flex flex-wrap items-center gap-1.5">
                    <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wide">
                      Extracted Concepts:
                    </span>
                    {classificationResult.extractedKeywords && classificationResult.extractedKeywords.length > 0 ? (
                      classificationResult.extractedKeywords.map((kw, idx) => (
                        <span key={idx} className="px-1.5 py-0.5 bg-indigo-50 text-indigo-700 text-[10px] font-medium rounded border border-indigo-200">
                          #{kw}
                        </span>
                      ))
                    ) : (
                      <span className="text-[10px] text-slate-400 italic">None detected</span>
                    )}
                  </div>

                  <p className="text-[11px] text-slate-600 leading-relaxed">
                    <strong>AI Rationale:</strong> {classificationResult.explanation}
                  </p>

                  <div className="flex items-center justify-between pt-1">
                    <span className="text-[10px] text-slate-500">
                      Confidence: <strong className="text-slate-800">{Math.round(classificationResult.confidence * 100)}%</strong> ({classificationResult.confidenceLevel})
                    </span>
                    <button
                      type="button"
                      onClick={handleApplySuggestions}
                      disabled={classificationApplied || classificationResult.manualReviewRequired}
                      className="inline-flex items-center px-2.5 py-1 text-xs font-semibold rounded-md text-white bg-indigo-600 hover:bg-indigo-700 transition-colors shadow-2xs disabled:bg-slate-300 disabled:cursor-not-allowed"
                    >
                      {classificationApplied ? (
                        <>
                          <Check className="w-3.5 h-3.5 mr-1" /> Applied to Form
                        </>
                      ) : (
                        'Apply Suggestions'
                      )}
                    </button>
                  </div>
                </div>

                {/* Mandatory Human Review Disclaimer */}
                <div className="text-[10px] text-indigo-900/80 bg-indigo-100/50 p-2 rounded-lg flex items-center space-x-1.5">
                  <Info className="w-3.5 h-3.5 text-indigo-600 flex-shrink-0" />
                  <span>
                    🤖 AI-generated suggestion • Verification and confirmation by user required before submission. You may edit all fields freely.
                  </span>
                </div>
              </div>
            )}
          </div>

          {/* Submit */}
          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-200">
            <button
              type="button"
              onClick={() => navigate('/complainant/complaints')}
              className="px-4 py-2 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center px-5 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors disabled:opacity-50"
            >
              <Send className="w-3.5 h-3.5 mr-1.5" />
              {submitting ? 'Submitting Grievance...' : 'Submit Grievance Dossier'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default RegisterComplaintPage;
