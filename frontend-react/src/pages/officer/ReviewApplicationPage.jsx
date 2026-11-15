import React, { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate, Link } from 'react-router-dom';
import { applicationApi, reviewApi } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import {
  FileText,
  Building,
  MapPin,
  CheckCircle2,
  AlertCircle,
  RotateCcw,
  Clock,
  ArrowLeft,
  Eye,
  ShieldCheck,
  Check,
  X
} from 'lucide-react';

export default function ReviewApplicationPage() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const reviewIdParam = searchParams.get('reviewId');
  const navigate = useNavigate();
  const { user } = useAuth();

  const [app, setApp] = useState(null);
  const [currentReview, setCurrentReview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [decision, setDecision] = useState('APPROVED');
  const [remarks, setRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadReviewData();
  }, [id]);

  const loadReviewData = async () => {
    try {
      setLoading(true);
      const res = await applicationApi.getById(id);
      if (res.success && res.data) {
        setApp(res.data);

        // Find relevant department review for this officer
        const reviews = res.data.departmentReviews || [];
        let matching = null;
        if (reviewIdParam) {
          matching = reviews.find(r => String(r.id) === reviewIdParam);
        }
        if (!matching && user?.departmentCode) {
          matching = reviews.find(r => r.departmentCode === user.departmentCode);
        }
        if (!matching && reviews.length > 0) {
          matching = reviews[0];
        }
        setCurrentReview(matching);
      }
    } catch (err) {
      setError(err.message || 'Failed to load application');
    } finally {
      setLoading(false);
    }
  };

  const handleDecisionSubmit = async (e) => {
    e.preventDefault();
    if (!currentReview) return;
    setError('');
    setSuccess('');
    setSubmitting(true);

    try {
      if (decision === 'APPROVED') {
        await reviewApi.approve(currentReview.id, remarks || 'Clearance granted compliant with NBC 2016 norms.');
      } else if (decision === 'REJECTED') {
        if (!remarks.trim()) throw new Error('Mandatory statutory rejection reasons must be specified.');
        await reviewApi.reject(currentReview.id, remarks);
      } else if (decision === 'REVISION_REQUESTED') {
        if (!remarks.trim()) throw new Error('Architectural revision requirements must be detailed.');
        await reviewApi.requestRevision(currentReview.id, remarks);
      }

      setSuccess(`Decision ${decision} recorded successfully!`);
      setTimeout(() => {
        navigate('/officer/inbox');
      }, 1500);
    } catch (err) {
      setError(err.message || 'Failed to submit decision');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="p-12 text-center text-xs text-slate-500">Loading application dossier...</div>;
  if (!app) return <div className="p-8 text-center text-xs text-red-600">Application not found.</div>;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-200 gap-4">
        <div>
          <Link to="/officer/inbox" className="text-xs font-semibold text-civicTeal-600 hover:underline flex items-center gap-1 mb-1">
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to Task Inbox</span>
          </Link>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-extrabold text-slate-900 font-mono">{app.applicationNumber}</h1>
            <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-amber-100 text-amber-800">
              {currentReview ? `${currentReview.departmentCode} Statutory Review` : 'Department Review'}
            </span>
          </div>
        </div>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs rounded-xl flex items-center gap-2">
          <Check className="w-4 h-4 shrink-0" />
          <span>{success}</span>
        </div>
      )}

      {/* Grid: Application Info & Document Inspection */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Left 2 Cols: Details & Documents */}
        <div className="lg:col-span-2 space-y-6">
          {/* Site & Specs Summary */}
          <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
            <h3 className="text-sm font-bold text-slate-800 pb-2 border-b border-slate-100 flex items-center gap-2">
              <Building className="w-4 h-4 text-civicTeal-600" />
              <span>Site & Architectural Specifications</span>
            </h3>

            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-xs">
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Applicant</span>
                <span className="font-semibold text-slate-800">{app.applicant?.fullName}</span>
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Category</span>
                <span className="font-semibold text-slate-800">{app.buildingType}</span>
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Built-Up Area</span>
                <span className="font-semibold text-slate-800">{app.builtUpArea} sq.ft</span>
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Floors & Height</span>
                <span className="font-semibold text-slate-800">{app.numberOfFloors} Floors ({app.buildingHeight}m)</span>
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Survey No</span>
                <span className="font-semibold text-slate-800">{app.property?.surveyNumber}</span>
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Ward / Zone</span>
                <span className="font-semibold text-slate-800">{app.property?.wardNumber} ({app.property?.zone})</span>
              </div>
            </div>

            <div className="pt-2 text-xs">
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Purpose Description</span>
              <p className="text-slate-700 mt-0.5">{app.constructionPurpose}</p>
            </div>
          </div>

          {/* Document Inspection Viewer */}
          <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
            <h3 className="text-sm font-bold text-slate-800 pb-2 border-b border-slate-100 flex items-center gap-2">
              <FileText className="w-4 h-4 text-civicTeal-600" />
              <span>Statutory Document Dossier ({app.documents?.length || 0})</span>
            </h3>

            <div className="space-y-2.5">
              {app.documents && app.documents.map((doc) => (
                <div key={doc.id} className="p-3.5 rounded-xl border border-slate-200 bg-slate-50 flex items-center justify-between text-xs">
                  <div className="space-y-0.5">
                    <span className="font-bold text-slate-900 block">{doc.fileName}</span>
                    <span className="text-[10px] text-slate-400 uppercase font-mono">{doc.documentType}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-100 text-emerald-800">
                      {doc.verificationStatus}
                    </span>
                    <span className="text-[11px] font-semibold text-civicTeal-600 hover:underline cursor-pointer flex items-center gap-1">
                      <Eye className="w-3.5 h-3.5" /> Inspect
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Col: Officer Decision Form */}
        <div className="space-y-6">
          <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-5 sticky top-20">
            <div className="pb-3 border-b border-slate-100">
              <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block">
                Statutory Action
              </span>
              <h3 className="text-base font-bold text-slate-800">Department Decision Form</h3>
              <p className="text-xs text-slate-500 mt-0.5">
                Acting as: <strong>{currentReview?.departmentCode || 'OFFICER'}</strong>
              </p>
            </div>

            <form onSubmit={handleDecisionSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-2">Review Decision</label>
                <div className="space-y-2 text-xs">
                  <label className={`flex items-center gap-2.5 p-3 rounded-xl border cursor-pointer transition ${decision === 'APPROVED' ? 'bg-emerald-50 border-emerald-300 text-emerald-900 ring-2 ring-emerald-100' : 'bg-slate-50 border-slate-200'}`}>
                    <input
                      type="radio"
                      name="decision"
                      value="APPROVED"
                      checked={decision === 'APPROVED'}
                      onChange={() => setDecision('APPROVED')}
                      className="text-emerald-600"
                    />
                    <div className="font-bold flex items-center gap-1.5">
                      <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                      <span>APPROVE CLEARANCE</span>
                    </div>
                  </label>

                  <label className={`flex items-center gap-2.5 p-3 rounded-xl border cursor-pointer transition ${decision === 'REVISION_REQUESTED' ? 'bg-indigo-50 border-indigo-300 text-indigo-900 ring-2 ring-indigo-100' : 'bg-slate-50 border-slate-200'}`}>
                    <input
                      type="radio"
                      name="decision"
                      value="REVISION_REQUESTED"
                      checked={decision === 'REVISION_REQUESTED'}
                      onChange={() => setDecision('REVISION_REQUESTED')}
                      className="text-indigo-600"
                    />
                    <div className="font-bold flex items-center gap-1.5">
                      <RotateCcw className="w-4 h-4 text-indigo-600" />
                      <span>REQUEST REVISIONS</span>
                    </div>
                  </label>

                  <label className={`flex items-center gap-2.5 p-3 rounded-xl border cursor-pointer transition ${decision === 'REJECTED' ? 'bg-rose-50 border-rose-300 text-rose-900 ring-2 ring-rose-100' : 'bg-slate-50 border-slate-200'}`}>
                    <input
                      type="radio"
                      name="decision"
                      value="REJECTED"
                      checked={decision === 'REJECTED'}
                      onChange={() => setDecision('REJECTED')}
                      className="text-rose-600"
                    />
                    <div className="font-bold flex items-center gap-1.5">
                      <X className="w-4 h-4 text-rose-600" />
                      <span>PERMANENTLY REJECT</span>
                    </div>
                  </label>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Official Technical Remarks <span className="text-red-500">*</span>
                </label>
                <textarea
                  rows={4}
                  required
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
                  placeholder={
                    decision === 'APPROVED'
                      ? 'Confirm statutory compliance details...'
                      : decision === 'REVISION_REQUESTED'
                      ? 'Detail specific blueprint deficiencies requiring correction...'
                      : 'Specify irrevocable non-compliance reasons...'
                  }
                />
              </div>

              <button
                type="submit"
                disabled={submitting}
                className={`w-full py-2.5 rounded-xl text-xs font-bold text-white shadow-md transition disabled:opacity-50 ${
                  decision === 'APPROVED'
                    ? 'bg-emerald-600 hover:bg-emerald-700'
                    : decision === 'REVISION_REQUESTED'
                    ? 'bg-indigo-600 hover:bg-indigo-700'
                    : 'bg-rose-600 hover:bg-rose-700'
                }`}
              >
                {submitting ? 'Recording Decision...' : `Submit ${decision} Decision`}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
