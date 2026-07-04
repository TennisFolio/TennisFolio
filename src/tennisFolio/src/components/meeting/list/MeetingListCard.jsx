function formatDateTime(value) {
  return value ? value.replace('T', ' ').slice(0, 16) : '';
}

function formatCount(value) {
  return Number.isFinite(Number(value)) ? Number(value) : 0;
}

function MeetingListCard({ meeting, onDelete, onManage, onShare }) {
  return (
    <article className="meeting-card">
      <div className="meeting-card-title-row">
        <div>
          <h2>{meeting.title}</h2>
          <span className="meeting-card-status">
            {meeting.competitionCreated ? '경기표 생성됨' : meeting.status}
          </span>
        </div>
        <button
          type="button"
          className="meeting-button danger small"
          onClick={() => onDelete(meeting)}
        >
          삭제
        </button>
      </div>

      <div className="meeting-card-meta">
        <span className="meeting-chip">{formatDateTime(meeting.startAt)}</span>
        <span className="meeting-chip">{formatDateTime(meeting.endAt)}</span>
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
          onClick={() => onManage(meeting.publicId)}
        >
          관리
        </button>
        <button
          type="button"
          className="meeting-button"
          onClick={() => onShare(meeting.publicId)}
        >
          공유
        </button>
      </div>
    </article>
  );
}

export default MeetingListCard;
