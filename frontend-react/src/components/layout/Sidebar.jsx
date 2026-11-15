import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  FilePlus,
  FolderOpen,
  ClipboardList,
  RotateCcw,
  CreditCard,
  Award,
  Users,
  Settings,
  Activity,
  History,
  Building,
  CheckSquare
} from 'lucide-react';

export default function Sidebar() {
  const { user } = useAuth();
  const role = user?.role;

  const applicantLinks = [
    { to: '/applicant/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/applicant/new-application', icon: FilePlus, label: 'New Permit Application' },
    { to: '/applicant/my-applications', icon: FolderOpen, label: 'My Applications' },
    { to: '/applicant/revisions', icon: RotateCcw, label: 'Revision Requests' },
    { to: '/applicant/payments', icon: CreditCard, label: 'Fee Payments' },
    { to: '/applicant/approved-permits', icon: Award, label: 'Approved Permits' },
  ];

  const officerLinks = [
    { to: '/officer/dashboard', icon: LayoutDashboard, label: 'Department Dashboard' },
    { to: '/officer/inbox', icon: ClipboardList, label: 'Assigned Task Inbox' },
  ];

  const adminLinks = [
    { to: '/admin/dashboard', icon: LayoutDashboard, label: 'System Overview' },
    { to: '/admin/applications', icon: FolderOpen, label: 'Application Monitor' },
    { to: '/admin/workflows', icon: Activity, label: 'Workflow Instances' },
    { to: '/admin/departments', icon: Building, label: 'Departments' },
    { to: '/admin/users', icon: Users, label: 'User & Role Access' },
    { to: '/admin/audit-logs', icon: History, label: 'Audit Trail' },
  ];

  const getLinks = () => {
    switch (role) {
      case 'ROLE_OFFICER':
        return officerLinks;
      case 'ROLE_ADMIN':
      case 'ROLE_SUPER_ADMIN':
        return adminLinks;
      case 'ROLE_APPLICANT':
      default:
        return applicantLinks;
    }
  };

  const links = getLinks();

  return (
    <aside className="w-64 bg-white border-r border-slate-200 min-h-[calc(100vh-4rem)] flex flex-col justify-between p-4 shadow-sm">
      <div className="space-y-6">
        <div>
          <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400 px-3">
            {role === 'ROLE_OFFICER' ? `${user?.departmentCode || 'OFFICER'} PORTAL` :
             role === 'ROLE_ADMIN' || role === 'ROLE_SUPER_ADMIN' ? 'MUNICIPAL CONSOLE' : 'APPLICANT PORTAL'}
          </span>
          <nav className="mt-2 space-y-1">
            {links.map((link) => {
              const Icon = link.icon;
              return (
                <NavLink
                  key={link.to}
                  to={link.to}
                  className={({ isActive }) =>
                    `flex items-center space-x-3 px-3 py-2.5 rounded-lg text-sm font-medium transition ${
                      isActive
                        ? 'bg-municipal-800 text-white shadow-sm'
                        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                    }`
                  }
                >
                  <Icon className="w-4 h-4" />
                  <span>{link.label}</span>
                </NavLink>
              );
            })}
          </nav>
        </div>

        {/* Camunda Engine Status Badge */}
        <div className="bg-slate-50 border border-slate-200 rounded-xl p-3.5 space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-700 flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              Camunda 8 Engine
            </span>
            <span className="text-[10px] bg-slate-200 text-slate-700 px-1.5 py-0.5 rounded font-mono">v8.5</span>
          </div>
          <p className="text-[11px] text-slate-500 leading-snug">
            Zeebe workflow orchestrator active. Multi-Instance parallel reviews & SLA timer boundary events synchronized.
          </p>
        </div>
      </div>

      <div className="pt-4 border-t border-slate-100 text-center">
        <span className="text-[11px] text-slate-400">
          CivicBuild Portal &bull; Enterprise 2026
        </span>
      </div>
    </aside>
  );
}
