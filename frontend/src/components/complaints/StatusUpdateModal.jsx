import React, { useState } from 'react';
import Modal from '../common/Modal';
import { COMPLAINT_STATUS } from '../../utils/constants';

const StatusUpdateModal = ({ isOpen, onClose, currentStatus, onSubmit }) => {
  const [newStatus, setNewStatus] = useState('');
  const [remarks, setRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const availableStatuses = Object.keys(COMPLAINT_STATUS).filter(
    (st) => st !== currentStatus
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!newStatus) {
      setError('Please select a target status');
      return;
    }
    if (!remarks.trim()) {
      setError('Official remarks are required to explain the status update');
      return;
    }

    try {
      setSubmitting(true);
      setError('');
      await onSubmit({ newStatus, remarks });
      setRemarks('');
      setNewStatus('');
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to update status');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Update Complaint Status">
      <form onSubmit={handleSubmit} className="space-y-4 text-xs">
        {error && (
          <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 font-medium">
            {error}
          </div>
        )}

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Current Status: <span className="font-bold text-slate-900">{currentStatus}</span>
          </label>
        </div>

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            New Status <span className="text-rose-500">*</span>
          </label>
          <select
            value={newStatus}
            onChange={(e) => setNewStatus(e.target.value)}
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 bg-white"
            required
          >
            <option value="">Select Target Status</option>
            {availableStatuses.map((st) => (
              <option key={st} value={st}>
                {COMPLAINT_STATUS[st]?.label || st}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Official Remarks / Resolution Details <span className="text-rose-500">*</span>
          </label>
          <textarea
            rows={4}
            value={remarks}
            onChange={(e) => setRemarks(e.target.value)}
            placeholder="Document actions taken, findings, committee decisions, or instructions..."
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
            required
          />
        </div>

        <div className="flex justify-end space-x-2 pt-2 border-t border-slate-200">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="px-4 py-2 font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-lg transition-colors disabled:opacity-50"
          >
            {submitting ? 'Updating...' : 'Submit Status Update'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default StatusUpdateModal;
