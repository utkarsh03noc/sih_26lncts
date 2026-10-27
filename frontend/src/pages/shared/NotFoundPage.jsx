import React from 'react';
import { Link } from 'react-router-dom';
import { FileQuestion, ArrowLeft } from 'lucide-react';

const NotFoundPage = () => {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center p-6">
      <div className="w-16 h-16 bg-slate-100 text-slate-500 rounded-2xl flex items-center justify-center mb-4 border border-slate-200">
        <FileQuestion className="w-8 h-8" />
      </div>
      <h1 className="text-xl font-bold text-slate-900 tracking-tight">Resource Not Found</h1>
      <p className="text-xs text-slate-500 max-w-sm mt-1">
        The grievance portal URL or dossier record you requested could not be located.
      </p>
      <Link
        to="/"
        className="mt-6 inline-flex items-center px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors"
      >
        <ArrowLeft className="w-4 h-4 mr-1.5" /> Return Home
      </Link>
    </div>
  );
};

export default NotFoundPage;
