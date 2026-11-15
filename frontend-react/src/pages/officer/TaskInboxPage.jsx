import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { taskApi } from '../../services/api';
import {
  ClipboardList,
  Clock,
  ArrowRight,
  Search,
  Filter,
  Building,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';

export default function TaskInboxPage() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    loadTasks();
  }, []);

  const loadTasks = async () => {
    try {
      setLoading(true);
      const res = await taskApi.getMyTasks();
      if (res.success && res.data) {
        setTasks(res.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const filteredTasks = tasks.filter((t) =>
    t.applicationNumber?.toLowerCase().includes(search.toLowerCase()) ||
    t.applicantName?.toLowerCase().includes(search.toLowerCase()) ||
    t.department?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="pb-4 border-b border-slate-200">
        <h1 className="text-2xl font-extrabold text-slate-900">Custom Tasklist & Queue</h1>
        <p className="text-xs text-slate-500">
          Statutory user tasks instantiated by the Camunda 8 Zeebe workflow engine for departmental action
        </p>
      </div>

      {/* Search Input */}
      <div className="relative max-w-md">
        <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
        <input
          type="text"
          placeholder="Filter tasks by application ref, citizen or department..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-9 pr-4 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
        />
      </div>

      {/* Tasks Table */}
      <div className="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-xs text-slate-500">Loading assigned tasks from engine...</div>
        ) : filteredTasks.length === 0 ? (
          <div className="p-12 text-center text-xs text-slate-500 space-y-2">
            <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto" />
            <p className="font-semibold text-slate-700">No pending tasks found</p>
            <p className="text-slate-400">Your department queue is completely up to date.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider text-[10px]">
                <tr>
                  <th className="py-3 px-4">Task ID & Name</th>
                  <th className="py-3 px-4">Application Ref</th>
                  <th className="py-3 px-4">Applicant</th>
                  <th className="py-3 px-4">Department</th>
                  <th className="py-3 px-4">Created Date</th>
                  <th className="py-3 px-4">SLA Due Date</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredTasks.map((t) => (
                  <tr key={t.taskId} className="hover:bg-slate-50/80 transition">
                    <td className="py-3.5 px-4">
                      <span className="font-semibold text-slate-900 block">{t.taskName}</span>
                      <span className="text-[10px] text-slate-400 font-mono">{t.taskId}</span>
                    </td>
                    <td className="py-3.5 px-4 font-mono font-bold text-municipal-800">
                      {t.applicationNumber}
                    </td>
                    <td className="py-3.5 px-4 text-slate-700 font-medium">
                      {t.applicantName}
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="bg-amber-100 text-amber-800 text-[10px] font-bold px-2 py-0.5 rounded">
                        {t.department}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-500">
                      {t.createdDate ? new Date(t.createdDate).toLocaleDateString() : 'Today'}
                    </td>
                    <td className="py-3.5 px-4 text-slate-600 font-medium">
                      {t.dueDate ? (
                        <span className="flex items-center gap-1 text-slate-700">
                          <Clock className="w-3 h-3 text-amber-500" />
                          {new Date(t.dueDate).toLocaleDateString()}
                        </span>
                      ) : (
                        'Standard 7-Day SLA'
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Link
                        to={`/officer/review/${t.applicationId}?reviewId=${t.taskVariables?.departmentReviewId || ''}`}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-municipal-800 hover:bg-municipal-900 text-white font-semibold transition"
                      >
                        <span>Review</span>
                        <ArrowRight className="w-3 h-3" />
                      </Link>
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
