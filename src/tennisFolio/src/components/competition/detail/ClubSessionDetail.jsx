import { useEffect, useMemo, useState } from 'react';
import CompetitionGameCard from './CompetitionGameCard';
import CompetitionGameEditor from './CompetitionGameEditor';

function isCompletedGame(game) {
  if (game?.status === 'COMPLETED') {
    return true;
  }

  const score = game?.score ?? {};
  return [
    score.teamAScore,
    score.teamBScore,
    score.teamATiebreakScore,
    score.teamBTiebreakScore,
  ].some((value) => Number(value ?? 0) > 0);
}

function isPlannedGame(game) {
  return game?.status === 'PLANNED';
}

function sortUpcomingGames(a, b) {
  if ((a.round ?? 0) !== (b.round ?? 0)) {
    return (a.round ?? 0) - (b.round ?? 0);
  }
  return (a.court ?? 0) - (b.court ?? 0);
}

function sortWaitingGames(a, b) {
  return (a.gameId ?? 0) - (b.gameId ?? 0);
}

function sortHistoryGames(a, b) {
  const bCompletedAt = new Date(b.updatedAt ?? 0).getTime();
  const aCompletedAt = new Date(a.updatedAt ?? 0).getTime();
  if (bCompletedAt !== aCompletedAt) {
    return bCompletedAt - aCompletedAt;
  }
  if ((b.gameId ?? 0) !== (a.gameId ?? 0)) {
    return (b.gameId ?? 0) - (a.gameId ?? 0);
  }
  return (b.round ?? 0) - (a.round ?? 0);
}

function getGamePlayerEntries(game) {
  return [
    ...(game?.teamA?.players ?? []),
    ...(game?.teamB?.players ?? []),
  ];
}

const HISTORY_PREVIEW_LIMIT = 5;

function ClubSessionDetail({
  competition,
  canManage,
  courtCount,
  courtGroups,
  formattedCreatedAt,
  isSavingCompetitionName,
  competitionNameError,
  competitionNameSuccess,
  clubActionError,
  clubActionFeedback,
  busyClubAction,
  clubCompletionDrafts,
  clubAvailableEntryCount,
  gameEditor,
  gameEditorError,
  isSavingGameEntries,
  isEntrySelectedElsewhere,
  getEntryRoundConflictLabel,
  onCreateNextCourtGame,
  onSaveCompetitionName,
  onOpenGameEditor,
  onPrepareCompletion,
  onUpdateCompletionScore,
  onSubmitCompletion,
  onSubmitCompletedScoreEdit,
  onDeleteGame,
  onCloseGameEditor,
  onSaveGameEntries,
  onUpdateGameEditorSelection,
}) {
  const [nameDraft, setNameDraft] = useState(competition?.name ?? '');
  const [completionConfirmGameId, setCompletionConfirmGameId] = useState(null);
  const [isHistoryExpanded, setIsHistoryExpanded] = useState(false);

  useEffect(() => {
    setNameDraft(competition?.name ?? '');
  }, [competition?.name]);

  const games = competition?.games ?? [];
  const currentGameIds = new Set();
  const currentCourts = courtGroups.map(({ court, games: courtGames }) => {
    const playableCourtGames = [...courtGames]
      .filter((game) => game.status === 'IN_PROGRESS' || game.status === 'READY')
      .sort(sortUpcomingGames);
    const currentGame =
      playableCourtGames.find((game) => game.status === 'IN_PROGRESS') ??
      playableCourtGames.find((game) => game.status === 'READY') ??
      null;
    if (currentGame) {
      currentGameIds.add(currentGame.gameId);
    }
    return { court, currentGame };
  });

  const upcomingGames = games
    .filter((game) => !isCompletedGame(game))
    .filter((game) => isPlannedGame(game) || !currentGameIds.has(game.gameId))
    .sort(sortWaitingGames);
  const historyGames = games.filter(isCompletedGame).sort(sortHistoryGames);
  const visibleHistoryGames = isHistoryExpanded
    ? historyGames
    : historyGames.slice(0, HISTORY_PREVIEW_LIMIT);
  const hiddenHistoryGameCount = Math.max(
    0,
    historyGames.length - HISTORY_PREVIEW_LIMIT
  );
  const currentCourtAssignments = currentCourts
    .filter(({ currentGame }) => Boolean(currentGame))
    .map(({ court, currentGame }) => ({
      game: currentGame,
      label: `${court}번 코트`,
    }));
  const assignmentsByEntryId = currentCourtAssignments.reduce(
    (assignmentsByEntry, assignment) => {
      getGamePlayerEntries(assignment.game).forEach((player) => {
        const entryId = String(player.competitionEntryId);
        const existing = assignmentsByEntry.get(entryId) ?? {
          playerName: player.playerName,
          labels: [],
        };
        existing.labels.push(assignment.label);
        assignmentsByEntry.set(entryId, existing);
      });
      return assignmentsByEntry;
    },
    new Map()
  );
  const duplicateAssignments = Array.from(assignmentsByEntryId.values())
    .map(({ playerName, labels }) => ({
      playerName,
      labels: Array.from(new Set(labels)),
    }))
    .filter(({ labels }) => labels.length > 1);
  const activeCourtCount = currentCourts.filter(({ currentGame }) =>
    Boolean(currentGame)
  ).length;
  const emptyCourts = currentCourts
    .filter(({ currentGame }) => !currentGame)
    .map(({ court }) => court);
  const firstEmptyCourt = emptyCourts[0];
  const queuedGameCountByCourt = upcomingGames.reduce((counts, game) => {
    counts.set(game.court, (counts.get(game.court) ?? 0) + 1);
    return counts;
  }, new Map());
  const nextCreateCourt =
    firstEmptyCourt ??
    [...currentCourts].sort((a, b) => {
      const queueDiff =
        (queuedGameCountByCourt.get(a.court) ?? 0) -
        (queuedGameCountByCourt.get(b.court) ?? 0);
      if (queueDiff !== 0) {
        return queueDiff;
      }
      return (
        (a.currentGame?.round ?? Number.MAX_SAFE_INTEGER) -
        (b.currentGame?.round ?? Number.MAX_SAFE_INTEGER)
      );
    })[0]?.court;
  const hasEnoughAvailableEntries = clubAvailableEntryCount >= 4;
  const isNameChanged = nameDraft.trim() !== (competition?.name ?? '');
  const canCreateSingleGame =
    canManage && nextCreateCourt && hasEnoughAvailableEntries;

  const operationStats = useMemo(
    () => [
      { label: '진행', value: activeCourtCount },
      { label: '대기 경기', value: upcomingGames.length },
      { label: '완료', value: historyGames.length },
    ],
    [activeCourtCount, historyGames.length, upcomingGames.length]
  );
  const overviewStats = [
    { label: '참가 가능', value: `${clubAvailableEntryCount}명` },
    { label: '코트', value: `${courtCount}개` },
    { label: '운영 방식', value: '진행형' },
  ];

  const handleNameSubmit = (event) => {
    event.preventDefault();
    if (!canManage || !isNameChanged || isSavingCompetitionName) {
      return;
    }
    onSaveCompetitionName(nameDraft);
  };

  const handleCreateSingleGame = () => {
    if (!canCreateSingleGame) {
      return;
    }
    onCreateNextCourtGame(nextCreateCourt);
  };

  const getCompletionConfirmationMessage = (game) => {
    return '완료 후에는 진행 상태로 변경할 수 없습니다.';
  };

  const handleRequestCompletionConfirm = (gameId) => {
    setCompletionConfirmGameId(gameId);
  };

  const handleCancelCompletionConfirm = (gameId) => {
    if (completionConfirmGameId === gameId) {
      setCompletionConfirmGameId(null);
    }
  };

  const handleUpdateCompletionScore = (gameId, field, value) => {
    if (completionConfirmGameId === gameId) {
      setCompletionConfirmGameId(null);
    }
    onUpdateCompletionScore(gameId, field, value);
  };

  const handleSubmitCompletion = async (gameId) => {
    if (completionConfirmGameId !== gameId) {
      handleRequestCompletionConfirm(gameId);
      return;
    }

    setCompletionConfirmGameId(null);
    await onSubmitCompletion(gameId);
  };

  const renderGameEditor = (game) => {
    const isEditing = gameEditor?.game?.gameId === game.gameId;
    if (!canManage || !isEditing) {
      return null;
    }

    return (
      <CompetitionGameEditor
        gameEditor={gameEditor}
        errorMessage={gameEditorError}
        isSaving={isSavingGameEntries}
        isEntrySelectedElsewhere={isEntrySelectedElsewhere}
        getEntryRoundConflictLabel={getEntryRoundConflictLabel}
        onClose={onCloseGameEditor}
        onSave={onSaveGameEntries}
        onUpdateSelection={onUpdateGameEditorSelection}
      />
    );
  };

  const renderGameCard = (
    game,
    {
      canEditGame = canManage,
      canCompleteGame = false,
    } = {}
  ) => {
    const isEditing = gameEditor?.game?.gameId === game.gameId;

    return (
      <div
        className={`game-card-group ${isEditing ? 'editing' : ''}`}
        key={game.gameId}
      >
        <CompetitionGameCard
          game={game}
          mode="manage"
          canManage={canEditGame}
          isClubSession
          canComplete={canCompleteGame}
          isEditing={isEditing}
          isTiebreakOpen={false}
          isSavingScore={false}
          scoreFeedback=""
          scoreError=""
          busyClubAction={busyClubAction}
          clubScoreDraft={clubCompletionDrafts[game.gameId]}
          canEditCompletedScore={game.status === 'COMPLETED'}
          isCompletionConfirming={completionConfirmGameId === game.gameId}
          completionConfirmationMessage={getCompletionConfirmationMessage(game)}
          onOpenGameEditor={onOpenGameEditor}
          onPrepareCompletion={onPrepareCompletion}
          onUpdateCompletionScore={handleUpdateCompletionScore}
          onRequestCompletionConfirm={handleRequestCompletionConfirm}
          onCancelCompletionConfirm={handleCancelCompletionConfirm}
          onSubmitCompletion={handleSubmitCompletion}
          onSubmitCompletedScoreEdit={onSubmitCompletedScoreEdit}
          onDeleteGame={onDeleteGame}
          onToggleTiebreak={() => {}}
          onUpdateGameScore={() => {}}
          onSaveGameScore={() => {}}
        />
        {renderGameEditor(game)}
      </div>
    );
  };

  return (
    <>
      {competition && (
        <section className="club-mobile-header">
          {canManage ? (
            <form
              className="club-session-name-editor"
              onSubmit={handleNameSubmit}
              aria-label="대회 이름 변경"
            >
              <div className="club-session-name-row">
                <input
                  id="club-session-name"
                  aria-label="대회 이름"
                  type="text"
                  value={nameDraft}
                  maxLength={50}
                  onChange={(event) => setNameDraft(event.target.value)}
                />
                <button
                  type="submit"
                  disabled={!isNameChanged || isSavingCompetitionName}
                >
                  {isSavingCompetitionName ? '저장 중' : '저장'}
                </button>
              </div>
              <span className="club-session-created-at">
                생성일 {formattedCreatedAt}
              </span>
              {competitionNameError && (
                <p className="competition-name-error">{competitionNameError}</p>
              )}
              {competitionNameSuccess && (
                <p className="competition-name-success">
                  {competitionNameSuccess}
                </p>
              )}
            </form>
          ) : (
            <div className="competition-name-view club-session-name-view">
              <h2>{competition.name}</h2>
              <span>생성일 {formattedCreatedAt}</span>
            </div>
          )}

          <div
            className="competition-detail-summary club-session-overview"
            aria-label="진행형 대진 요약"
          >
            {overviewStats.map((stat) => (
              <div key={stat.label}>
                <p>{stat.label}</p>
                <strong>{stat.value}</strong>
              </div>
            ))}
          </div>

          <div className="club-mobile-stats" aria-label="운영 상태">
            {operationStats.map((stat) => (
              <div key={stat.label}>
                <span>{stat.label}</span>
                <strong>{stat.value}</strong>
              </div>
            ))}
          </div>
        </section>
      )}

      {clubActionError && (
        <section className="club-session-feedback">
          <p className="club-action-message error">{clubActionError}</p>
        </section>
      )}
      {!clubActionError && clubActionFeedback && (
        <section className="club-session-feedback">
          <p className="club-action-message success">{clubActionFeedback}</p>
        </section>
      )}

      {courtGroups.length > 0 && (
        <section className="competition-schedule club-mobile-operation">
          {canManage && (
            <div className="club-primary-actions club-court-actions">
              <button
                type="button"
                disabled={
                  !canCreateSingleGame ||
                  busyClubAction === `create-${nextCreateCourt}`
                }
                onClick={handleCreateSingleGame}
              >
                다음 경기 준비
              </button>
            </div>
          )}

          <section className="club-current-board">
            <div className="club-current-board-heading">
              <div>
                <h2>현재 코트</h2>
                <p>코트별 진행 중인 경기를 보여줍니다.</p>
                {duplicateAssignments.length > 0 ? (
                  <div className="club-duplicate-summary inline">
                    <strong>중복 배정 확인</strong>
                    <div className="club-duplicate-chip-row">
                      {duplicateAssignments.map(({ playerName }) => (
                        <span className="club-duplicate-chip" key={playerName}>
                          <b>{playerName}</b>
                        </span>
                      ))}
                    </div>
                  </div>
                ) : null}
              </div>
            </div>

            <div className="club-current-courts">
              {currentCourts.map(({ court, currentGame }) => {
                return (
                  <div
                    className={`club-current-court ${
                      gameEditor?.game?.gameId === currentGame?.gameId
                        ? 'editing'
                        : ''
                    }`}
                    key={court}
                  >
                    {!currentGame && (
                      <div className="club-court-strip">
                        <div>
                          <span>{court}번 코트</span>
                          <strong>비어 있음</strong>
                        </div>
                      </div>
                    )}

                    {currentGame ? (
                      renderGameCard(currentGame, { canCompleteGame: true })
                    ) : (
                      <div className="empty-court-state">
                        <strong>바로 시작할 경기가 없습니다</strong>
                        <p>참가자 4명이 가능하면 이 코트에 대기 경기를 만들 수 있습니다.</p>
                        {!canManage && <span>현재 배정된 경기가 없습니다.</span>}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </section>

          <section className="club-current-board club-waiting-board">
            <div className="club-current-board-heading">
              <div>
                <h2>대기 경기</h2>
                <p>코트별로 미리 만든 경기가 순서대로 올라갑니다.</p>
              </div>
            </div>

            {upcomingGames.length > 0 ? (
              <div className="game-list">
                {upcomingGames.map((game) => renderGameCard(game))}
              </div>
            ) : (
              <div className="club-upcoming-empty">
                <strong>대기 중인 경기가 없습니다</strong>
                <p>상단의 다음 경기 준비 버튼으로 진행 중에 미리 만들 수 있습니다.</p>
              </div>
            )}
          </section>

          <section className="club-current-board club-waiting-board club-history">
            <div className="club-current-board-heading club-history-heading">
              <div>
                <h2>완료 경기</h2>
                <p>
                  {historyGames.length > HISTORY_PREVIEW_LIMIT && !isHistoryExpanded
                    ? `최근 ${HISTORY_PREVIEW_LIMIT}경기만 보여줍니다.`
                    : '완료된 경기를 최신순으로 보여줍니다.'}
                </p>
              </div>
              <strong>{historyGames.length}경기</strong>
            </div>
            {historyGames.length > 0 ? (
              <>
                <div className="game-list">
                  {visibleHistoryGames.map((game) =>
                    renderGameCard(game, { canEditGame: false })
                  )}
                </div>
                {hiddenHistoryGameCount > 0 && (
                  <button
                    type="button"
                    className="club-history-more-button"
                    onClick={() => setIsHistoryExpanded((prev) => !prev)}
                  >
                    {isHistoryExpanded
                      ? `${HISTORY_PREVIEW_LIMIT}개만 보기`
                      : '전체보기'}
                  </button>
                )}
              </>
            ) : (
              <div className="club-upcoming-empty">
                <strong>완료된 경기가 없습니다</strong>
                <p>점수를 입력하고 경기 완료를 누르면 여기에 표시됩니다.</p>
              </div>
            )}
          </section>
        </section>
      )}
    </>
  );
}

export default ClubSessionDetail;
