import React from 'react';
import {
  CheckCircle2,
  Clock,
  AlertCircle,
  FileText,
  Search,
  Users,
  RotateCcw,
  Calculator,
  CreditCard,
  Award
} from 'lucide-react';

export default function WorkflowTracker({ status, reviews = [], calculatedFee, permitNumber }) {
  // Map backend status to 7 canonical stages
  // Stages:
  // 1: Application Submitted
  // 2: Document Verification
  // 3: Department Review
  // 4: Revision (if needed)
  // 5: Fee Calculation
  // 6: Payment
  // 7: Permit Generation

  const getStageIndex = (st) => {
    switch (st) {
      case 'DRAFT': return 0;
      case 'SUBMITTED': return 1;
      case 'UNDER_DOCUMENT_VERIFICATION': return 2;
      case 'UNDER_DEPARTMENT_REVIEW': return 3;
      case 'REVISION_REQUESTED': return 4;
      case 'FEE_PENDING': return 5;
      case 'PAYMENT_PENDING': return 6;
      case 'APPROVED': return 7;
      case 'REJECTED': return -1;
      default: return 1;
    }
  };

  const currentStageIndex = getStageIndex(status);

  const stages = [
    { id: 1, name: 'Submission', icon: FileText, desc: 'Application filed online' },
    { id: 2, name: 'Verification', icon: Search, desc: 'Checklist & Land search' },
    { id: 3, name: 'Dept Review', icon: Users, desc: '4 Parallel clearances' },
    { id: 4, name: 'Revisions', icon: RotateCcw, desc: 'Architectural amendments' },
    { id: 5, name: 'Fee Calc', icon: Calculator, desc: 'Statutory fees evaluated' },
    { id: 6, name: 'Payment', icon: CreditCard, desc: 'Online fee transaction' },
    { id: 7, name: 'Permit Issued', icon: Award, desc: 'Official sanction certificate' },
  ];

  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-100 gap-2">
        <div>
          <h3 className="text-base font-bold text-slate-800 flex items-center gap-2">
            <span>Camunda 8 BPMN Process Tracker</span>
            <span className="text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded font-mono">
              BuildingPermitApprovalProcess
            </span>
          </h3>
          <p className="text-xs text-slate-500 mt-0.5">Real-time workflow execution state derived from Zeebe process instance.</p>
        </div>

        <div>
          {status === 'APPROVED' && (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">
              <CheckCircle2 className="w-3.5 h-3.5" /> Permit Sanctioned
            </span>
          )}
          {status === 'REJECTED' && (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-rose-100 text-rose-800">
              <AlertCircle className="w-3.5 h-3.5" /> Application Rejected
            </span>
          )}
          {status === 'UNDER_DEPARTMENT_REVIEW' && (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-amber-100 text-amber-800">
              <Clock className="w-3.5 h-3.5 animate-spin" /> Parallel Review (4 Depts)
            </span>
          )}
          {status === 'REVISION_REQUESTED' && (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-indigo-100 text-indigo-800">
              <RotateCcw className="w-3.5 h-3.5" /> Revision Required
            </span>
          )}
          {status === 'PAYMENT_PENDING' && (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-blue-100 text-blue-800">
              <CreditCard className="w-3.5 h-3.5" /> Fee Payment Due
            </span>
          )}
        </div>
      </div>

      {/* Step Indicators */}
      <div className="mt-8 relative">
        <div className="overflow-x-auto pb-4">
          <div className="min-w-[650px] flex items-center justify-between relative">
            {/* Connecting Progress Line */}
            <div className="absolute top-5 left-6 right-6 h-1 bg-slate-200 -z-0">
              <div
                className="h-full bg-civicTeal-600 transition-all duration-500"
                style={{
                  width: `${Math.max(0, Math.min(100, ((currentStageIndex - 1) / (stages.length - 1)) * 100))}%`
                }}
              ></div>
            </div>

            {stages.map((stage) => {
              const Icon = stage.icon;
              const isCompleted = currentStageIndex > stage.id || currentStageIndex === 7;
              const isCurrent = currentStageIndex === stage.id && status !== 'REJECTED';
              const isRejected = status === 'REJECTED' && stage.id === 3;

              return (
                <div key={stage.id} className="flex flex-col items-center relative z-10 text-center">
                  <div
                    className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm transition-all duration-300 shadow-sm ${
                      isCompleted
                        ? 'bg-civicTeal-600 text-white ring-4 ring-civicTeal-100'
                        : isCurrent
                        ? 'bg-municipal-800 text-white ring-4 ring-amber-200 scale-110'
                        : isRejected
                        ? 'bg-rose-600 text-white ring-4 ring-rose-100'
                        : 'bg-white border-2 border-slate-300 text-slate-400'
                    }`}
                  >
                    {isCompleted ? <CheckCircle2 className="w-5 h-5" /> : <Icon className="w-5 h-5" />}
                  </div>

                  <span
                    className={`mt-2.5 text-xs font-semibold block ${
                      isCurrent ? 'text-municipal-800' : isCompleted ? 'text-slate-800' : 'text-slate-400'
                    }`}
                  >
                    {stage.name}
                  </span>
                  <span className="text-[10px] text-slate-400 max-w-[85px] leading-tight mt-0.5">
                    {stage.desc}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Parallel Department Review Sub-Grid when in Review or Completed */}
      {reviews && reviews.length > 0 && (
        <div className="mt-6 pt-5 border-t border-slate-100">
          <span className="text-xs font-bold uppercase tracking-wider text-slate-500 block mb-3">
            Multi-Instance Parallel Clearance Matrix
          </span>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {reviews.map((rev) => {
              const isAppr = rev.status === 'APPROVED';
              const isRej = rev.status === 'REJECTED';
              const isRev = rev.status === 'REVISION_REQUESTED';

              return (
                <div
                  key={rev.id || rev.departmentCode}
                  className={`p-3 rounded-xl border text-xs flex flex-col justify-between ${
                    isAppr
                      ? 'bg-emerald-50/70 border-emerald-200 text-emerald-900'
                      : isRej
                      ? 'bg-rose-50/70 border-rose-200 text-rose-900'
                      : isRev
                      ? 'bg-indigo-50/70 border-indigo-200 text-indigo-900'
                      : 'bg-slate-50 border-slate-200 text-slate-700'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="font-bold text-[11px]">{rev.departmentCode}</span>
                    <span
                      className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                        isAppr
                          ? 'bg-emerald-200/80 text-emerald-800'
                          : isRej
                          ? 'bg-rose-200/80 text-rose-800'
                          : isRev
                          ? 'bg-indigo-200/80 text-indigo-800'
                          : 'bg-slate-200 text-slate-600'
                      }`}
                    >
                      {rev.status}
                    </span>
                  </div>
                  <p className="text-[10px] text-slate-500 italic line-clamp-2">
                    {rev.remarks || 'Pending officer evaluation'}
                  </p>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
