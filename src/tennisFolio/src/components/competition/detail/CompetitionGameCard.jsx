import { COMPETITION_MODES } from './CompetitionDetailSummary';

const MATCH_TYPE_LABELS = {
  MIXED: '혼복',
  MALE: '남복',
  FEMALE: '여복',
  M2F2_SPLIT: '2:2 배정',
  RANDOM_M3F1: '남3/여1',
  RANDOM_M1F3: '남1/여3',
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
  isEditing,
  isTiebreakOpen,
  isSavingScore,
  scoreFeedback,
  scoreError,
  onOpenGameEditor,
  onToggleTiebreak,
  onUpdateGameScore,
  onSaveGameScore,
}) {
  const renderGameTools = () => {
    if (mode === COMPETITION_MODES.MANAGE) {
      return (
        <div className="match-versus" aria-label="대결">
          VS
        </div>
      );
    }

    return (
      <div className="match-score-editor">
        <button
          className={`tiebreak-toggle ${isTiebreakOpen ? 'active' : ''}`}
          type="button"
          onClick={() => onToggleTiebreak(game.gameId)}
        >
          {isTiebreakOpen ? '타이브레이크 사용 중' : '타이브레이크'}
        </button>

        <div className="score-input-row">
          <input
            aria-label="A팀 점수"
            inputMode="numeric"
            min="0"
            max="99"
            type="number"
            value={game.score?.teamAScore ?? ''}
            onChange={(event) =>
              onUpdateGameScore(game.gameId, 'teamAScore', event.target.value)
            }
          />
          <span>:</span>
          <input
            aria-label="B팀 점수"
            inputMode="numeric"
            min="0"
            max="99"
            type="number"
            value={game.score?.teamBScore ?? ''}
            onChange={(event) =>
              onUpdateGameScore(game.gameId, 'teamBScore', event.target.value)
            }
          />
        </div>

        {isTiebreakOpen && (
          <div className="tiebreak-score-box">
            <p>타이브레이크 점수</p>
            <div className="tiebreak-input-row">
              <input
                aria-label="A팀 타이브레이크 점수"
                inputMode="numeric"
                min="0"
                max="99"
                type="number"
                value={game.score?.teamATiebreakScore ?? ''}
                onChange={(event) =>
                  onUpdateGameScore(
                    game.gameId,
                    'teamATiebreakScore',
                    event.target.value
                  )
                }
              />
              <span>:</span>
              <input
                aria-label="B팀 타이브레이크 점수"
                inputMode="numeric"
                min="0"
                max="99"
                type="number"
                value={game.score?.teamBTiebreakScore ?? ''}
                onChange={(event) =>
                  onUpdateGameScore(
                    game.gameId,
                    'teamBTiebreakScore',
                    event.target.value
                  )
                }
              />
            </div>
          </div>
        )}

        <button
          className="score-save-button"
          type="button"
          disabled={isSavingScore}
          onClick={() => onSaveGameScore(game.gameId)}
        >
          {isSavingScore ? '저장 중' : '점수 저장'}
        </button>
        {scoreError && <p className="score-save-message error">{scoreError}</p>}
        {!scoreError && scoreFeedback && (
          <p className="score-save-message success">{scoreFeedback}</p>
        )}
      </div>
    );
  };

  return (
    <article className={`game-card ${isEditing ? 'editing' : ''}`}>
      <div className="game-card-top">
        <span>{game.court}번 코트</span>
        <strong className={MATCH_TYPE_CLASSES[game.matchType] ?? 'random'}>
          {MATCH_TYPE_LABELS[game.matchType] ?? game.matchType}
        </strong>
      </div>

      <div className="game-match-row">
        <TeamView game={game} teamKey="teamA" label="A" />
        {renderGameTools()}
        <TeamView game={game} teamKey="teamB" label="B" />
      </div>

      {mode === COMPETITION_MODES.MANAGE && (
        <div className="ready-game-actions">
          <button type="button" onClick={() => onOpenGameEditor(game)}>
            선수 변경
          </button>
        </div>
      )}
    </article>
  );
}

export default CompetitionGameCard;
