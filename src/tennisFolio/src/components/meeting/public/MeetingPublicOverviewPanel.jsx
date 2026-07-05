import AttendanceStatusSummary from '../shared/AttendanceStatusSummary';
import CapacityChips from '../shared/CapacityChips';
import { formatDate, formatTimeRange } from '../shared/meetingAttendanceUtils';

function MeetingPublicOverviewPanel({
  meeting,
  attendances,
  onCopyShareLink,
  onOpenCompetition,
}) {
  return (
    <section className="meeting-panel">
      <div className="meeting-card-title-row">
        <div>
          <h1>{meeting.title}</h1>
        </div>
      </div>
      <div className="meeting-card-meta">
        <span className="meeting-chip">{formatDate(meeting.startAt)}</span>
        <span className="meeting-chip">
          {formatTimeRange(meeting.startAt, meeting.endAt)}
        </span>
        <span className="meeting-chip">{meeting.courtCount}코트</span>
        <span className="meeting-chip">{meeting.totalGames}경기</span>
      </div>
      <CapacityChips meeting={meeting} attendances={attendances} />
      {meeting.note && <p className="meeting-note-box">{meeting.note}</p>}
      <AttendanceStatusSummary attendances={attendances} />
      <button type="button" className="meeting-button full" onClick={onCopyShareLink}>
        공유 링크 복사
      </button>
      {meeting.competitionPublicId && (
        <button
          type="button"
          className="meeting-button primary full"
          onClick={onOpenCompetition}
        >
          경기표 보기
        </button>
      )}
    </section>
  );
}

export default MeetingPublicOverviewPanel;
