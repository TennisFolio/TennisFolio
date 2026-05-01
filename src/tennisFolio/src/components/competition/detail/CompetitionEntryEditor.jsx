import { motion } from 'framer-motion';

const itemTransition = { duration: 0.28, ease: [0.22, 1, 0.36, 1] };
const MotionSection = motion.section;

function CompetitionEntryEditor({
  entries,
  entryGameCounts,
  errorMessage,
  successMessage,
  isLoading,
  isSaving,
  onClose,
  onSave,
  onUpdateEntryName,
}) {
  return (
    <MotionSection
      className="entry-editor-panel"
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={itemTransition}
    >
      <div className="entry-editor-heading">
        <div>
          <h2>참가자 이름 수정</h2>
          <p>이름은 9자까지 입력할 수 있어요.</p>
        </div>
      </div>

      {errorMessage && <div className="entry-editor-error">{errorMessage}</div>}
      {successMessage && (
        <div className="entry-editor-success">{successMessage}</div>
      )}

      {isLoading ? (
        <div className="entry-editor-state">참가자 정보를 불러오고 있어요.</div>
      ) : (
        <div className="entry-editor-list">
          {entries.map((entry) => (
            <label className="entry-editor-row" key={entry.competitionEntryId}>
              <span className={entry.gender === 'MALE' ? 'male' : 'female'}>
                {entry.gender === 'MALE' ? '남' : '여'}
              </span>
              <input
                maxLength={9}
                value={entry.playerName}
                onChange={(event) =>
                  onUpdateEntryName(
                    entry.competitionEntryId,
                    event.target.value
                  )
                }
              />
              <strong>
                {entryGameCounts.get(entry.competitionEntryId) ?? 0}경기
              </strong>
            </label>
          ))}
        </div>
      )}

      <div className="entry-editor-actions">
        <button
          className="secondary"
          type="button"
          onClick={onClose}
          disabled={isSaving}
        >
          취소
        </button>
        <button type="button" onClick={onSave} disabled={isLoading || isSaving}>
          {isSaving ? '저장 중' : '저장'}
        </button>
      </div>
    </MotionSection>
  );
}

export default CompetitionEntryEditor;
