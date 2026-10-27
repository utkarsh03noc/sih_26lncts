import api from './api';

export const complaintService = {
  getComplaints: async (params = {}) => {
    const response = await api.get('/complaints', { params });
    return response.data;
  },

  getComplaintById: async (id) => {
    const response = await api.get(`/complaints/${id}`);
    return response.data;
  },

  trackPublicComplaint: async (complaintNumber) => {
    const response = await api.get(`/complaints/public/track/${complaintNumber}`);
    return response.data;
  },

  createComplaint: async (complaintData) => {
    const response = await api.post('/complaints', complaintData);
    return response.data;
  },

  checkDuplicates: async (checkData) => {
    const response = await api.post('/complaints/check-duplicate', checkData);
    return response.data;
  },

  classifyComplaint: async (data) => {
    const response = await api.post('/complaints/classify', data);
    return response.data;
  },

  getDuplicates: async (params = {}) => {
    const response = await api.get('/complaints/duplicates', { params });
    return response.data;
  },

  updateStatus: async (id, statusData) => {
    const response = await api.patch(`/complaints/${id}/status`, statusData);
    return response.data;
  },

  assignComplaint: async (id, assignData) => {
    const response = await api.post(`/complaints/${id}/assign`, assignData);
    return response.data;
  },

  transferComplaint: async (id, transferData) => {
    const response = await api.post(`/complaints/${id}/transfer`, transferData);
    return response.data;
  },

  getTransfers: async (id) => {
    const response = await api.get(`/complaints/${id}/transfers`);
    return response.data;
  },

  getComplaintsByInstitution: async (institutionId) => {
    const response = await api.get(`/complaints/institution/${institutionId}`);
    return response.data;
  },

  reopenComplaint: async (id, remarks) => {
    const response = await api.post(`/complaints/${id}/reopen`, { remarks });
    return response.data;
  },

  getUpdates: async (id) => {
    const response = await api.get(`/complaints/${id}/updates`);
    return response.data;
  },

  getAttachments: async (id) => {
    const response = await api.get(`/complaints/${id}/attachments`);
    return response.data;
  },

  submitFeedback: async (id, feedbackData) => {
    const response = await api.post(`/complaints/${id}/feedback`, feedbackData);
    return response.data;
  },

  getFeedback: async (id) => {
    const response = await api.get(`/complaints/${id}/feedback`);
    return response.data;
  },

  getEscalations: async (params = {}) => {
    const response = await api.get('/complaints/escalations', { params });
    return response.data;
  },

  mergeComplaints: async (mergeData) => {
    const response = await api.post('/complaints/merge', mergeData);
    return response.data;
  },

  unmergeComplaints: async (unmergeData) => {
    const response = await api.post('/complaints/unmerge', unmergeData);
    return response.data;
  },

  linkRelatedComplaints: async (linkData) => {
    const response = await api.post('/complaints/link-related', linkData);
    return response.data;
  },

  compareComplaints: async (caseA, caseB) => {
    const response = await api.get('/complaints/compare', {
      params: {
        caseA,
        caseB,
        caseAId: caseA,
        caseBId: caseB,
      },
    });
    return response.data;
  },

  getUnifiedCaseFile: async (id) => {
    const response = await api.get(`/complaints/${id}/unified-file`);
    return response.data;
  },

  getMergeCandidates: async (id) => {
    const response = await api.get(`/complaints/${id}/merge-candidates`);
    return response.data;
  },
};
