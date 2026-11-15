import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { applicationApi } from '../../services/api';
import {
  FolderOpen,
  Search,
  Filter,
  FilePlus,
  ArrowRight,
  Clock,
  CheckCircle2,
  AlertCircle,
  RotateCcw,
  CreditCard,
  Award
} from 'lucide-react';

export default function MyApplicationsPage() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');

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

  const getStatusBadge = (status) => {
    switch (status) {
      case 'APPROVED':
        return <span className="bg-emerald-100 text-emerald-800 text-xs px-2.5 py-1 rounded-full font-semibold">Approved</span>;
      case 'REJECTED':
        return <span className="bg-rose-100 text-rose-800 text-xs px-2.5 py-1 rounded-full font-semibold">Rejected</span>;
      case 'REVISION_REQUESTED':
        return <span className="bg-indigo-100 text-indigo-800 text-xs px-2.5 py-1 rounded-full font-semibold">Revision Needed</span>;
      case 'FEE_PENDING':
      case 'PAYMENT_PENDING':
        return <span className="bg-blue-100 text-blue-800 text-xs px-2.5 py-1 rounded-full font-semibold">Payment Due</span>;
      case 'UNDER_DEPARTMENT_REVIEW':
        return <span className="bg-amber-100 text-amber-800 text-xs px-2.5 py-1 rounded-full font-semibold">Under Review</span>;
      case 'DRAFT':
        return <span className="bg-slate-100 text-slate-700 text-xs px-2.5 py-1 rounded-full font-semibold">Draft</span>;
      default:
        return <span className="bg-slate-100 text-slate-800 text-xs px-2.5 py-1 rounded-full font-semibold">{status}</span>;
    }
  };

  const filtered = applications.filter((app) => {
    const matchesSearch =
      app.applicationNumber.toLowerCase().includes(search.toLowerCase()) ||
      app.propertyAddress.toLowerCase().includes(search.toLowerCase());

    if (filterStatus === 'ALL') return matchesSearch;
    return matchesSearch && app.status === filterStatus;
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-200 gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900">My Permit Applications</h1>
          <p className="text-xs text-slate-500">Track and monitor status of your building proposals</p>
        </div>
        <Link
          to="/applicant/new-application"
          className="bg-civicTeal-600 hover:bg-civicTeal-700 text-white font-semibold px-4 py-2.5 rounded-xl text-xs flex items-center gap-1.5 shadow-sm transition"
        >
          <FilePlus className="w-4 h-4" />
          <span>New Application</span>
        </Link>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
          <input
            type="text"
            placeholder="Search by application number or address..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
          />
        </div>

        <div className="flex items-center gap-2 overflow-x-auto pb-1 sm:pb-0">
          {['ALL', 'UNDER_DEPARTMENT_REVIEW', 'REVISION_REQUESTED', 'PAYMENT_PENDING', 'APPROVED', 'REJECTED'].map((st) => (
            <button
              key={st}
              onClick={() => setFilterStatus(st)}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition ${
                filterStatus === st
                  ? 'bg-municipal-800 text-white shadow-sm'
                  : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
              }`}
            >
              {st === 'ALL' ? 'All Applications' : st.replace(/_/g, ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* Applications List */}
      <div className="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-xs text-slate-500">Loading permit applications...</div>
        ) : filtered.length === 0 ? (
          <div className="p-12 text-center text-xs text-slate-500">No applications match your filter.</div>
        ) : (
          <div className="divide-y divide-slate-100">
            {filtered.map((app) => (
              <div key={app.id} className="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-slate-50 transition">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-bold text-sm text-municipal-800">{app.applicationNumber}</span>
                    {getStatusBadge(app.status)}
                    <span className="text-[11px] bg-slate-100 text-slate-600 px-2 py-0.5 rounded font-medium">
                      {app.buildingType}
                    </span>
                  </div>
                  <p className="text-xs text-slate-600">{app.propertyAddress} &bull; {app.wardNumber} ({app.zone})</p>
                  <p className="text-[11px] text-slate-400">
                    Area: {app.builtUpArea} sq.ft &bull; Floors: {app.numberOfFloors} &bull; Zeebe Key: {app.processInstanceKey || 'Pending'}
                  </p>
                </div>

                <div className="flex items-center gap-2 shrink-0">
                  <Link
                    to={`/applicant/applications/${app.id}`}
                    className="text-xs font-semibold px-3 py-1.5 rounded-xl border border-slate-300 hover:bg-white text-slate-700 transition"
                  >
                    View Details & Workflow
                  </Link>

                  {app.status === 'PAYMENT_PENDING' && (
                    <Link
                      to={`/applicant/payments/${app.id}`}
                      className="text-xs font-semibold px-3.5 py-1.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white shadow-sm transition"
                    >
                      Pay Fee
                    </Link>
                  )}

                  {app.status === 'APPROVED' && (
                    <a
                      href={`/api/permits/${app.id}/download`}
                      target="_blank"
                      rel="noreferrer"
                      className="text-xs font-semibold px-3 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm transition flex items-center gap-1"
                    >
                      <Award className="w-3.5 h-3.5" />
                      <span>Download Permit</span>
                    </a>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
