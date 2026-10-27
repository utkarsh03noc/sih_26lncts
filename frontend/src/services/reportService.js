import api from './api';

export const reportService = {
  getSummaryReport: async () => {
    const response = await api.get('/reports/summary');
    return response.data;
  },
};
