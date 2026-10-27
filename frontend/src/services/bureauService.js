import api from './api';

export const bureauService = {
  // Bureau operations
  getBureaus: async () => {
    const response = await api.get('/bureaus');
    return response.data;
  },

  getActiveBureaus: async () => {
    const response = await api.get('/bureaus/active');
    return response.data;
  },

  getBureauById: async (id) => {
    const response = await api.get(`/bureaus/${id}`);
    return response.data;
  },

  createBureau: async (data) => {
    const response = await api.post('/bureaus', data);
    return response.data;
  },

  updateBureau: async (id, data) => {
    const response = await api.put(`/bureaus/${id}`, data);
    return response.data;
  },

  deleteBureau: async (id) => {
    const response = await api.delete(`/bureaus/${id}`);
    return response.data;
  },

  // Cell operations
  getCells: async () => {
    const response = await api.get('/cells');
    return response.data;
  },

  getActiveCells: async () => {
    const response = await api.get('/cells/active');
    return response.data;
  },

  getCellsByBureau: async (bureauId) => {
    const response = await api.get(`/cells/bureau/${bureauId}`);
    return response.data;
  },

  getCellById: async (id) => {
    const response = await api.get(`/cells/${id}`);
    return response.data;
  },

  createCell: async (data) => {
    const response = await api.post('/cells', data);
    return response.data;
  },

  updateCell: async (id, data) => {
    const response = await api.put(`/cells/${id}`, data);
    return response.data;
  },

  deleteCell: async (id) => {
    const response = await api.delete(`/cells/${id}`);
    return response.data;
  },
};
