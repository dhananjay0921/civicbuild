import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { applicationApi } from '../../services/api';
import WorkflowTracker from '../../components/workflow/WorkflowTracker';
import {
  FileText,
  Calendar,
  Building,
  MapPin,
  CreditCard,
  Award,
  AlertCircle,
  Clock,
  CheckCircle2,
  RotateCcw,
  Download,
  Shield,
  Layers,
  ArrowLeft
} from 'lucide-react';

export default function ApplicationDetailsPage() {
  const { id } = useParams();
  const [app, setApp] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadDetails();
  }, [id]);

  const loadDetails = async () => {
    try {
      setLoading(true);
      const res = await applicationApi.getById(id);
      if (res.success && res.data) {
        setApp(res.data);
      }
    } catch (err) {
      setError(err.message || 'Failed to load application details');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="p-12 text-center text-sm text-slate-500">Loading permit details...</div>;
  }

  if (error || !app) {
    return (
      <div className="p-8 bg-red-50 border border-red-200 text-red-700 rounded-2xl text-xs flex items-center gap-2">
        <AlertCircle className="w-5 h-5 shrink-0" />
        <span>{error || 'Application not found'}</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-200 gap-4">
        <div className="space-y-1">
          <Link to="/applicant/my-applications" className="text-xs font-semibold text-civicTeal-600 hover:underline flex items-center gap-1 mb-1">
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to My Applications</span>
          </Link>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-extrabold text-slate-900 font-mono">{app.applicationNumber}</h1>
            <span className="text-xs bg-municipal-100 text-municipal-800 font-semibold px-2.5 py-0.5 rounded-full">
              {app.buildingType}
            </span>
          </div>
          <p className="text-xs text-slate-500">
            Process Instance Key: <span className="font-mono text-slate-700">{app.processInstanceKey || 'Pending'}</span>
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2 shrink-0">
          {app.status === 'PAYMENT_PENDING' && (
            <Link
              to={`/applicant/payments/${app.id}`}
              className="bg-blue-600 hover:bg-blue-700 text-white font-semibold px-4 py-2 rounded-xl text-xs flex items-center gap-2 shadow-sm transition"
            >
              <CreditCard className="w-4 h-4" />
              <span>Pay Statutory Fee (INR {app.calculatedFee})</span>
            </Link>
          )}

          {app.status === 'REVISION_REQUESTED' && (
            <Link
              to={`/applicant/revisions`}
              className="bg-indigo-600 hover:bg-indigo-700 text-white font-semibold px-4 py-2 rounded-xl text-xs flex items-center gap-2 shadow-sm transition"
            >
              <RotateCcw className="w-4 h-4" />
              <span>Respond to Revision Notice</span>
            </Link>
          )}

          {app.status === 'APPROVED' && (
            <a
              href={`/api/permits/${app.id}/download`}
              target="_blank"
              rel="noreferrer"
              className="bg-emerald-600 hover:bg-emerald-700 text-white font-semibold px-4 py-2 rounded-xl text-xs flex items-center gap-2 shadow-md transition"
            >
              <Award className="w-4 h-4" />
              <span>Download Official Permit (PDF)</span>
            </a>
          )}
        </div>
      </div>

      {/* Live Camunda 8 Workflow Tracker */}
      <WorkflowTracker
        status={app.status}
        reviews={app.departmentReviews}
        calculatedFee={app.calculatedFee}
        permitNumber={app.permit?.permitNumber}
      />

      {/* Grid: Site & Structural Details */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Site Details Card */}
        <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
          <h3 className="text-sm font-bold text-slate-800 flex items-center gap-2 pb-2 border-b border-slate-100">
            <MapPin className="w-4 h-4 text-civicTeal-600" />
            <span>Property & Cadastral Details</span>
          </h3>

          <div className="grid grid-cols-2 gap-3 text-xs">
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Address</span>
              <span className="font-medium text-slate-800">{app.property?.address}</span>
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Cadastral Survey</span>
              <span className="font-medium text-slate-800">{app.property?.surveyNumber}</span>
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Municipal Ward</span>
              <span className="font-medium text-slate-800">{app.property?.wardNumber}</span>
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Zoning Sector</span>
              <span className="font-medium text-slate-800">{app.property?.zone}</span>
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Plot Area</span>
              <span className="font-medium text-slate-800">{app.property?.plotArea} sq.ft</span>
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Title Ownership</span>
              <span className="font-medium text-slate-800">{app.property?.ownershipType}</span>
            </div>
          </div>
        </div>

        {/* Building Specifications Card */}
        <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
          <h3 className="text-sm font-bold text-slate-800 flex items-center gap-2 pb-2 border-b border-slate-100">
            <Building className="w-4 h-4 text-civicTeal-600" />
            <span>Building Specifications (NBC 2016)</span>
          </h3>

          <div className="grid grid-cols-2 gap-3 text-xs">
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Building Classification</span>
              <span className="font-medium text-slate-800">{app.buildingType}</span>
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Storeys & Height</span>
              <span className="font-medium text-slate-800">{app.numberOfFloors} Floors ({app.buildingHeight}m)</span>
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Total Built-Up Area</span>
              <span className="font-medium text-slate-800">{app.builtUpArea} sq.ft</span>
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Estimated Cost</span>
              <span className="font-medium text-slate-800">INR {Number(app.estimatedCost).toLocaleString()}</span>
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Parking Provision</span>
              <span className="font-medium text-slate-800">{app.parkingAvailable ? 'Yes (On-site)' : 'No'}</span>
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Statutory Fee</span>
              <span className="font-bold text-civicTeal-600">INR {app.calculatedFee ? Number(app.calculatedFee).toLocaleString() : 'Pending Calculation'}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Documents List */}
      <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
        <h3 className="text-sm font-bold text-slate-800 flex items-center gap-2 pb-2 border-b border-slate-100">
          <FileText className="w-4 h-4 text-civicTeal-600" />
          <span>Uploaded Statutory Documents ({app.documents?.length || 0})</span>
        </h3>

        {app.documents && app.documents.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {app.documents.map((doc) => (
              <div key={doc.id} className="p-3 rounded-xl border border-slate-200 bg-slate-50 flex items-center justify-between text-xs">
                <div className="space-y-0.5">
                  <span className="font-semibold text-slate-800 block truncate max-w-[180px]">{doc.fileName}</span>
                  <span className="text-[10px] text-slate-400 uppercase font-mono">{doc.documentType}</span>
                </div>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-100 text-emerald-800">
                  {doc.verificationStatus}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-xs text-slate-500">No documents attached.</p>
        )}
      </div>

      {/* Workflow History Timeline */}
      <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
        <h3 className="text-sm font-bold text-slate-800 flex items-center gap-2 pb-2 border-b border-slate-100">
          <Clock className="w-4 h-4 text-civicTeal-600" />
          <span>Workflow State Audit Timeline</span>
        </h3>

        {app.workflowHistories && app.workflowHistories.length > 0 ? (
          <div className="space-y-3">
            {app.workflowHistories.map((hist, idx) => (
              <div key={idx} className="flex items-start gap-3 text-xs border-l-2 border-civicTeal-500 pl-4 py-1">
                <div className="w-2 h-2 rounded-full bg-civicTeal-500 -ml-[21px] mt-1"></div>
                <div className="space-y-0.5">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-slate-800">{hist.action}</span>
                    <span className="text-[10px] text-slate-400">&bull; {new Date(hist.timestamp).toLocaleString()}</span>
                  </div>
                  <p className="text-slate-600">{hist.comments}</p>
                  <span className="text-[10px] text-slate-400 font-mono">Actor: {hist.actor}</span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-xs text-slate-500">No workflow history available.</p>
        )}
      </div>
    </div>
  );
}
