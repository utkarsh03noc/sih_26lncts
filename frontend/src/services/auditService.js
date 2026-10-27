import api from './api';

export const auditService = {
  getAuditLogs: async (params = {}) => {
    const response = await api.get('/audit-logs', { params });
    return response.data;
  },

  getEntityLogs: async (entityName, entityId) => {
    const response = await api.get(`/audit-logs/entity/${entityName}/${entityId}`);
    return response.data;
  },
};
