import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';

const UnauthorizedPage = () => {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center p-6">
      <div className="w-16 h-16 bg-rose-50 text-rose-600 rounded-2xl flex items-center justify-center mb-4 border border-rose-200">
        <ShieldAlert className="w-8 h-8" />
      </div>
      <h1 className="text-xl font-bold text-slate-900 tracking-tight">Access Restricted</h1>
      <p className="text-xs text-slate-500 max-w-sm mt-1">
        You do not possess the required role permissions to view or perform operations on this administrative portal module.
      </p>
      <Link
        to="/"
        className="mt-6 inline-flex items-center px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors"
      >
        <ArrowLeft className="w-4 h-4 mr-1.5" /> Return to Dashboard
      </Link>
    </div>
  );
};

export default UnauthorizedPage;
