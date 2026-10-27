import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import ComplaintFilter from '../../components/complaints/ComplaintFilter';
import StatusBadge from '../../components/common/StatusBadge';
import PriorityBadge from '../../components/common/PriorityBadge';
import CategoryBadge from '../../components/common/CategoryBadge';
import Pagination from '../../components/common/Pagination';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import { formatDate } from '../../utils/formatters';
import { FileText, PlusCircle } from 'lucide-react';

const MyComplaintsPage = () => {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pageData, setPageData] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
  });

  const [filters, setFilters] = useState({
    search: '',
    status: '',
    category: '',
    priority: '',
    sortBy: 'createdAt',
    sortDir: 'desc',
  });

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
      console.error('Failed to load my complaints:', err);
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
      status: '',
      category: '',
      priority: '',
      sortBy: 'createdAt',
      sortDir: 'desc',
    });
    setPageData((prev) => ({ ...prev, pageNumber: 0 }));
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            My Registered Grievance Cases
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Track real-time investigation stages, officer remarks, and resolutions for your complaints
          </p>
        </div>

        <Link
          to="/complainant/register-complaint"
          className="inline-flex items-center px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors"
        >
          <PlusCircle className="w-4 h-4 mr-1.5" />
          Lodge New Grievance
        </Link>
      </div>

      <ComplaintFilter
        filters={filters}
        onFilterChange={handleFilterChange}
        onReset={handleResetFilters}
      />

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <LoadingSpinner text="Retrieving your grievances..." />
        ) : complaints.length === 0 ? (
          <EmptyState
            icon={FileText}
            title="No complaints registered yet"
            description="You have not lodged any grievances matching the search criteria."
            action={
              <Link
                to="/complainant/register-complaint"
                className="px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors inline-block"
              >
                Register Grievance Now
              </Link>
            }
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-200">
                <tr>
                  <th className="px-5 py-3">Case ID</th>
                  <th className="px-5 py-3">Grievance Subject</th>
                  <th className="px-5 py-3">Institution</th>
                  <th className="px-5 py-3">Category</th>
                  <th className="px-5 py-3">Priority</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3">Lodged On</th>
                  <th className="px-5 py-3 text-right">View</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {complaints.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="px-5 py-3.5 font-mono font-semibold text-brand-700 whitespace-nowrap">
                      <Link
                        to={`/complainant/complaints/${c.complaintNumber}`}
                        className="hover:text-brand-900 hover:underline cursor-pointer"
                        title={`View complaint dossier ${c.complaintNumber}`}
                      >
                        {c.complaintNumber}
                      </Link>
                    </td>
                    <td className="px-5 py-3.5">
                      <Link
                        to={`/complainant/complaints/${c.complaintNumber}`}
                        className="font-semibold text-slate-900 hover:text-brand-600 transition-colors max-w-xs truncate block"
                        title={c.title}
                      >
                        {c.title}
                      </Link>
                    </td>
                    <td className="px-5 py-3.5 text-slate-600 max-w-[180px] truncate" title={c.institutionName}>
                      {c.institutionName}
                    </td>
                    <td className="px-5 py-3.5">
                      <CategoryBadge category={c.category} displayName={c.categoryDisplayName} />
                    </td>
                    <td className="px-5 py-3.5">
                      <PriorityBadge priority={c.priority} />
                    </td>
                    <td className="px-5 py-3.5">
                      <StatusBadge status={c.status} />
                    </td>
                    <td className="px-5 py-3.5 text-slate-500 whitespace-nowrap">
                      {formatDate(c.createdAt)}
                    </td>
                    <td className="px-5 py-3.5 text-right whitespace-nowrap">
                      <Link
                        to={`/complainant/complaints/${c.complaintNumber}`}
                        className="px-3 py-1 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-200 rounded-md transition-colors"
                      >
                        Timeline →
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

export default MyComplaintsPage;
