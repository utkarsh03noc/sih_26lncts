import api from './api';

export const userService = {
  getAllOfficers: async () => {
    const response = await api.get('/users/officers');
    return response.data;
  },

  getOfficersByInstitution: async (institutionId) => {
    const response = await api.get(`/users/officers/institution/${institutionId}`);
    return response.data;
  },

  getUserById: async (id) => {
    const response = await api.get(`/users/${id}`);
    return response.data;
  },
};
