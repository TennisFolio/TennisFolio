export function toNumberOrNull(value) {
  return value === '' ? null : Number(value);
}

export function buildDateTime(date, time) {
  return `${date}T${time}:00`;
}

export function toDateInputValue(value) {
  return value ? value.slice(0, 10) : '';
}

export function toTimeInputValue(value) {
  return value ? value.slice(11, 16) : '';
}

export function getQuotaMode(meeting) {
  if (meeting.maxParticipants != null) {
    return 'TOTAL';
  }
  if (meeting.maxMaleParticipants != null || meeting.maxFemaleParticipants != null) {
    return 'GENDER';
  }
  return 'NONE';
}

export function toMeetingForm(meeting, defaults = {}) {
  return {
    title: meeting.title || '',
    date: toDateInputValue(meeting.startAt),
    startTime: toTimeInputValue(meeting.startAt),
    endTime: toTimeInputValue(meeting.endAt),
    note: meeting.note || '',
    quotaMode: getQuotaMode(meeting),
    maxParticipants: meeting.maxParticipants?.toString() || '',
    maxMaleParticipants: meeting.maxMaleParticipants?.toString() || '',
    maxFemaleParticipants: meeting.maxFemaleParticipants?.toString() || '',
    courtCount: meeting.courtCount?.toString() || defaults.courtCount || '1',
    totalGames: meeting.totalGames?.toString() || defaults.totalGames || '1',
  };
}
