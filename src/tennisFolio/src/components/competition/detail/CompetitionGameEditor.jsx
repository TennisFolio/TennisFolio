import { useState } from 'react';
import { motion } from 'framer-motion';

const itemTransition = { duration: 0.28, ease: [0.22, 1, 0.36, 1] };
const MotionSection = motion.section;

function getGenderLabel(gender) {
  return gender === 'MALE' ? '남' : '여';
}

function getGenderClass(gender) {
  return gender === 'MALE' ? 'male' : 'female';
}

function CompetitionGameEditor({
  gameEditor,
  errorMessage,
  isSaving,
  isEntrySelectedElsewhere,
  getEntryRoundConflictLabel,
  onClose,
  onSave,
  onUpdateSelection,
}) {
  const [openDropdownKey, setOpenDropdownKey] = useState('');

  const getSelectedEntry = (team, index) => {
    const selectedId = gameEditor[team][index];
    return gameEditor.entries.find(
      (entry) => String(entry.competitionEntryId) === String(selectedId)
    );
  };

  const toggleDropdown = (team, index) => {
    const key = `${team}-${index}`;
    setOpenDropdownKey((prev) => (prev === key ? '' : key));
  };

  const selectEntry = (team, index, competitionEntryId) => {
    onUpdateSelection(team, index, String(competitionEntryId));
    setOpenDropdownKey('');
  };

  const renderPlayerDropdown = (team, index) => {
    const key = `${team}-${index}`;
    const selectedEntry = getSelectedEntry(team, index);

    return (
      <div className="player-dropdown">
        <button
          className={`player-dropdown-trigger ${
            selectedEntry ? 'selected' : ''
          }`}
          type="button"
          onClick={() => toggleDropdown(team, index)}
        >
          {selectedEntry ? (
            <>
              <span className={getGenderClass(selectedEntry.gender)}>
                {getGenderLabel(selectedEntry.gender)}
              </span>
              <strong>{selectedEntry.playerName}</strong>
            </>
          ) : (
            <>
              <span>-</span>
              <strong>선수 선택</strong>
            </>
          )}
        </button>

        {openDropdownKey === key && (
          <div className="player-dropdown-menu">
            {gameEditor.entries.map((entry) => {
              const isInactive = entry.status === 'INACTIVE';
              const isSelectedElsewhere = isEntrySelectedElsewhere(
                entry.competitionEntryId,
                team,
                index
              );
              const isDisabled = isInactive;
              const conflictLabel = getEntryRoundConflictLabel(
                entry.competitionEntryId
              );

              return (
                <button
                  className={`player-dropdown-option ${
                    isDisabled ? 'disabled' : ''
                  }`}
                  disabled={isDisabled}
                  key={entry.competitionEntryId}
                  type="button"
                  onClick={() =>
                    selectEntry(team, index, entry.competitionEntryId)
                  }
                >
                  <span className={getGenderClass(entry.gender)}>
                    {getGenderLabel(entry.gender)}
                  </span>
                  <strong>{entry.playerName}</strong>
                  {isInactive ? (
                    <em>대기</em>
                  ) : isSelectedElsewhere ? (
                    <em>선택됨</em>
                  ) : (
                    conflictLabel && <em>{conflictLabel}</em>
                  )}
                </button>
              );
            })}
          </div>
        )}
      </div>
    );
  };

  return (
    <MotionSection
      key={gameEditor.game.gameId}
      className="game-editor-panel"
      initial={{ opacity: 0, y: -12, scale: 0.98 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={itemTransition}
    >
      <div className="entry-editor-heading">
        <div>
          <h2>
            {gameEditor.game.round}라운드 {gameEditor.game.court}번 코트
          </h2>
          <p>A팀과 B팀 선수를 선택해 경기 편성을 변경합니다.</p>
        </div>
      </div>

      {errorMessage && <div className="entry-editor-error">{errorMessage}</div>}

      <div className="game-editor-teams">
        <div className="game-editor-team">
          <strong>A팀</strong>
          {renderPlayerDropdown('teamA', 0)}
          {renderPlayerDropdown('teamA', 1)}
        </div>
        <div className="game-editor-team">
          <strong>B팀</strong>
          {renderPlayerDropdown('teamB', 0)}
          {renderPlayerDropdown('teamB', 1)}
        </div>
      </div>

      <div className="entry-editor-actions">
        <button
          className="secondary"
          type="button"
          onClick={onClose}
          disabled={isSaving}
        >
          취소
        </button>
        <button type="button" onClick={onSave} disabled={isSaving}>
          {isSaving ? '저장 중' : '저장'}
        </button>
      </div>
    </MotionSection>
  );
}

export default CompetitionGameEditor;
