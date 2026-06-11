import { useMemo, useRef, useState } from 'react';
import { apiRequest } from '../utils/apiClient';
import {
  incrementSessionCompetitionCreateCount,
  trackEvent,
} from '../utils/analytics';
import {
  hasInvalidPlayerNameLength,
  syncPlayerNames,
} from './competitionCreateFormNames';
import {
  COMPETITION_CREATE_MODES,
  COMPETITION_FIELD_LIMITS,
  COMPETITION_FIELDS,
  createCompetitionPayload,
  getSameGenderDoublesOnlyUnavailableReason,
  INITIAL_COMPETITION_FORM,
} from './competitionCreateFormConfig';

export {
  COMPETITION_CREATE_MODES,
  COMPETITION_FIELD_LIMITS,
  COMPETITION_FIELDS,
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
    isClubSession ? competitionForm.courtCount : competitionForm.totalGames;
  const totalGameSlots = totalGames * 4;
  const minimumPlayers = competitionForm.courtCount * 4;
  const canCreateGames = totalPlayers >= 4 && totalPlayers >= minimumPlayers;
  const sameGenderDoublesOnlyUnavailableReason =
    getSameGenderDoublesOnlyUnavailableReason(competitionForm);
  const sameGenderDoublesOnlyUnavailable = Boolean(
    sameGenderDoublesOnlyUnavailableReason
  );
  const canSubmitCompetition =
    canCreateGames
    && !(
      !isClubSession
      && competitionForm.sameGenderDoublesOnly
      && sameGenderDoublesOnlyUnavailable
    );

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

    if (name === 'sameGenderDoublesOnly') {
      setCompetitionForm((prev) => ({
        ...prev,
        sameGenderDoublesOnly: value === true,
      }));
      resetCompetitionFeedback();
      return;
    }

    trackFieldInteraction(name, 'input');
    setCompetitionForm((prev) => ({
      ...prev,
      ...createCompetitionFormUpdate(
        prev,
        name,
        normalizeCompetitionValue(name, value)
      ),
    }));
    resetCompetitionFeedback();
  };

  const stepCompetitionField = (name, amount) => {
    trackFieldInteraction(name, 'stepper');
    setCompetitionForm((prev) => ({
      ...prev,
      ...createCompetitionFormUpdate(
        prev,
        name,
        normalizeCompetitionValue(name, prev[name] + amount)
      ),
    }));
    resetCompetitionFeedback();
  };

  const updateCompetitionPlayerName = (gender, index, value) => {
    const fieldName =
      gender === 'MALE' ? 'malePlayerNames' : 'femalePlayerNames';
    setCompetitionForm((prev) => ({
      ...prev,
      [fieldName]: prev[fieldName].map((playerName, playerIndex) =>
        playerIndex === index ? value : playerName
      ),
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
      (competitionForm.totalGames < 1 ||
        competitionForm.totalGames > competitionForm.courtCount * 20)
    ) {
      return `총 경기 수는 1게임부터 ${competitionForm.courtCount * 20}게임까지 선택할 수 있어요.`;
    }
    if (totalPlayers < minimumPlayers) {
      return '코트 1개당 최소 4명이 필요해요.';
    }
    if (
      !isClubSession &&
      competitionForm.sameGenderDoublesOnly &&
      sameGenderDoublesOnlyUnavailable
    ) {
      return sameGenderDoublesOnlyUnavailableReason;
    }
    if (
      hasInvalidPlayerNameLength(competitionForm.malePlayerNames) ||
      hasInvalidPlayerNameLength(competitionForm.femalePlayerNames)
    ) {
      return '참가자 이름은 9자까지 입력할 수 있어요.';
    }

    return '';
  };

  const createCompetition = async () => {
    const analyticsPayload = {
      male_count: competitionForm.maleCount,
      female_count: competitionForm.femaleCount,
      total_players: totalPlayers,
      court_count: competitionForm.courtCount,
      total_games: isClubSession ? undefined : competitionForm.totalGames,
      mode: competitionForm.mode,
      same_gender_doubles_only: isClubSession
        ? undefined
        : competitionForm.sameGenderDoublesOnly,
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

      const payload = createCompetitionPayload({
        form: competitionForm,
        isClubSession,
      });

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
    canSubmitCompetition,
    isClubSession,
    sameGenderDoublesOnlyUnavailable,
    sameGenderDoublesOnlyUnavailableReason,
    placementText,
    participantGameText,
    unavailableReasonText,
    updateCompetitionField,
    stepCompetitionField,
    updateCompetitionPlayerName,
    createCompetition,
  };
}

function createCompetitionFormUpdate(prev, name, value) {
  if (name === 'maleCount') {
    return {
      maleCount: value,
      malePlayerNames: syncPlayerNames(prev.malePlayerNames, 'M', value),
    };
  }

  if (name === 'femaleCount') {
    return {
      femaleCount: value,
      femalePlayerNames: syncPlayerNames(prev.femalePlayerNames, 'F', value),
    };
  }

  return {
    [name]: value,
  };
}
