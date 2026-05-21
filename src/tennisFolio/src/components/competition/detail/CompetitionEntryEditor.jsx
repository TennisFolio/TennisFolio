import { useState } from 'react';
import { motion } from 'framer-motion';

const itemTransition = { duration: 0.28, ease: [0.22, 1, 0.36, 1] };
const MotionSection = motion.section;
const GENDER_OPTIONS = [
  { value: 'MALE', label: '\uB0A8', className: 'male' },
  { value: 'FEMALE', label: '\uC5EC', className: 'female' },
];
const ENTRY_STATUS_LABELS = {
  ACTIVE: '\uCC38\uAC00',
  INACTIVE: '\uB300\uAE30',
};

function CompetitionEntryEditor({
  entries,
  entryGameCounts,
  canManage,
  errorMessage,
  successMessage,
  isLoading,
  isSaving,
  isClubSession = false,
  newEntryDraft,
  onClose,
  onSave,
  onUpdateEntryName,
  onUpdateEntryStatus,
  onUpdateNewEntry,
  onCreateEntry,
}) {
  const [isGenderDropdownOpen, setIsGenderDropdownOpen] = useState(false);
  const selectedGender =
    GENDER_OPTIONS.find((option) => option.value === newEntryDraft?.gender) ??
    GENDER_OPTIONS[0];

  const selectGender = (gender) => {
    onUpdateNewEntry((prev) => ({
      ...prev,
      gender,
    }));
    setIsGenderDropdownOpen(false);
  };

  return (
    <MotionSection
      className="entry-editor-panel"
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={itemTransition}
    >
      <div className="entry-editor-heading">
        <div>
          <h2>참가자 명단</h2>
          <p>
            {!canManage
              ? isClubSession
                ? '현재 참가 상태와 경기 수를 확인할 수 있어요.'
                : '참가자별 경기 수를 확인할 수 있어요.'
              : isClubSession
              ? '참가자를 추가하거나 현재 참가 상태를 바꿀 수 있어요.'
              : '이름은 9자까지 입력할 수 있어요.'}
          </p>
        </div>
      </div>

      {canManage && isClubSession && (
        <div className="entry-create-row">
          <input
            maxLength={9}
            placeholder="참가자 이름"
            value={newEntryDraft.playerName}
            onChange={(event) =>
              onUpdateNewEntry((prev) => ({
                ...prev,
                playerName: event.target.value,
              }))
            }
          />
          <div className="entry-gender-dropdown">
            <button
              className={`entry-gender-trigger ${selectedGender.className}`}
              type="button"
              aria-expanded={isGenderDropdownOpen}
              onClick={() => setIsGenderDropdownOpen((prev) => !prev)}
            >
              <span className={selectedGender.className}>
                {selectedGender.label}
              </span>
            </button>
            {isGenderDropdownOpen && (
              <div className="entry-gender-menu">
                {GENDER_OPTIONS.map((option) => (
                  <button
                    className={`entry-gender-option ${option.className} ${
                      option.value === selectedGender.value ? 'selected' : ''
                    }`}
                    key={option.value}
                    type="button"
                    onClick={() => selectGender(option.value)}
                  >
                    <span className={option.className}>{option.label}</span>
                  </button>
                ))}
              </div>
            )}
          </div>
          <button type="button" onClick={onCreateEntry} disabled={isSaving}>
            추가
          </button>
        </div>
      )}

      {errorMessage && <div className="entry-editor-error">{errorMessage}</div>}
      {successMessage && (
        <div className="entry-editor-success">{successMessage}</div>
      )}

      {isLoading ? (
        <div className="entry-editor-state">참가자 정보를 불러오고 있어요.</div>
      ) : (
        <div className="entry-editor-list">
          {entries.map((entry) => (
            <div
              className={`entry-editor-row ${isClubSession ? 'club-session' : ''} ${
                !canManage ? 'viewer' : ''
              }`}
              key={entry.competitionEntryId}
            >
              <span className={entry.gender === 'MALE' ? 'male' : 'female'}>
                {entry.gender === 'MALE' ? '남' : '여'}
              </span>
              {!canManage ? (
                <p className="entry-player-name">{entry.playerName}</p>
              ) : (
                <input
                  maxLength={9}
                  value={entry.playerName}
                  readOnly={!canManage}
                  onChange={(event) =>
                    onUpdateEntryName(
                      entry.competitionEntryId,
                      event.target.value
                    )
                  }
                />
              )}
              <strong>
                {entryGameCounts.get(entry.competitionEntryId) ?? 0}경기
              </strong>
              {canManage && isClubSession && (
                <div className="entry-status-control" aria-label="참가 상태">
                  <button
                    className={entry.status === 'ACTIVE' ? 'active' : ''}
                    type="button"
                    disabled={isSaving || entry.status === 'ACTIVE'}
                    onClick={() =>
                      onUpdateEntryStatus(entry.competitionEntryId, 'ACTIVE')
                    }
                  >
                    {'\uCC38\uAC00'}
                  </button>
                  <button
                    className={entry.status === 'INACTIVE' ? 'active' : ''}
                    type="button"
                    disabled={isSaving || entry.status === 'INACTIVE'}
                    onClick={() =>
                      onUpdateEntryStatus(entry.competitionEntryId, 'INACTIVE')
                    }
                  >
                    {'\uB300\uAE30'}
                  </button>
                </div>
              )}
              {!canManage && isClubSession && (
                <span
                  className={`entry-status-label ${
                    entry.status === 'ACTIVE' ? 'active' : 'inactive'
                  }`}
                >
                  {ENTRY_STATUS_LABELS[entry.status] ?? entry.status}
                </span>
              )}
              {/*
                <button
                  className={`entry-status-toggle ${
                    entry.status === 'ACTIVE' ? 'active' : ''
                  }`}
                  type="button"
                  disabled={isSaving}
                  onClick={() =>
                    onUpdateEntryStatus(
                      entry.competitionEntryId,
                      entry.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
                    )
                  }
                >
                  {entry.status === 'ACTIVE' ? '참가중' : '대기'}
                </button>
              */}
            </div>
          ))}
        </div>
      )}

      <div className={`entry-editor-actions ${canManage ? '' : 'viewer'}`}>
        <button
          className="secondary"
          type="button"
          onClick={onClose}
          disabled={isSaving}
        >
          취소
        </button>
        {canManage && (
          <button type="button" onClick={onSave} disabled={isLoading || isSaving}>
          {isSaving ? '저장 중' : '저장'}
          </button>
        )}
      </div>
    </MotionSection>
  );
}

export default CompetitionEntryEditor;
