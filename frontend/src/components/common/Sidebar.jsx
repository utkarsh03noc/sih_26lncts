import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  FileText,
  Building,
  AlertTriangle,
  BarChart3,
  FileCheck2,
  History,
  PlusCircle,
  Search,
  MessageSquare,
  Network,
  CopyCheck,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';

const Sidebar = ({ isOpen, onClose }) => {
  const { user, isAdmin, isOfficer, isComplainant } = useAuth();

  const adminNav = [
    { label: 'Executive Dashboard', path: '/admin/dashboard', icon: LayoutDashboard },
    { label: 'All Complaints', path: '/admin/complaints', icon: FileText },
    { label: 'Institution Monitoring', path: '/admin/institutions', icon: Building },
    { label: 'Bureaus & Cells', path: '/admin/bureaus', icon: Network },
    { label: 'Duplicate Alerts', path: '/admin/duplicate-alerts', icon: CopyCheck },
    { label: 'Escalations & SLA', path: '/admin/escalations', icon: AlertTriangle },
    { label: 'Analytics', path: '/admin/analytics', icon: BarChart3 },
    { label: 'Reports', path: '/admin/reports', icon: FileCheck2 },
    { label: 'Audit Logs', path: '/admin/audit-logs', icon: History },
  ];

  const officerNav = [
    { label: 'Officer Dashboard', path: '/officer/dashboard', icon: LayoutDashboard },
    { label: 'Assigned Complaints', path: '/officer/complaints', icon: FileText },
    { label: 'Institution Analytics', path: '/officer/analytics', icon: BarChart3 },
  ];

  const complainantNav = [
    { label: 'My Dashboard', path: '/complainant/dashboard', icon: LayoutDashboard },
    { label: 'My Complaints', path: '/complainant/complaints', icon: FileText },
    { label: 'Register Grievance', path: '/complainant/register-complaint', icon: PlusCircle },
    { label: 'Track Complaint', path: '/complainant/track', icon: Search },
    { label: 'My Feedback', path: '/complainant/feedback', icon: MessageSquare },
  ];

  const navItems = isAdmin ? adminNav : isOfficer ? officerNav : complainantNav;

  return (
    <>
      {/* Mobile backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/50 lg:hidden"
          onClick={onClose}
        />
      )}

      <aside
        className={`fixed top-0 bottom-0 left-0 z-40 w-64 bg-slate-900 text-slate-100 flex flex-col transition-transform duration-200 ease-in-out lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Sidebar Brand Header */}
        <div className="p-4 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center font-bold text-white shadow">
              AI
            </div>
            <div>
              <div className="text-sm font-bold tracking-tight text-white leading-none">
                AICTE Grievance
              </div>
              <div className="text-[10px] text-slate-400 mt-0.5">Monitoring Console</div>
            </div>
          </div>
        </div>

        {/* Institution Affiliation Badge for Officer */}
        {isOfficer && user?.institutionName && (
          <div className="m-3 p-2.5 rounded-lg bg-slate-800/80 border border-slate-700/60 text-xs text-slate-300">
            <div className="text-[10px] font-semibold uppercase tracking-wider text-slate-400">Affiliated Institution</div>
            <div className="font-medium text-white truncate mt-0.5">{user.institutionName}</div>
          </div>
        )}

        {/* Navigation Items */}
        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          <div className="px-3 pb-2 text-[10px] font-bold uppercase tracking-wider text-slate-400">
            Navigation Menu
          </div>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center px-3 py-2.5 text-xs font-medium rounded-lg transition-colors ${
                    isActive
                      ? 'bg-blue-600 text-white shadow-sm'
                      : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                  }`
                }
              >
                <Icon className="w-4 h-4 mr-3 flex-shrink-0" />
                <span className="truncate">{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* Academic Project Disclaimer */}
        <div className="p-3 border-t border-slate-800 text-[10px] text-slate-400 text-center leading-relaxed">
          Academic / Demo Prototype
          <div className="text-[9px] text-slate-500 mt-0.5">Not officially affiliated with AICTE</div>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;
