import React, { useState, useEffect } from 'react';
import { adminApi } from '../../services/api';
import { Users, UserPlus, Shield, Check, AlertCircle } from 'lucide-react';

export default function UserManagementPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [newUser, setNewUser] = useState({
    username: '',
    email: '',
    password: 'password123',
    fullName: '',
    phone: '',
    role: 'ROLE_OFFICER',
    departmentCode: 'FIRE',
  });

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const res = await adminApi.getUsers();
      if (res.success && res.data) {
        setUsers(res.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const res = await adminApi.createUser(newUser);
      if (res.success) {
        setSuccess(`User ${newUser.username} provisioned successfully!`);
        setShowAddModal(false);
        setNewUser({
          username: '',
          email: '',
          password: 'password123',
          fullName: '',
          phone: '',
          role: 'ROLE_OFFICER',
          departmentCode: 'FIRE',
        });
        await loadUsers();
      }
    } catch (err) {
      setError(err.message || 'Failed to create user');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-200 gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900">User & Officer Access Management</h1>
          <p className="text-xs text-slate-500">Provision municipal department officers, administrators, and applicant roles</p>
        </div>
        <button
          onClick={() => setShowAddModal(true)}
          className="bg-civicTeal-600 hover:bg-civicTeal-700 text-white font-semibold px-4 py-2.5 rounded-xl text-xs flex items-center gap-1.5 shadow-sm transition"
        >
          <UserPlus className="w-4 h-4" />
          <span>Provision New Officer</span>
        </button>
      </div>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}
      {success && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs rounded-xl flex items-center gap-2">
          <Check className="w-4 h-4 shrink-0" />
          <span>{success}</span>
        </div>
      )}

      {/* Users Table */}
      <div className="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-xs text-slate-500">Loading user credentials...</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider text-[10px]">
                <tr>
                  <th className="py-3 px-4">Full Name & Username</th>
                  <th className="py-3 px-4">Email</th>
                  <th className="py-3 px-4">Phone</th>
                  <th className="py-3 px-4">Assigned Role</th>
                  <th className="py-3 px-4">Department</th>
                  <th className="py-3 px-4">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {users.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-50/80 transition">
                    <td className="py-3 px-4">
                      <span className="font-bold text-slate-900 block">{u.fullName}</span>
                      <span className="text-[10px] text-slate-400 font-mono">@{u.username}</span>
                    </td>
                    <td className="py-3 px-4 text-slate-600 font-mono">{u.email}</td>
                    <td className="py-3 px-4 text-slate-600">{u.phone}</td>
                    <td className="py-3 px-4">
                      <span className="bg-slate-100 text-slate-700 font-bold text-[10px] px-2 py-0.5 rounded">
                        {u.role?.name}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      {u.departmentCode ? (
                        <span className="bg-amber-100 text-amber-800 font-bold text-[10px] px-2 py-0.5 rounded">
                          {u.departmentCode}
                        </span>
                      ) : (
                        <span className="text-slate-400">-</span>
                      )}
                    </td>
                    <td className="py-3 px-4">
                      <span className="text-[10px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full">
                        Active
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal for Provisioning User */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 space-y-4 shadow-xl">
            <h3 className="text-base font-bold text-slate-800">Provision Municipal Account</h3>
            <form onSubmit={handleCreateUser} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Full Name</label>
                <input
                  type="text"
                  required
                  value={newUser.fullName}
                  onChange={(e) => setNewUser({ ...newUser, fullName: e.target.value })}
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 outline-none"
                  placeholder="e.g. Inspector S. Rao"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Username</label>
                <input
                  type="text"
                  required
                  value={newUser.username}
                  onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 outline-none"
                  placeholder="e.g. fire_officer_2"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Email</label>
                <input
                  type="email"
                  required
                  value={newUser.email}
                  onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 outline-none"
                  placeholder="e.g. srao@civicbuild.gov"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Phone</label>
                <input
                  type="text"
                  required
                  value={newUser.phone}
                  onChange={(e) => setNewUser({ ...newUser, phone: e.target.value })}
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 outline-none"
                  placeholder="+91 98765 00000"
                />
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Role</label>
                  <select
                    value={newUser.role}
                    onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 outline-none bg-white"
                  >
                    <option value="ROLE_OFFICER">Officer</option>
                    <option value="ROLE_ADMIN">Admin</option>
                    <option value="ROLE_APPLICANT">Applicant</option>
                  </select>
                </div>

                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Department</label>
                  <select
                    value={newUser.departmentCode}
                    onChange={(e) => setNewUser({ ...newUser, departmentCode: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 outline-none bg-white"
                  >
                    <option value="FIRE">FIRE</option>
                    <option value="WATER">WATER</option>
                    <option value="ZONING">ZONING</option>
                    <option value="ENVIRONMENTAL">ENVIRONMENTAL</option>
                  </select>
                </div>
              </div>

              <div className="pt-3 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 border rounded-xl text-slate-600 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-civicTeal-600 hover:bg-civicTeal-700 text-white rounded-xl font-semibold"
                >
                  Create Account
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
