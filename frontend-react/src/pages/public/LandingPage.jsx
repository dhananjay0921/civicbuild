import React from 'react';
import { Link } from 'react-router-dom';
import {
  Building2,
  ShieldCheck,
  CheckCircle,
  Zap,
  Users,
  FileCheck2,
  Clock,
  ArrowRight,
  ChevronRight
} from 'lucide-react';

export default function LandingPage() {
  const departments = [
    { name: 'Fire Safety Bureau', desc: 'Hydrant access, emergency egress & suppression standards', color: 'bg-red-50 text-red-700 border-red-200' },
    { name: 'Water & Sewerage', desc: 'Drainage infrastructure, connections & stormwater audits', color: 'bg-blue-50 text-blue-700 border-blue-200' },
    { name: 'Town Planning & Zoning', desc: 'FAR verification, structural height & land-use compliance', color: 'bg-amber-50 text-amber-700 border-amber-200' },
    { name: 'Environmental Protection', desc: 'Green clearances, rainwater retention & solar exposure', color: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
  ];

  return (
    <div className="space-y-16 py-4">
      {/* Hero Section */}
      <section className="bg-gradient-to-br from-municipal-900 via-municipal-800 to-slate-900 text-white rounded-3xl p-8 sm:p-14 shadow-xl relative overflow-hidden">
        <div className="relative z-10 max-w-2xl space-y-6">
          <div className="inline-flex items-center gap-2 bg-civicTeal-500/20 text-civicTeal-300 border border-civicTeal-500/30 px-3.5 py-1.5 rounded-full text-xs font-semibold">
            <Zap className="w-3.5 h-3.5 text-civicTeal-400" />
            <span>Camunda 8 Multi-Department Orchestration</span>
          </div>

          <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight leading-tight">
            Municipal Building Permits, <span className="text-civicTeal-400">Automated & Transparent.</span>
          </h1>

          <p className="text-slate-300 text-base sm:text-lg leading-relaxed">
            CivicBuild digitizes the complete municipal construction approval lifecycle. Submit architectural blueprints, track multi-departmental clearances in parallel, resolve revision requests, and receive digitally signed building permits.
          </p>

          <div className="flex flex-wrap items-center gap-4 pt-2">
            <Link
              to="/applicant/new-application"
              className="bg-civicTeal-600 hover:bg-civicTeal-700 text-white font-semibold px-6 py-3 rounded-xl shadow-md flex items-center gap-2 transition"
            >
              <span>Apply for Building Permit</span>
              <ArrowRight className="w-4 h-4" />
            </Link>

            <Link
              to="/login"
              className="bg-municipal-700 hover:bg-municipal-600 border border-slate-600 text-white font-medium px-5 py-3 rounded-xl transition"
            >
              Department Officer Login
            </Link>
          </div>
        </div>

        {/* Decorative Grid Pattern */}
        <div className="absolute right-0 top-0 bottom-0 w-1/3 opacity-10 bg-[radial-gradient(#38bdf8_1px,transparent_1px)] [background-size:16px_16px] pointer-events-none hidden md:block"></div>
      </section>

      {/* 4 Multi-Department Clearances */}
      <section className="space-y-6">
        <div className="text-center max-w-xl mx-auto space-y-2">
          <h2 className="text-2xl font-bold text-slate-800">Parallel Departmental Clearances</h2>
          <p className="text-sm text-slate-500">
            Powered by Camunda 8 Parallel Multi-Instance activities, four departments review your permit application concurrently without serial delays.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {departments.map((dept, idx) => (
            <div key={idx} className={`p-6 rounded-2xl border ${dept.color} shadow-sm flex flex-col justify-between space-y-4`}>
              <div>
                <span className="text-xs font-mono font-bold uppercase opacity-75">Department 0{idx + 1}</span>
                <h3 className="text-base font-bold mt-1 text-slate-900">{dept.name}</h3>
                <p className="text-xs text-slate-600 mt-2 leading-relaxed">{dept.desc}</p>
              </div>
              <div className="pt-2 border-t border-current/20 flex items-center justify-between text-xs font-semibold">
                <span>SLA: 5–10 Days</span>
                <CheckCircle className="w-4 h-4" />
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Key Architectural Features */}
      <section className="bg-white border border-slate-200 rounded-3xl p-8 sm:p-12 shadow-sm space-y-8">
        <div className="max-w-xl space-y-2">
          <h2 className="text-2xl font-bold text-slate-800">Enterprise Civic Architecture</h2>
          <p className="text-sm text-slate-500">
            Built using standard BPMN 2.0 specifications, event-driven Zeebe workers, and secure Spring Boot microservices.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="p-5 rounded-xl bg-slate-50 border border-slate-200 space-y-2">
            <div className="w-9 h-9 rounded-lg bg-municipal-800 text-white flex items-center justify-center font-bold">
              1
            </div>
            <h4 className="text-base font-bold text-slate-800">Sequential Document Checklist</h4>
            <p className="text-xs text-slate-600 leading-relaxed">
              Iterates over statutory documents sequentially via Zeebe Multi-Instance tasks. Corrupt or missing mandatory documents trigger named BPMN business errors with recovery paths.
            </p>
          </div>

          <div className="p-5 rounded-xl bg-slate-50 border border-slate-200 space-y-2">
            <div className="w-9 h-9 rounded-lg bg-civicTeal-600 text-white flex items-center justify-center font-bold">
              2
            </div>
            <h4 className="text-base font-bold text-slate-800">Embedded Revision Sub-Process</h4>
            <p className="text-xs text-slate-600 leading-relaxed">
              When an officer requires blueprint changes, an embedded sub-process notifies the citizen, halts parallel approval, accepts revised documents, and enforces statutory cycle limits.
            </p>
          </div>

          <div className="p-5 rounded-xl bg-slate-50 border border-slate-200 space-y-2">
            <div className="w-9 h-9 rounded-lg bg-amber-600 text-white flex items-center justify-center font-bold">
              3
            </div>
            <h4 className="text-base font-bold text-slate-800">SLA Timer Boundary Events</h4>
            <p className="text-xs text-slate-600 leading-relaxed">
              Overdue reviews trigger non-interrupting timer boundary events, notifying administrators to prevent departmental stalling and ensure prompt citizen service delivery.
            </p>
          </div>
        </div>
      </section>
    </div>
  );
}
