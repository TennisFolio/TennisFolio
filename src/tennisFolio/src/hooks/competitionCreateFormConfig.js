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
  mode: COMPETITION_CREATE_MODES.FIXED_SCHEDULE,
  sameGenderDoublesOnly: false,
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
    payload.sameGenderDoublesOnly = form.sameGenderDoublesOnly === true;
  }

  return payload;
}

export function isSameGenderDoublesOnlyUnavailable(maleCount, femaleCount) {
  return isIncludedGenderBelowDoublesMinimum(maleCount)
    || isIncludedGenderBelowDoublesMinimum(femaleCount);
}

export function canAllocateSameGenderDoublesOnlyGames(
  maleCount,
  femaleCount,
  totalGames
) {
  const playerCount = maleCount + femaleCount;
  if (playerCount <= 0 || totalGames <= 0) {
    return false;
  }

  const totalSlots = totalGames * 4;
  const minGames = Math.floor(totalSlots / playerCount);
  const extraSlots = totalSlots - playerCount * minGames;
  const minMaleExtraSlots = Math.max(0, extraSlots - femaleCount);
  const maxMaleExtraSlots = Math.min(maleCount, extraSlots);

  for (
    let maleExtraSlots = minMaleExtraSlots;
    maleExtraSlots <= maxMaleExtraSlots;
    maleExtraSlots += 1
  ) {
    const femaleExtraSlots = extraSlots - maleExtraSlots;
    const maleSlots = maleCount * minGames + maleExtraSlots;
    const femaleSlots = femaleCount * minGames + femaleExtraSlots;
    const maleGames = maleSlots / 4;
    const femaleGames = femaleSlots / 4;

    if (
      maleSlots % 4 === 0
      && femaleSlots % 4 === 0
      && maleGames + femaleGames === totalGames
      && (maleGames === 0 || maleCount >= 4)
      && (femaleGames === 0 || femaleCount >= 4)
    ) {
      return true;
    }
  }

  return false;
}

export function getSameGenderDoublesOnlyUnavailableReason(form) {
  if (isSameGenderDoublesOnlyUnavailable(form.maleCount, form.femaleCount)) {
    return '남자 또는 여자가 1~3명일 때는 사용할 수 없어요.';
  }

  if (
    !canAllocateSameGenderDoublesOnlyGames(
      form.maleCount,
      form.femaleCount,
      form.totalGames
    )
  ) {
    return '남복/여복만으로는 현재 인원과 총 경기 수를 공정하게 배분할 수 없어요.';
  }

  return '';
}

function isIncludedGenderBelowDoublesMinimum(playerCount) {
  return playerCount > 0 && playerCount < 4;
}
