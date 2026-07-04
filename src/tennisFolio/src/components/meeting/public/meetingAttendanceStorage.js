function getRememberedAttendanceKey(publicId) {
  return `meetingPublic:${publicId}:attendance`;
}

export function forgetRememberedAttendance(publicId) {
  localStorage.removeItem(getRememberedAttendanceKey(publicId));
}

export function rememberAttendance(publicId, attendance) {
  const attendanceId = attendance?.id || attendance?.attendanceId;

  if (!attendanceId) {
    return;
  }

  localStorage.setItem(
    getRememberedAttendanceKey(publicId),
    JSON.stringify({
      attendanceId,
      participantName: attendance.participantName,
    }),
  );
}

export function findRememberedAttendance(publicId, attendances) {
  const rememberedValue = localStorage.getItem(getRememberedAttendanceKey(publicId));

  if (!rememberedValue) {
    return null;
  }

  try {
    const remembered = JSON.parse(rememberedValue);
    const attendance = attendances.find(
      (attendance) => attendance.id === remembered.attendanceId,
    );

    if (attendance) {
      return attendance;
    }
  } catch {
    // Invalid localStorage values are treated the same as stale attendance ids.
  }

  forgetRememberedAttendance(publicId);
  return null;
}
