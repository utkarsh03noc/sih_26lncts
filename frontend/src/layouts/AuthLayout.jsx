import React from 'react';
import { Outlet } from 'react-router-dom';
import { Info } from 'lucide-react';

const AuthLayout = () => {
  return (
    <div className="min-h-screen bg-slate-900 flex flex-col justify-between">
      {/* Top Academic Disclaimer Banner */}
      <div className="bg-amber-500 text-slate-950 px-4 py-1.5 text-xs font-semibold text-center flex items-center justify-center space-x-2 border-b border-amber-600 shadow-sm">
        <Info className="w-4 h-4 flex-shrink-0" />
        <span>
          Academic / Demo Prototype: Integrated Dashboard for Monitoring and Tracking Institution-Related Complaint Cases Across AICTE.
          Not officially affiliated with or endorsed by AICTE.
        </span>
      </div>

      <div className="flex-1 flex items-center justify-center p-4 sm:p-6">
        <Outlet />
      </div>

      {/* Footer */}
      <footer className="py-4 text-center text-xs text-slate-400">
        AICTE Grievance Monitoring System • Demonstration & Viva Evaluation Project
      </footer>
    </div>
  );
};

export default AuthLayout;
