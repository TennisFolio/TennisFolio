import { motion } from 'framer-motion';
import CompetitionGameCard from './CompetitionGameCard';
import CompetitionGameEditor from './CompetitionGameEditor';
import { COMPETITION_MODES } from './CompetitionDetailSummary';

const MotionSection = motion.section;

function FixedScheduleDetail({
  visibleRounds,
  gameEditor,
  gameEditorError,
  isSavingGameEntries,
  tiebreakOpenGames,
  savingScoreGameIds,
  scoreFeedbackByGameId,
  scoreErrorByGameId,
  onOpenResult,
  onOpenGameEditor,
  onToggleTiebreak,
  onUpdateGameScore,
  onSaveGameScore,
  isEntrySelectedElsewhere,
  getEntryRoundConflictLabel,
  onCloseGameEditor,
  onSaveGameEntries,
  onUpdateGameEditorSelection,
  canManage,
}) {
  return (
    <>
      <section className="competition-secondary-actions">
        <button
          className="competition-result-button"
          type="button"
          onClick={onOpenResult}
        >
          경기 결과 보기
        </button>
      </section>

      {visibleRounds.length > 0 && (
        <MotionSection
          className="competition-schedule"
          initial={{ opacity: 0, y: 14 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
        >
          {visibleRounds.map(({ round, games, roundPlayerStatus }) => (
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
                          .map((player) => `${player.playerName} ${player.count}회`)
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
                        mode={COMPETITION_MODES.SCORE}
                        canManage={canManage}
                        isClubSession={false}
                        isEditing={isEditing}
                        isTiebreakOpen={Boolean(tiebreakOpenGames[game.gameId])}
                        isSavingScore={savingScoreGameIds.includes(game.gameId)}
                        scoreFeedback={scoreFeedbackByGameId[game.gameId]}
                        scoreError={scoreErrorByGameId[game.gameId]}
                        scoreSaveLabel="경기 완료"
                        onOpenGameEditor={onOpenGameEditor}
                        onCompleteGame={() => {}}
                        onDeleteGame={() => {}}
                        onToggleTiebreak={onToggleTiebreak}
                        onUpdateGameScore={onUpdateGameScore}
                        onSaveGameScore={onSaveGameScore}
                      />

                      {canManage && isEditing && (
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
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </MotionSection>
      )}
    </>
  );
}

export default FixedScheduleDetail;
