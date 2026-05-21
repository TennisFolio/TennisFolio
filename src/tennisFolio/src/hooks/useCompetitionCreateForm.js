import { useMemo, useRef, useState } from 'react';
import { apiRequest } from '../utils/apiClient';
import {
  incrementSessionCompetitionCreateCount,
  trackEvent,
} from '../utils/analytics';

const GAMES_PER_HOUR = 2;

export const COMPETITION_CREATE_MODES = {
  CLUB_SESSION: 'CLUB_SESSION',
  FIXED_SCHEDULE: 'FIXED_SCHEDULE',
};

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
    name: 'hours',
    label: '진행 시간',
    unit: '시간',
  },
];

const INITIAL_COMPETITION_FORM = {
  mode: COMPETITION_CREATE_MODES.CLUB_SESSION,
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
  const touchedFieldsRef = useRef(new Set());

  const totalPlayers = competitionForm.maleCount + competitionForm.femaleCount;
  const isClubSession =
    competitionForm.mode === COMPETITION_CREATE_MODES.CLUB_SESSION;
  const totalGames =
    competitionForm.courtCount *
    (isClubSession ? 1 : competitionForm.hours * GAMES_PER_HOUR);
  const totalGameSlots = totalGames * 4;
  const minimumPlayers = competitionForm.courtCount * 4;
  const canCreateGames = totalPlayers >= 4 && totalPlayers >= minimumPlayers;

  const placementText = useMemo(() => {
    if (canCreateGames) {
      return isClubSession
        ? '대기 경기를 만들고 코트별로 운영해요'
        : '이 조건이면 바로 대진표를 만들 수 있어요';
    }
    return '인원을 늘리거나 코트를 줄이면 만들 수 있어요';
  }, [canCreateGames, isClubSession]);

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
    ? isClubSession
      ? '대기 경기 순차 생성'
      : `${expectedGamesText} 예상`
    : '아직 어려워요';
  const unavailableReasonText = `필요 인원 ${minimumPlayers}명, 현재 ${totalPlayers}명`;

  const resetCompetitionFeedback = () => {
    setCompetitionError('');
    setCompetitionResult(null);
  };

  const trackFieldInteraction = (name, interactionType) => {
    if (touchedFieldsRef.current.has(name)) {
      return;
    }

    touchedFieldsRef.current.add(name);
    trackEvent('competition_create_field_interaction', {
      field_name: name,
      interaction_type: interactionType,
    });
    trackEvent('competition_create_funnel_step', {
      funnel_step: 'field_interaction',
      field_name: name,
    });
  };

  const updateCompetitionField = (name, value) => {
    if (name === 'mode') {
      setCompetitionForm((prev) => ({
        ...prev,
        mode: value,
      }));
      resetCompetitionFeedback();
      return;
    }

    trackFieldInteraction(name, 'input');
    setCompetitionForm((prev) => ({
      ...prev,
      [name]: normalizeCompetitionValue(name, value),
    }));
    resetCompetitionFeedback();
  };

  const stepCompetitionField = (name, amount) => {
    trackFieldInteraction(name, 'stepper');
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
      return '코트는 1개부터 10개까지 입력해 주세요.';
    }
    if (
      !isClubSession &&
      (competitionForm.hours < 1 || competitionForm.hours > 10)
    ) {
      return '진행 시간은 1시간부터 10시간까지 선택할 수 있어요.';
    }
    if (totalPlayers < minimumPlayers) {
      return '코트 1개당 최소 4명이 필요해요.';
    }

    return '';
  };

  const createCompetition = async () => {
    const analyticsPayload = {
      male_count: competitionForm.maleCount,
      female_count: competitionForm.femaleCount,
      total_players: totalPlayers,
      court_count: competitionForm.courtCount,
      hours: isClubSession ? undefined : competitionForm.hours,
      mode: competitionForm.mode,
      can_create_games: canCreateGames,
    };

    trackEvent('competition_create_attempt', analyticsPayload);
    trackEvent('competition_create_funnel_step', {
      ...analyticsPayload,
      funnel_step: 'submit',
    });

    const errorMessage = validateCompetitionForm();
    if (errorMessage) {
      setCompetitionError(errorMessage);
      trackEvent('competition_create_failure', {
        ...analyticsPayload,
        failure_type: 'validation',
        failure_reason: errorMessage,
      });
      return null;
    }

    try {
      setIsCreatingCompetition(true);
      setCompetitionError('');
      setCompetitionResult(null);

      const payload = {
        competitionName: isClubSession
          ? '진행형 Tennisfolio 경기'
          : 'TennisFolio 대진표',
        mode: competitionForm.mode,
        maleCount: competitionForm.maleCount,
        femaleCount: competitionForm.femaleCount,
        courtCount: competitionForm.courtCount,
      };

      if (!isClubSession) {
        payload.hours = competitionForm.hours;
      }

      const response = await apiRequest.post('/api/competitions', payload);

      if (response.data.code === '0000') {
        setCompetitionResult(response.data.data);
        const sessionCreateCount = incrementSessionCompetitionCreateCount();
        trackEvent('competition_create_success', {
          ...analyticsPayload,
          public_id: response.data.data.publicId,
          session_competition_create_count: sessionCreateCount,
        });
        trackEvent('competition_create_funnel_step', {
          ...analyticsPayload,
          public_id: response.data.data.publicId,
          funnel_step: 'created',
        });
        return response.data.data;
      }

      setCompetitionError(
        '대진표를 만들지 못했어요. 잠시 후 다시 시도해 주세요.'
      );
      trackEvent('competition_create_failure', {
        ...analyticsPayload,
        failure_type: 'api_code',
        failure_reason: response.data.message,
      });
      return null;
    } catch (error) {
      const message =
        error.response?.data?.message ||
        '대진표를 만드는 중 문제가 생겼어요. 잠시 후 다시 시도해 주세요.';
      setCompetitionError(message);
      trackEvent('competition_create_failure', {
        ...analyticsPayload,
        failure_type: 'network_or_server',
        failure_reason: message,
        status_code: error.response?.status,
      });
      return null;
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
    isClubSession,
    placementText,
    participantGameText,
    unavailableReasonText,
    updateCompetitionField,
    stepCompetitionField,
    createCompetition,
  };
}
