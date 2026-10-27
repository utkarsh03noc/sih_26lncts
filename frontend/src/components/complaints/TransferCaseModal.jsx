import React, { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import { bureauService } from '../../services/bureauService';
import { userService } from '../../services/userService';
import { ArrowRightLeft, Building2, User, AlertCircle, FileText } from 'lucide-react';

const TransferCaseModal = ({ isOpen, onClose, complaint, onSubmit }) => {
  const [bureaus, setBureaus] = useState([]);
  const [cells, setCells] = useState([]);
  const [filteredCells, setFilteredCells] = useState([]);
  const [officers, setOfficers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    targetBureauId: '',
    targetCellId: '',
    targetOfficerId: '',
    transferReason: '',
    remarks: '',
  });

  useEffect(() => {
    if (isOpen) {
      loadInitialData();
      setFormData({
        targetBureauId: complaint?.bureauId ? String(complaint.bureauId) : '',
        targetCellId: complaint?.cellId ? String(complaint.cellId) : '',
        targetOfficerId: complaint?.assignedOfficerId ? String(complaint.assignedOfficerId) : '',
        transferReason: '',
        remarks: '',
      });
      setError('');
    }
  }, [isOpen, complaint]);

  const loadInitialData = async () => {
    try {
      setLoading(true);
      const [bureauRes, cellRes] = await Promise.all([
        bureauService.getActiveBureaus(),
        bureauService.getActiveCells(),
      ]);
      setBureaus(bureauRes.data || []);
      setCells(cellRes.data || []);
      setFilteredCells(cellRes.data || []);

      if (complaint?.institutionId) {
        const offRes = await userService.getOfficersByInstitution(complaint.institutionId);
        setOfficers(offRes.data || []);
      }
    } catch (err) {
      console.error('Failed to load transfer metadata:', err);
      setError('Could not load Bureau and Cell configuration.');
    } finally {
      setLoading(false);
    }
  };

  const handleBureauChange = (bureauId) => {
    setFormData((prev) => ({ ...prev, targetBureauId: bureauId, targetCellId: '' }));
    if (!bureauId) {
      setFilteredCells(cells);
    } else {
      const bId = Number(bureauId);
      const related = cells.filter((c) => !c.bureauId || c.bureauId === bId);
      setFilteredCells(related);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.transferReason.trim()) {
      setError('Please provide a mandatory transfer reason for institutional record.');
      return;
    }

    try {
      setSubmitting(true);
      setError('');
      await onSubmit({
        targetBureauId: formData.targetBureauId ? Number(formData.targetBureauId) : null,
        targetCellId: formData.targetCellId ? Number(formData.targetCellId) : null,
        targetOfficerId: formData.targetOfficerId ? Number(formData.targetOfficerId) : null,
        transferReason: formData.transferReason.trim(),
        remarks: formData.remarks.trim(),
      });
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to transfer complaint.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Transfer Case Across Bureau / Cell"
      maxWidth="max-w-xl"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center">
            <AlertCircle className="w-4 h-4 mr-2 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Current Ownership Summary */}
        <div className="p-3.5 bg-slate-50 border border-slate-200 rounded-lg text-xs space-y-1">
          <div className="font-semibold text-slate-800 text-[11px] uppercase tracking-wider">
            Current Case Ownership
          </div>
          <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-slate-600">
            <span>
              <strong className="text-slate-700">Bureau:</strong>{' '}
              {complaint?.bureauName ? `${complaint.bureauName} (${complaint.bureauCode})` : 'Unassigned'}
            </span>
            <span>
              <strong className="text-slate-700">Cell:</strong>{' '}
              {complaint?.cellName ? `${complaint.cellName} (${complaint.cellCode})` : 'Unassigned'}
            </span>
            <span>
              <strong className="text-slate-700">Officer:</strong>{' '}
              {complaint?.assignedOfficerName || 'Unassigned'}
            </span>
          </div>
        </div>

        {/* Target Bureau */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Target AICTE Bureau
          </label>
          <select
            value={formData.targetBureauId}
            onChange={(e) => handleBureauChange(e.target.value)}
            disabled={loading}
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 bg-white"
          >
            <option value="">Select Responsible Bureau...</option>
            {bureaus.map((b) => (
              <option key={b.id} value={b.id}>
                {b.name} ({b.code})
              </option>
            ))}
          </select>
        </div>

        {/* Target Cell */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Target Responsible Cell
          </label>
          <select
            value={formData.targetCellId}
            onChange={(e) => setFormData((prev) => ({ ...prev, targetCellId: e.target.value }))}
            disabled={loading}
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 bg-white"
          >
            <option value="">Select Specialized Cell (Optional / Independent)...</option>
            {filteredCells.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name} ({c.code}) {c.bureauName ? `- under ${c.bureauCode}` : '(Independent)'}
              </option>
            ))}
          </select>
        </div>

        {/* Target Officer */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Assign to Officer (Optional)
          </label>
          <select
            value={formData.targetOfficerId}
            onChange={(e) => setFormData((prev) => ({ ...prev, targetOfficerId: e.target.value }))}
            disabled={loading}
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 bg-white"
          >
            <option value="">Keep Existing or Unassigned...</option>
            {officers.map((off) => (
              <option key={off.id} value={off.id}>
                {off.fullName} ({off.email})
              </option>
            ))}
          </select>
        </div>

        {/* Mandatory Transfer Reason */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Transfer Reason <span className="text-rose-500">*</span>
          </label>
          <input
            type="text"
            required
            placeholder="e.g., Regulatory jurisdiction re-evaluation, scholarship policy dispute..."
            value={formData.transferReason}
            onChange={(e) => setFormData((prev) => ({ ...prev, transferReason: e.target.value }))}
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        {/* Additional Remarks */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Transfer Remarks / Instructions
          </label>
          <textarea
            rows="3"
            placeholder="Provide context, required actions, or specific guidelines for the receiving unit..."
            value={formData.remarks}
            onChange={(e) => setFormData((prev) => ({ ...prev, remarks: e.target.value }))}
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        {/* Footer actions */}
        <div className="flex justify-end space-x-2 pt-3 border-t border-slate-200">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="inline-flex items-center px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors disabled:opacity-50"
          >
            <ArrowRightLeft className="w-3.5 h-3.5 mr-1.5" />
            {submitting ? 'Transferring...' : 'Execute Case Transfer'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default TransferCaseModal;
