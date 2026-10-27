import api from './api';

export const dashboardService = {
  getOverallStats: async (params = {}) => {
    const response = await api.get('/dashboard/stats', { params });
    return response.data;
  },

  getInstitutionStats: async (institutionId) => {
    const response = await api.get('/dashboard/institution-stats', {
      params: institutionId ? { institutionId } : {},
    });
    return response.data;
  },

  getMyStats: async () => {
    const response = await api.get('/dashboard/my-stats');
    return response.data;
  },
};
