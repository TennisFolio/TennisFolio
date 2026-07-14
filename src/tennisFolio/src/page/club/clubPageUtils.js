export const emptyClubForm = {
  name: '',
  description: '',
};

export const emptyMemberForm = {
  name: '',
  gender: 'MALE',
  role: 'MEMBER',
  skillNote: '',
  contactMemo: '',
  memo: '',
};

export function unwrapData(response, fallback) {
  return response?.data?.data ?? fallback;
}

export function normalizeClub(club) {
  if (!club) {
    return null;
  }

  return {
    ...club,
    role: club.role ?? club.currentUserRole,
    memberCount: club.memberCount ?? club.activeMemberCount ?? 0,
  };
}

export function errorMessage(error, fallback) {
  const status = error?.response?.status;
  const serverMessage = error?.response?.data?.message;

  if (serverMessage) {
    return serverMessage;
  }
  if (status === 401) {
    return '로그인이 필요합니다.';
  }
  if (status === 403) {
    return '관리자만 실행할 수 있습니다.';
  }
  if (status === 409) {
    return '현재 상태에서는 실행할 수 없습니다.';
  }

  return fallback;
}

export function memberPayload(form, name) {
  return {
    ...form,
    name,
    skillNote: form.skillNote.trim() || null,
    contactMemo: form.contactMemo.trim() || null,
    memo: form.memo.trim() || null,
  };
}
