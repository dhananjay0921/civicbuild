import React, { useState, useEffect } from 'react';
import { applicationApi, revisionApi } from '../../services/api';
import {
  RotateCcw,
  Upload,
  CheckCircle,
  AlertCircle,
  Building,
  ArrowRight,
  FileText
} from 'lucide-react';

export default function RevisionsPage() {
  const [revisions, setRevisions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedRev, setSelectedRev] = useState(null);
  const [remarks, setRemarks] = useState('');
  const [revisedFile, setRevisedFile] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [statusMsg, setStatusMsg] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadRevisions();
  }, []);

  const loadRevisions = async () => {
    try {
      setLoading(true);
      const res = await applicationApi.getAll();
      if (res.success && res.data) {
        // Collect all revision requests
        const revList = [];
        for (const app of res.data) {
          if (app.status === 'REVISION_REQUESTED') {
            const detailRes = await applicationApi.getById(app.id);
            if (detailRes.success && detailRes.data?.revisionRequests) {
              detailRes.data.revisionRequests.forEach(r => {
                revList.push({ ...r, application: detailRes.data });
              });
            }
          }
        }
        setRevisions(revList);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitRevision = async (e) => {
    e.preventDefault();
    if (!selectedRev) return;
    setError('');
    setStatusMsg('');
    setSubmitting(true);

    try {
      // 1. Upload corrected blueprint if selected
      if (revisedFile) {
        const form = new FormData();
        form.append('file', revisedFile);
        form.append('documentType', 'BUILDING_BLUEPRINT');
        await applicationApi.uploadDocument(selectedRev.application.id, form);
      }

      // 2. Submit revision remarks (triggers Camunda sub-process re-evaluation)
      await revisionApi.submit(selectedRev.id, remarks || 'Corrected blueprints attached addressing officer feedback.');

      setStatusMsg('Revision successfully submitted! Department review has resumed.');
      setSelectedRev(null);
      setRemarks('');
      setRevisedFile(null);
      await loadRevisions();
    } catch (err) {
      setError(err.message || 'Failed to submit revision');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="pb-4 border-b border-slate-200">
        <h1 className="text-2xl font-extrabold text-slate-900">Department Revision Requests</h1>
        <p className="text-xs text-slate-500">Address officer architectural feedback and submit amended plans</p>
      </div>

      {statusMsg && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs rounded-xl flex items-center gap-2">
          <CheckCircle className="w-4 h-4 shrink-0" />
          <span>{statusMsg}</span>
        </div>
      )}

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div className="p-12 text-center text-xs text-slate-500">Loading pending revisions...</div>
      ) : revisions.length === 0 ? (
        <div className="bg-white border border-slate-200 rounded-2xl p-12 text-center space-y-3 shadow-sm">
          <CheckCircle className="w-10 h-10 text-emerald-500 mx-auto" />
          <h3 className="text-sm font-bold text-slate-800">No Pending Revisions</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            None of your permit applications currently require architectural revisions. All reviews are progressing normally.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Revisions List */}
          <div className="space-y-4">
            <h3 className="text-sm font-bold text-slate-700">Active Officer Revision Notices</h3>
            {revisions.map((rev) => (
              <div
                key={rev.id}
                onClick={() => setSelectedRev(rev)}
                className={`p-5 rounded-2xl border cursor-pointer transition shadow-sm ${
                  selectedRev?.id === rev.id
                    ? 'bg-indigo-50/70 border-indigo-400 ring-2 ring-indigo-200'
                    : 'bg-white border-slate-200 hover:border-slate-300'
                }`}
              >
                <div className="flex items-center justify-between">
                  <span className="font-mono font-bold text-sm text-municipal-800">
                    {rev.application.applicationNumber}
                  </span>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-indigo-100 text-indigo-800">
                    Cycle #{rev.cycleNumber} &bull; {rev.departmentCode}
                  </span>
                </div>

                <div className="mt-3 p-3 rounded-xl bg-slate-50 border border-slate-200 text-xs space-y-1">
                  <span className="font-bold text-slate-700 block text-[10px] uppercase">Officer Remarks:</span>
                  <p className="text-slate-600 italic">"{rev.officerRemarks}"</p>
                </div>

                <div className="mt-3 flex items-center justify-between text-[11px] text-slate-400">
                  <span>Requested: {new Date(rev.requestedAt).toLocaleDateString()}</span>
                  <span className="font-semibold text-indigo-600 flex items-center gap-1">
                    <span>Respond</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </span>
                </div>
              </div>
            ))}
          </div>

          {/* Response Form */}
          <div>
            {selectedRev ? (
              <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
                <div className="border-b border-slate-100 pb-3">
                  <h3 className="text-base font-bold text-slate-800">
                    Respond to {selectedRev.departmentCode} Department
                  </h3>
                  <p className="text-xs text-slate-500">
                    Application: <span className="font-mono text-slate-700">{selectedRev.application.applicationNumber}</span> &bull; Cycle {selectedRev.cycleNumber} of 3
                  </p>
                </div>

                <form onSubmit={handleSubmitRevision} className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">
                      Upload Corrected Architectural Blueprint (PDF)
                    </label>
                    <div className="p-4 border-2 border-dashed border-slate-300 rounded-xl text-center bg-slate-50 hover:bg-slate-100/60 transition cursor-pointer">
                      <input
                        type="file"
                        accept=".pdf"
                        onChange={(e) => setRevisedFile(e.target.files[0])}
                        className="hidden"
                        id="revised-file-input"
                      />
                      <label htmlFor="revised-file-input" className="cursor-pointer block">
                        <Upload className="w-6 h-6 text-slate-400 mx-auto mb-1" />
                        <span className="text-xs font-semibold text-slate-700 block">
                          {revisedFile ? revisedFile.name : 'Click to select corrected blueprint PDF'}
                        </span>
                        <span className="text-[10px] text-slate-400">Max 20MB &bull; Standard Architectural Drawing</span>
                      </label>
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">
                      Response Remarks for Department Officer
                    </label>
                    <textarea
                      rows={3}
                      required
                      value={remarks}
                      onChange={(e) => setRemarks(e.target.value)}
                      className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
                      placeholder="Explain how the architectural blueprints have been modified to satisfy statutory objections..."
                    />
                  </div>

                  <button
                    type="submit"
                    disabled={submitting}
                    className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-2.5 rounded-xl text-xs flex items-center justify-center gap-2 shadow-sm transition disabled:opacity-50"
                  >
                    {submitting ? <span className="animate-spin">&bull;</span> : <RotateCcw className="w-4 h-4" />}
                    <span>Submit Corrected Blueprint</span>
                  </button>
                </form>
              </div>
            ) : (
              <div className="bg-slate-50 border border-dashed border-slate-200 rounded-2xl p-12 text-center text-xs text-slate-400">
                Select a revision request from the left list to upload corrected blueprints.
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
