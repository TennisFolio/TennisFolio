export function roleLabel(role) {
  return role === 'ADMIN' ? '관리자' : '클럽원';
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

export function meetingStateLabel(meeting) {
  if (meeting.competitionCreated) {
    return '경기표 생성';
  }
  if (meeting.status === 'CLOSED') {
    return '마감';
  }
  if (meeting.status === 'CANCELLED') {
    return '취소';
  }
  return '경기표 전';
}

export function sortClubMembers(members = []) {
  const creator = members
    .filter((member) => member.role === 'ADMIN')
    .sort((first, second) => {
      const firstHasUser = first.userId == null ? 1 : 0;
      const secondHasUser = second.userId == null ? 1 : 0;

      if (firstHasUser !== secondHasUser) {
        return firstHasUser - secondHasUser;
      }

      return Number(first.id ?? 0) - Number(second.id ?? 0);
    })[0];

  const creatorId = creator?.id;

  return [...members].sort((first, second) => {
    if (first.id === creatorId && second.id !== creatorId) {
      return -1;
    }
    if (second.id === creatorId && first.id !== creatorId) {
      return 1;
    }

    const roleRank = { ADMIN: 0, MEMBER: 1 };
    const firstRank = roleRank[first.role] ?? 2;
    const secondRank = roleRank[second.role] ?? 2;

    if (firstRank !== secondRank) {
      return firstRank - secondRank;
    }

    return Number(first.id ?? 0) - Number(second.id ?? 0);
  });
}
