import { COMPETITION_MODES } from './CompetitionDetailSummary';

import GameScoreEditor from './GameScoreEditor';

const MATCH_TYPE_LABELS = {
  MIXED: '혼복',
  MALE: '남복',
  FEMALE: '여복',
  M2F2_SPLIT: '남2:여2',
  RANDOM_M3F1: '랜덤',
  RANDOM_M1F3: '랜덤',
};

const MATCH_TYPE_CLASSES = {
  MIXED: 'mixed',
  MALE: 'male',
  FEMALE: 'female',
  M2F2_SPLIT: 'mixed',
  RANDOM_M3F1: 'random',
  RANDOM_M1F3: 'random',
};

function getTeamPlayers(team) {
  return team?.players ?? [];
}

function hasScoreValue(score = {}) {
  return [score.teamAScore, score.teamBScore].some(
    (value) => value !== null && value !== undefined && value !== ''
  );
}

function TeamView({ game, teamKey, label }) {
  const isTeamB = teamKey === 'teamB';

  return (
    <div className={`match-team ${isTeamB ? 'match-team-b' : 'match-team-a'}`}>
      <span className="team-badge">{label}팀</span>
      <div className="team-player-list">
        {getTeamPlayers(game[teamKey]).map((player, index) => (
          <p key={`${game.gameId}-${label}-${index}`}>
            <span
              className={`player-gender-badge ${
                player.gender === 'MALE' ? 'male' : 'female'
              }`}
            >
              {player.gender === 'MALE' ? '남' : '여'}
            </span>
            <span>{player.playerName}</span>
          </p>
        ))}
      </div>
    </div>
  );
}

function CompetitionGameCard({
  game,
  mode,
  canManage,
  isClubSession = false,
  canComplete = false,
  isEditing,
  isTiebreakOpen,
  isSavingScore,
  scoreFeedback,
  scoreError,
  scoreAutoSave = false,
  scoreSaveLabel = '점수 저장',
  busyClubAction,
  clubScoreDraft,
  canEditCompletedScore = false,
  isCompletionConfirming = false,
  completionConfirmationMessage = '',
  onOpenGameEditor,
  onPrepareCompletion,
  onUpdateCompletionScore,
  onRequestCompletionConfirm,
  onCancelCompletionConfirm,
  onSubmitCompletion,
  onSubmitCompletedScoreEdit = () => {},
  onDeleteGame,
  onToggleTiebreak,
  onUpdateGameScore,
  onSaveGameScore,
}) {
  const isReadyClubGame =
    isClubSession &&
    canComplete &&
    mode === COMPETITION_MODES.MANAGE &&
    (game.status === 'READY' || game.status === 'IN_PROGRESS');
  const isCompletedClubGame =
    isClubSession &&
    mode === COMPETITION_MODES.MANAGE &&
    game.status === 'COMPLETED';
  const isWaitingClubGame =
    isClubSession &&
    mode === COMPETITION_MODES.MANAGE &&
    !canComplete &&
    (game.status === 'READY' || game.status === 'IN_PROGRESS');
  const clubScore =
    clubScoreDraft ??
    (isReadyClubGame ? { teamAScore: 0, teamBScore: 0 } : game.score);

  const renderGameTools = () => {
    if (isReadyClubGame) {
      return (
        <GameScoreEditor
          score={clubScore}
          variant="compact"
          showSave
          isSaving={busyClubAction === `complete-${game.gameId}`}
          saveLabel="경기 완료"
          savingLabel="완료 중"
          onFocus={() => onPrepareCompletion(game.gameId)}
          onChange={(field, value) =>
            onUpdateCompletionScore(game.gameId, field, value)
          }
          onSave={() => onRequestCompletionConfirm(game.gameId)}
        />
      );
    }

    if (isCompletedClubGame && hasScoreValue(game.score)) {
      if (canEditCompletedScore) {
        return (
          <GameScoreEditor
            score={clubScore}
            variant="compact"
            showSave
            isSaving={busyClubAction === `score-${game.gameId}`}
            onChange={(field, value) =>
              onUpdateCompletionScore(game.gameId, field, value)
            }
            onSave={() => onSubmitCompletedScoreEdit(game.gameId)}
          />
        );
      }

      return (
        <div className="completed-score completed-score-center">
          {game.score?.teamAScore ?? 0} : {game.score?.teamBScore ?? 0}
        </div>
      );
    }

    if (mode === COMPETITION_MODES.MANAGE) {
      return (
        <div className="match-versus" aria-label="대기">
          VS
        </div>
      );
    }

    return (
      <GameScoreEditor
        score={game.score}
        variant="full"
        showTiebreak
        isTiebreakOpen={isTiebreakOpen}
        isSaving={isSavingScore}
        saveLabel={scoreSaveLabel}
        showSave={!scoreAutoSave}
        errorMessage={scoreError}
        feedbackMessage={scoreFeedback}
        onToggleTiebreak={() => onToggleTiebreak(game.gameId)}
        onChange={(field, value) => onUpdateGameScore(game.gameId, field, value)}
        onSave={() => onSaveGameScore(game.gameId)}
      />
    );
  };

  const renderClubCompletionPanel = () => {
    if (!isReadyClubGame || !isCompletionConfirming) {
      return null;
    }

    return (
      <div className="club-completion-modal-backdrop" role="presentation">
        <div
          className="club-completion-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby={`completion-confirm-title-${game.gameId}`}
        >
          <div>
            <h3 id={`completion-confirm-title-${game.gameId}`}>
              경기 완료 확인
            </h3>
            <p>{completionConfirmationMessage}</p>
          </div>
          <div className="club-completion-modal-actions">
            <button
              type="button"
              className="secondary"
              onClick={() => onCancelCompletionConfirm(game.gameId)}
            >
              취소
            </button>
            <button type="button" onClick={() => onSubmitCompletion(game.gameId)}>
              완료 확정
            </button>
          </div>
            </div>
      </div>
    );
  };

  return (
    <article
      className={`game-card ${isClubSession ? 'club-game-card' : ''} ${
        isWaitingClubGame ? 'waiting' : ''
      } ${isEditing ? 'editing' : ''}`}
    >
      <div className="game-card-top">
        {isClubSession ? (
          <span className="game-card-court-label">
            <b>{game.court}번 코트</b>
            <em>{isWaitingClubGame ? `대기 #${game.round}` : `#${game.round}`}</em>
          </span>
        ) : (
          <span>{game.court}번 코트</span>
        )}
        <strong className={MATCH_TYPE_CLASSES[game.matchType] ?? 'random'}>
          {MATCH_TYPE_LABELS[game.matchType] ?? game.matchType}
        </strong>
      </div>

      <div
        className={`game-match-row ${
          mode === COMPETITION_MODES.SCORE ? 'score-mode' : ''
        }`}
      >
        <TeamView game={game} teamKey="teamA" label="A" />
        {renderGameTools()}
        <TeamView game={game} teamKey="teamB" label="B" />
      </div>

      {renderClubCompletionPanel()}

      {canManage &&
        ((isClubSession && mode === COMPETITION_MODES.MANAGE &&
          (game.status === 'READY' || game.status === 'IN_PROGRESS')) ||
          !isClubSession) && (
        <div className="ready-game-actions">
          {isClubSession && (
            <>
              <button
                type="button"
                className="action-secondary"
                onClick={() => onOpenGameEditor(game)}
              >
                선수 변경
              </button>
              <button
                type="button"
                className="danger"
                disabled={busyClubAction === `delete-${game.gameId}`}
                onClick={() => onDeleteGame(game.gameId)}
              >
                삭제
              </button>
            </>
          )}
          {!isClubSession && (
            <button
              type="button"
              className="action-secondary"
              onClick={() => onOpenGameEditor(game)}
            >
              선수 변경
            </button>
          )}
        </div>
      )}
    </article>
  );
}

export default CompetitionGameCard;
