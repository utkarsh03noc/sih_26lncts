import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import { bureauService } from '../../services/bureauService';
import ComplaintFilter from '../../components/complaints/ComplaintFilter';
import StatusBadge from '../../components/common/StatusBadge';
import PriorityBadge from '../../components/common/PriorityBadge';
import CategoryBadge from '../../components/common/CategoryBadge';
import Pagination from '../../components/common/Pagination';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import { formatDate } from '../../utils/formatters';
import { AlertCircle, FileText, ArrowUpDown, CopyCheck, Network } from 'lucide-react';

const AdminComplaintsPage = () => {
  const [complaints, setComplaints] = useState([]);
  const [bureaus, setBureaus] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pageData, setPageData] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
  });

  const [filters, setFilters] = useState({
    search: '',
    bureauId: '',
    status: '',
    category: '',
    priority: '',
    sortBy: 'createdAt',
    sortDir: 'desc',
  });

  useEffect(() => {
    bureauService.getActiveBureaus().then((res) => setBureaus(res.data || [])).catch(() => {});
  }, []);

  useEffect(() => {
    fetchComplaints();
  }, [filters, pageData.pageNumber]);

  const fetchComplaints = async () => {
    try {
      setLoading(true);
      const params = {
        page: pageData.pageNumber,
        size: pageData.pageSize,
        sortBy: filters.sortBy,
        sortDir: filters.sortDir,
      };

      if (filters.search) params.search = filters.search;
      if (filters.bureauId) params.bureauId = Number(filters.bureauId);
      if (filters.status) params.status = filters.status;
      if (filters.category) params.category = filters.category;
      if (filters.priority) params.priority = filters.priority;

      const res = await complaintService.getComplaints(params);
      if (res.success && res.data) {
        setComplaints(res.data.content || []);
        setPageData((prev) => ({
          ...prev,
          totalElements: res.data.totalElements,
          totalPages: res.data.totalPages,
        }));
      }
    } catch (err) {
      console.error('Error fetching complaints:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPageData((prev) => ({ ...prev, pageNumber: 0 }));
  };

  const handleResetFilters = () => {
    setFilters({
      search: '',
      bureauId: '',
      status: '',
      category: '',
      priority: '',
      sortBy: 'createdAt',
      sortDir: 'desc',
    });
    setPageData((prev) => ({ ...prev, pageNumber: 0 }));
  };

  const toggleSort = (column) => {
    setFilters((prev) => ({
      ...prev,
      sortBy: column,
      sortDir: prev.sortBy === column && prev.sortDir === 'asc' ? 'desc' : 'asc',
    }));
    setPageData((prev) => ({ ...prev, pageNumber: 0 }));
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">
          All Institution Complaint Cases
        </h1>
        <p className="text-xs text-slate-500 mt-1">
          Comprehensive repository of all grievance cases registered across technical institutions and AICTE Bureaus
        </p>
      </div>

      {/* Filter Component */}
      <ComplaintFilter
        filters={filters}
        onFilterChange={handleFilterChange}
        onReset={handleResetFilters}
        bureaus={bureaus}
      />

      {/* Table Card */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <LoadingSpinner text="Retrieving complaints catalog..." />
        ) : complaints.length === 0 ? (
          <EmptyState
            icon={FileText}
            title="No complaints match your filters"
            description="Try modifying search keywords or clearing active filters to see all records."
            action={
              <button
                onClick={handleResetFilters}
                className="px-3 py-1.5 text-xs font-semibold text-brand-600 bg-brand-50 hover:bg-brand-100 rounded-lg transition-colors"
              >
                Clear All Filters
              </button>
            }
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-200 text-[10px]">
                <tr>
                  <th
                    className="px-4 py-3 cursor-pointer hover:bg-slate-100 transition-colors"
                    onClick={() => toggleSort('complaintNumber')}
                  >
                    <div className="flex items-center space-x-1">
                      <span>Case ID</span>
                      <ArrowUpDown className="w-3 h-3" />
                    </div>
                  </th>
                  <th className="px-4 py-3">Subject & Complainant</th>
                  <th className="px-4 py-3">Institution</th>
                  <th className="px-4 py-3">Bureau / Cell</th>
                  <th className="px-4 py-3">Category</th>
                  <th
                    className="px-4 py-3 cursor-pointer hover:bg-slate-100 transition-colors"
                    onClick={() => toggleSort('priority')}
                  >
                    <div className="flex items-center space-x-1">
                      <span>Priority</span>
                      <ArrowUpDown className="w-3 h-3" />
                    </div>
                  </th>
                  <th
                    className="px-4 py-3 cursor-pointer hover:bg-slate-100 transition-colors"
                    onClick={() => toggleSort('status')}
                  >
                    <div className="flex items-center space-x-1">
                      <span>Status</span>
                      <ArrowUpDown className="w-3 h-3" />
                    </div>
                  </th>
                  <th
                    className="px-4 py-3 cursor-pointer hover:bg-slate-100 transition-colors"
                    onClick={() => toggleSort('dueDate')}
                  >
                    <div className="flex items-center space-x-1">
                      <span>SLA Due</span>
                      <ArrowUpDown className="w-3 h-3" />
                    </div>
                  </th>
                  <th className="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {complaints.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="px-4 py-3.5 font-mono font-semibold text-brand-700 whitespace-nowrap">
                      <div className="flex items-center space-x-1">
                        <Link
                          to={`/admin/complaints/${c.complaintNumber || c.id}`}
                          className="hover:text-brand-900 hover:underline cursor-pointer"
                          title={`Open complaint dossier ${c.complaintNumber}`}
                        >
                          {c.complaintNumber}
                        </Link>
                        {c.potentialDuplicate && (
                          <span title="Potential Duplicate / Overlapping Case" className="text-amber-500">
                            <CopyCheck className="w-3.5 h-3.5 inline" />
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="px-4 py-3.5">
                      <Link
                        to={`/admin/complaints/${c.complaintNumber || c.id}`}
                        className="font-semibold text-slate-900 hover:text-brand-600 transition-colors max-w-xs truncate block"
                        title={c.title}
                      >
                        {c.title}
                      </Link>
                      <div className="text-[11px] text-slate-500 mt-0.5">
                        Lodged by: {c.complainantName}
                      </div>
                    </td>
                    <td className="px-4 py-3.5 text-slate-700 max-w-[180px] truncate" title={c.institutionName}>
                      {c.institutionName}
                    </td>
                    <td className="px-4 py-3.5">
                      <div className="flex flex-col gap-0.5">
                        <span className="font-mono text-[10px] font-bold text-blue-700 bg-blue-50 px-1.5 py-0.2 rounded border border-blue-200 w-max">
                          {c.bureauCode || 'RB'}
                        </span>
                        {c.cellCode && (
                          <span className="text-[10px] text-slate-500">
                            {c.cellCode}
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="px-4 py-3.5">
                      <CategoryBadge category={c.category} displayName={c.categoryDisplayName} />
                    </td>
                    <td className="px-4 py-3.5">
                      <PriorityBadge priority={c.priority} />
                    </td>
                    <td className="px-4 py-3.5">
                      <StatusBadge status={c.status} />
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <div className="flex items-center space-x-1">
                        {c.overdue && (
                          <AlertCircle className="w-3.5 h-3.5 text-rose-500" title="Overdue / SLA Breached" />
                        )}
                        <span className={c.overdue ? 'font-semibold text-rose-600' : 'text-slate-600'}>
                          {formatDate(c.dueDate)}
                        </span>
                      </div>
                    </td>
                    <td className="px-4 py-3.5 text-right whitespace-nowrap">
                      <Link
                        to={`/admin/complaints/${c.complaintNumber || c.id}`}
                        className="px-2.5 py-1 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-200 rounded-md transition-colors"
                      >
                        Manage
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination
          currentPage={pageData.pageNumber}
          totalPages={pageData.totalPages}
          totalElements={pageData.totalElements}
          pageSize={pageData.pageSize}
          onPageChange={(newPage) => setPageData((prev) => ({ ...prev, pageNumber: newPage }))}
        />
      </div>
    </div>
  );
};

export default AdminComplaintsPage;
