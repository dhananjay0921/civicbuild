import React, { useState, useEffect } from 'react';
import { adminApi } from '../../services/api';
import { Activity, Clock, CheckCircle2, AlertCircle, RefreshCw, Cpu, Server } from 'lucide-react';

export default function WorkflowMonitoringPage() {
  const [workflows, setWorkflows] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadWorkflows();
  }, []);

  const loadWorkflows = async () => {
    try {
      setLoading(true);
      const res = await adminApi.getWorkflows();
      if (res.success && res.data) {
        setWorkflows(res.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-200 gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900">Camunda 8 Workflow Monitoring</h1>
          <p className="text-xs text-slate-500">Live inspection of Zeebe BPMN process instances, multi-instance parallel splits, and SLA boundaries</p>
        </div>
        <button
          onClick={loadWorkflows}
          className="bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 font-semibold px-3 py-2 rounded-xl text-xs flex items-center gap-1.5 shadow-sm transition"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Refresh Instances</span>
        </button>
      </div>

      {/* Camunda Engine Health Card */}
      <div className="p-4 rounded-2xl bg-slate-900 text-white flex flex-col sm:flex-row sm:items-center justify-between gap-4 shadow-sm">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-civicTeal-600/30 text-civicTeal-400 flex items-center justify-center">
            <Cpu className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs font-mono font-bold text-slate-300 block">Orchestrator Cluster</span>
            <span className="text-sm font-bold text-white flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              Zeebe Broker v8.5 (Dual-Mode Synchronized Engine Active)
            </span>
          </div>
        </div>

        <div className="flex items-center gap-4 text-xs font-mono text-slate-400">
          <div>Partitions: <strong className="text-white">1</strong></div>
          <div>Replication: <strong className="text-white">1</strong></div>
          <div>Active Instances: <strong className="text-white">{workflows.length}</strong></div>
        </div>
      </div>

      {/* Workflow Instances Table */}
      <div className="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-xs text-slate-500">Querying Zeebe workflow engine...</div>
        ) : workflows.length === 0 ? (
          <div className="p-12 text-center text-xs text-slate-500">No active workflow instances found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider text-[10px]">
                <tr>
                  <th className="py-3 px-4">Process Instance Key</th>
                  <th className="py-3 px-4">BPMN Process ID</th>
                  <th className="py-3 px-4">Application Ref</th>
                  <th className="py-3 px-4">Citizen</th>
                  <th className="py-3 px-4">Engine State</th>
                  <th className="py-3 px-4">Started At</th>
                  <th className="py-3 px-4">Parallel Clearances</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-mono">
                {workflows.map((wf, idx) => (
                  <tr key={idx} className="hover:bg-slate-50/80 transition">
                    <td className="py-3.5 px-4 font-bold text-municipal-800">
                      {wf.processInstanceKey}
                    </td>
                    <td className="py-3.5 px-4 text-slate-600">
                      {wf.bpmnProcessId}
                    </td>
                    <td className="py-3.5 px-4 text-slate-800 font-bold">
                      {wf.applicationNumber}
                    </td>
                    <td className="py-3.5 px-4 text-slate-700 font-sans">
                      {wf.applicantName}
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="font-sans font-bold text-[10px] px-2 py-0.5 rounded-full bg-slate-100 text-slate-800">
                        {wf.status}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-400 font-sans">
                      {wf.startDate ? new Date(wf.startDate).toLocaleDateString() : 'Active'}
                    </td>
                    <td className="py-3.5 px-4 font-sans">
                      <div className="flex items-center gap-1 text-[10px]">
                        {wf.reviews && wf.reviews.map((r) => (
                          <span
                            key={r.departmentCode}
                            title={`${r.departmentCode}: ${r.status}`}
                            className={`w-4 h-4 rounded flex items-center justify-center font-bold text-[9px] ${
                              r.status === 'APPROVED' ? 'bg-emerald-500 text-white' :
                              r.status === 'REJECTED' ? 'bg-rose-500 text-white' :
                              r.status === 'REVISION_REQUESTED' ? 'bg-indigo-500 text-white' :
                              'bg-amber-400 text-white'
                            }`}
                          >
                            {r.departmentCode[0]}
                          </span>
                        ))}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
