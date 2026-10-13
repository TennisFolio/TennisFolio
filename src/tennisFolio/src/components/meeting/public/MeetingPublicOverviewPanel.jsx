import AttendanceStatusSummary from '../shared/AttendanceStatusSummary';
import CapacityChips from '../shared/CapacityChips';
import MeetingScheduleSummary from '../shared/MeetingScheduleSummary';

function MeetingPublicOverviewPanel({
  meeting,
  attendances,
  onCopyShareLink,
  onOpenCompetition,
  onManageMeeting,
}) {
  return (
    <section className="meeting-panel">
      <MeetingScheduleSummary startAt={meeting.startAt} endAt={meeting.endAt} />
      <div className="meeting-card-title-row">
        <div>
          <h1>{meeting.title}</h1>
        </div>
        <div className="meeting-title-chip-row">
          {meeting.clubName && (
            <span className="meeting-chip">{meeting.clubName}</span>
          )}
        </div>
      </div>
      <div className="meeting-card-meta" aria-label="모임 부가 정보">
        <span className="meeting-chip">{meeting.courtCount}코트</span>
        <span className="meeting-chip">{meeting.totalGames}경기</span>
      </div>
      <CapacityChips meeting={meeting} attendances={attendances} />
      {meeting.note && <p className="meeting-note-box">{meeting.note}</p>}
      <AttendanceStatusSummary attendances={attendances} />
      <div className="meeting-manage-actions">
        <button type="button" className="meeting-button full" onClick={onCopyShareLink}>
          공유 링크 복사
        </button>
        <button type="button" className="meeting-button full" onClick={onManageMeeting}>
          모임 관리
        </button>
      </div>
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
