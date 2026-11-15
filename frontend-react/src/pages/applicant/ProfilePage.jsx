import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { User, Mail, Phone, Shield, Building, Award } from 'lucide-react';

export default function ProfilePage() {
  const { user } = useAuth();

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="pb-4 border-b border-slate-200">
        <h1 className="text-2xl font-extrabold text-slate-900">User Profile & Credentials</h1>
        <p className="text-xs text-slate-500">Verified citizen applicant details for municipal records</p>
      </div>

      <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 shadow-sm space-y-6">
        <div className="flex items-center gap-4 pb-6 border-b border-slate-100">
          <div className="w-16 h-16 rounded-2xl bg-municipal-800 text-civicTeal-400 flex items-center justify-center font-bold text-xl shadow-inner">
            {user?.fullName?.charAt(0) || 'U'}
          </div>
          <div>
            <h2 className="text-xl font-bold text-slate-900">{user?.fullName}</h2>
            <span className="text-xs font-mono text-slate-500">@{user?.username}</span>
            <div className="mt-1">
              <span className="text-[10px] font-bold uppercase tracking-wider bg-teal-100 text-teal-800 px-2 py-0.5 rounded-full">
                {user?.role}
              </span>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-100 space-y-1">
            <span className="text-slate-400 text-[10px] uppercase font-bold flex items-center gap-1">
              <Mail className="w-3.5 h-3.5" /> Official Email
            </span>
            <span className="font-semibold text-slate-800 font-mono">{user?.email}</span>
          </div>

          <div className="p-4 rounded-xl bg-slate-50 border border-slate-100 space-y-1">
            <span className="text-slate-400 text-[10px] uppercase font-bold flex items-center gap-1">
              <Shield className="w-3.5 h-3.5" /> System Identity ID
            </span>
            <span className="font-semibold text-slate-800 font-mono">USER-ID-{user?.id}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
