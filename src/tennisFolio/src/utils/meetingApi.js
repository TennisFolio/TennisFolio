import { apiRequestSilent } from './apiClient';

export const getMyMeetings = () => apiRequestSilent.get('/api/me/meetings');

export const createMeeting = (meeting) =>
  apiRequestSilent.post('/api/meetings', meeting);

export const getPublicMeeting = (publicId) =>
  apiRequestSilent.get(`/api/meetings/${publicId}`);

export const getManagedMeeting = (publicId) =>
  apiRequestSilent.get(`/api/meetings/${publicId}/manage`);

export const updateMeeting = (publicId, meeting) =>
  apiRequestSilent.patch(`/api/meetings/${publicId}`, meeting);

export const updateMeetingStatus = (publicId, status) =>
  apiRequestSilent.patch(`/api/meetings/${publicId}/status`, { status });

export const deleteMeeting = (publicId) =>
  apiRequestSilent.delete(`/api/meetings/${publicId}`);

export const upsertAttendance = (publicId, attendance) =>
  apiRequestSilent.post(`/api/meetings/${publicId}/attendances`, attendance);

export const updateAttendance = (publicId, attendanceId, attendance) =>
  apiRequestSilent.patch(
    `/api/meetings/${publicId}/attendances/${attendanceId}`,
    attendance,
  );

export const deleteAttendance = (publicId, attendanceId) =>
  apiRequestSilent.delete(
    `/api/meetings/${publicId}/attendances/${attendanceId}`,
  );

export const createMeetingCompetition = (publicId) =>
  apiRequestSilent.post(`/api/meetings/${publicId}/competition`);

export const deleteMeetingCompetition = (publicId) =>
  apiRequestSilent.delete(`/api/meetings/${publicId}/competition`);
