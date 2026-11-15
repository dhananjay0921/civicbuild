import React from 'react';
import { Building2, Award, ShieldCheck, Cpu, Code2 } from 'lucide-react';

export default function AboutPage() {
  return (
    <div className="max-w-4xl mx-auto space-y-10 py-6">
      <div className="space-y-3">
        <span className="text-xs font-bold uppercase tracking-wider text-civicTeal-600 bg-civicTeal-50 px-2.5 py-1 rounded">
          Academic Capstone Project &bull; Civic Governance
        </span>
        <h1 className="text-3xl font-extrabold text-slate-900">About the CivicBuild Platform</h1>
        <p className="text-base text-slate-600 leading-relaxed">
          CivicBuild is an enterprise-grade full-stack workflow automation portal designed to solve persistent municipal permit approval bottlenecks through Camunda 8 Zeebe orchestration.
        </p>
      </div>

      <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 space-y-6 shadow-sm">
        <h2 className="text-xl font-bold text-slate-800 flex items-center gap-2">
          <ShieldCheck className="w-5 h-5 text-civicTeal-600" />
          <span>Statutory Governance Objectives</span>
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm text-slate-600">
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-100">
            <h4 className="font-bold text-slate-900 mb-1">Parallel Inter-Departmental Collaboration</h4>
            <p className="text-xs text-slate-500">
              Eliminates the traditional sequential delays where Fire, Water, Zoning, and Environmental departments review permits in serial queue.
            </p>
          </div>
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-100">
            <h4 className="font-bold text-slate-900 mb-1">Transparent Citizen Tracking</h4>
            <p className="text-xs text-slate-500">
              Applicants maintain real-time visibility into each stage, rejection reason, revision requirement, and statutory fee calculations.
            </p>
          </div>
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-100">
            <h4 className="font-bold text-slate-900 mb-1">Digital Sanction Certificates</h4>
            <p className="text-xs text-slate-500">
              Authorized permits are generated as verifiable PDF certificates with cryptographic metadata, unique permit numbers, and QR seal verification.
            </p>
          </div>
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-100">
            <h4 className="font-bold text-slate-900 mb-1">Auditability and SLA Enforcement</h4>
            <p className="text-xs text-slate-500">
              Every workflow state transition, review remark, document upload, and timeout escalation is permanently recorded in PostgreSQL audit logs.
            </p>
          </div>
        </div>
      </div>

      <div className="bg-municipal-900 text-white rounded-2xl p-6 sm:p-8 space-y-4 shadow-md">
        <h3 className="text-lg font-bold flex items-center gap-2">
          <Cpu className="w-5 h-5 text-civicTeal-400" />
          <span>BPMN 2.0 & Camunda 8 Engine Specification</span>
        </h3>
        <p className="text-xs text-slate-300 leading-relaxed">
          The core orchestration is driven by <code>BuildingPermitApprovalProcess</code> with 3 reusable child processes (<code>DocumentVerificationProcess</code>, <code>MunicipalFeeCalculationProcess</code>, <code>PermitNotificationProcess</code>) executed via BPMN Call Activities, parallel and sequential Multi-Instance activities, Error Boundary Events catching <code>MISSING_CRITICAL_DOCUMENT</code>, and non-interrupting Timer Boundary Events for 48h SLA escalations.
        </p>
      </div>
    </div>
  );
}
