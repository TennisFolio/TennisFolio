function GameScoreEditor({
  score = {},
  variant = 'compact',
  showTiebreak = false,
  isTiebreakOpen = false,
  isSaving = false,
  saveLabel = '점수 저장',
  savingLabel = '저장 중',
  showSave = true,
  errorMessage = '',
  feedbackMessage = '',
  onChange,
  onSave,
  onToggleTiebreak,
  onFocus,
}) {
  const isCompleteAction = saveLabel === '경기 완료';
  const handleChange = (field) => (event) => {
    onChange?.(field, event.target.value);
  };

  const renderScoreInput = (field, label) => (
    <input
      aria-label={label}
      inputMode="numeric"
      min="0"
      max="99"
      type="number"
      value={score?.[field] ?? ''}
      onFocus={onFocus}
      onChange={handleChange(field)}
    />
  );

  return (
    <div className={`game-score-editor ${variant}`} aria-label="경기 점수">
      {showTiebreak && (
        <button
          className={`game-score-tiebreak-toggle ${
            isTiebreakOpen ? 'active' : ''
          }`}
          type="button"
          onClick={onToggleTiebreak}
        >
          타이브레이크
        </button>
      )}

      <div className="game-score-input-row">
        {renderScoreInput('teamAScore', 'A팀 점수')}
        <span>:</span>
        {renderScoreInput('teamBScore', 'B팀 점수')}
      </div>

      {showTiebreak && isTiebreakOpen && (
        <div className="game-score-tiebreak-box">
          <p>타이브레이크 점수</p>
          <div className="game-score-tiebreak-row">
            {renderScoreInput('teamATiebreakScore', 'A팀 타이브레이크 점수')}
            <span>:</span>
            {renderScoreInput('teamBTiebreakScore', 'B팀 타이브레이크 점수')}
          </div>
        </div>
      )}

      {showSave && (
        <button
          className={`game-score-save-button ${
            isCompleteAction ? 'complete-action' : ''
          }`}
          type="button"
          disabled={isSaving}
          onClick={onSave}
        >
          {isSaving ? savingLabel : saveLabel}
        </button>
      )}

      {errorMessage && (
        <p className="game-score-message error">{errorMessage}</p>
      )}
      {!errorMessage && feedbackMessage && (
        <p className="game-score-message success">{feedbackMessage}</p>
      )}
    </div>
  );
}

export default GameScoreEditor;
