const quotaModes = [
  { value: 'NONE', label: '제한 없음' },
  { value: 'TOTAL', label: '전체 정원' },
  { value: 'GENDER', label: '성별 정원' },
];

function MeetingSettingsStep({
  form,
  isSubmitting,
  submitLabel,
  submittingLabel,
  onFieldChange,
  onPrevious,
  onSubmit,
}) {
  return (
    <section className="meeting-panel meeting-settings-panel" aria-label="모임 설정">
      <h2>2 / 2 모임 설정</h2>
      <div>
        <div
          className="meeting-toggle meeting-settings-toggle"
          role="group"
          aria-label="정원 방식"
        >
          {quotaModes.map((mode) => (
            <button
              type="button"
              key={mode.value}
              className={`meeting-button ${
                form.quotaMode === mode.value ? 'primary' : ''
              }`}
              onClick={() => onFieldChange('quotaMode', mode.value)}
            >
              {mode.label}
            </button>
          ))}
        </div>
      </div>
      {form.quotaMode === 'TOTAL' && (
        <label className="meeting-field">
          <span>전체 참석 정원</span>
          <input
            type="number"
            min="1"
            value={form.maxParticipants}
            onChange={(event) =>
              onFieldChange('maxParticipants', event.target.value)
            }
          />
        </label>
      )}
      {form.quotaMode === 'GENDER' && (
        <div className="meeting-grid two meeting-settings-quota-grid">
          <label className="meeting-field">
            <span>남성 정원</span>
            <input
              type="number"
              min="1"
              value={form.maxMaleParticipants}
              onChange={(event) =>
                onFieldChange('maxMaleParticipants', event.target.value)
              }
            />
          </label>
          <label className="meeting-field">
            <span>여성 정원</span>
            <input
              type="number"
              min="1"
              value={form.maxFemaleParticipants}
              onChange={(event) =>
                onFieldChange('maxFemaleParticipants', event.target.value)
              }
            />
          </label>
        </div>
      )}
      <div className="meeting-grid two meeting-settings-game-grid">
        <label className="meeting-field">
          <span>코트 수</span>
          <input
            type="number"
            min="1"
            value={form.courtCount}
            onChange={(event) => onFieldChange('courtCount', event.target.value)}
          />
        </label>
        <label className="meeting-field">
          <span>총 경기 수</span>
          <input
            type="number"
            min="1"
            value={form.totalGames}
            onChange={(event) => onFieldChange('totalGames', event.target.value)}
          />
        </label>
      </div>
      <div className="meeting-action-row meeting-form-action-row">
        <button type="button" className="meeting-button" onClick={onPrevious}>
          이전
        </button>
        <button
          type="button"
          className="meeting-button primary"
          disabled={isSubmitting}
          onClick={onSubmit}
        >
          {isSubmitting ? submittingLabel : submitLabel}
        </button>
      </div>
    </section>
  );
}

export default MeetingSettingsStep;
