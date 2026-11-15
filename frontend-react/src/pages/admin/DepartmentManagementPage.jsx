import React, { useState, useEffect } from 'react';
import { departmentApi } from '../../services/api';
import { Building, Mail, Clock, CheckCircle2 } from 'lucide-react';

export default function DepartmentManagementPage() {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDepartments();
  }, []);

  const loadDepartments = async () => {
    try {
      setLoading(true);
      const res = await departmentApi.getAll();
      if (res.success && res.data) {
        setDepartments(res.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="pb-4 border-b border-slate-200">
        <h1 className="text-2xl font-extrabold text-slate-900">Municipal Review Departments</h1>
        <p className="text-xs text-slate-500">Configured statutory clearance entities and SLA parameters</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {departments.map((dept) => (
          <div key={dept.id} className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <span className="font-mono font-bold text-xs bg-slate-100 text-slate-700 px-2 py-0.5 rounded">
                CODE: {dept.code}
              </span>
              <span className="text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full flex items-center gap-1">
                <CheckCircle2 className="w-3 h-3" /> Active Clearance Bureau
              </span>
            </div>

            <div>
              <h3 className="text-base font-bold text-slate-900">{dept.name}</h3>
              <p className="text-xs text-slate-500 mt-1 leading-relaxed">{dept.description}</p>
            </div>

            <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-100 text-xs space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-slate-500 flex items-center gap-1.5">
                  <Mail className="w-3.5 h-3.5 text-slate-400" /> Statutory Email:
                </span>
                <span className="font-mono font-semibold text-slate-700">{dept.contactEmail}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-slate-500 flex items-center gap-1.5">
                  <Clock className="w-3.5 h-3.5 text-slate-400" /> Target SLA Turnaround:
                </span>
                <span className="font-semibold text-slate-800">{dept.slaDays} Working Days</span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
