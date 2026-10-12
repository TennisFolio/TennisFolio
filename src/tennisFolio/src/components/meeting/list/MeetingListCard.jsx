import MeetingScheduleSummary from '../shared/MeetingScheduleSummary';

function formatCount(value) {
  return Number.isFinite(Number(value)) ? Number(value) : 0;
}

function MeetingListCard({ meeting, onOpen }) {
  return (
    <article className="meeting-card">
      <MeetingScheduleSummary startAt={meeting.startAt} endAt={meeting.endAt} />
      <div className="meeting-card-title-row">
        <div>
          <h2>{meeting.title}</h2>
          <span className="meeting-card-status">
            {meeting.competitionCreated ? '경기표 생성됨' : meeting.status}
          </span>
        </div>
      </div>

      <div className="meeting-card-meta" aria-label="모임 부가 정보">
        <span className="meeting-chip">
          {formatCount(meeting.courtCount)}코트
        </span>
        <span className="meeting-chip">
          {formatCount(meeting.totalGames)}경기
        </span>
      </div>

      <div className="meeting-chip-row">
        <span className="meeting-chip ok">
          참석 {formatCount(meeting.attendingCount)}
        </span>
        <span className="meeting-chip warning">
          대기 {formatCount(meeting.waitingCount)}
        </span>
        <span className="meeting-chip danger">
          불참 {formatCount(meeting.notAttendingCount)}
        </span>
      </div>

      <div className="meeting-card-actions">
        <button
          type="button"
          className="meeting-button primary"
          onClick={() => onOpen(meeting.publicId)}
        >
          상세 보기
        </button>
      </div>
    </article>
  );
}

export default MeetingListCard;
