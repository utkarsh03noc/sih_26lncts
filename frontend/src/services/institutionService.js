import api from './api';

export const institutionService = {
  getInstitutions: async (params = {}) => {
    const response = await api.get('/institutions', { params });
    return response.data;
  },

  getActiveInstitutions: async () => {
    const response = await api.get('/institutions/active');
    return response.data;
  },

  getInstitutionById: async (id) => {
    const response = await api.get(`/institutions/${id}`);
    return response.data;
  },

  getMonitoringSummary: async (params = {}) => {
    const response = await api.get('/institutions/monitoring', { params });
    return response.data;
  },

  getInstitutionAuditLogs: async (id, params = {}) => {
    const response = await api.get(`/institutions/${id}/audit-logs`, { params });
    return response.data;
  },

  createInstitution: async (institutionData) => {
    const response = await api.post('/institutions', institutionData);
    return response.data;
  },

  updateInstitution: async (id, institutionData) => {
    const response = await api.put(`/institutions/${id}`, institutionData);
    return response.data;
  },
};
