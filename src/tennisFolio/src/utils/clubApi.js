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

export const getClubMeetings = (publicId) =>
  apiRequestSilent.get(`/api/clubs/${publicId}/meetings`);

export const createClubMeeting = (publicId, meeting) =>
  apiRequestSilent.post(`/api/clubs/${publicId}/meetings`, meeting);

export const getClubMeeting = (publicId, meetingPublicId) =>
  apiRequestSilent.get(`/api/clubs/${publicId}/meetings/${meetingPublicId}`);

export const updateClubMeeting = (publicId, meetingPublicId, meeting) =>
  apiRequestSilent.patch(
    `/api/clubs/${publicId}/meetings/${meetingPublicId}`,
    meeting,
  );

export const updateClubMeetingStatus = (publicId, meetingPublicId, status) =>
  apiRequestSilent.patch(
    `/api/clubs/${publicId}/meetings/${meetingPublicId}/status`,
    { status },
  );

export const deleteClubMeeting = (publicId, meetingPublicId) =>
  apiRequestSilent.delete(`/api/clubs/${publicId}/meetings/${meetingPublicId}`);
