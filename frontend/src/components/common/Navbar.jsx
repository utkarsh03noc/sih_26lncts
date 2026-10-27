import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Menu,
  LogOut,
  User as UserIcon,
  Building2,
  ShieldCheck,
  UserCheck,
  Search,
  ArrowRight,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';

const Navbar = ({ onToggleSidebar }) => {
  const { user, logout, isAdmin, isOfficer } = useAuth();
  const [globalSearch, setGlobalSearch] = useState('');
  const navigate = useNavigate();

  const handleGlobalSearch = (e) => {
    e.preventDefault();
    const query = globalSearch.trim();
    if (!query) return;

    if (query.toUpperCase().startsWith('CMP-')) {
      navigate(`/complaints/${query.toUpperCase()}`);
    } else if (isAdmin) {
      if (query.toUpperCase().startsWith('AICTE-')) {
        navigate(`/admin/institutions?search=${encodeURIComponent(query)}`);
      } else {
        navigate(`/admin/complaints?search=${encodeURIComponent(query)}`);
      }
    } else if (isOfficer) {
      navigate(`/officer/complaints?search=${encodeURIComponent(query)}`);
    } else {
      navigate(`/complainant/track?number=${encodeURIComponent(query)}`);
    }
    setGlobalSearch('');
  };

  const getRoleBadge = (role) => {
    switch (role) {
      case 'ADMIN':
        return { label: 'AICTE Central Admin', color: 'bg-rose-100 text-rose-800 border-rose-200', icon: ShieldCheck };
      case 'INSTITUTION_OFFICER':
        return { label: 'Nodal Officer', color: 'bg-indigo-100 text-indigo-800 border-indigo-200', icon: Building2 };
      default:
        return { label: 'Complainant', color: 'bg-blue-100 text-blue-800 border-blue-200', icon: UserCheck };
    }
  };

  const badge = getRoleBadge(user?.role);
  const BadgeIcon = badge.icon;

  return (
    <header className="sticky top-0 z-30 bg-white border-b border-slate-200 shadow-sm">
      <div className="flex items-center justify-between px-4 py-2.5 sm:px-6">
        <div className="flex items-center space-x-3">
          <button
            onClick={onToggleSidebar}
            className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 lg:hidden focus:outline-none"
            title="Toggle Menu"
          >
            <Menu className="w-5 h-5" />
          </button>

          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg bg-blue-700 text-white flex items-center justify-center font-bold text-sm tracking-wider shadow-inner">
              AI
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h1 className="text-sm font-bold text-slate-900 tracking-tight sm:text-base leading-none">
                  AICTE Grievance Portal
                </h1>
                <span className="hidden sm:inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-semibold bg-amber-100 text-amber-800 border border-amber-300">
                  DEMO DATA
                </span>
              </div>
              <p className="text-[10px] text-slate-500 hidden sm:block mt-0.5">
                Centralized Institution Complaint Monitoring Across Bureaus (Academic Prototype)
              </p>
            </div>
          </div>
        </div>

        {/* Global Search Input */}
        <div className="hidden md:flex items-center flex-1 max-w-md mx-6">
          <form onSubmit={handleGlobalSearch} className="relative w-full">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search complaint ID (CMP-...), institution code or keyword..."
              value={globalSearch}
              onChange={(e) => setGlobalSearch(e.target.value)}
              className="w-full pl-9 pr-8 py-1.5 text-xs bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all placeholder:text-slate-400"
            />
            {globalSearch && (
              <button
                type="submit"
                className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-400 hover:text-blue-600"
              >
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            )}
          </form>
        </div>

        {/* User Info & Sign Out */}
        <div className="flex items-center space-x-3">
          {user && (
            <div className="hidden sm:flex flex-col items-end">
              <span className="text-xs font-semibold text-slate-800">{user.fullName || user.username}</span>
              <span className={`inline-flex items-center px-2 py-0.2 rounded text-[10px] font-medium border ${badge.color}`}>
                <BadgeIcon className="w-3 h-3 mr-1" />
                {badge.label}
              </span>
            </div>
          )}

          <div className="w-8 h-8 rounded-full bg-slate-100 text-slate-600 flex items-center justify-center border border-slate-200">
            <UserIcon className="w-4 h-4" />
          </div>

          <button
            onClick={logout}
            className="inline-flex items-center px-3 py-1.5 text-xs font-medium text-rose-700 bg-rose-50 border border-rose-200 rounded-lg hover:bg-rose-100 transition-colors"
            title="Sign Out"
          >
            <LogOut className="w-3.5 h-3.5 mr-1" />
            <span className="hidden sm:inline">Sign Out</span>
          </button>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
