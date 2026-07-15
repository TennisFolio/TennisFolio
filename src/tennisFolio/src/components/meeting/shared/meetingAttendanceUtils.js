export const emptyAttendance = {
  attendanceId: '',
  participantName: '',
  gender: 'MALE',
  attendanceStatus: 'ATTENDING',
};

export const statusLabels = {
  ATTENDING: '참석',
  WAITING: '대기',
  NOT_ATTENDING: '불참',
};

export function normalizeAttendances(meeting) {
  return meeting?.attendances || meeting?.attendanceResponses || [];
}

export function getAttendanceForm(attendance) {
  return {
    attendanceId: attendance.id || '',
    participantName: attendance.participantName,
    gender: attendance.gender,
    attendanceStatus: attendance.attendanceStatus,
  };
}

export function findCurrentUserAttendance(currentUser, meeting, attendances) {
  const currentUserAttendanceId = meeting?.currentUserAttendanceId;
  if (!currentUser || !currentUserAttendanceId) {
    return null;
  }

  return attendances.find(
    (attendance) => Number(attendance.id) === Number(currentUserAttendanceId),
  );
}

export function getCurrentUserForm(currentUser, meeting) {
  const clubMemberName = meeting?.currentClubMemberName?.trim();

  if (clubMemberName) {
    return {
      ...emptyAttendance,
      participantName: clubMemberName,
      gender: meeting.currentClubMemberGender || emptyAttendance.gender,
    };
  }

  const nickName = currentUser?.nickName?.trim();

  if (!nickName) {
    return null;
  }

  return {
    ...emptyAttendance,
    participantName: nickName,
    gender: currentUser.gender || emptyAttendance.gender,
  };
}

export function countByStatus(attendances, status) {
  return attendances.filter((attendance) => attendance.attendanceStatus === status).length;
}

function countByStatusAndGender(attendances, status, gender) {
  return attendances.filter(
    (attendance) =>
      attendance.attendanceStatus === status && attendance.gender === gender,
  ).length;
}

export function getCapacityChips(meeting, attendances) {
  const attendingCount = countByStatus(attendances, 'ATTENDING');
  const maleCount = countByStatusAndGender(attendances, 'ATTENDING', 'MALE');
  const femaleCount = countByStatusAndGender(attendances, 'ATTENDING', 'FEMALE');

  if (meeting.maxParticipants) {
    return [`정원 ${attendingCount}/${meeting.maxParticipants}`];
  }

  const capacityChips = [];

  if (meeting.maxMaleParticipants) {
    capacityChips.push(`남성 ${maleCount}/${meeting.maxMaleParticipants}`);
  }

  if (meeting.maxFemaleParticipants) {
    capacityChips.push(`여성 ${femaleCount}/${meeting.maxFemaleParticipants}`);
  }

  return capacityChips.length > 0 ? capacityChips : ['정원 제한 없음'];
}

export function formatDate(startAt) {
  if (!startAt) {
    return '-';
  }
  const date = new Date(startAt);
  return `${date.getMonth() + 1}월 ${date.getDate()}일`;
}

export function formatTimeRange(startAt, endAt) {
  if (!startAt || !endAt) {
    return '-';
  }
  const formatTime = (value) =>
    new Date(value).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });
  return `${formatTime(startAt)}-${formatTime(endAt)}`;
}

export function groupAttendances(attendances) {
  return {
    attendingMale: attendances.filter(
      (attendance) =>
        attendance.attendanceStatus === 'ATTENDING' && attendance.gender === 'MALE',
    ),
    attendingFemale: attendances.filter(
      (attendance) =>
        attendance.attendanceStatus === 'ATTENDING' && attendance.gender === 'FEMALE',
    ),
    waiting: attendances.filter(
      (attendance) => attendance.attendanceStatus === 'WAITING',
    ),
    notAttending: attendances.filter(
      (attendance) => attendance.attendanceStatus === 'NOT_ATTENDING',
    ),
  };
}

export function isOwnerAttendance(attendance, meeting) {
  const ownerNickName = meeting?.ownerNickName?.trim();
  return Boolean(ownerNickName) && attendance.participantName === ownerNickName;
}
