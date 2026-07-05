function MeetingCompetitionPanel({
  meeting,
  onOpenCompetition,
  onCreateCompetition,
  onAskDeleteCompetition,
  sameGenderDoublesOnly,
  onSameGenderDoublesOnlyChange,
  sameGenderDoublesOnlyUnavailable,
  sameGenderDoublesOnlyUnavailableReason,
}) {
  return (
    <div className="meeting-operation-primary">
      <div className="meeting-operation-head">
        <div>
          <h2>대진표 생성</h2>
          <p>
            참석자 기준으로 대진표를 만들거나 생성된 대진표를 확인합니다.
          </p>
        </div>
      </div>
      {meeting.competitionCreated ? (
        <>
          {meeting.competitionPublicId && (
            <button
              type="button"
              className="meeting-button primary full"
              onClick={onOpenCompetition}
            >
              대진표 보기
            </button>
          )}
          <button
            type="button"
            className="meeting-button danger full"
            onClick={onAskDeleteCompetition}
          >
            대진표 삭제
          </button>
        </>
      ) : (
        <>
          <div
            className={`meeting-same-gender-option ${
              sameGenderDoublesOnlyUnavailable ? 'disabled' : ''
            }`}
          >
            <button
              type="button"
              className={`meeting-same-gender-switch ${
                sameGenderDoublesOnly ? 'active' : ''
              }`}
              role="switch"
              aria-checked={sameGenderDoublesOnly}
              onClick={() =>
                onSameGenderDoublesOnlyChange(!sameGenderDoublesOnly)
              }
            >
              <span className="meeting-same-gender-switch-track">
                <span className="meeting-same-gender-switch-thumb" />
              </span>
              <span className="meeting-same-gender-switch-copy">
                <strong>혼복 제외</strong>
              </span>
            </button>
            {sameGenderDoublesOnlyUnavailable && (
              <p className="meeting-same-gender-option-warning">
                {sameGenderDoublesOnlyUnavailableReason}
              </p>
            )}
          </div>
        <button
          type="button"
          className="meeting-button primary full"
          onClick={onCreateCompetition}
          disabled={sameGenderDoublesOnlyUnavailable}
        >
          참석자로 대진표 생성
        </button>
        </>
      )}
    </div>
  );
}

export default MeetingCompetitionPanel;
