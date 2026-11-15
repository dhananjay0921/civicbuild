import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { applicationApi } from '../../services/api';
import {
  FilePlus,
  FolderOpen,
  Clock,
  RotateCcw,
  CreditCard,
  Award,
  ArrowRight,
  AlertCircle,
  Building,
  CheckCircle2
} from 'lucide-react';

export default function ApplicantDashboard() {
  const { user } = useAuth();
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchApplications();
  }, []);

  const fetchApplications = async () => {
    try {
      setLoading(true);
      const res = await applicationApi.getAll();
      if (res.success && res.data) {
        setApplications(res.data);
      }
    } catch (err) {
      setError(err.message || 'Failed to load applications');
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
        return <span className="bg-amber-100 text-amber-800 text-xs px-2.5 py-1 rounded-full font-semibold">In Parallel Review</span>;
      case 'DRAFT':
        return <span className="bg-slate-100 text-slate-700 text-xs px-2.5 py-1 rounded-full font-semibold">Draft</span>;
      default:
        return <span className="bg-slate-100 text-slate-800 text-xs px-2.5 py-1 rounded-full font-semibold">{status}</span>;
    }
  };

  const total = applications.length;
  const inReview = applications.filter(a => a.status === 'UNDER_DEPARTMENT_REVIEW' || a.status === 'UNDER_DOCUMENT_VERIFICATION').length;
  const revisions = applications.filter(a => a.status === 'REVISION_REQUESTED').length;
  const approved = applications.filter(a => a.status === 'APPROVED').length;

  return (
    <div className="space-y-8">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-municipal-800 to-municipal-900 text-white rounded-3xl p-6 sm:p-8 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase tracking-wider text-civicTeal-400 font-bold">Applicant Portal</span>
          <h1 className="text-2xl sm:text-3xl font-extrabold mt-1">Welcome, {user?.fullName}</h1>
          <p className="text-xs sm:text-sm text-slate-300 mt-1 max-w-xl">
            Track your municipal building permit submissions, upload required documents, respond to department revision notices, and pay statutory fees.
          </p>
        </div>
        <Link
          to="/applicant/new-application"
          className="bg-civicTeal-600 hover:bg-civicTeal-700 text-white font-semibold px-5 py-3 rounded-xl shadow-md flex items-center gap-2 text-sm transition shrink-0"
        >
          <FilePlus className="w-4 h-4" />
          <span>New Application</span>
        </Link>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold uppercase tracking-wider">Total Filed</span>
            <FolderOpen className="w-4 h-4 text-municipal-700" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{total}</div>
          <p className="text-[11px] text-slate-500">Permits submitted to council</p>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold uppercase tracking-wider">Under Review</span>
            <Clock className="w-4 h-4 text-amber-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{inReview}</div>
          <p className="text-[11px] text-slate-500">Active departmental review</p>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold uppercase tracking-wider">Revisions Needed</span>
            <RotateCcw className="w-4 h-4 text-indigo-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{revisions}</div>
          <p className="text-[11px] text-slate-500">Action requested by officers</p>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold uppercase tracking-wider">Approved Permits</span>
            <Award className="w-4 h-4 text-emerald-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{approved}</div>
          <p className="text-[11px] text-slate-500">Ready for download</p>
        </div>
      </div>

      {/* Recent Applications List */}
      <div className="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
        <div className="p-5 border-b border-slate-100 flex items-center justify-between">
          <h3 className="text-base font-bold text-slate-800">Recent Building Permit Applications</h3>
          <Link to="/applicant/my-applications" className="text-xs font-semibold text-civicTeal-600 hover:text-civicTeal-700 flex items-center gap-1">
            <span>View All</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {loading ? (
          <div className="p-8 text-center text-sm text-slate-500">Loading permit applications...</div>
        ) : error ? (
          <div className="p-6 text-center text-xs text-red-600">{error}</div>
        ) : applications.length === 0 ? (
          <div className="p-12 text-center space-y-3">
            <Building className="w-10 h-10 text-slate-300 mx-auto" />
            <h4 className="text-sm font-bold text-slate-700">No applications found</h4>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              You haven't submitted any building permit applications yet. Start a new submission to begin the automated approval process.
            </p>
            <Link
              to="/applicant/new-application"
              className="inline-flex items-center gap-2 bg-civicTeal-600 hover:bg-civicTeal-700 text-white font-semibold px-4 py-2 rounded-xl text-xs shadow-sm transition"
            >
              <FilePlus className="w-4 h-4" />
              <span>Create First Application</span>
            </Link>
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {applications.slice(0, 5).map((app) => (
              <div key={app.id} className="p-4 sm:p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-slate-50 transition">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-bold text-sm text-municipal-800">{app.applicationNumber}</span>
                    {getStatusBadge(app.status)}
                    <span className="text-[11px] text-slate-400 bg-slate-100 px-2 py-0.5 rounded font-medium">
                      {app.buildingType}
                    </span>
                  </div>
                  <p className="text-xs text-slate-600">{app.propertyAddress} &bull; {app.zone}</p>
                  <p className="text-[11px] text-slate-400">
                    Area: {app.builtUpArea} sq.ft &bull; Floors: {app.numberOfFloors} &bull; Process Key: {app.processInstanceKey || 'Pending'}
                  </p>
                </div>

                <div className="flex items-center gap-3 shrink-0">
                  <Link
                    to={`/applicant/applications/${app.id}`}
                    className="text-xs font-semibold px-3 py-1.5 rounded-lg border border-slate-300 hover:bg-white text-slate-700 transition"
                  >
                    View Details & Tracker
                  </Link>

                  {app.status === 'PAYMENT_PENDING' && (
                    <Link
                      to={`/applicant/payments/${app.id}`}
                      className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white shadow-sm transition"
                    >
                      Pay Fee
                    </Link>
                  )}

                  {app.status === 'APPROVED' && (
                    <a
                      href={`/api/permits/${app.id}/download`}
                      target="_blank"
                      rel="noreferrer"
                      className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm transition flex items-center gap-1"
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
