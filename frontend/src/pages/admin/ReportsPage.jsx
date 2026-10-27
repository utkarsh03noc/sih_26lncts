import React, { useState, useEffect } from 'react';
import { reportService } from '../../services/reportService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Printer, Download, FileCheck, CheckCircle2 } from 'lucide-react';
import { formatDate } from '../../utils/formatters';

const ReportsPage = () => {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadReport();
  }, []);

  const loadReport = async () => {
    try {
      setLoading(true);
      const res = await reportService.getSummaryReport();
      setReport(res.data);
    } catch (err) {
      console.error('Failed to load report:', err);
    } finally {
      setLoading(false);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  if (loading || !report) {
    return <LoadingSpinner text="Generating executive compliance report..." size="lg" />;
  }

  const { stats } = report;

  return (
    <div className="space-y-6">
      {/* Action Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 print:hidden">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Grievance Redressal Audit & Performance Report
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Official summary document for accreditation, oversight reviews, and academic viva evaluation
          </p>
        </div>

        <button
          onClick={handlePrint}
          className="inline-flex items-center px-4 py-2 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-lg shadow-sm hover:bg-slate-50 transition-colors"
        >
          <Printer className="w-4 h-4 mr-2" />
          Print / Save PDF
        </button>
      </div>

      {/* Printable Report Document */}
      <div className="bg-white rounded-xl border border-slate-200 p-8 shadow-sm print:border-none print:shadow-none space-y-6 text-xs text-slate-800">
        {/* Document Header */}
        <div className="border-b-2 border-slate-800 pb-6 text-center">
          <div className="w-12 h-12 mx-auto rounded-xl bg-slate-900 text-white flex items-center justify-center font-bold text-lg mb-2">
            AI
          </div>
          <h2 className="text-lg font-bold uppercase tracking-wide text-slate-900">
            All India Council for Technical Education (AICTE)
          </h2>
          <h3 className="text-sm font-semibold text-slate-700 mt-1">
            Institutional Grievance Monitoring & Redressal Audit Report
          </h3>
          <p className="text-[11px] text-slate-500 mt-1">
            Generated on: {formatDate(new Date())} • Academic Demonstration Project
          </p>
        </div>

        {/* Executive Summary Cards */}
        <div>
          <h4 className="font-bold text-slate-900 uppercase tracking-wider text-xs mb-3">
            1. Key Performance Indicators
          </h4>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
              <span className="text-[10px] uppercase font-bold text-slate-400">Total Cases</span>
              <p className="text-xl font-bold text-slate-900">{stats.totalComplaints}</p>
            </div>
            <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
              <span className="text-[10px] uppercase font-bold text-slate-400">Resolved Cases</span>
              <p className="text-xl font-bold text-emerald-600">{stats.resolvedComplaints}</p>
            </div>
            <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
              <span className="text-[10px] uppercase font-bold text-slate-400">Escalated Cases</span>
              <p className="text-xl font-bold text-rose-600">{stats.escalatedComplaints}</p>
            </div>
            <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
              <span className="text-[10px] uppercase font-bold text-slate-400">Resolution Rate</span>
              <p className="text-xl font-bold text-blue-600">{stats.resolutionRate}%</p>
            </div>
          </div>
        </div>

        {/* Category Breakdown Table */}
        <div>
          <h4 className="font-bold text-slate-900 uppercase tracking-wider text-xs mb-3">
            2. Category-Wise Distribution
          </h4>
          <table className="w-full text-left border border-slate-200">
            <thead className="bg-slate-100 font-bold text-slate-700">
              <tr>
                <th className="p-2 border">Category</th>
                <th className="p-2 border text-center">Case Volume</th>
                <th className="p-2 border text-center">Share (%)</th>
              </tr>
            </thead>
            <tbody>
              {stats.categoryStats?.map((c) => {
                const pct =
                  stats.totalComplaints > 0
                    ? Math.round((c.count / stats.totalComplaints) * 100)
                    : 0;
                return (
                  <tr key={c.category} className="border-b">
                    <td className="p-2 border font-medium">{c.displayName}</td>
                    <td className="p-2 border text-center">{c.count}</td>
                    <td className="p-2 border text-center">{pct}%</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {/* Institution Performance Table */}
        <div>
          <h4 className="font-bold text-slate-900 uppercase tracking-wider text-xs mb-3">
            3. Institution Compliance League
          </h4>
          <table className="w-full text-left border border-slate-200">
            <thead className="bg-slate-100 font-bold text-slate-700">
              <tr>
                <th className="p-2 border">Institution</th>
                <th className="p-2 border text-center">Total</th>
                <th className="p-2 border text-center">Resolved</th>
                <th className="p-2 border text-center">Escalated</th>
                <th className="p-2 border text-right">Resolution Rate</th>
              </tr>
            </thead>
            <tbody>
              {stats.institutionStats?.map((inst) => (
                <tr key={inst.institutionId} className="border-b">
                  <td className="p-2 border font-medium">{inst.institutionName}</td>
                  <td className="p-2 border text-center">{inst.totalComplaints}</td>
                  <td className="p-2 border text-center text-emerald-700 font-semibold">{inst.resolvedComplaints}</td>
                  <td className="p-2 border text-center text-rose-700 font-semibold">{inst.escalatedComplaints}</td>
                  <td className="p-2 border text-right font-bold">{inst.resolutionRate}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Academic Disclaimer Sign-off */}
        <div className="pt-6 border-t border-slate-200 text-center text-slate-500 text-[11px]">
          <p>This report was generated automatically by the AICTE Complaint Tracking Platform Demonstration System.</p>
          <p className="font-semibold text-slate-700 mt-0.5">Academic Prototype — Not an official government publication.</p>
        </div>
      </div>
    </div>
  );
};

export default ReportsPage;
