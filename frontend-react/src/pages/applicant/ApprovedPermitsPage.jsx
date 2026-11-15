import React, { useState, useEffect } from 'react';
import { permitApi } from '../../services/api';
import { Award, Download, Calendar, ShieldCheck, CheckCircle2, FileText } from 'lucide-react';

export default function ApprovedPermitsPage() {
  const [permits, setPermits] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadPermits();
  }, []);

  const loadPermits = async () => {
    try {
      setLoading(true);
      const res = await permitApi.getAll();
      if (res.success && res.data) {
        setPermits(res.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="pb-4 border-b border-slate-200">
        <h1 className="text-2xl font-extrabold text-slate-900">Approved Building Permits</h1>
        <p className="text-xs text-slate-500">
          Official statutory building permits electronically sanctioned through the Camunda 8 workflow engine
        </p>
      </div>

      {loading ? (
        <div className="p-12 text-center text-xs text-slate-500">Loading permit archive...</div>
      ) : permits.length === 0 ? (
        <div className="bg-white border border-slate-200 rounded-2xl p-12 text-center space-y-3 shadow-sm">
          <Award className="w-10 h-10 text-slate-300 mx-auto" />
          <h3 className="text-sm font-bold text-slate-800">No Approved Permits Yet</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            Once your applications receive all 4 departmental approvals and statutory fee payment is confirmed, official PDF certificates will appear here.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {permits.map((permit) => (
            <div
              key={permit.id}
              className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm hover:shadow-md transition space-y-4 flex flex-col justify-between"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-mono font-bold text-civicTeal-600 bg-civicTeal-50 px-2 py-0.5 rounded">
                    {permit.permitNumber}
                  </span>
                  <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200">
                    <CheckCircle2 className="w-3 h-3" /> Legally Valid
                  </span>
                </div>

                <div>
                  <h3 className="text-base font-bold text-slate-800">
                    Statutory Building Construction Sanction
                  </h3>
                  <p className="text-xs text-slate-500 mt-0.5">
                    Authorized by: <strong className="text-slate-700">{permit.issuedBy}</strong>
                  </p>
                </div>

                <div className="p-3 rounded-xl bg-slate-50 border border-slate-100 text-xs space-y-1.5">
                  <div className="flex items-center justify-between text-slate-600">
                    <span>Sanction Date:</span>
                    <strong className="text-slate-800">{new Date(permit.issueDate).toLocaleDateString()}</strong>
                  </div>
                  <div className="flex items-center justify-between text-slate-600">
                    <span>Statutory Expiry:</span>
                    <strong className="text-slate-800">{new Date(permit.expiryDate).toLocaleDateString()} (3 Years)</strong>
                  </div>
                </div>

                <div className="text-[10px] text-slate-400 font-mono truncate">
                  QR Payload: {permit.qrCodeData}
                </div>
              </div>

              <div className="pt-2 border-t border-slate-100">
                <a
                  href={`/api/permits/${permit.id}/download`}
                  target="_blank"
                  rel="noreferrer"
                  className="w-full bg-municipal-800 hover:bg-municipal-900 text-white font-semibold py-2.5 rounded-xl text-xs flex items-center justify-center gap-2 shadow-sm transition"
                >
                  <Download className="w-4 h-4" />
                  <span>Download Sanction Certificate (PDF)</span>
                </a>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
