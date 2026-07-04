function MeetingCompetitionPanel({
  meeting,
  onOpenCompetition,
  onCreateCompetition,
  onAskDeleteCompetition,
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
        <button
          type="button"
          className="meeting-button primary full"
          onClick={onCreateCompetition}
        >
          참석자로 대진표 생성
        </button>
      )}
    </div>
  );
}

export default MeetingCompetitionPanel;
