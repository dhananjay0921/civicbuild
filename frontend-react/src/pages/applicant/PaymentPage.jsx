import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { applicationApi, paymentApi } from '../../services/api';
import {
  CreditCard,
  CheckCircle,
  AlertCircle,
  Building,
  RefreshCw,
  Award,
  ArrowRight,
  ShieldCheck,
  Receipt
} from 'lucide-react';

export default function PaymentPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [applications, setApplications] = useState([]);
  const [selectedAppId, setSelectedAppId] = useState(id || '');
  const [payment, setPayment] = useState(null);
  const [loading, setLoading] = useState(false);
  const [paying, setPaying] = useState(false);
  const [method, setMethod] = useState('UPI');
  const [mockSuccess, setMockSuccess] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadPaymentPendingApps();
  }, []);

  useEffect(() => {
    if (selectedAppId) {
      loadPaymentDetails(selectedAppId);
    }
  }, [selectedAppId]);

  const loadPaymentPendingApps = async () => {
    try {
      const res = await applicationApi.getAll();
      if (res.success && res.data) {
        setApplications(res.data);
        if (!selectedAppId && res.data.length > 0) {
          const dueApp = res.data.find(a => a.status === 'PAYMENT_PENDING' || a.status === 'FEE_PENDING');
          if (dueApp) setSelectedAppId(dueApp.id);
          else setSelectedAppId(res.data[0].id);
        }
      }
    } catch (err) {
      console.error(err);
    }
  };

  const loadPaymentDetails = async (appId) => {
    try {
      setLoading(true);
      setError('');
      setSuccess('');
      const res = await paymentApi.get(appId);
      if (res.success && res.data) {
        setPayment(res.data);
      }
    } catch (err) {
      setError(err.message || 'Failed to fetch payment invoice');
    } finally {
      setLoading(false);
    }
  };

  const handleExecutePayment = async () => {
    if (!payment) return;
    setError('');
    setSuccess('');
    setPaying(true);

    try {
      const res = await paymentApi.confirm(selectedAppId, payment.transactionRef, mockSuccess);
      if (res.success && res.data) {
        setSuccess('Payment successfully confirmed by mock gateway! Building permit generated.');
        setPayment(res.data);
        setTimeout(() => {
          navigate(`/applicant/applications/${selectedAppId}`);
        }, 2000);
      }
    } catch (err) {
      setError(err.message || 'Payment transaction failed');
      // Refresh state to show failed status
      loadPaymentDetails(selectedAppId);
    } finally {
      setPaying(false);
    }
  };

  const handleRetryPayment = async () => {
    setError('');
    setSuccess('');
    try {
      setLoading(true);
      const res = await paymentApi.initiate(selectedAppId, method);
      if (res.success && res.data) {
        setPayment(res.data);
      }
    } catch (err) {
      setError(err.message || 'Failed to initiate new payment session');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="pb-4 border-b border-slate-200">
        <h1 className="text-2xl font-extrabold text-slate-900">Statutory Fee Settlement</h1>
        <p className="text-xs text-slate-500">
          Electronic Municipal Fee Payment Gateway (Statutory NBC 2016 Permitting Schedule)
        </p>
      </div>

      {/* Application Selector */}
      <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-3">
        <label className="block text-xs font-semibold text-slate-700">Select Permit Application</label>
        <select
          value={selectedAppId}
          onChange={(e) => setSelectedAppId(e.target.value)}
          className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none bg-white font-mono"
        >
          {applications.map((app) => (
            <option key={app.id} value={app.id}>
              {app.applicationNumber} - {app.buildingType} ({app.status})
            </option>
          ))}
        </select>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border border-red-200 text-red-700 text-xs rounded-2xl flex items-center gap-2.5">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs rounded-2xl flex items-center gap-2.5">
          <CheckCircle className="w-5 h-5 shrink-0" />
          <span>{success}</span>
        </div>
      )}

      {loading ? (
        <div className="p-12 text-center text-xs text-slate-500">Loading statutory fee invoice...</div>
      ) : payment ? (
        <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 shadow-sm space-y-6">
          <div className="flex items-center justify-between pb-4 border-b border-slate-100">
            <div>
              <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Invoice Reference</span>
              <h3 className="font-mono font-bold text-sm text-municipal-800">{payment.transactionRef}</h3>
            </div>
            <span
              className={`text-xs font-bold px-3 py-1 rounded-full ${
                payment.status === 'SUCCESS'
                  ? 'bg-emerald-100 text-emerald-800'
                  : payment.status === 'FAILED'
                  ? 'bg-red-100 text-red-800'
                  : 'bg-amber-100 text-amber-800'
              }`}
            >
              {payment.status}
            </span>
          </div>

          {/* Amount Box */}
          <div className="p-6 rounded-2xl bg-gradient-to-r from-municipal-900 to-municipal-800 text-white flex items-center justify-between shadow-inner">
            <div className="space-y-1">
              <span className="text-xs uppercase tracking-wider text-slate-300 font-medium">Total Sanction Fee Payable</span>
              <div className="text-3xl font-extrabold text-civicTeal-400">
                INR {Number(payment.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
              </div>
              <p className="text-[10px] text-slate-400">Calculated via MunicipalFeeCalculationProcess (FEEL formula)</p>
            </div>
            <Receipt className="w-12 h-12 text-slate-600" />
          </div>

          {/* Payment Method Selector */}
          {payment.status !== 'SUCCESS' && (
            <div className="space-y-3">
              <label className="block text-xs font-semibold text-slate-700">Choose Payment Channel</label>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                {['UPI', 'CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING'].map((m) => (
                  <button
                    key={m}
                    type="button"
                    onClick={() => setMethod(m)}
                    className={`p-3 rounded-xl border text-xs font-semibold transition ${
                      method === m
                        ? 'bg-municipal-800 text-white border-municipal-800 shadow-sm'
                        : 'bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100'
                    }`}
                  >
                    {m.replace('_', ' ')}
                  </button>
                ))}
              </div>

              {/* Mock Gateway Toggle (For Testing Scenarios 10 & 11) */}
              <div className="pt-3 p-3.5 rounded-xl bg-slate-50 border border-slate-200 space-y-2">
                <span className="text-[11px] font-bold text-slate-700 block">
                  Mock Gateway Simulation Parameters:
                </span>
                <div className="flex items-center gap-4 text-xs">
                  <label className="flex items-center gap-2 cursor-pointer font-medium text-slate-700">
                    <input
                      type="radio"
                      name="mockSuccess"
                      checked={mockSuccess === true}
                      onChange={() => setMockSuccess(true)}
                      className="text-emerald-600 focus:ring-emerald-500"
                    />
                    <span>Simulate Successful Payment (Scenario 11)</span>
                  </label>
                  <label className="flex items-center gap-2 cursor-pointer font-medium text-slate-700">
                    <input
                      type="radio"
                      name="mockSuccess"
                      checked={mockSuccess === false}
                      onChange={() => setMockSuccess(false)}
                      className="text-red-600 focus:ring-red-500"
                    />
                    <span>Simulate Failed Payment / Retry (Scenario 10)</span>
                  </label>
                </div>
              </div>
            </div>
          )}

          {/* Action Buttons */}
          <div className="pt-2 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            {payment.status === 'SUCCESS' ? (
              <div className="flex items-center gap-3 w-full justify-between">
                <span className="text-xs text-emerald-700 font-semibold flex items-center gap-1.5">
                  <CheckCircle className="w-4 h-4" /> Paid on {new Date(payment.paymentDate).toLocaleString()}
                </span>
                <Link
                  to={`/applicant/applications/${selectedAppId}`}
                  className="bg-municipal-800 text-white font-semibold px-4 py-2 rounded-xl text-xs flex items-center gap-2 hover:bg-municipal-900 transition"
                >
                  <span>View Sanctioned Permit</span>
                  <ArrowRight className="w-4 h-4" />
                </Link>
              </div>
            ) : payment.status === 'FAILED' ? (
              <button
                type="button"
                onClick={handleRetryPayment}
                disabled={loading}
                className="w-full sm:w-auto bg-amber-600 hover:bg-amber-700 text-white font-semibold px-6 py-2.5 rounded-xl text-xs flex items-center justify-center gap-2 transition"
              >
                <RefreshCw className="w-4 h-4" />
                <span>Retry Payment Session</span>
              </button>
            ) : (
              <button
                type="button"
                onClick={handleExecutePayment}
                disabled={paying}
                className="w-full bg-civicTeal-600 hover:bg-civicTeal-700 text-white font-semibold py-3 rounded-xl text-xs flex items-center justify-center gap-2 shadow-md transition disabled:opacity-50"
              >
                {paying ? (
                  <>
                    <span className="animate-spin">&bull;</span>
                    <span>Communicating with Mock Payment Gateway...</span>
                  </>
                ) : (
                  <>
                    <CreditCard className="w-4 h-4" />
                    <span>Authorize & Confirm Payment of INR {Number(payment.amount).toLocaleString()}</span>
                  </>
                )}
              </button>
            )}
          </div>
        </div>
      ) : (
        <div className="p-8 text-center text-xs text-slate-500">No payment record found.</div>
      )}
    </div>
  );
}
