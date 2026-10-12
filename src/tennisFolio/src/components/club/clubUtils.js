export function roleLabel(role) {
  if (role === 'OWNER') return 'OWNER';
  if (role === 'ADMIN') return 'ADMIN';
  return '클럽원';
}

export function isClubOperator(club) {
  return club?.admin === true || ['OWNER', 'ADMIN'].includes(club?.role);
}

export function genderLabel(gender) {
  return gender === 'FEMALE' ? '여성' : '남성';
}

export function formatMeetingDate(startAt) {
  if (!startAt) {
    return '-';
  }

  const date = new Date(startAt);
  return `${date.getMonth() + 1}월 ${date.getDate()}일`;
}

export function sortClubMembers(members = []) {
  return [...members].sort((first, second) => Number(first.id ?? 0) - Number(second.id ?? 0));
}
