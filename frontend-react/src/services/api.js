import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for JWT
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('civicbuild_token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for handling 401
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response && error.response.status === 401) {
      // If unauthorized and not already on login page, handle logout
      if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/register')) {
        localStorage.removeItem('civicbuild_token');
        localStorage.removeItem('civicbuild_user');
      }
    }
    const message = error.response?.data?.message || error.message || 'An error occurred';
    return Promise.reject(new Error(message));
  }
);

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  getMe: () => api.get('/auth/me'),
};

export const applicationApi = {
  create: (data) => api.post('/applications', data),
  getAll: () => api.get('/applications'),
  getById: (id) => api.get(`/applications/${id}`),
  update: (id, data) => api.put(`/applications/${id}`, data),
  submit: (id) => api.post(`/applications/${id}/submit`),
  uploadDocument: (id, formData) => api.post(`/applications/${id}/documents`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  getStatus: (id) => api.get(`/applications/${id}/status`),
  getHistory: (id) => api.get(`/applications/${id}/history`),
};

export const departmentApi = {
  getAll: () => api.get('/departments'),
};

export const reviewApi = {
  getAssigned: () => api.get('/reviews'),
  getById: (id) => api.get(`/reviews/${id}`),
  approve: (id, remarks) => api.post(`/reviews/${id}/approve`, { status: 'APPROVED', remarks }),
  reject: (id, remarks) => api.post(`/reviews/${id}/reject`, { status: 'REJECTED', remarks }),
  requestRevision: (id, remarks) => api.post(`/reviews/${id}/request-revision`, { status: 'REVISION_REQUESTED', remarks }),
};

export const taskApi = {
  getMyTasks: () => api.get('/tasks'),
  getById: (taskId) => api.get(`/tasks/${taskId}`),
  complete: (taskId, variables) => api.post(`/tasks/${taskId}/complete`, { variables }),
};

export const revisionApi = {
  getById: (id) => api.get(`/revisions/${id}`),
  submit: (id, remarks) => api.post(`/revisions/${id}/submit`, { remarks }),
};

export const paymentApi = {
  get: (applicationId) => api.get(`/payments/${applicationId}`),
  initiate: (applicationId, paymentMethod) => api.post(`/payments/${applicationId}/initiate`, { paymentMethod }),
  confirm: (applicationId, paymentReference, mockSuccess = true) => 
    api.post(`/payments/${applicationId}/confirm`, { paymentReference, mockSuccess }),
};

export const permitApi = {
  getAll: () => api.get('/permits'),
  getById: (id) => api.get(`/permits/${id}`),
  getDownloadUrl: (id) => `/api/permits/${id}/download`,
};

export const dashboardApi = {
  getSummary: () => api.get('/dashboard/summary'),
  getDepartmentStats: () => api.get('/dashboard/department-stats'),
  getApplicationStats: () => api.get('/dashboard/application-stats'),
};

export const adminApi = {
  getUsers: () => api.get('/admin/users'),
  createUser: (data) => api.post('/admin/users', data),
  updateUser: (id, data) => api.put(`/admin/users/${id}`, data),
  getWorkflows: () => api.get('/admin/workflows'),
  getAuditLogs: () => api.get('/admin/audit-logs'),
};

export default api;
