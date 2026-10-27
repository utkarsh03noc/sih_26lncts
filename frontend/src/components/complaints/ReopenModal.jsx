import React, { useState } from 'react';
import Modal from '../common/Modal';

const ReopenModal = ({ isOpen, onClose, complaintNumber, onSubmit }) => {
  const [remarks, setRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!remarks.trim()) {
      setError('Please provide specific reasons why the previous resolution was unsatisfactory');
      return;
    }

    try {
      setSubmitting(true);
      setError('');
      await onSubmit(remarks);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to reopen complaint');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Reopen Grievance Case">
      <form onSubmit={handleSubmit} className="space-y-4 text-xs">
        {error && (
          <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 font-medium">
            {error}
          </div>
        )}

        <div className="p-3 bg-amber-50 rounded-lg border border-amber-200 text-amber-800">
          Reopening grievance <strong>{complaintNumber}</strong> will reactivate the complaint and alert the
          supervising authority for re-examination.
        </div>

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Reason for Reopening <span className="text-rose-500">*</span>
          </label>
          <textarea
            rows={4}
            value={remarks}
            onChange={(e) => setRemarks(e.target.value)}
            placeholder="Explain why the resolution was incomplete, what issues persist, or provide additional evidence..."
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
            className="px-4 py-2 font-semibold text-white bg-orange-600 hover:bg-orange-700 rounded-lg transition-colors disabled:opacity-50"
          >
            {submitting ? 'Reopening...' : 'Confirm Reopen'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default ReopenModal;
