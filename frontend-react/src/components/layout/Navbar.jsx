import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Building2, LogOut, User, Bell, ShieldCheck } from 'lucide-react';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getRoleBadge = (role) => {
    switch (role) {
      case 'ROLE_SUPER_ADMIN':
        return <span className="bg-purple-100 text-purple-800 text-xs px-2 py-0.5 rounded-full font-semibold">Super Admin</span>;
      case 'ROLE_ADMIN':
        return <span className="bg-blue-100 text-blue-800 text-xs px-2 py-0.5 rounded-full font-semibold">Municipal Admin</span>;
      case 'ROLE_OFFICER':
        return <span className="bg-amber-100 text-amber-800 text-xs px-2 py-0.5 rounded-full font-semibold">{user?.departmentCode || 'Officer'}</span>;
      case 'ROLE_APPLICANT':
      default:
        return <span className="bg-teal-100 text-teal-800 text-xs px-2 py-0.5 rounded-full font-semibold">Applicant</span>;
    }
  };

  return (
    <header className="bg-municipal-900 border-b border-municipal-700 text-white sticky top-0 z-50 shadow-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        
        {/* Brand Logo */}
        <Link to="/" className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-lg bg-civicTeal-600 flex items-center justify-center text-white shadow-inner">
            <Building2 className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xl font-bold tracking-tight text-white flex items-center gap-1.5">
              CIVIC<span className="text-civicTeal-500">BUILD</span>
            </span>
            <span className="text-[10px] uppercase tracking-wider block text-slate-400 font-medium">
              Municipal Permit Automation Portal
            </span>
          </div>
        </Link>

        {/* Public Nav or User Profile */}
        <div className="flex items-center space-x-4">
          {!isAuthenticated ? (
            <div className="flex items-center space-x-3">
              <Link to="/about" className="text-sm text-slate-300 hover:text-white transition">About</Link>
              <Link to="/login" className="text-sm font-medium text-white px-3 py-1.5 rounded-md hover:bg-municipal-800 transition">
                Sign In
              </Link>
              <Link to="/register" className="text-sm font-semibold text-white bg-civicTeal-600 px-4 py-1.5 rounded-md hover:bg-civicTeal-700 shadow-sm transition">
                Register
              </Link>
            </div>
          ) : (
            <div className="flex items-center space-x-4">
              <div className="hidden sm:flex flex-col text-right">
                <span className="text-sm font-semibold text-white">{user?.fullName}</span>
                <div className="flex items-center justify-end gap-1.5">
                  {getRoleBadge(user?.role)}
                </div>
              </div>

              <div className="h-8 w-px bg-municipal-700 hidden sm:block"></div>

              <button
                onClick={handleLogout}
                className="flex items-center space-x-1.5 text-xs font-medium text-slate-300 hover:text-white bg-municipal-800 hover:bg-red-900/40 hover:text-red-300 px-3 py-1.5 rounded-md border border-municipal-700 transition"
                title="Sign out"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Logout</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
