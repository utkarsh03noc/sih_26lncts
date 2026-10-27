import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Sidebar from '../components/common/Sidebar';
import { Info } from 'lucide-react';

const DashboardLayout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      {/* Prominent Academic & Demo Disclaimer Banner */}
      <div className="bg-amber-500 text-slate-950 px-4 py-1.5 text-xs font-semibold text-center flex items-center justify-center space-x-2 border-b border-amber-600 shadow-sm">
        <Info className="w-4 h-4 flex-shrink-0" />
        <span>
          Academic / Demo Prototype: Integrated Dashboard for Monitoring and Tracking Institution-Related Complaint Cases Across AICTE.
          Not officially affiliated with or endorsed by AICTE.
        </span>
      </div>

      <div className="flex-1 flex">
        {/* Sidebar */}
        <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

        {/* Main Content Area */}
        <div className="flex-1 flex flex-col min-w-0 lg:pl-64">
          <Navbar onToggleSidebar={() => setSidebarOpen((prev) => !prev)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 max-w-7xl w-full mx-auto">
            <Outlet />
          </main>

          {/* Institutional Footer */}
          <footer className="bg-white border-t border-slate-200 py-4 px-6 text-center text-xs text-slate-500">
            <p>
              Integrated Grievance Monitoring Platform • AICTE Demonstration Project • Developed with Spring Boot & React
            </p>
          </footer>
        </div>
      </div>
    </div>
  );
};

export default DashboardLayout;
