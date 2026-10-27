import React, { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import { userService } from '../../services/userService';
import { institutionService } from '../../services/institutionService';

const AssignOfficerModal = ({ isOpen, onClose, complaint, onSubmit, isAdmin }) => {
  const [officers, setOfficers] = useState([]);
  const [institutions, setInstitutions] = useState([]);
  const [selectedInstitutionId, setSelectedInstitutionId] = useState(complaint?.institutionId || '');
  const [selectedOfficerId, setSelectedOfficerId] = useState(complaint?.assignedOfficerId || '');
  const [remarks, setRemarks] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen) {
      setSelectedInstitutionId(complaint?.institutionId || '');
      setSelectedOfficerId(complaint?.assignedOfficerId || '');
      loadData();
    }
  }, [isOpen, complaint]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [offRes, instRes] = await Promise.all([
        userService.getAllOfficers(),
        isAdmin ? institutionService.getActiveInstitutions() : Promise.resolve({ data: [] }),
      ]);
      setOfficers(offRes.data || []);
      if (isAdmin && instRes.data) {
        setInstitutions(instRes.data);
      }
    } catch (err) {
      setError('Failed to load officers or institutions');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedOfficerId) {
      setError('Please select an officer to assign');
      return;
    }

    try {
      setSubmitting(true);
      setError('');
      await onSubmit({
        officerId: selectedOfficerId,
        institutionId: selectedInstitutionId || null,
        remarks: remarks || `Complaint assigned to officer`,
      });
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to assign officer');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Assign Grievance to Nodal Officer">
      <form onSubmit={handleSubmit} className="space-y-4 text-xs">
        {error && (
          <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 font-medium">
            {error}
          </div>
        )}

        {isAdmin && institutions.length > 0 && (
          <div>
            <label className="block font-semibold text-slate-700 mb-1">
              Reassign Institution (Optional)
            </label>
            <select
              value={selectedInstitutionId}
              onChange={(e) => setSelectedInstitutionId(e.target.value)}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 bg-white"
            >
              <option value="">Keep Current Institution</option>
              {institutions.map((inst) => (
                <option key={inst.id} value={inst.id}>
                  {inst.name} ({inst.code})
                </option>
              ))}
            </select>
          </div>
        )}

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Assign Nodal Officer <span className="text-rose-500">*</span>
          </label>
          <select
            value={selectedOfficerId}
            onChange={(e) => setSelectedOfficerId(e.target.value)}
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 bg-white"
            required
            disabled={loading}
          >
            <option value="">Select Officer</option>
            {officers.map((off) => (
              <option key={off.id} value={off.id}>
                {off.fullName} - {off.institutionName || 'AICTE Officer'} ({off.email})
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Assignment Directive / Remarks
          </label>
          <textarea
            rows={3}
            value={remarks}
            onChange={(e) => setRemarks(e.target.value)}
            placeholder="Directives for inquiry, expected timeline, or context for the officer..."
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
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
            disabled={submitting || loading}
            className="px-4 py-2 font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-lg transition-colors disabled:opacity-50"
          >
            {submitting ? 'Assigning...' : 'Confirm Assignment'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default AssignOfficerModal;
