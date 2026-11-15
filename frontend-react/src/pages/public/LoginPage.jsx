import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Building2, LogIn, AlertCircle, KeyRound, UserCheck } from 'lucide-react';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const user = await login(username, password);
      if (user.role === 'ROLE_OFFICER') {
        navigate('/officer/dashboard');
      } else if (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_SUPER_ADMIN') {
        navigate('/admin/dashboard');
      } else {
        navigate('/applicant/dashboard');
      }
    } catch (err) {
      setError(err.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickFill = (user, pass) => {
    setUsername(user);
    setPassword(pass);
  };

  return (
    <div className="max-w-md mx-auto my-8 space-y-6">
      <div className="text-center space-y-2">
        <div className="w-12 h-12 rounded-xl bg-municipal-800 text-civicTeal-400 mx-auto flex items-center justify-center shadow-md">
          <Building2 className="w-7 h-7" />
        </div>
        <h2 className="text-2xl font-bold text-slate-800">Sign in to CivicBuild</h2>
        <p className="text-xs text-slate-500">Enter your credentials to access your portal</p>
      </div>

      <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 shadow-sm space-y-6">
        {error && (
          <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Username or Email</label>
            <input
              type="text"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-civicTeal-500 focus:border-civicTeal-500 outline-none transition"
              placeholder="e.g. applicant1 or admin"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-civicTeal-500 focus:border-civicTeal-500 outline-none transition"
              placeholder="Enter your password"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-municipal-800 hover:bg-municipal-900 text-white font-semibold py-2.5 rounded-xl shadow-md transition flex items-center justify-center gap-2 text-sm disabled:opacity-50"
          >
            {loading ? <span className="animate-spin">&bull;</span> : <LogIn className="w-4 h-4" />}
            <span>Sign In</span>
          </button>
        </form>

        {/* 1-Click Demo Accounts for Fast Evaluation */}
        <div className="pt-4 border-t border-slate-100 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1">
              <UserCheck className="w-3.5 h-3.5 text-civicTeal-600" />
              <span>Demo Quick-Fill Accounts</span>
            </span>
            <span className="text-[10px] text-slate-400">pwd: password123</span>
          </div>

          <div className="grid grid-cols-2 gap-2 text-xs">
            <button
              type="button"
              onClick={() => handleQuickFill('applicant1', 'password123')}
              className="p-2 text-left rounded-lg bg-teal-50 hover:bg-teal-100 text-teal-800 border border-teal-200 font-medium transition"
            >
              Applicant
            </button>
            <button
              type="button"
              onClick={() => handleQuickFill('fire_officer', 'password123')}
              className="p-2 text-left rounded-lg bg-red-50 hover:bg-red-100 text-red-800 border border-red-200 font-medium transition"
            >
              Fire Officer
            </button>
            <button
              type="button"
              onClick={() => handleQuickFill('water_officer', 'password123')}
              className="p-2 text-left rounded-lg bg-blue-50 hover:bg-blue-100 text-blue-800 border border-blue-200 font-medium transition"
            >
              Water Officer
            </button>
            <button
              type="button"
              onClick={() => handleQuickFill('zoning_officer', 'password123')}
              className="p-2 text-left rounded-lg bg-amber-50 hover:bg-amber-100 text-amber-800 border border-amber-200 font-medium transition"
            >
              Zoning Officer
            </button>
            <button
              type="button"
              onClick={() => handleQuickFill('env_officer', 'password123')}
              className="p-2 text-left rounded-lg bg-emerald-50 hover:bg-emerald-100 text-emerald-800 border border-emerald-200 font-medium transition"
            >
              Env Officer
            </button>
            <button
              type="button"
              onClick={() => handleQuickFill('admin', 'password123')}
              className="p-2 text-left rounded-lg bg-purple-50 hover:bg-purple-100 text-purple-800 border border-purple-200 font-medium transition"
            >
              Municipal Admin
            </button>
          </div>
        </div>
      </div>

      <p className="text-center text-xs text-slate-500">
        Don't have an applicant account?{' '}
        <Link to="/register" className="text-civicTeal-600 font-semibold hover:underline">
          Register here
        </Link>
      </p>
    </div>
  );
}
