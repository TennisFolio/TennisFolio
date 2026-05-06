import { useEffect, useMemo, useState } from 'react';
import { motion } from 'framer-motion';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import CompetitionDetailSummary, {
  COMPETITION_MODES,
} from '../components/competition/detail/CompetitionDetailSummary';
import CompetitionEntryEditor from '../components/competition/detail/CompetitionEntryEditor';
import CompetitionGameCard from '../components/competition/detail/CompetitionGameCard';
import CompetitionGameEditor from '../components/competition/detail/CompetitionGameEditor';
import { apiRequest } from '../utils/apiClient';
import {
  createEditTokenHeaders,
  dismissCompetitionAdminLinkPrompt,
  getCompetitionEditToken,
  saveCompetitionEditToken,
  shouldShowCompetitionAdminLinkPrompt,
} from '../utils/competitionEditToken';
import { markCompetitionRevisit, trackEvent } from '../utils/analytics';
import './CompetitionDetail.css';

const itemTransition = { duration: 0.22, ease: [0.22, 1, 0.36, 1] };
const MotionHeader = motion.header;
const MotionSection = motion.section;

function getResponseData(response) {
  return response.data?.data ?? response.data;
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

function createSavedScoreMap(games = []) {
  return games.reduce((scoreMap, game) => {
    scoreMap[game.gameId] = game.score ?? {};
    return scoreMap;
  }, {});
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
  const [savedScoreByGameId, setSavedScoreByGameId] = useState({});
  const [isSavingCompetitionName, setIsSavingCompetitionName] = useState(false);
  const [competitionNameError, setCompetitionNameError] = useState('');
  const [competitionNameSuccess, setCompetitionNameSuccess] = useState('');
  const [shareFeedback, setShareFeedback] = useState('');
  const [adminToken, setAdminToken] = useState('');
  const [showOnlyUnscoredGames, setShowOnlyUnscoredGames] = useState(false);
  const [isSharePanelOpen, setIsSharePanelOpen] = useState(false);
  const [showAdminLinkBanner, setShowAdminLinkBanner] = useState(false);

  const canManage = Boolean(adminToken);
  const isManageAccessMissing = false;
  const showFullHeader = true;
  const adminRequestOptions = {
    headers: createEditTokenHeaders(adminToken),
  };
  const permissionDeniedMessage = '관리자 권한이 필요합니다. 관리자 링크로 접속해 주세요.';
  const heroTitle = isManageAccessMissing
    ? '관리자 링크 필요'
    : showFullHeader
      ? mode === COMPETITION_MODES.MANAGE
        ? '대진표 관리'
        : '점수 입력 화면'
      : '경기 점수 입력';
  const heroDescription = isManageAccessMissing
    ? '대진표 관리는 관리자 링크로 접속해야 사용할 수 있습니다.'
      : showFullHeader
      ? mode === COMPETITION_MODES.MANAGE
        ? '경기 시작 전 참가자 정보와 경기 배정을 조정하는 화면입니다.'
        : '배정된 경기를 확인하고 점수를 입력하세요.'
      : '배정된 경기를 확인하고 점수를 입력하세요.';
  const userShareUrl = `${window.location.origin}/competitions/${publicId}`;
  const manageShareUrl =
    canManage && adminToken
      ? `${window.location.origin}/competitions/${publicId}?token=${adminToken}`
      : '';
  const requestedView = new URLSearchParams(location.search).get('view');

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

  const dismissAdminLinkBanner = () => {
    dismissCompetitionAdminLinkPrompt(publicId);
    setShowAdminLinkBanner(false);
  };

  const copyAdminLinkFromBanner = async () => {
    const copied = await copyUrl(manageShareUrl, '관리자 링크를 복사했어요.');
    if (copied) {
      dismissAdminLinkBanner();
    }
  };

  const toggleSharePanel = () => {
    setIsSharePanelOpen((prev) => !prev);
  };

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const tokenFromUrl = searchParams.get('token');

    if (tokenFromUrl) {
      saveCompetitionEditToken(publicId, tokenFromUrl);
      setAdminToken(tokenFromUrl);
      navigate(`/competitions/${publicId}`, { replace: true });
      return;
    }

    setAdminToken(getCompetitionEditToken(publicId));
  }, [location.pathname, location.search, navigate, publicId]);

  useEffect(() => {
    setShowAdminLinkBanner(shouldShowCompetitionAdminLinkPrompt(publicId));
  }, [publicId]);

  useEffect(() => {
    let isActive = true;

    async function fetchCompetition() {
      try {
        setIsLoading(true);
        setErrorMessage('');
        const response = await apiRequest.get(`/api/competitions/${publicId}`);
        const entriesResponse = await apiRequest.get(
          `/api/competitions/${publicId}/entries`
        );
        if (isActive) {
          const data = getResponseData(response);
          const entryData = getResponseData(entriesResponse);
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
          setCompetition(data);
          setTiebreakOpenGames(createTiebreakOpenState(data?.games));
          setSavedScoreByGameId(createSavedScoreMap(data?.games));
          setBalance(createBalanceFromStat(data?.stat));
          setEntries(entryData);
          setOriginalEntries(entryData);
          setMode(
            resolveInitialMode({
              status: data?.status,
              requestedView,
            })
          );
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
  }, [canManage, publicId, requestedView]);

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

  const visibleRounds = useMemo(() => {
    if (mode !== COMPETITION_MODES.SCORE || !showOnlyUnscoredGames) {
      return rounds;
    }

    return rounds
      .map((roundGroup) => ({
        ...roundGroup,
        games: roundGroup.games.filter(
          (game) => !hasRecordedScore({ score: savedScoreByGameId[game.gameId] })
        ),
      }))
      .filter((roundGroup) => roundGroup.games.length > 0);
  }, [mode, rounds, savedScoreByGameId, showOnlyUnscoredGames]);

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
        payload
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
      setSavedScoreByGameId((prev) => ({
        ...prev,
        [data.gameId]: data.score ?? {},
      }));
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
    setGameEditor((prev) => {
      if (!prev) {
        return prev;
      }

      const nextTeam = [...prev[team]];
      nextTeam[index] = value;

      return {
        ...prev,
        [team]: nextTeam,
      };
    });
  };

  const selectedGameEditorIds = gameEditor
    ? [...gameEditor.teamA, ...gameEditor.teamB].filter(Boolean)
    : [];

  const getEntryRoundConflictLabel = (entryId) => {
    if (!gameEditor || !competition?.games?.length) {
      return '';
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

    const currentValue = gameEditor[team][index];
    const entryIdText = String(entryId);

    return (
      selectedGameEditorIds.includes(entryIdText) && currentValue !== entryIdText
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
      setGameEditorError('한 경기 안에서 같은 선수를 중복 선택할 수 없어요.');
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
      setSavedScoreByGameId((prev) => ({
        ...prev,
        [data.game.gameId]: data.game.score ?? {},
      }));
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
    <main className="competition-detail-page">
      <MotionHeader
        className="competition-detail-hero"
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={itemTransition}
      >
        <div className="competition-detail-hero-content">
          {canManage && <span className="competition-admin-badge">관리자</span>}
          <h1>{heroTitle}</h1>
          <p>{heroDescription}</p>
          {isManageAccessMissing && (
            <div className="competition-admin-warning">
              관리자 링크로 접속해야 대진표를 수정할 수 있습니다.
            </div>
          )}
          {
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
          }
        </div>
      </MotionHeader>

      {!isLoading &&
        !errorMessage &&
        competition &&
        mode === COMPETITION_MODES.MANAGE && (
        <section className="competition-admin-actions">
          {canManage && showAdminLinkBanner && (
            <div className="competition-admin-link-banner">
              <p>
                경기 시작 전 조정이 끝나면 일반 링크만 공유해도 됩니다.
                관리자 링크는 나중에 다시 수정해야 할 때를 대비해 운영자만 보관해 주세요.
              </p>
              <div>
                <button type="button" onClick={copyAdminLinkFromBanner}>
                  관리자 링크 복사
                </button>
                <button
                  type="button"
                  className="secondary"
                  onClick={dismissAdminLinkBanner}
                >
                  나중에 하기
                </button>
              </div>
            </div>
          )}

          <div className={`competition-share-panel ${isSharePanelOpen ? 'open' : ''}`}>
            <button
              type="button"
              className="competition-share-toggle"
              onClick={
                canManage
                  ? toggleSharePanel
                  : () =>
                    copyUrl(
                      userShareUrl,
                      '참여 링크를 복사했어요.'
                    )
              }
              aria-expanded={canManage && isSharePanelOpen}
            >
              링크 복사
            </button>
            {!canManage && shareFeedback && (
              <p className="competition-share-feedback">{shareFeedback}</p>
            )}

            {canManage && isSharePanelOpen && (
              <div className={`competition-share-actions ${canManage ? '' : 'viewer'}`}>
                <button
                  type="button"
                  className="secondary"
                  onClick={() =>
                    copyUrl(
                      userShareUrl,
                      '참여 링크를 복사했어요.'
                    )
                  }
                >
                  참여 링크 복사
                </button>
                {canManage && (
                  <>
                <button
                  type="button"
                  onClick={() =>
                    copyUrl(
                      manageShareUrl,
                      '관리자 링크를 복사했어요.'
                    )
                  }
                >
                  관리자 링크 복사
                </button>
                <p>
                  관리자 링크는 수정 권한이 있으니 필요한 사람에게만 공유해주세요.
                </p>
                  </>
                )}
                {shareFeedback && (
                  <p className="competition-share-feedback">{shareFeedback}</p>
                )}
              </div>
            )}
          </div>

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
          onClose={closeEntryEditor}
          onSave={saveEntryNames}
          onUpdateEntryName={updateEntryName}
        />
      )}

      <MotionSection
        className="competition-detail-panel"
        initial={{ opacity: 0, y: 18 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ ...itemTransition, delay: 0.08 }}
      >
        {renderSummaryPanel()}
      </MotionSection>

      {!isLoading &&
        !errorMessage &&
        competition &&
        mode === COMPETITION_MODES.SCORE && (
        <section className="competition-secondary-actions">
          <button
            className="competition-result-button"
            type="button"
            onClick={() => navigate(`/competitions/${publicId}/result`)}
          >
            경기 결과 보기
          </button>
        </section>
      )}

      {!isLoading && !errorMessage && rounds.length > 0 && (
        <MotionSection
          key={mode}
          className="competition-schedule"
          initial={{ opacity: 0, y: 14 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
        >
          {mode === COMPETITION_MODES.SCORE && (
            <div className="score-filter-bar">
              <button
                type="button"
                className={showOnlyUnscoredGames ? 'active' : ''}
                onClick={() => setShowOnlyUnscoredGames((prev) => !prev)}
              >
                {showOnlyUnscoredGames ? '전체 경기 보기' : '미입력 경기만 보기'}
              </button>
              <span>
                {showOnlyUnscoredGames
                  ? `${visibleRounds.reduce((count, item) => count + item.games.length, 0)}경기 남음`
                  : '점수 입력이 필요한 경기만 빠르게 볼 수 있어요.'}
              </span>
            </div>
          )}

          {visibleRounds.length === 0 ? (
            <div className="competition-detail-state">
              점수를 입력할 경기가 없어요.
            </div>
          ) : (
            visibleRounds.map(({ round, games, roundPlayerStatus }) => (
            <div className="round-section" key={round}>
              <div className="round-heading">
                <h2>
                  <span>{round}</span>
                  Round
                </h2>
                {(roundPlayerStatus.duplicatedPlayers.length > 0 ||
                  roundPlayerStatus.idlePlayers.length > 0) && (
                  <div className="round-warning">
                    {roundPlayerStatus.duplicatedPlayers.length > 0 && (
                      <p>
                        <strong>중복 출전</strong>
                        {roundPlayerStatus.duplicatedPlayers
                          .map(
                            (player) =>
                              `${player.playerName} ${player.count}회`
                          )
                          .join(', ')}
                      </p>
                    )}
                    {roundPlayerStatus.idlePlayers.length > 0 && (
                      <p>
                        <strong>쉬는 선수</strong>
                        {roundPlayerStatus.idlePlayers
                          .map((player) => player.playerName)
                          .join(', ')}
                      </p>
                    )}
                  </div>
                )}
              </div>

              <div className="game-list">
                {games.map((game) => {
                  const isEditing = gameEditor?.game?.gameId === game.gameId;

                  return (
                    <div className="game-card-group" key={game.gameId}>
                      <CompetitionGameCard
                        game={game}
                        mode={mode}
                        canManage={canManage}
                        isEditing={isEditing}
                        isTiebreakOpen={Boolean(tiebreakOpenGames[game.gameId])}
                        isSavingScore={savingScoreGameIds.includes(game.gameId)}
                        scoreFeedback={scoreFeedbackByGameId[game.gameId]}
                        scoreError={scoreErrorByGameId[game.gameId]}
                        onOpenGameEditor={openGameEditor}
                        onToggleTiebreak={toggleTiebreak}
                        onUpdateGameScore={updateGameScore}
                        onSaveGameScore={saveGameScore}
                      />

                      {canManage && isEditing && (
                        <CompetitionGameEditor
                          gameEditor={gameEditor}
                          errorMessage={gameEditorError}
                          isSaving={isSavingGameEntries}
                          isEntrySelectedElsewhere={isEntrySelectedElsewhere}
                          getEntryRoundConflictLabel={
                            getEntryRoundConflictLabel
                          }
                          onClose={closeGameEditor}
                          onSave={saveGameEntries}
                          onUpdateSelection={updateGameEditorSelection}
                        />
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
            ))
          )}
        </MotionSection>
      )}
    </main>
  );
}

export default CompetitionDetail;
