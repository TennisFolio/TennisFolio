let nextRowId = 1;

const GENDER_BY_LABEL = { 남: 'MALE', 남성: 'MALE', MALE: 'MALE', 여: 'FEMALE', 여성: 'FEMALE', FEMALE: 'FEMALE' };
const PASTE_HEADERS = ['이름', '성별', '티어', '가입일'];

export function createBulkMemberRow(member = {}) {
  return { rowId: nextRowId++, name: member.name ?? '', gender: member.gender ?? 'MALE', skillTierId: member.skillTierId ?? null, joinedAt: member.joinedAt ?? '', invalidGender: member.invalidGender ?? false, invalidSkillTier: member.invalidSkillTier ?? false, invalidJoinedAt: member.invalidJoinedAt ?? false, invalidColumnCount: member.invalidColumnCount ?? false };
}

export function createBulkMemberRows(count = 1) { return Array.from({ length: count }, () => createBulkMemberRow()); }

export function parseBulkMemberRows(text, skillTiers) {
  const lines = text.split(/\r?\n/).filter((line) => line.trim());
  const dataLines = isHeaderRow(lines[0]) ? lines.slice(1) : lines;
  return dataLines.map((line) => {
    const cells = line.split('\t');
    const [name = '', genderLabel = '', tierName = '', joinedAtInput = ''] = cells.map((cell) => cell.trim());
    const gender = GENDER_BY_LABEL[genderLabel];
    const skillTier = skillTiers.find((tier) => tier.name.trim() === tierName);
    const joinedAt = normalizeJoinedAt(joinedAtInput);
    return createBulkMemberRow({ name, gender: gender ?? 'MALE', skillTierId: skillTier?.id ?? null, joinedAt: joinedAt ?? joinedAtInput, invalidGender: !gender, invalidSkillTier: Boolean(tierName) && !skillTier, invalidJoinedAt: Boolean(joinedAtInput) && !joinedAt, invalidColumnCount: cells.length < 3 || cells.length > 4 });
  });
}

export function validateBulkMembers(rows, existingMembers) {
  const existingNames = new Set(existingMembers.map((member) => member.name?.trim()).filter(Boolean));
  const inputNames = new Set();
  return rows.map((row) => {
    const name = row.name.trim();
    const errors = [];
    const hasInput = Boolean(name || row.skillTierId || row.joinedAt || row.gender !== 'MALE' || row.invalidGender || row.invalidSkillTier || row.invalidJoinedAt || row.invalidColumnCount);
    if (hasInput && !name) errors.push('이름은 필수입니다.');
    if (row.invalidColumnCount) errors.push('열은 이름, 성별, 티어, 가입일 순서로 4개여야 합니다.');
    if (row.invalidGender) errors.push('성별은 남 또는 여로 입력해 주세요.');
    if (row.invalidSkillTier) errors.push('등록되지 않은 티어입니다.');
    if (row.invalidJoinedAt) errors.push('가입일은 YYYY-MM-DD 형식이어야 합니다.');
    if (name && inputNames.has(name)) errors.push('입력 명단에 같은 이름이 있습니다.');
    if (name && existingNames.has(name)) errors.push('이미 등록된 이름입니다.');
    if (name) inputNames.add(name);
    return { ...row, name, errors };
  });
}

export function bulkMemberPayload(rows) {
  return rows.map((row) => ({ name: row.name.trim(), gender: row.gender, skillTierId: row.skillTierId ? Number(row.skillTierId) : null, joinedAt: row.joinedAt || null, memo: null, contactMemo: null }));
}

function isHeaderRow(line) {
  return Boolean(line) && line
    .split('\t')
    .map((cell) => cell.trim().replace(/^\uFEFF/, ''))
    .every((cell, index) => cell === PASTE_HEADERS[index]);
}

function normalizeJoinedAt(value) {
  if (!value) return '';
  const matched = value.match(/^(\d{4})\s*(?:-|\.|\/)\s*(\d{1,2})\s*(?:-|\.|\/)\s*(\d{1,2})\.?$/)
    ?? value.match(/^(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일$/);
  if (!matched) return null;
  const normalized = `${matched[1]}-${matched[2].padStart(2, '0')}-${matched[3].padStart(2, '0')}`;
  return isIsoDate(normalized) ? normalized : null;
}

function isIsoDate(value) { if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false; const date = new Date(`${value}T00:00:00Z`); return !Number.isNaN(date.getTime()) && date.toISOString().slice(0, 10) === value; }
