import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { institutionService } from '../../services/institutionService';
import { useToast } from '../../hooks/useToast';
import Pagination from '../../components/common/Pagination';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import Modal from '../../components/common/Modal';
import {
  Building2,
  Search,
  Plus,
  ExternalLink,
  Shield,
  Layers,
  AlertTriangle,
  CheckCircle,
  XCircle,
  MapPin,
  Mail,
  Phone,
  BarChart2,
  FileText,
} from 'lucide-react';

const InstitutionsPage = () => {
  const [activeTab, setActiveTab] = useState('monitoring'); // 'monitoring' or 'master'
  const [monitoringData, setMonitoringData] = useState([]);
  const [institutions, setInstitutions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [pageData, setPageData] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');
  const [formData, setFormData] = useState({
    name: '',
    code: '',
    location: '',
    state: '',
    district: '',
    address: '',
    aicteReferenceCode: '',
    type: 'Engineering & Technology',
    contactEmail: '',
    contactPhone: '',
  });

  const toast = useToast();

  useEffect(() => {
    if (activeTab === 'monitoring') {
      fetchMonitoringSummary();
    } else {
      fetchInstitutions();
    }
  }, [activeTab, pageData.pageNumber, search]);

  const fetchMonitoringSummary = async () => {
    try {
      setLoading(true);
      const res = await institutionService.getMonitoringSummary({
        query: search || undefined,
        page: pageData.pageNumber,
        size: pageData.pageSize,
      });
      if (res.success && res.data) {
        setMonitoringData(res.data.content || []);
        setPageData((prev) => ({
          ...prev,
          totalElements: res.data.totalElements || 0,
          totalPages: res.data.totalPages || 0,
        }));
      }
    } catch (err) {
      console.error('Failed to load institution monitoring:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchInstitutions = async () => {
    try {
      setLoading(true);
      const res = await institutionService.getInstitutions({
        query: search || undefined,
        page: pageData.pageNumber,
        size: pageData.pageSize,
      });
      if (res.success && res.data) {
        setInstitutions(res.data.content || []);
        setPageData((prev) => ({
          ...prev,
          totalElements: res.data.totalElements || 0,
          totalPages: res.data.totalPages || 0,
        }));
      }
    } catch (err) {
      console.error('Failed to load institutions:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      setFormError('');
      await institutionService.createInstitution(formData);
      toast.success('Institution created successfully!');
      setModalOpen(false);
      setFormData({
        name: '',
        code: '',
        location: '',
        state: '',
        district: '',
        address: '',
        aicteReferenceCode: '',
        type: 'Engineering & Technology',
        contactEmail: '',
        contactPhone: '',
      });
      if (activeTab === 'monitoring') fetchMonitoringSummary();
      else fetchInstitutions();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to create institution';
      setFormError(msg);
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">
              Institution Monitoring & Master Management
            </h1>
            <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-blue-100 text-blue-800 border border-blue-200">
              <Building2 className="w-3 h-3 mr-1" />
              Consolidated Hub
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Cross-bureau grievance monitoring, SLA tracking, and institution master registry
          </p>
        </div>

        <button
          onClick={() => {
            setFormError('');
            setModalOpen(true);
          }}
          className="inline-flex items-center px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors"
        >
          <Plus className="w-3.5 h-3.5 mr-1.5" /> Register Institution
        </button>
      </div>

      {/* Tabs & Search Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-200 pb-3">
        <div className="flex space-x-2">
          <button
            onClick={() => {
              setActiveTab('monitoring');
              setPageData((p) => ({ ...p, pageNumber: 0 }));
            }}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors flex items-center ${
              activeTab === 'monitoring'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            <BarChart2 className="w-3.5 h-3.5 mr-1.5" />
            Consolidated Monitoring
          </button>
          <button
            onClick={() => {
              setActiveTab('master');
              setPageData((p) => ({ ...p, pageNumber: 0 }));
            }}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors flex items-center ${
              activeTab === 'master'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            <Building2 className="w-3.5 h-3.5 mr-1.5" />
            Master Directory ({pageData.totalElements})
          </button>
        </div>

        {/* Search */}
        <div className="relative w-full sm:w-72">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search by code, state, or name..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPageData((prev) => ({ ...prev, pageNumber: 0 }));
            }}
            className="w-full pl-9 pr-3 py-1.5 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
          />
        </div>
      </div>

      {loading ? (
        <LoadingSpinner text="Retrieving institution metrics..." size="lg" />
      ) : activeTab === 'monitoring' ? (
        /* Consolidated Monitoring Table */
        monitoringData.length === 0 ? (
          <EmptyState
            icon={Building2}
            title="No Institution Data"
            description="No institutions match your search criteria."
          />
        ) : (
          <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[10px]">
                  <tr>
                    <th className="px-4 py-3">Institution</th>
                    <th className="px-4 py-3">State & District</th>
                    <th className="px-4 py-3 text-center">Total Cases</th>
                    <th className="px-4 py-3 text-center">Open / In Progress</th>
                    <th className="px-4 py-3 text-center">Resolved</th>
                    <th className="px-4 py-3 text-center">Overdue SLA</th>
                    <th className="px-4 py-3">Active Bureaus / Cells</th>
                    <th className="px-4 py-3 text-right">Dossier</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200">
                  {monitoringData.map((inst) => (
                    <tr key={inst.institutionId} className="hover:bg-slate-50/80 transition-colors">
                      <td className="px-4 py-3 max-w-xs">
                        <div className="font-semibold text-slate-900 line-clamp-1">
                          <Link
                            to={`/admin/institutions/${inst.institutionId}`}
                            className="hover:text-blue-600 hover:underline"
                          >
                            {inst.institutionName}
                          </Link>
                        </div>
                        <div className="font-mono text-[11px] text-blue-600 font-semibold">
                          {inst.institutionCode}
                        </div>
                      </td>

                      <td className="px-4 py-3 text-slate-600">
                        <div className="font-medium text-slate-800">{inst.state}</div>
                        <div className="text-[11px] text-slate-400">{inst.district}</div>
                      </td>

                      <td className="px-4 py-3 text-center font-bold text-slate-900">
                        {inst.totalComplaints}
                      </td>

                      <td className="px-4 py-3 text-center">
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold bg-amber-50 text-amber-800 border border-amber-200">
                          {inst.openComplaints}
                        </span>
                      </td>

                      <td className="px-4 py-3 text-center">
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold bg-emerald-50 text-emerald-800 border border-emerald-200">
                          {inst.resolvedComplaints}
                        </span>
                      </td>

                      <td className="px-4 py-3 text-center">
                        {inst.overdueComplaints > 0 ? (
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-bold bg-rose-100 text-rose-800 border border-rose-300 animate-pulse">
                            <AlertTriangle className="w-3 h-3 mr-1" />
                            {inst.overdueComplaints}
                          </span>
                        ) : (
                          <span className="text-slate-400 text-[11px]">0</span>
                        )}
                      </td>

                      <td className="px-4 py-3">
                        <div className="space-y-1">
                          <div className="flex flex-wrap gap-1">
                            {inst.involvedBureauNames && inst.involvedBureauNames.length > 0 ? (
                              inst.involvedBureauNames.map((bName, idx) => (
                                <span
                                  key={idx}
                                  className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-bold bg-blue-50 text-blue-700 border border-blue-200"
                                >
                                  {bName}
                                </span>
                              ))
                            ) : (
                              <span className="text-slate-400 text-[11px] italic">No active cases</span>
                            )}
                          </div>
                          {inst.involvedCellNames && inst.involvedCellNames.length > 0 && (
                            <div className="flex flex-wrap gap-1">
                              {inst.involvedCellNames.map((cName, idx) => (
                                <span
                                  key={idx}
                                  className="inline-flex items-center px-1.5 py-0.2 rounded text-[9px] font-medium bg-slate-100 text-slate-600 border border-slate-200"
                                >
                                  {cName}
                                </span>
                              ))}
                            </div>
                          )}
                        </div>
                      </td>

                      <td className="px-4 py-3 text-right">
                        <Link
                          to={`/admin/institutions/${inst.institutionId}`}
                          className="inline-flex items-center px-2.5 py-1 text-xs font-semibold text-blue-600 hover:bg-blue-50 rounded transition-colors"
                        >
                          View Profile
                          <ExternalLink className="w-3 h-3 ml-1" />
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <Pagination
              pageNumber={pageData.pageNumber}
              totalPages={pageData.totalPages}
              totalElements={pageData.totalElements}
              pageSize={pageData.pageSize}
              onPageChange={(page) => setPageData((prev) => ({ ...prev, pageNumber: page }))}
            />
          </div>
        )
      ) : (
        /* Master Directory Table */
        institutions.length === 0 ? (
          <EmptyState
            icon={Building2}
            title="No Institutions Found"
            description="No institutions match your search criteria."
          />
        ) : (
          <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[10px]">
                  <tr>
                    <th className="px-4 py-3">Code & AICTE Ref</th>
                    <th className="px-4 py-3">Institution Name</th>
                    <th className="px-4 py-3">Location & State</th>
                    <th className="px-4 py-3">Type</th>
                    <th className="px-4 py-3">Contact</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200">
                  {institutions.map((inst) => (
                    <tr key={inst.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="px-4 py-3 font-mono">
                        <div className="font-bold text-blue-600">{inst.code}</div>
                        {inst.aicteReferenceCode && (
                          <div className="text-[10px] text-slate-500 font-medium">{inst.aicteReferenceCode}</div>
                        )}
                      </td>

                      <td className="px-4 py-3 font-semibold text-slate-900">
                        <Link
                          to={`/admin/institutions/${inst.id}`}
                          className="hover:text-blue-600 hover:underline"
                        >
                          {inst.name}
                        </Link>
                      </td>

                      <td className="px-4 py-3 text-slate-600">
                        <div>{inst.location}</div>
                        {inst.state && (
                          <div className="text-[11px] text-slate-400">
                            {inst.district ? `${inst.district}, ` : ''}{inst.state}
                          </div>
                        )}
                      </td>

                      <td className="px-4 py-3 text-slate-600">
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-medium bg-slate-100 text-slate-700 border border-slate-200">
                          {inst.type}
                        </span>
                      </td>

                      <td className="px-4 py-3 text-slate-600">
                        <div>{inst.contactEmail}</div>
                        <div className="text-[10px] text-slate-400">{inst.contactPhone}</div>
                      </td>

                      <td className="px-4 py-3">
                        <span
                          className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold border ${
                            inst.active
                              ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                              : 'bg-slate-100 text-slate-500 border-slate-200'
                          }`}
                        >
                          {inst.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>

                      <td className="px-4 py-3 text-right">
                        <Link
                          to={`/admin/institutions/${inst.id}`}
                          className="inline-flex items-center px-2.5 py-1 text-xs font-semibold text-blue-600 hover:bg-blue-50 rounded transition-colors"
                        >
                          Details
                          <ExternalLink className="w-3 h-3 ml-1" />
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <Pagination
              pageNumber={pageData.pageNumber}
              totalPages={pageData.totalPages}
              totalElements={pageData.totalElements}
              pageSize={pageData.pageSize}
              onPageChange={(page) => setPageData((prev) => ({ ...prev, pageNumber: page }))}
            />
          </div>
        )
      )}

      {/* Add Institution Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Register Technical Institution"
        maxWidth="max-w-xl"
      >
        <form onSubmit={handleCreateSubmit} className="space-y-4">
          {formError && (
            <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-xs text-rose-700 flex items-center">
              <AlertTriangle className="w-4 h-4 mr-2 flex-shrink-0" />
              <span>{formError}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Unique Institution Code <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="e.g., AICTE-IIT-013"
                value={formData.code}
                onChange={(e) => setFormData((prev) => ({ ...prev, code: e.target.value.toUpperCase() }))}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 uppercase font-mono font-bold"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                AICTE Reference Code
              </label>
              <input
                type="text"
                placeholder="e.g., AICTE-ND-1-9988221"
                value={formData.aicteReferenceCode}
                onChange={(e) => setFormData((prev) => ({ ...prev, aicteReferenceCode: e.target.value }))}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Institution Name <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g., National Institute of Advanced Technology"
              value={formData.name}
              onChange={(e) => setFormData((prev) => ({ ...prev, name: e.target.value }))}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                State <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="e.g., Maharashtra, Delhi, Karnataka"
                value={formData.state}
                onChange={(e) => setFormData((prev) => ({ ...prev, state: e.target.value }))}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                District <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="e.g., Pune, South Delhi, Bengaluru"
                value={formData.district}
                onChange={(e) => setFormData((prev) => ({ ...prev, district: e.target.value }))}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                City / Location <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="e.g., Hauz Khas, New Delhi"
                value={formData.location}
                onChange={(e) => setFormData((prev) => ({ ...prev, location: e.target.value }))}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Institution Type <span className="text-rose-500">*</span>
              </label>
              <select
                value={formData.type}
                onChange={(e) => setFormData((prev) => ({ ...prev, type: e.target.value }))}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
              >
                <option value="Engineering & Technology">Engineering & Technology</option>
                <option value="Deemed University">Deemed University</option>
                <option value="Autonomous Engineering">Autonomous Engineering</option>
                <option value="State University">State University</option>
                <option value="National Institute">National Institute</option>
                <option value="Government College">Government College</option>
                <option value="Polytechnic Diploma">Polytechnic Diploma</option>
                <option value="Management & MCA">Management & MCA</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Full Campus Postal Address
            </label>
            <textarea
              rows="2"
              placeholder="Door No, Street, Landmark, PIN code..."
              value={formData.address}
              onChange={(e) => setFormData((prev) => ({ ...prev, address: e.target.value }))}
              className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Contact Email <span className="text-rose-500">*</span>
              </label>
              <input
                type="email"
                required
                placeholder="director@institution.edu.in"
                value={formData.contactEmail}
                onChange={(e) => setFormData((prev) => ({ ...prev, contactEmail: e.target.value }))}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Contact Phone <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="e.g., 011-26591000"
                value={formData.contactPhone}
                onChange={(e) => setFormData((prev) => ({ ...prev, contactPhone: e.target.value }))}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div className="flex justify-end space-x-2 pt-3 border-t border-slate-200">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="px-4 py-2 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors disabled:opacity-50"
            >
              {submitting ? 'Registering...' : 'Save Institution'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default InstitutionsPage;
