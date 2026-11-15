import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { applicationApi } from '../../services/api';
import {
  FolderOpen,
  Search,
  Filter,
  Eye,
  CheckCircle2,
  Clock,
  AlertCircle,
  RotateCcw,
  CreditCard,
  Award
} from 'lucide-react';

export default function ApplicationMonitoringPage() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    loadApplications();
  }, []);

  const loadApplications = async () => {
    try {
      setLoading(true);
      const res = await applicationApi.getAll();
      if (res.success && res.data) {
        setApplications(res.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const filtered = applications.filter((app) => {
    const matches =
      app.applicationNumber.toLowerCase().includes(search.toLowerCase()) ||
      app.applicantName.toLowerCase().includes(search.toLowerCase()) ||
      app.propertyAddress.toLowerCase().includes(search.toLowerCase());

    if (filter === 'ALL') return matches;
    return matches && app.status === filter;
  });

  return (
    <div className="space-y-6">
      <div className="pb-4 border-b border-slate-200">
        <h1 className="text-2xl font-extrabold text-slate-900">Application Monitoring Console</h1>
        <p className="text-xs text-slate-500">Live inspection of all municipal building permits submitted across sectors</p>
      </div>

      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
          <input
            type="text"
            placeholder="Search by ref, citizen name, address..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
          />
        </div>

        <select
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="px-3.5 py-2 rounded-xl border border-slate-300 text-xs bg-white outline-none"
        >
          <option value="ALL">All Statuses</option>
          <option value="UNDER_DEPARTMENT_REVIEW">Under Review</option>
          <option value="REVISION_REQUESTED">Revision Requested</option>
          <option value="PAYMENT_PENDING">Payment Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      <div className="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-xs text-slate-500">Loading permit applications...</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider text-[10px]">
                <tr>
                  <th className="py-3 px-4">Ref Number</th>
                  <th className="py-3 px-4">Citizen</th>
                  <th className="py-3 px-4">Category</th>
                  <th className="py-3 px-4">Location</th>
                  <th className="py-3 px-4">Current Status</th>
                  <th className="py-3 px-4">Submission Date</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filtered.map((app) => (
                  <tr key={app.id} className="hover:bg-slate-50/80 transition">
                    <td className="py-3.5 px-4 font-mono font-bold text-municipal-800">
                      {app.applicationNumber}
                    </td>
                    <td className="py-3.5 px-4 font-medium text-slate-800">
                      {app.applicantName}
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="bg-slate-100 text-slate-700 px-2 py-0.5 rounded font-mono text-[10px]">
                        {app.buildingType}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-600 truncate max-w-[200px]">
                      {app.propertyAddress} ({app.zone})
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-100 text-slate-800">
                        {app.status}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {app.submissionDate ? new Date(app.submissionDate).toLocaleDateString() : 'Draft'}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Link
                        to={`/applicant/applications/${app.id}`}
                        className="inline-flex items-center gap-1 text-civicTeal-600 hover:text-civicTeal-700 font-semibold"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        <span>Inspect</span>
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
