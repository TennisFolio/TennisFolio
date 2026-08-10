let nextRowId = 1;

export function createBulkMemberRow(member = {}) {
  return {
    rowId: nextRowId++,
    name: member.name ?? '',
    gender: member.gender ?? 'MALE',
    role: member.role ?? 'MEMBER',
    skillTierId: member.skillTierId ?? null,
    invalidGender: member.invalidGender ?? false,
    invalidSkillTier: member.invalidSkillTier ?? false,
    memo: member.memo ?? '',
    contactMemo: member.contactMemo ?? '',
  };
}

export function createBulkMemberRows(count = 5) {
  return Array.from({ length: count }, () => createBulkMemberRow());
}

export function parseBulkMemberRows(text, skillTiers) {
  return text
    .split(/\r?\n/)
    .filter((line) => line.trim())
    .map((line) => {
      const [name = '', genderLabel = '', roleLabel = '', tierName = '', memo = '', contactMemo = ''] = line.split('\t');
      const gender = genderLabel.trim() === '여' ? 'FEMALE' : 'MALE';
      const normalizedGender = genderLabel.trim();
      const normalizedTierName = tierName.trim();
      const skillTier = skillTiers.find((tier) => tier.name === normalizedTierName);

      return createBulkMemberRow({
        name: name.trim(),
        gender,
        role: roleLabel.trim() === '관리자' ? 'ADMIN' : 'MEMBER',
        skillTierId: skillTier?.id ?? null,
        invalidGender: Boolean(normalizedGender) && !['남', '여'].includes(normalizedGender),
        invalidSkillTier: false,
        memo: memo.trim(),
        contactMemo: contactMemo.trim(),
      });
    });
}

export function validateBulkMembers(rows, existingMembers) {
  const existingNames = new Set(existingMembers.map((member) => member.name?.trim()).filter(Boolean));
  const inputNames = new Set();

  return rows.map((row) => {
    const name = row.name.trim();
    const errors = [];
    const hasInput = Boolean(
      name ||
        row.memo.trim() ||
        row.contactMemo.trim() ||
        row.skillTierId ||
        row.role !== 'MEMBER' ||
        row.gender !== 'MALE' ||
        row.invalidGender ||
        row.invalidSkillTier,
    );

    if (hasInput && !name) errors.push('이름 필수');
    if (row.invalidGender) errors.push('성별은 남 또는 여');
    if (row.invalidSkillTier) errors.push('유효하지 않은 등급');
    if (name && inputNames.has(name)) errors.push('입력 명단 내 이름 중복');
    if (name && existingNames.has(name)) errors.push('이미 등록된 클럽원');
    if (name) inputNames.add(name);

    return { ...row, name, errors };
  });
}

export function bulkMemberPayload(rows) {
  return rows.map((row) => ({
    name: row.name.trim(),
    gender: row.gender,
    role: row.role,
    skillTierId: row.skillTierId ? Number(row.skillTierId) : null,
    memo: row.memo.trim() || null,
    contactMemo: row.contactMemo.trim() || null,
  }));
}
