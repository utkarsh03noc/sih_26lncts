export const ROLES = {
  ADMIN: 'ADMIN',
  INSTITUTION_OFFICER: 'INSTITUTION_OFFICER',
  COMPLAINANT: 'COMPLAINANT',
};

export const COMPLAINT_STATUS = {
  SUBMITTED: { label: 'Submitted', color: 'bg-blue-100 text-blue-800 border-blue-200' },
  ASSIGNED: { label: 'Assigned', color: 'bg-indigo-100 text-indigo-800 border-indigo-200' },
  UNDER_REVIEW: { label: 'Under Review', color: 'bg-amber-100 text-amber-800 border-amber-200' },
  IN_PROGRESS: { label: 'In Progress', color: 'bg-sky-100 text-sky-800 border-sky-200' },
  RESOLVED: { label: 'Resolved', color: 'bg-emerald-100 text-emerald-800 border-emerald-200' },
  CLOSED: { label: 'Closed', color: 'bg-slate-100 text-slate-700 border-slate-200' },
  REOPENED: { label: 'Reopened', color: 'bg-orange-100 text-orange-800 border-orange-200' },
  ESCALATED: { label: 'Escalated', color: 'bg-red-100 text-red-800 border-red-200' },
  MERGED: { label: 'Merged', color: 'bg-purple-100 text-purple-800 border-purple-200' },
};

export const PRIORITIES = {
  LOW: { label: 'Low', color: 'bg-slate-100 text-slate-700 border-slate-300' },
  MEDIUM: { label: 'Medium', color: 'bg-blue-100 text-blue-700 border-blue-300' },
  HIGH: { label: 'High', color: 'bg-amber-100 text-amber-800 border-amber-300' },
  CRITICAL: { label: 'Critical', color: 'bg-rose-100 text-rose-800 border-rose-300' },
};

export const CATEGORIES = [
  { value: 'ACADEMIC', label: 'Academic' },
  { value: 'EXAMINATION', label: 'Examination' },
  { value: 'ADMISSION', label: 'Admission' },
  { value: 'FEES', label: 'Fees' },
  { value: 'SCHOLARSHIP', label: 'Scholarship' },
  { value: 'HOSTEL', label: 'Hostel' },
  { value: 'INFRASTRUCTURE', label: 'Infrastructure' },
  { value: 'FACULTY_STAFF', label: 'Faculty/Staff' },
  { value: 'HARASSMENT', label: 'Harassment' },
  { value: 'ADMINISTRATION', label: 'Administration' },
  { value: 'OTHER', label: 'Other' },
];

export const STATUS_OPTIONS = Object.entries(COMPLAINT_STATUS).map(([value, item]) => ({
  value,
  label: item.label,
  color: item.color,
}));

export const PRIORITY_OPTIONS = Object.entries(PRIORITIES).map(([value, item]) => ({
  value,
  label: item.label,
  color: item.color,
}));

export const CHART_COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'];
