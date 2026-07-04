export function getRememberedAttendanceKey(publicId) {
  return `meetingPublic:${publicId}:attendance`;
}

export function forgetRememberedAttendance(publicId) {
  localStorage.removeItem(getRememberedAttendanceKey(publicId));
}

export function clearRememberedMeetingAttendances() {
  Object.keys(localStorage)
    .filter((key) => /^meetingPublic:[^:]+:attendance$/.test(key))
    .forEach((key) => localStorage.removeItem(key));
}
