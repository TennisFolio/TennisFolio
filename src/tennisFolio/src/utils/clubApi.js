import { apiRequestSilent } from './apiClient';

export const getMyClubs = () => apiRequestSilent.get('/api/clubs');

export const createClub = (club) => apiRequestSilent.post('/api/clubs', club);

export const getClub = (publicId) => apiRequestSilent.get(`/api/clubs/${publicId}`);

export const updateClub = (publicId, club) =>
  apiRequestSilent.patch(`/api/clubs/${publicId}`, club);

export const deleteClub = (publicId) =>
  apiRequestSilent.delete(`/api/clubs/${publicId}`);

export const getClubMembers = (publicId, keyword = '') =>
  apiRequestSilent.get(`/api/clubs/${publicId}/members`, { keyword });

export const addClubMember = (publicId, member) =>
  apiRequestSilent.post(`/api/clubs/${publicId}/members`, member);

export const updateClubMember = (publicId, memberId, member) =>
  apiRequestSilent.patch(`/api/clubs/${publicId}/members/${memberId}`, member);

export const deleteClubMember = (publicId, memberId) =>
  apiRequestSilent.delete(`/api/clubs/${publicId}/members/${memberId}`);
