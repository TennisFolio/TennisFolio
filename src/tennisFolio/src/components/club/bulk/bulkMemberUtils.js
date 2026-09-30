let nextRowId = 1;

const GENDER_BY_LABEL = { 남: 'MALE', 남성: 'MALE', MALE: 'MALE', 여: 'FEMALE', 여성: 'FEMALE', FEMALE: 'FEMALE' };
const ROLE_BY_LABEL = { 관리자: 'ADMIN', ADMIN: 'ADMIN', 회원: 'MEMBER', 일반: 'MEMBER', MEMBER: 'MEMBER' };
const PASTE_HEADERS = ['이름', '성별', '역할', '티어', '가입일'];

export function createBulkMemberRow(member = {}) {
  return { rowId: nextRowId++, name: member.name ?? '', gender: member.gender ?? 'MALE', role: member.role ?? 'MEMBER', skillTierId: member.skillTierId ?? null, joinedAt: member.joinedAt ?? '', invalidGender: member.invalidGender ?? false, invalidRole: member.invalidRole ?? false, invalidSkillTier: member.invalidSkillTier ?? false, invalidJoinedAt: member.invalidJoinedAt ?? false, invalidColumnCount: member.invalidColumnCount ?? false };
}

export function createBulkMemberRows(count = 1) { return Array.from({ length: count }, () => createBulkMemberRow()); }

export function parseBulkMemberRows(text, skillTiers) {
  const lines = text.split(/\r?\n/).filter((line) => line.trim());
  const dataLines = isHeaderRow(lines[0]) ? lines.slice(1) : lines;
  return dataLines.map((line) => {
    const cells = line.split('\t');
    const [name = '', genderLabel = '', roleLabel = '', tierName = '', joinedAt = ''] = cells.map((cell) => cell.trim());
    const gender = GENDER_BY_LABEL[genderLabel];
    const role = ROLE_BY_LABEL[roleLabel || '회원'];
    const skillTier = skillTiers.find((tier) => tier.name.trim() === tierName);
    return createBulkMemberRow({ name, gender: gender ?? 'MALE', role: role ?? 'MEMBER', skillTierId: skillTier?.id ?? null, joinedAt, invalidGender: !gender, invalidRole: !role, invalidSkillTier: Boolean(tierName) && !skillTier, invalidJoinedAt: Boolean(joinedAt) && !isIsoDate(joinedAt), invalidColumnCount: cells.length !== 5 });
  });
}

export function validateBulkMembers(rows, existingMembers) {
  const existingNames = new Set(existingMembers.map((member) => member.name?.trim()).filter(Boolean));
  const inputNames = new Set();
  return rows.map((row) => {
    const name = row.name.trim();
    const errors = [];
    const hasInput = Boolean(name || row.skillTierId || row.joinedAt || row.role !== 'MEMBER' || row.gender !== 'MALE' || row.invalidGender || row.invalidRole || row.invalidSkillTier || row.invalidJoinedAt || row.invalidColumnCount);
    if (hasInput && !name) errors.push('이름은 필수입니다.');
    if (row.invalidColumnCount) errors.push('열은 이름, 성별, 역할, 티어, 가입일 순서로 5개여야 합니다.');
    if (row.invalidGender) errors.push('성별은 남 또는 여로 입력해 주세요.');
    if (row.invalidRole) errors.push('역할은 관리자 또는 회원으로 입력해 주세요.');
    if (row.invalidSkillTier) errors.push('등록되지 않은 티어입니다.');
    if (row.invalidJoinedAt) errors.push('가입일은 YYYY-MM-DD 형식이어야 합니다.');
    if (name && inputNames.has(name)) errors.push('입력 명단에 같은 이름이 있습니다.');
    if (name && existingNames.has(name)) errors.push('이미 등록된 이름입니다.');
    if (name) inputNames.add(name);
    return { ...row, name, errors };
  });
}

export function bulkMemberPayload(rows) {
  return rows.map((row) => ({ name: row.name.trim(), gender: row.gender, role: row.role, skillTierId: row.skillTierId ? Number(row.skillTierId) : null, joinedAt: row.joinedAt || null, memo: null, contactMemo: null }));
}

function isHeaderRow(line) {
  return Boolean(line) && line
    .split('\t')
    .map((cell) => cell.trim().replace(/^\uFEFF/, ''))
    .every((cell, index) => cell === PASTE_HEADERS[index]);
}
function isIsoDate(value) { if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false; const date = new Date(`${value}T00:00:00Z`); return !Number.isNaN(date.getTime()) && date.toISOString().slice(0, 10) === value; }
