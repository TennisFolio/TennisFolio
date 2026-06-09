import { createDefaultPlayerNames } from './competitionCreateFormNames.js';

export const COMPETITION_CREATE_MODES = {
  CLUB_SESSION: 'CLUB_SESSION',
  FIXED_SCHEDULE: 'FIXED_SCHEDULE',
};

export const COMPETITION_FIELD_LIMITS = {
  maleCount: { min: 0, max: 40 },
  femaleCount: { min: 0, max: 40 },
  courtCount: { min: 1, max: 10 },
  totalGames: { min: 1, max: 200 },
};

export const COMPETITION_FIELDS = [
  {
    name: 'maleCount',
    label: '남자 인원',
  },
  {
    name: 'femaleCount',
    label: '여자 인원',
  },
  {
    name: 'courtCount',
    label: '코트 수',
  },
  {
    name: 'totalGames',
    label: '총 경기 수',
    unit: '게임',
  },
];

export const INITIAL_COMPETITION_FORM = {
  mode: COMPETITION_CREATE_MODES.CLUB_SESSION,
  maleCount: 4,
  femaleCount: 4,
  courtCount: 2,
  totalGames: 8,
  malePlayerNames: createDefaultPlayerNames('M', 4),
  femalePlayerNames: createDefaultPlayerNames('F', 4),
};

export function createCompetitionPayload({ form, isClubSession }) {
  const payload = {
    competitionName: isClubSession
      ? '진행형 Tennisfolio 경기'
      : 'TennisFolio 대진표',
    mode: form.mode,
    maleCount: form.maleCount,
    femaleCount: form.femaleCount,
    courtCount: form.courtCount,
    malePlayerNames: form.malePlayerNames,
    femalePlayerNames: form.femalePlayerNames,
  };

  if (!isClubSession) {
    payload.totalGames = form.totalGames;
  }

  return payload;
}
