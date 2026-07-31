export const emptyClubForm = {
  name: '',
  description: '',
  skillTiers: [],
};

export const emptyMemberForm = {
  name: '',
  gender: 'MALE',
  role: 'MEMBER',
  skillTierId: null,
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
    skillTiers: normalizeSkillTiers(club.skillTiers),
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
    name,
    gender: form.gender,
    role: form.role,
    skillTierId: form.skillTierId ? Number(form.skillTierId) : null,
    contactMemo: form.contactMemo.trim() || null,
    memo: form.memo.trim() || null,
  };
}

export function normalizeSkillTiers(skillTiers) {
  if (!Array.isArray(skillTiers)) {
    return [];
  }

  return skillTiers
    .map((skillTier) => ({
      id: skillTier.id ?? null,
      name: skillTier.name ?? '',
      level: skillTier.level ?? 0,
    }))
    .sort((left, right) => right.level - left.level);
}

export function clubPayload(form, name) {
  return {
    name,
    description: form.description.trim(),
    skillTiers: form.skillTiers.map((skillTier) => ({
      ...(skillTier.id != null ? { id: skillTier.id } : {}),
      name: skillTier.name.trim(),
    })),
  };
}

export function skillTierValidationMessage(skillTiers) {
  const names = skillTiers.map((skillTier) => skillTier.name.trim());
  if (names.some((name) => !name)) {
    return '등급 이름을 입력해 주세요.';
  }
  if (names.some((name) => name.length > 10)) {
    return '등급 이름은 10자까지 입력할 수 있습니다.';
  }
  if (new Set(names.map((name) => name.toLocaleLowerCase())).size !== names.length) {
    return '등급 이름은 중복될 수 없습니다.';
  }
  return '';
}
