import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { motion } from 'framer-motion';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { default_oauth_provider } from '@/constants';
import CompetitionDetailSummary, {
  COMPETITION_MODES,
} from '../components/competition/detail/CompetitionDetailSummary';
import CompetitionEntryEditor from '../components/competition/detail/CompetitionEntryEditor';
import ClubSessionDetail from '../components/competition/detail/ClubSessionDetail';
import FixedScheduleDetail from '../components/competition/detail/FixedScheduleDetail';
import { apiRequest } from '../utils/apiClient';
import {
  claimMyCompetition,
  getCurrentUser,
  loginWithProvider,
} from '../utils/authApi';
import {
  createAdminTokenHeaders,
  getCompetitionAdminToken,
  saveCompetitionAdminToken,
} from '../utils/competitionEditToken';
import { markCompetitionRevisit, trackEvent } from '../utils/analytics';
import { createDebouncedAction } from '../utils/debouncedAction';
import './CompetitionDetail.css';

const itemTransition = { duration: 0.22, ease: [0.22, 1, 0.36, 1] };
const MotionHeader = motion.header;
const MotionSection = motion.section;
const SCORE_AUTOSAVE_DELAY_MS = 500;
const DUPLICATE_GAME_PLAYER_MESSAGE =
  '1게임에 동일한 사람이 포함되어 있어 저장할 수 없어요.';

function getResponseData(response) {
  return response.data?.data ?? response.data;
}

function getErrorMessage(error, fallbackMessage) {
  const message = error.response?.data?.message;
  if (!message || message === 'ERROR') {
    return fallbackMessage;
  }
  return message;
}

function resolveInitialMode({ status, requestedView }) {
  if (requestedView === 'score') {
    return COMPETITION_MODES.SCORE;
  }

  if (requestedView === 'manage') {
    return COMPETITION_MODES.MANAGE;
  }

  if (status === 'INPROGRESS') {
    return COMPETITION_MODES.SCORE;
  }

  return COMPETITION_MODES.MANAGE;
}

function normalizeScoreValue(value) {
  if (value === '') {
    return '';
  }

  const numberValue = Number(value);
  if (Number.isNaN(numberValue)) {
    return '';
  }

  return Math.max(0, Math.min(99, Math.trunc(numberValue)));
}

function toScoreNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return 0;
  }

  const numberValue = Number(value);
  if (!Number.isInteger(numberValue) || numberValue < 0 || numberValue > 99) {
    return null;
  }

  return numberValue;
}

function formatCompetitionCreatedAt(value) {
  if (!value) {
    return '-';
  }

  const date = Array.isArray(value)
    ? new Date(
        value[0],
        (value[1] ?? 1) - 1,
        value[2] ?? 1,
        value[3] ?? 0,
        value[4] ?? 0,
        value[5] ?? 0
      )
    : new Date(value);

  if (Number.isNaN(date.getTime())) {
    return '-';
  }

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(date);
}

function createGameScorePayload(game) {
  const payload = {
    teamAScore: toScoreNumber(game.score?.teamAScore),
    teamBScore: toScoreNumber(game.score?.teamBScore),
    teamATiebreakScore: toScoreNumber(game.score?.teamATiebreakScore),
    teamBTiebreakScore: toScoreNumber(game.score?.teamBTiebreakScore),
  };

  if (Object.values(payload).some((value) => value === null)) {
    return null;
  }

  return payload;
}

function hasRecordedTiebreakScore(game) {
  const teamATiebreakScore = Number(game?.score?.teamATiebreakScore ?? 0);
  const teamBTiebreakScore = Number(game?.score?.teamBTiebreakScore ?? 0);

  return teamATiebreakScore !== 0 || teamBTiebreakScore !== 0;
}

function hasRecordedScore(game) {
  const score = game?.score ?? {};
  return [
    score.teamAScore,
    score.teamBScore,
    score.teamATiebreakScore,
    score.teamBTiebreakScore,
  ].some((value) => Number(value ?? 0) > 0);
}

function hasGameEditorPlayerChanged(gameEditor) {
  if (!gameEditor?.game) {
    return false;
  }

  const currentTeamA = (gameEditor.game.teamA?.players ?? []).map((player) =>
    String(player.competitionEntryId)
  );
  const currentTeamB = (gameEditor.game.teamB?.players ?? []).map((player) =>
    String(player.competitionEntryId)
  );

  return (
    currentTeamA.join('|') !== gameEditor.teamA.join('|') ||
    currentTeamB.join('|') !== gameEditor.teamB.join('|')
  );
}

function createTiebreakOpenState(games = []) {
  return games.reduce((openState, game) => {
    if (hasRecordedTiebreakScore(game)) {
      openState[game.gameId] = true;
    }
    return openState;
  }, {});
}

function groupGamesByRound(games) {
  return games.reduce((rounds, game) => {
    const round = game.round ?? 0;
    if (!rounds.has(round)) {
      rounds.set(round, []);
    }
    rounds.get(round).push(game);
    return rounds;
  }, new Map());
}

function groupGamesByCourt(games) {
  return games.reduce((courts, game) => {
    const court = game.court ?? 0;
    if (!courts.has(court)) {
      courts.set(court, []);
    }
    courts.get(court).push(game);
    return courts;
  }, new Map());
}

function getCurrentClubSessionGameIds(games = [], courtCount = 0) {
  const gamesByCourt = groupGamesByCourt(games);
  const currentGameIds = new Set();

  for (let court = 1; court <= courtCount; court++) {
    const currentGame = (gamesByCourt.get(court) ?? [])
      .filter((game) => game.status === 'READY' || game.status === 'IN_PROGRESS')
      .filter((game) => !hasRecordedScore(game))
      .sort((a, b) => {
        if ((a.round ?? 0) !== (b.round ?? 0)) {
          return (a.round ?? 0) - (b.round ?? 0);
        }
        return (a.gameId ?? 0) - (b.gameId ?? 0);
      })[0];

    if (currentGame) {
      currentGameIds.add(currentGame.gameId);
    }
  }

  return currentGameIds;
}

function calculateEntryGameCounts(games = []) {
  const counts = new Map();

  games.forEach((game) => {
    [...(game.teamA?.players ?? []), ...(game.teamB?.players ?? [])].forEach(
      (player) => {
        counts.set(
          player.competitionEntryId,
          (counts.get(player.competitionEntryId) ?? 0) + 1
        );
      }
    );
  });

  return counts;
}

function analyzeRoundPlayers(games = [], entries = []) {
  const playerCounts = new Map(
    entries.map((entry) => [entry.competitionEntryId, 0])
  );
  const playerNames = new Map(
    entries.map((entry) => [entry.competitionEntryId, entry.playerName])
  );

  games.forEach((game) => {
    [...(game.teamA?.players ?? []), ...(game.teamB?.players ?? [])].forEach(
      (player) => {
        playerCounts.set(
          player.competitionEntryId,
          (playerCounts.get(player.competitionEntryId) ?? 0) + 1
        );
        playerNames.set(player.competitionEntryId, player.playerName);
      }
    );
  });

  const players = Array.from(playerCounts.entries()).map(
    ([competitionEntryId, count]) => ({
      competitionEntryId,
      playerName: playerNames.get(competitionEntryId),
      count,
    })
  );

  return {
    duplicatedPlayers: players.filter((player) => player.count >= 2),
    idlePlayers: players.filter((player) => player.count === 0),
  };
}

function createBalanceFromStat(stat) {
  if (!stat) {
    return null;
  }

  const difference = (stat.maxGames ?? 0) - (stat.minGames ?? 0);
  return {
    maxGames: stat.maxGames ?? 0,
    minGames: stat.minGames ?? 0,
    difference,
    message:
      difference === 0
        ? '1명당 게임 수가 동일해요.'
        : `1명당 게임 수가 최대 ${difference}경기 차이예요.`,
  };
}

function CompetitionDetail() {
  const { publicId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const [competition, setCompetition] = useState(null);
  const [balance, setBalance] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [mode, setMode] = useState(COMPETITION_MODES.MANAGE);
  const [tiebreakOpenGames, setTiebreakOpenGames] = useState({});

  const [isEntryEditorOpen, setIsEntryEditorOpen] = useState(false);
  const [entries, setEntries] = useState([]);
  const [originalEntries, setOriginalEntries] = useState([]);
  const [isLoadingEntries, setIsLoadingEntries] = useState(false);
  const [isSavingEntries, setIsSavingEntries] = useState(false);
  const [entryEditorError, setEntryEditorError] = useState('');
  const [entryEditorSuccess, setEntryEditorSuccess] = useState('');

  const [gameEditor, setGameEditor] = useState(null);
  const [gameEditorError, setGameEditorError] = useState('');
  const [isSavingGameEntries, setIsSavingGameEntries] = useState(false);
  const [savingScoreGameIds, setSavingScoreGameIds] = useState([]);
  const [scoreFeedbackByGameId, setScoreFeedbackByGameId] = useState({});
  const [scoreErrorByGameId, setScoreErrorByGameId] = useState({});
  const [isSavingCompetitionName, setIsSavingCompetitionName] = useState(false);
  const [competitionNameError, setCompetitionNameError] = useState('');
  const [competitionNameSuccess, setCompetitionNameSuccess] = useState('');
  const [shareFeedback, setShareFeedback] = useState('');
  const [adminToken, setAdminToken] = useState(() =>
    getCompetitionAdminToken(publicId)
  );
  const [accountClaimMessage, setAccountClaimMessage] = useState('');
  const [accountClaimError, setAccountClaimError] = useState('');
  const [isClaimingAccount, setIsClaimingAccount] = useState(false);
  const [accountUser, setAccountUser] = useState(null);
  const [isAccountChecked, setIsAccountChecked] = useState(false);
  const [showOnlyUnscoredGames, setShowOnlyUnscoredGames] = useState(false);
  const [adminPasswordModalOpen, setAdminPasswordModalOpen] = useState(false);
  const [adminPasswordMode, setAdminPasswordMode] = useState('login');
  const [adminPasswordDraft, setAdminPasswordDraft] = useState('');
  const [adminPasswordError, setAdminPasswordError] = useState('');
  const [isSubmittingAdminPassword, setIsSubmittingAdminPassword] = useState(false);
  const [clubActionError, setClubActionError] = useState('');
  const [clubActionFeedback, setClubActionFeedback] = useState('');
  const [busyClubAction, setBusyClubAction] = useState('');
  const [clubCompletionDrafts, setClubCompletionDrafts] = useState({});
  const [newEntryDraft, setNewEntryDraft] = useState({
    playerName: '',
    gender: 'MALE',
  });
  const saveGameScoreRef = useRef(null);
  const debouncedScoreSaveRef = useRef(null);

  if (debouncedScoreSaveRef.current === null) {
    debouncedScoreSaveRef.current = createDebouncedAction((gameId) => {
      saveGameScoreRef.current?.(gameId);
    }, SCORE_AUTOSAVE_DELAY_MS);
  }

  const canManage = Boolean(adminToken) || competition?.ownedByCurrentUser === true;
  const isClubSession = competition?.mode === 'CLUB_SESSION';
  const requiresAdminPasswordBeforeShare =
    canManage && competition?.adminPasswordSet === false;
  const isManageAccessMissing = false;
  const showFullHeader = true;
  const adminRequestOptions = {
    headers: createAdminTokenHeaders(adminToken),
  };
  const permissionDeniedMessage = '관리자 권한이 필요합니다. 관리자 모드로 로그인해 주세요.';
  const heroTitle = isManageAccessMissing
    ? '관리자 링크 필요'
    : isClubSession
      ? '진행형 대진'
    : showFullHeader
      ? mode === COMPETITION_MODES.MANAGE
        ? '대진표 관리'
        : '점수 입력 화면'
      : '경기 점수 입력';
  const heroDescription = isManageAccessMissing
    ? '대진표 관리는 관리자 링크로 접속해야 사용할 수 있습니다.'
      : isClubSession
      ? '경기를 대기열에 쌓아두고, 코트별 진행과 완료를 관리하세요.'
      : showFullHeader
      ? mode === COMPETITION_MODES.MANAGE
        ? '경기 시작 전 참가자 정보와 경기 배정을 조정하는 화면입니다.'
        : '배정된 경기를 확인하고 점수를 입력하세요.'
      : '배정된 경기를 확인하고 점수를 입력하세요.';
  const userShareUrl = `${window.location.origin}/competitions/${publicId}`;
  const searchParams = new URLSearchParams(location.search);
  const requestedView = searchParams.get('view');
  const claimAfterLogin = searchParams.get('claimAfterLogin') === '1';
  const canShowAccountClaim =
    competition?.ownerUserIdSet === false && Boolean(adminToken);
  const isAccountLoggedIn = Boolean(accountUser);
  const accountClaimTitle = isAccountLoggedIn
    ? '이 경기를 내 계정에 저장할 수 있어요.'
    : '로그인하면 이 경기를 저장할 수 있어요.';
  const accountClaimDescription = isAccountLoggedIn
    ? '저장하면 내 경기에서 다시 찾고 관리할 수 있습니다.'
    : '로그인 후 내 경기에서 다시 찾고 관리할 수 있습니다.';
  const accountClaimButtonLabel = !isAccountChecked
    ? '확인 중'
    : isAccountLoggedIn
      ? '내 계정에 저장'
      : '로그인 후 저장';

  const rejectWithoutPermission = (setError, setSuccess) => {
    if (canManage) {
      return false;
    }

    setSuccess?.('');
    setError(permissionDeniedMessage);
    return true;
  };

  const copyUrl = async (url, successMessage) => {
    setShareFeedback('');

    if (!url) {
      setShareFeedback(permissionDeniedMessage);
      return false;
    }

    try {
      await navigator.clipboard.writeText(url);
      setShareFeedback(successMessage);
      return true;
    } catch {
      setShareFeedback('링크 복사에 실패했습니다. 잠시 후 다시 시도해 주세요.');
      return false;
    }
  };

  const openAdminLogin = () => {
    if (competition?.adminPasswordSet !== true) {
      return;
    }
    setAdminPasswordMode('login');
    setAdminPasswordDraft('');
    setAdminPasswordError('');
    setAdminPasswordModalOpen(true);
  };

  const closeAdminPasswordModal = () => {
    setAdminPasswordModalOpen(false);
    setAdminPasswordDraft('');
    setAdminPasswordError('');
  };

  const openAdminPasswordSetup = () => {
    if (!canManage || competition?.adminPasswordSet !== false) {
      return;
    }
    setAdminPasswordMode('setup');
    setAdminPasswordDraft('');
    setAdminPasswordError('');
    setAdminPasswordModalOpen(true);
  };

  const handleShareClick = () => {
    if (canManage && competition?.adminPasswordSet === false) {
      openAdminPasswordSetup();
      return;
    }
    copyUrl(userShareUrl, '참여 링크를 복사했어요.');
  };

  const validateAdminPasswordDraft = () => {
    if (!/^\d{4,6}$/.test(adminPasswordDraft)) {
      setAdminPasswordError('관리자 비밀번호는 4~6자리 숫자로 입력해 주세요.');
      return false;
    }
    return true;
  };

  const submitAdminPasswordSetup = async () => {
    if (!validateAdminPasswordDraft()) {
      return;
    }

    try {
      setIsSubmittingAdminPassword(true);
      setAdminPasswordError('');
      const response = await apiRequest.post(
        `/api/competitions/${publicId}/admin-password`,
        { password: adminPasswordDraft },
        adminRequestOptions
      );
      const data = getResponseData(response);
      saveCompetitionAdminToken(publicId, data.competitionAdminToken);
      setAdminToken(data.competitionAdminToken);
      closeAdminPasswordModal();
      await refreshCompetition();
      await copyUrl(userShareUrl, '참여 링크를 복사했어요.');
    } catch (error) {
      setAdminPasswordError(
        getErrorMessage(
          error,
          '관리자 비밀번호를 설정하지 못했어요. 잠시 후 다시 시도해 주세요.'
        )
      );
    } finally {
      setIsSubmittingAdminPassword(false);
    }
  };

  const submitAdminLogin = async () => {
    if (!validateAdminPasswordDraft()) {
      return;
    }

    try {
      setIsSubmittingAdminPassword(true);
      setAdminPasswordError('');
      const response = await apiRequest.post(
        `/api/competitions/${publicId}/admin-login`,
        { password: adminPasswordDraft }
      );
      const data = getResponseData(response);
      saveCompetitionAdminToken(publicId, data.competitionAdminToken);
      setAdminToken(data.competitionAdminToken);
      closeAdminPasswordModal();
    } catch (error) {
      setAdminPasswordError(
        getErrorMessage(error, '관리자 비밀번호가 올바르지 않습니다.')
      );
    } finally {
      setIsSubmittingAdminPassword(false);
    }
  };

  useEffect(() => {
    setAdminToken(getCompetitionAdminToken(publicId));
  }, [publicId]);

  useEffect(() => {
    let isActive = true;

    getCurrentUser()
      .then((response) => {
        if (isActive) {
          setAccountUser(response.data?.data ?? null);
        }
      })
      .catch(() => {
        if (isActive) {
          setAccountUser(null);
        }
      })
      .finally(() => {
        if (isActive) {
          setIsAccountChecked(true);
        }
      });

    return () => {
      isActive = false;
    };
  }, []);

  useEffect(() => {
    if (!accountClaimMessage && !accountClaimError) {
      return undefined;
    }

    const timerId = setTimeout(() => {
      setAccountClaimMessage('');
      setAccountClaimError('');
    }, 2400);

    return () => {
      clearTimeout(timerId);
    };
  }, [accountClaimMessage, accountClaimError]);

  useEffect(() => {
    const handleAdminTokenCleared = (event) => {
      if (event.detail?.publicId === publicId) {
        setAdminToken('');
        setCompetition((prev) =>
          prev ? { ...prev, ownedByCurrentUser: false } : prev
        );
      }
    };

    window.addEventListener(
      'competition-admin-token-cleared',
      handleAdminTokenCleared
    );

    return () => {
      window.removeEventListener(
        'competition-admin-token-cleared',
        handleAdminTokenCleared
      );
    };
  }, [publicId]);

  const refreshCompetition = useCallback(
    async ({ showLoading = false } = {}) => {
      if (showLoading) {
        setIsLoading(true);
      }

      const response = await apiRequest.get(`/api/competitions/${publicId}`);
      const entriesResponse = await apiRequest.get(
        `/api/competitions/${publicId}/entries`
      );
      const data = getResponseData(response);
      const entryData = getResponseData(entriesResponse);

      setCompetition(data);
      setTiebreakOpenGames(createTiebreakOpenState(data?.games));
      setBalance(createBalanceFromStat(data?.stat));
      setEntries(entryData);
      setOriginalEntries(entryData);
      setMode(
        data?.mode === 'CLUB_SESSION'
          ? COMPETITION_MODES.MANAGE
          : resolveInitialMode({
              status: data?.status,
              requestedView,
            })
      );

      if (showLoading) {
        setIsLoading(false);
      }

      return { data, entryData };
    },
    [publicId, requestedView]
  );

  const removeClaimAfterLoginParam = useCallback(() => {
    const params = new URLSearchParams(location.search);
    if (!params.has('claimAfterLogin')) {
      return;
    }

    params.delete('claimAfterLogin');
    const nextSearch = params.toString();
    navigate(
      {
        pathname: location.pathname,
        search: nextSearch ? `?${nextSearch}` : '',
      },
      { replace: true }
    );
  }, [location.pathname, location.search, navigate]);

  const claimCompetitionForAccount = useCallback(async () => {
    if (!adminToken) {
      setAccountClaimMessage('');
      setAccountClaimError('관리자 권한이 있어야 내 계정에 저장할 수 있습니다.');
      removeClaimAfterLoginParam();
      return;
    }

    try {
      setIsClaimingAccount(true);
      setAccountClaimMessage('');
      setAccountClaimError('');
      await claimMyCompetition(publicId, adminToken);
      setAccountClaimMessage('내 계정에 저장했어요.');
      await refreshCompetition();
    } catch (error) {
      setAccountClaimMessage('');
      if (error.response?.status === 409) {
        setAccountClaimError('이미 다른 계정에 저장된 경기입니다.');
      } else if (error.response?.status === 401) {
        setAccountClaimError('로그인 후 다시 시도해 주세요.');
      } else {
        setAccountClaimError(
          error.response?.data?.message || '내 계정에 저장하지 못했어요.'
        );
      }
    } finally {
      setIsClaimingAccount(false);
      removeClaimAfterLoginParam();
    }
  }, [
    adminToken,
    publicId,
    refreshCompetition,
    removeClaimAfterLoginParam,
  ]);

  const handleAccountClaimClick = async () => {
    if (!isAccountChecked) {
      return;
    }

    if (!isAccountLoggedIn) {
      const params = new URLSearchParams(location.search);
      params.set('claimAfterLogin', '1');
      const redirectPath = `${location.pathname}?${params.toString()}`;
      sessionStorage.setItem('tennisfolio:postLoginRedirect', redirectPath);
      localStorage.setItem('tennisfolio:postLoginRedirect', redirectPath);
      loginWithProvider(default_oauth_provider);
      return;
    }

    await claimCompetitionForAccount();
  };

  useEffect(() => {
    let isActive = true;

    async function fetchCompetition() {
      try {
        setErrorMessage('');
        const { data } = await refreshCompetition({ showLoading: true });
        if (isActive) {
          const revisit = markCompetitionRevisit(publicId);
          trackEvent('competition_detail_view', {
            public_id: publicId,
            mode: requestedView || 'default',
            status: data?.status,
            has_edit_token: canManage,
            is_revisit: revisit,
          });
          if (revisit) {
            trackEvent('competition_revisit', {
              public_id: publicId,
              status: data?.status,
              has_edit_token: canManage,
            });
          }
        }
      } catch (error) {
        if (isActive) {
          setErrorMessage(
            error.response?.data?.message ||
              '경기 일정을 불러오지 못했어요. 잠시 후 다시 시도해 주세요.'
          );
        }
      } finally {
        if (isActive) {
          setIsLoading(false);
        }
      }
    }

    fetchCompetition();

    return () => {
      isActive = false;
    };
  }, [canManage, publicId, refreshCompetition, requestedView]);

  useEffect(() => {
    if (!claimAfterLogin || !competition || isClaimingAccount) {
      return;
    }

    if (!isAccountChecked) {
      return;
    }

    if (!isAccountLoggedIn) {
      setAccountClaimMessage('');
      setAccountClaimError('로그인 후 다시 시도해 주세요.');
      removeClaimAfterLoginParam();
      return;
    }

    claimCompetitionForAccount();
  }, [
    accountUser,
    claimAfterLogin,
    competition,
    isAccountChecked,
    isClaimingAccount,
    isAccountLoggedIn,
    claimCompetitionForAccount,
    removeClaimAfterLoginParam,
  ]);

  const rounds = useMemo(() => {
    if (!competition?.games?.length) {
      return [];
    }

    return Array.from(groupGamesByRound(competition.games).entries())
      .sort(([a], [b]) => a - b)
      .map(([round, games]) => ({
        round,
        games: games.sort((a, b) => (a.court ?? 0) - (b.court ?? 0)),
        roundPlayerStatus: analyzeRoundPlayers(games, entries),
      }));
  }, [competition, entries]);

  const courtGroups = useMemo(() => {
    if (!competition) {
      return [];
    }

    const gamesByCourt = groupGamesByCourt(competition.games ?? []);
    return Array.from(
      { length: competition.courtCount ?? 0 },
      (_, index) => index + 1
    ).map((court) => ({
      court,
      games: (gamesByCourt.get(court) ?? []).sort(
        (a, b) => (b.round ?? 0) - (a.round ?? 0)
      ),
    }));
  }, [competition]);

  const dirtyEntries = useMemo(
    () =>
      entries.filter((entry) => {
        const originalEntry = originalEntries.find(
          (original) =>
            original.competitionEntryId === entry.competitionEntryId
        );
        return (
          originalEntry &&
          originalEntry.playerName !== entry.playerName.trim()
        );
      }),
    [entries, originalEntries]
  );

  const entryGameCounts = useMemo(
    () => calculateEntryGameCounts(competition?.games),
    [competition?.games]
  );

  const clubAvailableEntryCount = useMemo(() => {
    if (!isClubSession) {
      return 0;
    }

    return entries.filter(
      (entry) => entry.status === 'ACTIVE'
    ).length;
  }, [entries, isClubSession]);

  const fetchCompetitionEntries = async () => {
    const response = await apiRequest.get(
      `/api/competitions/${publicId}/entries`
    );
    const data = getResponseData(response);
    setEntries(data);
    setOriginalEntries(data);
    return data;
  };

  const saveCompetitionName = async (name) => {
    if (rejectWithoutPermission(setCompetitionNameError, setCompetitionNameSuccess)) {
      return;
    }

    const normalizedName = name.trim();
    if (!normalizedName) {
      setCompetitionNameError('대회 이름을 입력해 주세요.');
      setCompetitionNameSuccess('');
      return;
    }

    try {
      setIsSavingCompetitionName(true);
      setCompetitionNameError('');
      setCompetitionNameSuccess('');
      const response = await apiRequest.patch(
        `/api/competitions/${publicId}`,
        {
          name: normalizedName,
        },
        adminRequestOptions
      );
      const data = getResponseData(response);

      setCompetition((prev) => {
        if (!prev) {
          return prev;
        }

        return {
          ...prev,
          name: data.name ?? normalizedName,
          createdAt: data.createdAt ?? prev.createdAt,
        };
      });
      setCompetitionNameSuccess('대회 이름을 저장했어요.');
    } catch (error) {
      setCompetitionNameError(
        error.response?.data?.message ||
          '대회 이름을 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.'
      );
    } finally {
      setIsSavingCompetitionName(false);
    }
  };

  const updateGameScore = (gameId, field, value) => {
    const nextValue = normalizeScoreValue(value);
    setScoreErrorByGameId((prev) => ({ ...prev, [gameId]: '' }));
    setScoreFeedbackByGameId((prev) => ({ ...prev, [gameId]: '' }));

    setCompetition((prev) => {
      if (!prev) {
        return prev;
      }

      return {
        ...prev,
        games: prev.games.map((game) => {
          if (game.gameId !== gameId) {
            return game;
          }

          const nextScore = {
            ...game.score,
            [field]: nextValue,
          };

          return {
            ...game,
            score: nextScore,
          };
        }),
      };
    });

    debouncedScoreSaveRef.current?.schedule(gameId);
  };

  const clearTiebreakScore = (gameId) => {
    setCompetition((prev) => {
      if (!prev) {
        return prev;
      }

      return {
        ...prev,
        games: prev.games.map((game) => {
          if (game.gameId !== gameId) {
            return game;
          }

          return {
            ...game,
            score: {
              ...game.score,
              teamATiebreakScore: 0,
              teamBTiebreakScore: 0,
            },
          };
        }),
      };
    });
  };

  const toggleTiebreak = (gameId) => {
    const isOpen = Boolean(tiebreakOpenGames[gameId]);

    if (isOpen) {
      const game = competition?.games?.find((item) => item.gameId === gameId);
      const teamATiebreakScore = Number(game?.score?.teamATiebreakScore ?? 0);
      const teamBTiebreakScore = Number(game?.score?.teamBTiebreakScore ?? 0);
      const hasTiebreakScore =
        teamATiebreakScore > 0 || teamBTiebreakScore > 0;

      if (
        hasTiebreakScore &&
        !window.confirm(
          '타이브레이크를 끄면 입력한 타이브레이크 점수가 초기화됩니다.'
        )
      ) {
        return;
      }
    }

    setTiebreakOpenGames((prev) => ({
      ...prev,
      [gameId]: !isOpen,
    }));

    if (isOpen) {
      clearTiebreakScore(gameId);
    } else {
      trackEvent('competition_tiebreak_enabled', {
        public_id: publicId,
        game_id: gameId,
      });
    }
  };

  const saveGameScore = async (gameId) => {
    const game = competition?.games?.find((item) => item.gameId === gameId);
    if (!game) {
      return;
    }

    const payload = createGameScorePayload(game);
    if (!payload) {
      setScoreFeedbackByGameId((prev) => ({ ...prev, [gameId]: '' }));
      setScoreErrorByGameId((prev) => ({
        ...prev,
        [gameId]: '점수는 0~99 사이 숫자로 입력해 주세요.',
      }));
      return;
    }

    try {
      setSavingScoreGameIds((prev) => [...prev, gameId]);
      setScoreFeedbackByGameId((prev) => ({ ...prev, [gameId]: '' }));
      setScoreErrorByGameId((prev) => ({ ...prev, [gameId]: '' }));
      const response = await apiRequest.patch(
        `/api/competitions/${publicId}/games/${gameId}/score`,
        payload,
        adminRequestOptions
      );
      const data = getResponseData(response);

      setCompetition((prev) => {
        if (!prev) {
          return prev;
        }

        return {
          ...prev,
          games: prev.games.map((item) =>
            item.gameId === data.gameId ? data : item
          ),
        };
      });
      trackEvent('competition_score_saved', {
        public_id: publicId,
        game_id: data.gameId,
        round: data.round,
        court: data.court,
        team_a_score: data.score?.teamAScore ?? 0,
        team_b_score: data.score?.teamBScore ?? 0,
        team_a_tiebreak_score: data.score?.teamATiebreakScore ?? 0,
        team_b_tiebreak_score: data.score?.teamBTiebreakScore ?? 0,
        tiebreak_used:
          Number(data.score?.teamATiebreakScore ?? 0) > 0 ||
          Number(data.score?.teamBTiebreakScore ?? 0) > 0,
      });
      trackEvent('competition_score_entry_completed', {
        public_id: publicId,
        game_id: data.gameId,
      });
      setScoreFeedbackByGameId((prev) => ({
        ...prev,
        [gameId]: '저장됐어요.',
      }));
    } catch (error) {
      setScoreErrorByGameId((prev) => ({
        ...prev,
        [gameId]:
          error.response?.data?.message ||
          '점수를 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.',
      }));
    } finally {
      setSavingScoreGameIds((prev) => prev.filter((id) => id !== gameId));
    }
  };

  saveGameScoreRef.current = saveGameScore;

  const openEntryEditor = async () => {
    if (isEntryEditorOpen) {
      closeEntryEditor();
      return;
    }

    closeGameEditor();
    setIsEntryEditorOpen(true);
    setEntryEditorError('');
    setEntryEditorSuccess('');
    setIsLoadingEntries(true);

    try {
      await fetchCompetitionEntries();
    } catch (error) {
      setEntryEditorError(
        error.response?.data?.message ||
          '참가자 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.'
      );
    } finally {
      setIsLoadingEntries(false);
    }
  };

  const closeEntryEditor = () => {
    setIsEntryEditorOpen(false);
    setEntryEditorError('');
    setEntryEditorSuccess('');
    setEntries(originalEntries);
  };

  const changeMode = (nextMode) => {
    setMode(nextMode);
    if (nextMode === COMPETITION_MODES.SCORE) {
      closeEntryEditor();
      closeGameEditor();
    }
  };

  const updateEntryName = (competitionEntryId, playerName) => {
    if (!canManage) {
      return;
    }

    const nextPlayerName = playerName.slice(0, 9);
    setEntryEditorError('');
    setEntryEditorSuccess('');
    setEntries((prev) =>
      prev.map((entry) =>
        entry.competitionEntryId === competitionEntryId
          ? { ...entry, playerName: nextPlayerName }
          : entry
      )
    );
  };

  const syncCompetitionPlayerNames = (updatedEntries) => {
    const namesByEntryId = new Map(
      updatedEntries.map((entry) => [
        entry.competitionEntryId,
        entry.playerName,
      ])
    );

    setCompetition((prev) => {
      if (!prev) {
        return prev;
      }

      return {
        ...prev,
        games: prev.games.map((game) => ({
          ...game,
          teamA: {
            ...game.teamA,
            players: game.teamA.players.map((player) => ({
              ...player,
              playerName:
                namesByEntryId.get(player.competitionEntryId) ??
                player.playerName,
            })),
          },
          teamB: {
            ...game.teamB,
            players: game.teamB.players.map((player) => ({
              ...player,
              playerName:
                namesByEntryId.get(player.competitionEntryId) ??
                player.playerName,
            })),
          },
        })),
      };
    });
  };

  const saveEntryNames = async () => {
    if (rejectWithoutPermission(setEntryEditorError, setEntryEditorSuccess)) {
      return;
    }

    const hasBlankName = entries.some(
      (entry) => entry.playerName.trim() === ''
    );
    if (hasBlankName) {
      setEntryEditorSuccess('');
      setEntryEditorError('참가자 이름은 비워둘 수 없어요.');
      return;
    }

    const hasLongName = entries.some(
      (entry) => entry.playerName.trim().length > 9
    );
    if (hasLongName) {
      setEntryEditorSuccess('');
      setEntryEditorError('참가자 이름은 9자까지 입력할 수 있어요.');
      return;
    }

    const nameCounts = entries.reduce((counts, entry) => {
      const playerName = entry.playerName.trim();
      counts.set(playerName, (counts.get(playerName) ?? 0) + 1);
      return counts;
    }, new Map());
    const duplicatedNames = Array.from(nameCounts.entries())
      .filter(([, count]) => count > 1)
      .map(([playerName]) => playerName);
    if (duplicatedNames.length > 0) {
      setEntryEditorSuccess('');
      setEntryEditorError(
        `동일한 참가자 이름이 있어요: ${duplicatedNames.join(', ')}`
      );
      return;
    }

    if (dirtyEntries.length === 0) {
      setEntryEditorError('');
      setEntryEditorSuccess('변경된 참가자 이름이 없어요.');
      return;
    }

    try {
      setIsSavingEntries(true);
      setEntryEditorError('');
      setEntryEditorSuccess('');

      const responses = await Promise.all(
        dirtyEntries.map((entry) =>
          apiRequest.patch(
            `/api/competitions/${publicId}/entries/${entry.competitionEntryId}`,
            { playerName: entry.playerName.trim() },
            adminRequestOptions
          )
        )
      );
      const updatedEntries = responses.map(getResponseData);
      const nextEntries = entries.map((entry) => {
        const updatedEntry = updatedEntries.find(
          (item) => item.competitionEntryId === entry.competitionEntryId
        );
        return updatedEntry ?? { ...entry, playerName: entry.playerName.trim() };
      });

      setEntries(nextEntries);
      setOriginalEntries(nextEntries);
      syncCompetitionPlayerNames(updatedEntries);
      setEntryEditorSuccess('참가자 이름을 저장했어요.');
    } catch (error) {
      setEntryEditorError(
        error.response?.data?.message ||
          '참가자 이름을 저장하지 못했어요. 잠시 후 다시 시도해 주세요.'
      );
    } finally {
      setIsSavingEntries(false);
    }
  };

  const openGameEditor = async (game) => {
    if (!canManage) {
      return;
    }

    if (gameEditor?.game?.gameId === game.gameId) {
      closeGameEditor();
      return;
    }

    closeEntryEditor();
    setGameEditorError('');

    try {
      const nextEntries =
        entries.length > 0 ? entries : await fetchCompetitionEntries();
      setGameEditor({
        game,
        entries: nextEntries,
        teamA: game.teamA.players.map((player) =>
          String(player.competitionEntryId)
        ),
        teamB: game.teamB.players.map((player) =>
          String(player.competitionEntryId)
        ),
      });
    } catch (error) {
      setGameEditorError(
        error.response?.data?.message ||
          '참가자 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.'
      );
    }
  };

  const closeGameEditor = () => {
    setGameEditor(null);
    setGameEditorError('');
  };

  const updateGameEditorSelection = (team, index, value) => {
    if (!gameEditor) {
      return;
    }

    const nextTeam = [...gameEditor[team]];
    nextTeam[index] = value;
    const nextGameEditor = {
      ...gameEditor,
      [team]: nextTeam,
    };
    const selectedIds = [...nextGameEditor.teamA, ...nextGameEditor.teamB];
    const hasDuplicate =
      selectedIds.every(Boolean) &&
      new Set(selectedIds).size !== selectedIds.length;

    setGameEditor(nextGameEditor);
    setGameEditorError(hasDuplicate ? DUPLICATE_GAME_PLAYER_MESSAGE : '');
  };

  const getReadyGameAssignment = (entryId) => {
    if (!gameEditor || !competition?.games?.length) {
      return null;
    }

    const currentClubSessionGameIds = isClubSession
      ? getCurrentClubSessionGameIds(
          competition.games,
          competition.courtCount ?? 0
        )
      : null;

    return (
      competition.games.find(
        (game) =>
          game.status === 'READY' &&
          (!isClubSession || currentClubSessionGameIds.has(game.gameId)) &&
          game.gameId !== gameEditor.game.gameId &&
          [...(game.teamA?.players ?? []), ...(game.teamB?.players ?? [])].some(
            (player) =>
              String(player.competitionEntryId) === String(entryId)
          )
      ) ?? null
    );
  };

  const getEntryRoundConflictLabel = (entryId) => {
    if (!gameEditor || !competition?.games?.length) {
      return '';
    }

    if (isClubSession) {
      const conflictGame = getReadyGameAssignment(entryId);
      return conflictGame ? `${conflictGame.court}번 코트 배정됨` : '';
    }

    const conflictGame = competition.games.find(
      (game) =>
        game.round === gameEditor.game.round &&
        game.gameId !== gameEditor.game.gameId &&
        [...(game.teamA?.players ?? []), ...(game.teamB?.players ?? [])].some(
          (player) =>
            String(player.competitionEntryId) === String(entryId)
        )
    );

    return conflictGame ? `${conflictGame.court}번 코트 배정됨` : '';
  };

  const isEntrySelectedElsewhere = (entryId, team, index) => {
    if (!gameEditor) {
      return false;
    }

    const entryIdText = String(entryId);
    const selectedSlots = [
      { team: 'teamA', index: 0, value: gameEditor.teamA[0] },
      { team: 'teamA', index: 1, value: gameEditor.teamA[1] },
      { team: 'teamB', index: 0, value: gameEditor.teamB[0] },
      { team: 'teamB', index: 1, value: gameEditor.teamB[1] },
    ];

    return selectedSlots.some(
      (slot) =>
        slot.value === entryIdText &&
        (slot.team !== team || slot.index !== index)
    );
  };

  const saveGameEntries = async () => {
    if (!gameEditor) {
      return;
    }

    if (rejectWithoutPermission(setGameEditorError)) {
      return;
    }

    const selectedIds = [...gameEditor.teamA, ...gameEditor.teamB];
    if (selectedIds.some((id) => !id)) {
      setGameEditorError('A팀과 B팀 선수를 모두 선택해 주세요.');
      return;
    }
    if (new Set(selectedIds).size !== 4) {
      setGameEditorError(DUPLICATE_GAME_PLAYER_MESSAGE);
      return;
    }

    const willResetScore =
      hasGameEditorPlayerChanged(gameEditor) && hasRecordedScore(gameEditor.game);

    if (
      willResetScore &&
      !window.confirm(
        '이 경기에는 이미 입력된 점수가 있습니다. 선수를 변경하면 기존 점수가 초기화됩니다. 계속할까요?'
      )
    ) {
      return;
    }

    try {
      setIsSavingGameEntries(true);
      setGameEditorError('');

      const response = await apiRequest.patch(
        `/api/competitions/${publicId}/games/${gameEditor.game.gameId}/entries`,
        {
          teamA: gameEditor.teamA.map((competitionEntryId, index) => ({
            competitionEntryId: Number(competitionEntryId),
            position: index + 1,
          })),
          teamB: gameEditor.teamB.map((competitionEntryId, index) => ({
            competitionEntryId: Number(competitionEntryId),
            position: index + 1,
          })),
        },
        adminRequestOptions
      );
      const data = getResponseData(response);

      if (isClubSession) {
        await refreshCompetition();
        setClubActionError('');
        setClubActionFeedback('경기 편성을 저장했어요.');
      } else {
        setCompetition((prev) => {
          if (!prev) {
            return prev;
          }

          return {
            ...prev,
            stat: data.stat ?? prev.stat,
            games: prev.games.map((game) =>
              game.gameId === data.game.gameId ? data.game : game
            ),
          };
        });
      }
      trackEvent('competition_game_entries_saved', {
        public_id: publicId,
        game_id: data.game.gameId,
        round: data.game.round,
        court: data.game.court,
        score_reset: willResetScore,
      });
      setBalance(data.balance ?? createBalanceFromStat(data.stat));
      closeGameEditor();
    } catch (error) {
      setGameEditorError(
        error.response?.data?.message ||
          '경기 변경 내용을 저장하지 못했어요. 잠시 후 다시 시도해 주세요.'
      );
    } finally {
      setIsSavingGameEntries(false);
    }
  };

  const createNextCourtGame = async (court) => {
    if (rejectWithoutPermission(setClubActionError, setClubActionFeedback)) {
      return;
    }

    try {
      setBusyClubAction(`create-${court}`);
      setClubActionError('');
      setClubActionFeedback('');
      await apiRequest.post(
        `/api/competitions/${publicId}/courts/${court}/games`,
        null,
        adminRequestOptions
      );
      await refreshCompetition();
      setClubActionFeedback(`${court}번 코트 대기 경기를 생성했어요.`);
    } catch (error) {
      setClubActionError(
        clubAvailableEntryCount < 4
          ? `배정 가능한 참가자가 ${clubAvailableEntryCount}명입니다. 새 경기를 만들려면 최소 4명이 필요해요.`
          : error.response?.data?.message ||
              '대기 경기를 생성하지 못했어요. 참가 가능 인원을 확인해 주세요.'
      );
    } finally {
      setBusyClubAction('');
    }
  };

  const prepareClubCompletion = (gameId) => {
    setClubCompletionDrafts((prev) =>
      prev[gameId]
        ? prev
        : {
            ...prev,
            [gameId]: {
              teamAScore: 0,
              teamBScore: 0,
            },
          }
    );
    setClubActionError('');
    setClubActionFeedback('');
  };

  const updateClubCompletionScore = (gameId, field, value) => {
    if (!field) {
      prepareClubCompletion(gameId);
      return;
    }

    const nextValue = normalizeScoreValue(value);
    const game = competition?.games?.find((item) => item.gameId === gameId);
    const defaultScore = {
      teamAScore: game?.score?.teamAScore ?? 0,
      teamBScore: game?.score?.teamBScore ?? 0,
    };
    setClubActionError('');
    setClubCompletionDrafts((prev) => ({
      ...prev,
      [gameId]: {
        ...defaultScore,
        ...(prev[gameId] ?? {}),
        [field]: nextValue,
      },
    }));
  };

  const submitClubGameCompletion = async (gameId) => {
    const game = competition?.games?.find((item) => item.gameId === gameId);
    const score = clubCompletionDrafts[gameId] ?? game?.score ?? {};
    const hasAnyScoreInput =
      score.teamAScore !== '' &&
      score.teamAScore !== null &&
      score.teamAScore !== undefined &&
      score.teamBScore !== '' &&
      score.teamBScore !== null &&
      score.teamBScore !== undefined;

    if (!hasAnyScoreInput) {
      setClubActionFeedback('');
      setClubActionError('점수를 입력해 주세요.');
      return;
    }

    const scorePayload = {
      teamAScore: toScoreNumber(score.teamAScore),
      teamBScore: toScoreNumber(score.teamBScore),
    };

    if (Object.values(scorePayload).some((value) => value === null)) {
      setClubActionFeedback('');
      setClubActionError('점수는 0~99 사이 숫자로 입력해 주세요.');
      return;
    }

    try {
      setBusyClubAction(`complete-${gameId}`);
      setClubActionError('');
      setClubActionFeedback('');
      await apiRequest.patch(
        `/api/competitions/${publicId}/games/${gameId}/status`,
        { status: 'COMPLETED', ...scorePayload },
        adminRequestOptions
      );
      await refreshCompetition();
      setClubCompletionDrafts((prev) => {
        const next = { ...prev };
        delete next[gameId];
        return next;
      });
      setClubActionFeedback('점수와 함께 경기를 완료 처리했어요.');
    } catch (error) {
      try {
        const refreshed = await refreshCompetition();
        const refreshedGame = refreshed?.data?.games?.find(
          (item) => item.gameId === gameId
        );
        if (refreshedGame?.status === 'COMPLETED') {
          setClubActionError('');
          setClubActionFeedback('이미 완료된 경기입니다. 최신 상태로 갱신했어요.');
          setClubCompletionDrafts((prev) => {
            const next = { ...prev };
            delete next[gameId];
            return next;
          });
          return;
        }
      } catch {
        // Keep the original completion error below if the refresh also fails.
      }
      setClubActionError(
        error.response?.data?.message || '경기를 완료 처리하지 못했어요.'
      );
    } finally {
      setBusyClubAction('');
    }
  };

  const submitClubCompletedScoreEdit = async (gameId) => {
    const game = competition?.games?.find((item) => item.gameId === gameId);
    const score = clubCompletionDrafts[gameId] ?? game?.score ?? {};
    const scorePayload = {
      teamAScore: toScoreNumber(score.teamAScore),
      teamBScore: toScoreNumber(score.teamBScore),
      teamATiebreakScore: 0,
      teamBTiebreakScore: 0,
    };

    if (
      score.teamAScore === '' ||
      score.teamBScore === '' ||
      Object.values(scorePayload).some((value) => value === null)
    ) {
      setClubActionFeedback('');
      setClubActionError('점수는 0~99 사이 숫자로 입력해 주세요.');
      return false;
    }

    try {
      setBusyClubAction(`score-${gameId}`);
      setClubActionError('');
      setClubActionFeedback('');
      await apiRequest.patch(
        `/api/competitions/${publicId}/games/${gameId}/score`,
        scorePayload,
        adminRequestOptions
      );
      await refreshCompetition();
      setClubCompletionDrafts((prev) => {
        const next = { ...prev };
        delete next[gameId];
        return next;
      });
      setClubActionFeedback('완료 경기 점수를 수정했어요.');
      return true;
    } catch (error) {
      setClubActionError(
        error.response?.data?.message || '점수를 수정하지 못했어요.'
      );
      return false;
    } finally {
      setBusyClubAction('');
    }
  };

  const deleteClubGame = async (gameId) => {
    if (rejectWithoutPermission(setClubActionError, setClubActionFeedback)) {
      return;
    }
    if (!window.confirm('이 경기를 삭제할까요?')) {
      return;
    }

    try {
      setBusyClubAction(`delete-${gameId}`);
      setClubActionError('');
      setClubActionFeedback('');
      await apiRequest.delete(
        `/api/competitions/${publicId}/games/${gameId}`,
        adminRequestOptions
      );
      await refreshCompetition();
      setClubCompletionDrafts((prev) => {
        const next = { ...prev };
        delete next[gameId];
        return next;
      });
      setClubActionFeedback('경기를 삭제했어요.');
    } catch (error) {
      setClubActionError(
        error.response?.data?.message || '경기를 삭제하지 못했어요.'
      );
    } finally {
      setBusyClubAction('');
    }
  };

  const createCompetitionEntry = async () => {
    if (rejectWithoutPermission(setEntryEditorError, setEntryEditorSuccess)) {
      return;
    }

    const playerName = newEntryDraft.playerName.trim();
    if (!playerName) {
      setEntryEditorSuccess('');
      setEntryEditorError('추가할 참가자 이름을 입력해 주세요.');
      return;
    }

    try {
      setIsSavingEntries(true);
      setEntryEditorError('');
      setEntryEditorSuccess('');
      const response = await apiRequest.post(
        `/api/competitions/${publicId}/entries`,
        {
          playerName,
          gender: newEntryDraft.gender,
        },
        adminRequestOptions
      );
      const entry = getResponseData(response);
      const nextEntries = [...entries, entry];
      setEntries(nextEntries);
      setOriginalEntries(nextEntries);
      setCompetition((prev) =>
        prev
          ? {
              ...prev,
              maleCount:
                entry.gender === 'MALE'
                  ? (prev.maleCount ?? 0) + 1
                  : prev.maleCount,
              femaleCount:
                entry.gender === 'FEMALE'
                  ? (prev.femaleCount ?? 0) + 1
                  : prev.femaleCount,
            }
          : prev
      );
      setNewEntryDraft({ playerName: '', gender: 'MALE' });
      setEntryEditorSuccess('참가자를 추가했어요.');
    } catch (error) {
      setEntryEditorError(
        error.response?.data?.message || '참가자를 추가하지 못했어요.'
      );
    } finally {
      setIsSavingEntries(false);
    }
  };

  const updateEntryStatus = async (competitionEntryId, status) => {
    if (rejectWithoutPermission(setEntryEditorError, setEntryEditorSuccess)) {
      return;
    }

    try {
      setIsSavingEntries(true);
      setEntryEditorError('');
      setEntryEditorSuccess('');
      const response = await apiRequest.patch(
        `/api/competitions/${publicId}/entries/${competitionEntryId}`,
        { status },
        adminRequestOptions
      );
      const updatedEntry = getResponseData(response);
      const nextEntries = entries.map((entry) =>
        entry.competitionEntryId === updatedEntry.competitionEntryId
          ? updatedEntry
          : entry
      );
      setEntries(nextEntries);
      setOriginalEntries(nextEntries);
      setEntryEditorSuccess(
        status === 'ACTIVE' ? '참가 상태로 변경했어요.' : '대기 상태로 변경했어요.'
      );
    } catch (error) {
      setEntryEditorError(
        error.response?.data?.message ||
          '참가 상태를 변경하지 못했어요. 잠시 후 다시 시도해 주세요.'
      );
    } finally {
      setIsSavingEntries(false);
    }
  };

  const renderSummaryPanel = () => {
    if (isLoading) {
      return (
        <div className="competition-detail-state">
          일정을 불러오고 있어요.
        </div>
      );
    }

    if (errorMessage) {
      return <div className="competition-message error">{errorMessage}</div>;
    }

    if (!competition) {
      return null;
    }

    return (
      <CompetitionDetailSummary
        competition={competition}
        balance={balance}
        mode={mode}
        canManage={canManage}
        isSavingName={isSavingCompetitionName}
        nameError={competitionNameError}
        nameSuccess={competitionNameSuccess}
        onSaveName={saveCompetitionName}
      />
    );
  };

  return (
    <main
      className={`competition-detail-page ${
        isClubSession ? 'club-session-detail-page' : ''
      }`}
    >
      {!isClubSession && (
        <MotionHeader
          className="competition-detail-hero"
          initial={{ opacity: 0, y: 14 }}
          animate={{ opacity: 1, y: 0 }}
          transition={itemTransition}
        >
          <div className="competition-detail-hero-content">
            {canManage && (
              <div className="competition-detail-label-row">
                <span className="competition-admin-badge">관리자</span>
              </div>
            )}
            <h1>{heroTitle}</h1>
            <p>{heroDescription}</p>
            {isManageAccessMissing && (
              <div className="competition-admin-warning">
                관리자 링크로 접속해야 대진표를 수정할 수 있습니다.
              </div>
            )}
            <div className="competition-hero-points" aria-label="대진 운영 상태">
              <span>복식 대진표</span>
              <span>점수 입력</span>
            </div>
            <div className="competition-view-switch" aria-label="관리자 보기 전환">
              <button
                type="button"
                className={mode === COMPETITION_MODES.MANAGE ? 'active' : ''}
                onClick={() => changeMode(COMPETITION_MODES.MANAGE)}
              >
                대진표 관리
              </button>
              <button
                type="button"
                className={mode === COMPETITION_MODES.SCORE ? 'active' : ''}
                onClick={() => changeMode(COMPETITION_MODES.SCORE)}
              >
                점수 입력 화면
              </button>
            </div>
          </div>
        </MotionHeader>
      )}

      {!isLoading &&
        !errorMessage &&
        competition &&
        (canShowAccountClaim ||
          accountClaimMessage ||
          accountClaimError) && (
          <section className="competition-account-claim-panel">
            {canShowAccountClaim && (
              <div className="competition-account-claim">
                <div>
                  <strong>{accountClaimTitle}</strong>
                  <p>{accountClaimDescription}</p>
                </div>
                <button
                  type="button"
                  disabled={isClaimingAccount || !isAccountChecked}
                  onClick={handleAccountClaimClick}
                >
                  {isClaimingAccount ? '저장 중' : accountClaimButtonLabel}
                </button>
              </div>
            )}
            {accountClaimMessage && (
              <div className="competition-account-claim-message success">
                {accountClaimMessage}
              </div>
            )}
            {accountClaimError && (
              <div className="competition-account-claim-message error">
                {accountClaimError}
              </div>
            )}
          </section>
        )}

      {!isLoading &&
        !errorMessage &&
        competition && (
        <section className="competition-admin-actions">
          <div className="competition-share-panel">
            <button
              type="button"
              className="competition-share-toggle"
              onClick={handleShareClick}
            >
              {requiresAdminPasswordBeforeShare
                ? '관리자 비밀번호 설정 후 링크 복사'
                : '공유 링크 복사'}
            </button>
            {shareFeedback && (
              <p className="competition-share-feedback">{shareFeedback}</p>
            )}
          </div>

          {!canManage && competition.adminPasswordSet === false && (
            <p className="competition-admin-notice">
              아직 관리자가 관리자 비밀번호를 설정하지 않았습니다.
            </p>
          )}

          {!canManage && competition.adminPasswordSet === true && (
            <button
              type="button"
              className="competition-admin-mode-button secondary"
              onClick={openAdminLogin}
            >
              관리자 모드
            </button>
          )}

          <button
            className={`competition-entry-edit-button ${
              isEntryEditorOpen ? 'active' : ''
            }`}
            type="button"
            onClick={openEntryEditor}
          >
            {canManage ? '참가자 명단 관리' : '참가자 명단 보기'}
          </button>
        </section>
      )}

      {isEntryEditorOpen && (
        <CompetitionEntryEditor
          entries={entries}
          entryGameCounts={entryGameCounts}
          canManage={canManage}
          errorMessage={entryEditorError}
          successMessage={entryEditorSuccess}
          isLoading={isLoadingEntries}
          isSaving={isSavingEntries}
          isClubSession={isClubSession}
          newEntryDraft={newEntryDraft}
          onClose={closeEntryEditor}
          onSave={saveEntryNames}
          onUpdateEntryName={updateEntryName}
          onUpdateEntryStatus={updateEntryStatus}
          onUpdateNewEntry={setNewEntryDraft}
          onCreateEntry={createCompetitionEntry}
        />
      )}

      {!isLoading && !errorMessage && competition && isClubSession && (
        <ClubSessionDetail
          competition={competition}
          canManage={canManage}
          courtCount={competition.courtCount ?? 0}
          courtGroups={courtGroups}
          formattedCreatedAt={formatCompetitionCreatedAt(
            competition.createdAt
          )}
          isSavingCompetitionName={isSavingCompetitionName}
          competitionNameError={competitionNameError}
          competitionNameSuccess={competitionNameSuccess}
          clubActionError={clubActionError}
          clubActionFeedback={clubActionFeedback}
          busyClubAction={busyClubAction}
          clubCompletionDrafts={clubCompletionDrafts}
          clubAvailableEntryCount={clubAvailableEntryCount}
          gameEditor={gameEditor}
          gameEditorError={gameEditorError}
          isSavingGameEntries={isSavingGameEntries}
          isEntrySelectedElsewhere={isEntrySelectedElsewhere}
          getEntryRoundConflictLabel={getEntryRoundConflictLabel}
          onCreateNextCourtGame={createNextCourtGame}
          onSaveCompetitionName={saveCompetitionName}
          onOpenGameEditor={openGameEditor}
          onPrepareCompletion={prepareClubCompletion}
          onUpdateCompletionScore={updateClubCompletionScore}
          onSubmitCompletion={submitClubGameCompletion}
          onSubmitCompletedScoreEdit={submitClubCompletedScoreEdit}
          onDeleteGame={deleteClubGame}
          onCloseGameEditor={closeGameEditor}
          onSaveGameEntries={saveGameEntries}
          onUpdateGameEditorSelection={updateGameEditorSelection}
        />
      )}

      {!isClubSession && (
        <MotionSection
          className="competition-detail-panel"
          initial={{ opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ ...itemTransition, delay: 0.08 }}
        >
          {renderSummaryPanel()}
        </MotionSection>
      )}

      {!isLoading && !errorMessage && competition && !isClubSession && (
        <FixedScheduleDetail
          mode={mode}
          canManage={canManage}
          visibleRounds={rounds}
          showOnlyUnscoredGames={showOnlyUnscoredGames}
          gameEditor={gameEditor}
          gameEditorError={gameEditorError}
          isSavingGameEntries={isSavingGameEntries}
          tiebreakOpenGames={tiebreakOpenGames}
          savingScoreGameIds={savingScoreGameIds}
          scoreFeedbackByGameId={scoreFeedbackByGameId}
          scoreErrorByGameId={scoreErrorByGameId}
          onToggleUnscoredGames={() => setShowOnlyUnscoredGames((prev) => !prev)}
          onOpenResult={() => navigate(`/competitions/${publicId}/result`)}
          onOpenGameEditor={openGameEditor}
          onToggleTiebreak={toggleTiebreak}
          onUpdateGameScore={updateGameScore}
          onSaveGameScore={saveGameScore}
          isEntrySelectedElsewhere={isEntrySelectedElsewhere}
          getEntryRoundConflictLabel={getEntryRoundConflictLabel}
          onCloseGameEditor={closeGameEditor}
          onSaveGameEntries={saveGameEntries}
          onUpdateGameEditorSelection={updateGameEditorSelection}
        />
      )}

      {adminPasswordModalOpen && (
        <div className="competition-admin-password-modal" role="presentation">
          <div
            className="competition-admin-password-dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="competition-admin-password-title"
          >
            <div className="competition-admin-password-heading">
              <h2 id="competition-admin-password-title">
                {adminPasswordMode === 'setup'
                  ? '관리자 비밀번호 설정'
                  : '관리자 모드'}
              </h2>
              <button
                type="button"
                aria-label="닫기"
                onClick={closeAdminPasswordModal}
              >
                ×
              </button>
            </div>
            <p>
              {adminPasswordMode === 'setup'
                ? '공유 전에 운영자가 사용할 4~6자리 숫자를 설정해 주세요.'
                : '관리자 비밀번호를 입력하면 이 브라우저에서 관리자 기능을 사용할 수 있어요.'}
            </p>
            <input
              type="password"
              inputMode="numeric"
              pattern="[0-9]*"
              maxLength={6}
              value={adminPasswordDraft}
              onChange={(event) => {
                setAdminPasswordDraft(event.target.value.replace(/\D/g, '').slice(0, 6));
                setAdminPasswordError('');
              }}
              placeholder="4~6자리 숫자"
              autoFocus
            />
            {adminPasswordError && (
              <div className="competition-admin-password-error">
                {adminPasswordError}
              </div>
            )}
            <div className="competition-admin-password-actions">
              <button
                type="button"
                className="secondary"
                onClick={closeAdminPasswordModal}
                disabled={isSubmittingAdminPassword}
              >
                취소
              </button>
              <button
                type="button"
                onClick={
                  adminPasswordMode === 'setup'
                    ? submitAdminPasswordSetup
                    : submitAdminLogin
                }
                disabled={isSubmittingAdminPassword}
              >
                {isSubmittingAdminPassword
                  ? '확인 중'
                  : adminPasswordMode === 'setup'
                    ? '설정하고 공유'
                    : '관리자 로그인'}
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}

export default CompetitionDetail;
