import { useMemo, useState } from 'react';
import { apiRequest } from '../utils/apiClient';

const GAMES_PER_HOUR = 2;

export const COMPETITION_FIELD_LIMITS = {
  maleCount: { min: 0, max: 40 },
  femaleCount: { min: 0, max: 40 },
  courtCount: { min: 1, max: 10 },
  hours: { min: 1, max: 10 },
};

export const COMPETITION_FIELDS = [
  {
    name: 'maleCount',
    label: '남자 인원',
    unit: '명',
  },
  {
    name: 'femaleCount',
    label: '여자 인원',
    unit: '명',
  },
  {
    name: 'courtCount',
    label: '코트 수',
    unit: '개',
  },
  {
    name: 'hours',
    label: '진행 시간',
    unit: '시간',
  },
];

const INITIAL_COMPETITION_FORM = {
  maleCount: 4,
  femaleCount: 4,
  courtCount: 2,
  hours: 2,
};

function normalizeCompetitionValue(name, value) {
  const numberValue = Number(value);
  if (Number.isNaN(numberValue)) {
    return COMPETITION_FIELD_LIMITS[name].min;
  }

  return Math.min(
    COMPETITION_FIELD_LIMITS[name].max,
    Math.max(COMPETITION_FIELD_LIMITS[name].min, Math.trunc(numberValue))
  );
}

export function useCompetitionCreateForm() {
  const [competitionForm, setCompetitionForm] = useState(
    INITIAL_COMPETITION_FORM
  );
  const [competitionError, setCompetitionError] = useState('');
  const [competitionResult, setCompetitionResult] = useState(null);
  const [isCreatingCompetition, setIsCreatingCompetition] = useState(false);

  const totalPlayers = competitionForm.maleCount + competitionForm.femaleCount;
  const totalGames =
    competitionForm.courtCount * competitionForm.hours * GAMES_PER_HOUR;
  const totalGameSlots = totalGames * 4;
  const minimumPlayers = competitionForm.courtCount * 4;
  const canCreateGames = totalPlayers >= 4 && totalPlayers >= minimumPlayers;

  const placementText = useMemo(() => {
    if (canCreateGames) {
      return '이 조건이면 문제없이 진행돼요';
    }
    return '인원을 늘리거나 코트를 줄이면 좋아요';
  }, [canCreateGames]);

  const expectedGamesText = useMemo(() => {
    if (!canCreateGames) {
      return '경기 불가';
    }

    const minGames = Math.floor(totalGameSlots / totalPlayers);
    const maxGames = Math.ceil(totalGameSlots / totalPlayers);

    if (minGames === maxGames) {
      return `${minGames}경기`;
    }

    return `${minGames}~${maxGames}경기`;
  }, [canCreateGames, totalGameSlots, totalPlayers]);

  const participantGameText = canCreateGames
    ? `${expectedGamesText}씩`
    : '아직 어려워요';
  const unavailableReasonText = `필요 인원 ${minimumPlayers}명, 현재 ${totalPlayers}명`;

  const resetCompetitionFeedback = () => {
    setCompetitionError('');
    setCompetitionResult(null);
  };

  const updateCompetitionField = (name, value) => {
    setCompetitionForm((prev) => ({
      ...prev,
      [name]: normalizeCompetitionValue(name, value),
    }));
    resetCompetitionFeedback();
  };

  const stepCompetitionField = (name, amount) => {
    setCompetitionForm((prev) => ({
      ...prev,
      [name]: normalizeCompetitionValue(name, prev[name] + amount),
    }));
    resetCompetitionFeedback();
  };

  const validateCompetitionForm = () => {
    if (totalPlayers < 4) {
      return '최소 4명은 있어야 경기를 만들 수 있어요.';
    }
    if (totalPlayers > 40) {
      return '전체 인원은 40명까지만 받을 수 있어요.';
    }
    if (competitionForm.courtCount < 1 || competitionForm.courtCount > 10) {
      return '코트는 1개부터 10개까지만 입력해 주세요.';
    }
    if (competitionForm.hours < 1 || competitionForm.hours > 10) {
      return '진행 시간은 1시간부터 10시간까지 선택할 수 있어요.';
    }
    if (totalPlayers < minimumPlayers) {
      return '코트 1개당 최소 4명이 필요해요.';
    }

    return '';
  };

  const createCompetition = async () => {
    const errorMessage = validateCompetitionForm();
    if (errorMessage) {
      setCompetitionError(errorMessage);
      return;
    }

    try {
      setIsCreatingCompetition(true);
      setCompetitionError('');
      setCompetitionResult(null);

      const response = await apiRequest.post('/api/competitions', {
        competitionName: 'TennisFolio 대진표',
        ...competitionForm,
      });

      if (response.data.code === '0000') {
        setCompetitionResult(response.data.data);
      } else {
        setCompetitionError('대진표를 만들지 못했어요. 잠시 후 다시 시도해 주세요.');
      }
    } catch (error) {
      const message =
        error.response?.data?.message ||
        '대진표를 만드는 중 문제가 생겼어요. 잠시 후 다시 시도해 주세요.';
      setCompetitionError(message);
    } finally {
      setIsCreatingCompetition(false);
    }
  };

  return {
    competitionForm,
    competitionError,
    competitionResult,
    isCreatingCompetition,
    totalPlayers,
    canCreateGames,
    placementText,
    participantGameText,
    unavailableReasonText,
    updateCompetitionField,
    stepCompetitionField,
    createCompetition,
  };
}
