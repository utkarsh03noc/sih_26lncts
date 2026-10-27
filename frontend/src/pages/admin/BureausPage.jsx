import React, { useState, useEffect } from 'react';
import { bureauService } from '../../services/bureauService';
import { useToast } from '../../hooks/useToast';
import Modal from '../../components/common/Modal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import {
  Layers,
  Network,
  Plus,
  Edit2,
  Trash2,
  CheckCircle,
  XCircle,
  Building2,
  Mail,
  Shield,
  FileText,
} from 'lucide-react';

const BureausPage = () => {
  const [bureaus, setBureaus] = useState([]);
  const [cells, setCells] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('bureaus'); // 'bureaus' or 'cells'

  // Bureau Modal State
  const [bureauModalOpen, setBureauModalOpen] = useState(false);
  const [editingBureau, setEditingBureau] = useState(null);
  const [bureauForm, setBureauForm] = useState({
    code: '',
    name: '',
    description: '',
    contactEmail: '',
    active: true,
  });

  // Cell Modal State
  const [cellModalOpen, setCellModalOpen] = useState(false);
  const [editingCell, setEditingCell] = useState(null);
  const [cellForm, setCellForm] = useState({
    code: '',
    name: '',
    description: '',
    bureauId: '',
    contactEmail: '',
    active: true,
  });

  const [submitting, setSubmitting] = useState(false);
  const toast = useToast();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [bRes, cRes] = await Promise.all([
        bureauService.getBureaus(),
        bureauService.getCells(),
      ]);
      setBureaus(bRes.data || []);
      setCells(cRes.data || []);
    } catch (err) {
      toast.error('Failed to load bureaus and cells configuration');
    } finally {
      setLoading(false);
    }
  };

  // Bureau Handlers
  const handleOpenBureauModal = (bureau = null) => {
    setEditingBureau(bureau);
    if (bureau) {
      setBureauForm({
        code: bureau.code,
        name: bureau.name,
        description: bureau.description || '',
        contactEmail: bureau.contactEmail || '',
        active: bureau.active,
      });
    } else {
      setBureauForm({
        code: '',
        name: '',
        description: '',
        contactEmail: '',
        active: true,
      });
    }
    setBureauModalOpen(true);
  };

  const handleBureauSubmit = async (e) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      if (editingBureau) {
        await bureauService.updateBureau(editingBureau.id, bureauForm);
        toast.success(`Bureau "${bureauForm.name}" updated successfully`);
      } else {
        await bureauService.createBureau(bureauForm);
        toast.success(`Bureau "${bureauForm.name}" created successfully`);
      }
      setBureauModalOpen(false);
      fetchData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save bureau');
    } finally {
      setSubmitting(false);
    }
  };

  // Cell Handlers
  const handleOpenCellModal = (cell = null) => {
    setEditingCell(cell);
    if (cell) {
      setCellForm({
        code: cell.code,
        name: cell.name,
        description: cell.description || '',
        bureauId: cell.bureauId ? String(cell.bureauId) : '',
        contactEmail: cell.contactEmail || '',
        active: cell.active,
      });
    } else {
      setCellForm({
        code: '',
        name: '',
        description: '',
        bureauId: bureaus.length > 0 ? String(bureaus[0].id) : '',
        contactEmail: '',
        active: true,
      });
    }
    setCellModalOpen(true);
  };

  const handleCellSubmit = async (e) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      const payload = {
        ...cellForm,
        bureauId: cellForm.bureauId ? Number(cellForm.bureauId) : null,
      };
      if (editingCell) {
        await bureauService.updateCell(editingCell.id, payload);
        toast.success(`Cell "${cellForm.name}" updated successfully`);
      } else {
        await bureauService.createCell(payload);
        toast.success(`Cell "${cellForm.name}" created successfully`);
      }
      setCellModalOpen(false);
      fetchData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save cell');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <LoadingSpinner text="Loading organizational departments & bureaus..." size="lg" />;
  }

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">
              AICTE Bureaus & Operational Cells
            </h1>
            <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-indigo-100 text-indigo-800 border border-indigo-200">
              <Network className="w-3 h-3 mr-1" /> Dynamic Master
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Configure central bureaus, regulatory committees, and specialized grievance cells for case allocation
          </p>
        </div>

        <div className="flex items-center space-x-2">
          {activeTab === 'bureaus' ? (
            <button
              onClick={() => handleOpenBureauModal()}
              className="inline-flex items-center px-3.5 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors"
            >
              <Plus className="w-3.5 h-3.5 mr-1.5" /> Add Bureau
            </button>
          ) : (
            <button
              onClick={() => handleOpenCellModal()}
              className="inline-flex items-center px-3.5 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow-sm transition-colors"
            >
              <Plus className="w-3.5 h-3.5 mr-1.5" /> Add Specialized Cell
            </button>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200 text-xs font-semibold">
        <button
          onClick={() => setActiveTab('bureaus')}
          className={`pb-3 px-4 flex items-center border-b-2 transition-colors ${
            activeTab === 'bureaus'
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          <Layers className="w-4 h-4 mr-2" />
          Bureaus ({bureaus.length})
        </button>
        <button
          onClick={() => setActiveTab('cells')}
          className={`pb-3 px-4 flex items-center border-b-2 transition-colors ${
            activeTab === 'cells'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          <Network className="w-4 h-4 mr-2" />
          Specialized Cells ({cells.length})
        </button>
      </div>

      {/* Bureaus View */}
      {activeTab === 'bureaus' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {bureaus.map((b) => {
            const childCells = cells.filter((c) => c.bureauId === b.id);
            return (
              <div
                key={b.id}
                className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm hover:shadow-md transition-shadow flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="font-mono text-xs font-bold text-blue-700 bg-blue-50 px-2 py-0.5 rounded border border-blue-200">
                        {b.code}
                      </span>
                      <h3 className="text-sm font-bold text-slate-900 mt-2">{b.name}</h3>
                    </div>
                    <span
                      className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold border ${
                        b.active
                          ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                          : 'bg-slate-100 text-slate-500 border-slate-200'
                      }`}
                    >
                      {b.active ? 'Active' : 'Inactive'}
                    </span>
                  </div>

                  <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                    {b.description || 'Central administrative and regulatory bureau of AICTE.'}
                  </p>

                  {b.contactEmail && (
                    <div className="flex items-center text-xs text-slate-500 mt-3">
                      <Mail className="w-3.5 h-3.5 mr-1.5 text-slate-400" />
                      <span>{b.contactEmail}</span>
                    </div>
                  )}

                  {/* Attached Cells */}
                  <div className="mt-4 pt-3 border-t border-slate-100">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-1.5">
                      Operational Cells Under Bureau ({childCells.length})
                    </span>
                    {childCells.length === 0 ? (
                      <span className="text-xs text-slate-400 italic">No assigned cells</span>
                    ) : (
                      <div className="flex flex-wrap gap-1.5">
                        {childCells.map((c) => (
                          <span
                            key={c.id}
                            className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-medium bg-slate-100 text-slate-700 border border-slate-200"
                          >
                            <span className="font-semibold mr-1">{c.code}:</span> {c.name}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                <div className="mt-5 pt-3 border-t border-slate-100 flex justify-end">
                  <button
                    onClick={() => handleOpenBureauModal(b)}
                    className="inline-flex items-center px-2.5 py-1 text-xs font-semibold text-slate-700 hover:text-blue-700 hover:bg-blue-50 rounded transition-colors"
                  >
                    <Edit2 className="w-3 h-3 mr-1" /> Edit Bureau
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Cells View */}
      {activeTab === 'cells' && (
        <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[10px]">
                <tr>
                  <th className="px-4 py-3">Code</th>
                  <th className="px-4 py-3">Cell Name</th>
                  <th className="px-4 py-3">Parent Bureau</th>
                  <th className="px-4 py-3">Contact Email</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {cells.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="px-4 py-3 font-mono font-bold text-indigo-700">
                      {c.code}
                    </td>
                    <td className="px-4 py-3">
                      <div className="font-semibold text-slate-900">{c.name}</div>
                      <div className="text-[11px] text-slate-500 line-clamp-1">{c.description}</div>
                    </td>
                    <td className="px-4 py-3">
                      {c.bureauName ? (
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-medium bg-blue-50 text-blue-700 border border-blue-200">
                          {c.bureauName} ({c.bureauCode})
                        </span>
                      ) : (
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-medium bg-amber-50 text-amber-700 border border-amber-200">
                          Independent / Statutory
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-slate-600">
                      {c.contactEmail || 'N/A'}
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold border ${
                          c.active
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                            : 'bg-slate-100 text-slate-500 border-slate-200'
                        }`}
                      >
                        {c.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => handleOpenCellModal(c)}
                        className="inline-flex items-center px-2.5 py-1 text-xs font-semibold text-indigo-600 hover:bg-indigo-50 rounded transition-colors"
                      >
                        <Edit2 className="w-3 h-3 mr-1" /> Edit
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Bureau Modal */}
      <Modal
        isOpen={bureauModalOpen}
        onClose={() => setBureauModalOpen(false)}
        title={editingBureau ? 'Edit Bureau' : 'Add New AICTE Bureau'}
        maxWidth="max-w-md"
      >
        <form onSubmit={handleBureauSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Bureau Code <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g., RB, AB, PAPB"
              value={bureauForm.code}
              onChange={(e) => setBureauForm((prev) => ({ ...prev, code: e.target.value.toUpperCase() }))}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 uppercase font-mono font-bold"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Bureau Full Name <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g., Regulation Bureau"
              value={bureauForm.name}
              onChange={(e) => setBureauForm((prev) => ({ ...prev, name: e.target.value }))}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Description & Jurisdiction
            </label>
            <textarea
              rows="3"
              placeholder="Outline administrative role and jurisdiction..."
              value={bureauForm.description}
              onChange={(e) => setBureauForm((prev) => ({ ...prev, description: e.target.value }))}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Official Bureau Contact Email
            </label>
            <input
              type="email"
              placeholder="bureau@aicte-demo.gov.in"
              value={bureauForm.contactEmail}
              onChange={(e) => setBureauForm((prev) => ({ ...prev, contactEmail: e.target.value }))}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="flex items-center space-x-2 pt-2">
            <input
              type="checkbox"
              id="bureauActive"
              checked={bureauForm.active}
              onChange={(e) => setBureauForm((prev) => ({ ...prev, active: e.target.checked }))}
              className="w-4 h-4 text-blue-600 rounded border-slate-300"
            />
            <label htmlFor="bureauActive" className="text-xs font-medium text-slate-700">
              Active in grievance assignment workflow
            </label>
          </div>

          <div className="flex justify-end space-x-2 pt-3 border-t border-slate-200">
            <button
              type="button"
              onClick={() => setBureauModalOpen(false)}
              className="px-4 py-2 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm disabled:opacity-50"
            >
              {submitting ? 'Saving...' : editingBureau ? 'Update Bureau' : 'Create Bureau'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Cell Modal */}
      <Modal
        isOpen={cellModalOpen}
        onClose={() => setCellModalOpen(false)}
        title={editingCell ? 'Edit Specialized Cell' : 'Add New Operational Cell'}
        maxWidth="max-w-md"
      >
        <form onSubmit={handleCellSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Cell Code <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g., PGRC, SDC, ICC"
              value={cellForm.code}
              onChange={(e) => setCellForm((prev) => ({ ...prev, code: e.target.value.toUpperCase() }))}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 uppercase font-mono font-bold"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Cell Full Name <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g., Public Grievance Redressal Cell"
              value={cellForm.name}
              onChange={(e) => setCellForm((prev) => ({ ...prev, name: e.target.value }))}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Parent Bureau (Optional)
            </label>
            <select
              value={cellForm.bureauId}
              onChange={(e) => setCellForm((prev) => ({ ...prev, bureauId: e.target.value }))}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
            >
              <option value="">Independent / Statutory Committee (No Parent Bureau)</option>
              {bureaus.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.name} ({b.code})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Description & Mandate
            </label>
            <textarea
              rows="3"
              placeholder="Describe cell role and operational focus..."
              value={cellForm.description}
              onChange={(e) => setCellForm((prev) => ({ ...prev, description: e.target.value }))}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Contact Email
            </label>
            <input
              type="email"
              placeholder="cell@aicte-demo.gov.in"
              value={cellForm.contactEmail}
              onChange={(e) => setCellForm((prev) => ({ ...prev, contactEmail: e.target.value }))}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="flex items-center space-x-2 pt-2">
            <input
              type="checkbox"
              id="cellActive"
              checked={cellForm.active}
              onChange={(e) => setCellForm((prev) => ({ ...prev, active: e.target.checked }))}
              className="w-4 h-4 text-indigo-600 rounded border-slate-300"
            />
            <label htmlFor="cellActive" className="text-xs font-medium text-slate-700">
              Active in grievance assignment workflow
            </label>
          </div>

          <div className="flex justify-end space-x-2 pt-3 border-t border-slate-200">
            <button
              type="button"
              onClick={() => setCellModalOpen(false)}
              className="px-4 py-2 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow-sm disabled:opacity-50"
            >
              {submitting ? 'Saving...' : editingCell ? 'Update Cell' : 'Create Cell'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default BureausPage;
