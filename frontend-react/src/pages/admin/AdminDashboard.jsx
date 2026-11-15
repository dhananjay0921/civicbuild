import React, { useState, useEffect } from 'react';
import { dashboardApi } from '../../services/api';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Legend,
  PieChart, Pie, Cell
} from 'recharts';
import {
  FolderOpen,
  Clock,
  CheckCircle2,
  AlertCircle,
  Activity,
  Building,
  TrendingUp,
  ShieldCheck
} from 'lucide-react';

export default function AdminDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      const res = await dashboardApi.getSummary();
      if (res.success && res.data) {
        setData(res.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const COLORS = ['#0D9488', '#F59E0B', '#EF4444', '#6366F1'];

  const deptChartData = data?.departmentPendingStats
    ? Object.keys(data.departmentPendingStats).map((k) => ({
        name: k,
        pending: data.departmentPendingStats[k]
      }))
    : [];

  return (
    <div className="space-y-8">
      {/* Title */}
      <div className="pb-4 border-b border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase tracking-wider text-civicTeal-600 font-bold">Municipal Governance Console</span>
          <h1 className="text-2xl font-extrabold text-slate-900">Executive Permitting Dashboard</h1>
          <p className="text-xs text-slate-500">Cross-departmental performance metrics & Camunda 8 workflow monitoring</p>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1">
          <span className="text-slate-400 text-xs font-bold uppercase tracking-wider">Total Filed</span>
          <div className="text-2xl font-extrabold text-slate-900">{data?.totalApplications || 0}</div>
          <p className="text-[11px] text-slate-500">Permit proposals</p>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1">
          <span className="text-slate-400 text-xs font-bold uppercase tracking-wider">In Clearance</span>
          <div className="text-2xl font-extrabold text-amber-600">{data?.underReviewApplications || 0}</div>
          <p className="text-[11px] text-slate-500">Multi-instance reviews</p>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1">
          <span className="text-slate-400 text-xs font-bold uppercase tracking-wider">Sanctioned</span>
          <div className="text-2xl font-extrabold text-emerald-600">{data?.approvedApplications || 0}</div>
          <p className="text-[11px] text-slate-500">Permits approved</p>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1">
          <span className="text-slate-400 text-xs font-bold uppercase tracking-wider">Rejected</span>
          <div className="text-2xl font-extrabold text-rose-600">{data?.rejectedApplications || 0}</div>
          <p className="text-[11px] text-slate-500">NBC non-compliance</p>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1 col-span-2 sm:col-span-1">
          <span className="text-slate-400 text-xs font-bold uppercase tracking-wider">Avg Processing</span>
          <div className="text-2xl font-extrabold text-municipal-800">{data?.avgProcessingDays || 4.2}d</div>
          <p className="text-[11px] text-slate-500">SLA completion time</p>
        </div>
      </div>

      {/* Visual Analytics Charts (Recharts) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Monthly Applications Bar Chart */}
        <div className="lg:col-span-2 bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
          <div className="flex items-center justify-between pb-2 border-b border-slate-100">
            <h3 className="text-sm font-bold text-slate-800 flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-civicTeal-600" />
              <span>Monthly Permitting Trends (2026)</span>
            </h3>
            <span className="text-xs text-slate-400">Submissions vs Approvals</span>
          </div>

          <div className="h-72 w-full pt-4">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data?.monthlyStats || []}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#F1F5F9" />
                <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip />
                <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '8px' }} />
                <Bar dataKey="submitted" name="Applications Submitted" fill="#0F2942" radius={[4, 4, 0, 0]} />
                <Bar dataKey="approved" name="Permits Sanctioned" fill="#0D9488" radius={[4, 4, 0, 0]} />
                <Bar dataKey="rejected" name="Rejections" fill="#F43F5E" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Department Pending Reviews Workload */}
        <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
          <div className="pb-2 border-b border-slate-100">
            <h3 className="text-sm font-bold text-slate-800 flex items-center gap-2">
              <Building className="w-4 h-4 text-amber-500" />
              <span>Department Clearance Queue</span>
            </h3>
            <p className="text-[11px] text-slate-400 mt-0.5">Active reviews awaiting officer action</p>
          </div>

          <div className="h-64 w-full flex items-center justify-center">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={deptChartData} layout="vertical" margin={{ left: 20 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#F1F5F9" />
                <XAxis type="number" tick={{ fontSize: 11 }} />
                <YAxis type="category" dataKey="name" tick={{ fontSize: 10, fontWeight: 'bold' }} />
                <Tooltip />
                <Bar dataKey="pending" fill="#346DAE" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
