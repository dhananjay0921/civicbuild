import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { reviewApi, taskApi } from '../../services/api';
import {
  ClipboardList,
  Clock,
  CheckCircle2,
  AlertCircle,
  Building,
  RotateCcw,
  ArrowRight
} from 'lucide-react';

export default function OfficerDashboard() {
  const { user } = useAuth();
  const [reviews, setReviews] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadOfficerData();
  }, []);

  const loadOfficerData = async () => {
    try {
      setLoading(true);
      const [revRes, taskRes] = await Promise.all([
        reviewApi.getAssigned(),
        taskApi.getMyTasks()
      ]);
      if (revRes.success && revRes.data) setReviews(revRes.data);
      if (taskRes.success && taskRes.data) setTasks(taskRes.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const pending = reviews.filter(r => r.status === 'PENDING' || r.status === 'IN_PROGRESS').length;
  const approved = reviews.filter(r => r.status === 'APPROVED').length;
  const revisions = reviews.filter(r => r.status === 'REVISION_REQUESTED').length;
  const rejected = reviews.filter(r => r.status === 'REJECTED').length;

  return (
    <div className="space-y-8">
      {/* Department Banner */}
      <div className="bg-gradient-to-r from-municipal-900 to-slate-900 text-white rounded-3xl p-6 sm:p-8 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase tracking-wider text-amber-400 font-bold">
            {user?.departmentCode || 'MUNICIPAL'} DEPARTMENT DESK
          </span>
          <h1 className="text-2xl sm:text-3xl font-extrabold mt-1">Officer: {user?.fullName}</h1>
          <p className="text-xs sm:text-sm text-slate-300 mt-1 max-w-xl">
            Evaluate building permit submissions assigned to your department. Inspect architectural blueprints, check statutory compliance, and record official clearances.
          </p>
        </div>

        <Link
          to="/officer/inbox"
          className="bg-civicTeal-600 hover:bg-civicTeal-700 text-white font-semibold px-5 py-3 rounded-xl shadow-md flex items-center gap-2 text-sm transition shrink-0"
        >
          <ClipboardList className="w-4 h-4" />
          <span>Open Task Inbox ({tasks.length})</span>
        </Link>
      </div>

      {/* Metrics */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold uppercase tracking-wider">Pending Tasks</span>
            <Clock className="w-4 h-4 text-amber-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{pending}</div>
          <p className="text-[11px] text-slate-500">Awaiting your evaluation</p>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold uppercase tracking-wider">Approved by Dept</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{approved}</div>
          <p className="text-[11px] text-slate-500">Statutory clearance issued</p>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold uppercase tracking-wider">Revisions Asked</span>
            <RotateCcw className="w-4 h-4 text-indigo-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{revisions}</div>
          <p className="text-[11px] text-slate-500">Awaiting applicant update</p>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-1">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold uppercase tracking-wider">Rejected</span>
            <AlertCircle className="w-4 h-4 text-rose-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{rejected}</div>
          <p className="text-[11px] text-slate-500">Non-compliant proposals</p>
        </div>
      </div>

      {/* Immediate Task Inbox Preview */}
      <div className="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
        <div className="p-5 border-b border-slate-100 flex items-center justify-between">
          <h3 className="text-base font-bold text-slate-800">Assigned Department Review Queue</h3>
          <Link to="/officer/inbox" className="text-xs font-semibold text-civicTeal-600 hover:text-civicTeal-700 flex items-center gap-1">
            <span>View All Tasks</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {loading ? (
          <div className="p-8 text-center text-xs text-slate-500">Loading task queue...</div>
        ) : tasks.length === 0 ? (
          <div className="p-12 text-center text-xs text-slate-500">No pending tasks in your department inbox.</div>
        ) : (
          <div className="divide-y divide-slate-100">
            {tasks.map((task) => (
              <div key={task.taskId} className="p-4 sm:p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-slate-50 transition">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-bold text-sm text-municipal-800">{task.applicationNumber}</span>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-amber-100 text-amber-800">
                      {task.department}
                    </span>
                    <span className="text-xs font-medium text-slate-500">&bull; {task.applicantName}</span>
                  </div>
                  <p className="text-xs text-slate-700 font-semibold">{task.taskName}</p>
                  <p className="text-[11px] text-slate-400">
                    Category: {task.buildingType} &bull; Due Date: {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : '7 Days'}
                  </p>
                </div>

                <Link
                  to={`/officer/review/${task.applicationId}?reviewId=${task.taskVariables?.departmentReviewId || ''}`}
                  className="bg-municipal-800 hover:bg-municipal-900 text-white font-semibold px-4 py-2 rounded-xl text-xs flex items-center gap-2 shadow-sm transition shrink-0"
                >
                  <span>Review & Decide</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
