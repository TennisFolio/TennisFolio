function MeetingCompetitionPanel({
  meeting,
  onOpenCompetition,
  onCreateCompetition,
  onAskDeleteCompetition,
  sameGenderDoublesOnly,
  onSameGenderDoublesOnlyChange,
  isClubMeeting,
  skillBalancedSchedule,
  onSkillBalancedScheduleChange,
  sameGenderDoublesOnlyUnavailable,
  sameGenderDoublesOnlyUnavailableReason,
  skillBalancedScheduleUnavailable,
  skillBalancedScheduleUnavailableReason,
  attendanceStatusCounts,
}) {
  return (
    <div className="meeting-operation-primary">
      <div className="meeting-operation-head">
        <div>
          <h2>대진표 생성</h2>
          <p>
            참석으로 표시한 인원만 대진표에 포함됩니다.
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
          <div className="meeting-competition-attendance-guide">
            <strong>대진표 포함: 참석 {attendanceStatusCounts.ATTENDING}명</strong>
            <span>
              대기 {attendanceStatusCounts.WAITING}명 · 불참 {attendanceStatusCounts.NOT_ATTENDING}명은 포함되지 않습니다.
            </span>
          </div>
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
          {isClubMeeting && (
            <div
              className={`meeting-same-gender-option ${
                skillBalancedScheduleUnavailable ? 'disabled' : ''
              }`}
            >
              <button
                type="button"
                className={`meeting-same-gender-switch ${
                  skillBalancedSchedule ? 'active' : ''
                }`}
                role="switch"
                aria-checked={skillBalancedSchedule}
                onClick={() => onSkillBalancedScheduleChange(!skillBalancedSchedule)}
              >
                <span className="meeting-same-gender-switch-track">
                  <span className="meeting-same-gender-switch-thumb" />
                </span>
                <span className="meeting-same-gender-switch-copy">
                  <strong>실력 기반 대진</strong>
                </span>
              </button>
              {skillBalancedScheduleUnavailable && (
                <p className="meeting-same-gender-option-warning">
                  {skillBalancedScheduleUnavailableReason}
                </p>
              )}
            </div>
          )}
        <button
          type="button"
          className="meeting-button primary full"
          onClick={onCreateCompetition}
          disabled={sameGenderDoublesOnlyUnavailable || skillBalancedScheduleUnavailable}
        >
          참석 {attendanceStatusCounts.ATTENDING}명으로 대진표 생성
        </button>
        </>
      )}
    </div>
  );
}

export default MeetingCompetitionPanel;
