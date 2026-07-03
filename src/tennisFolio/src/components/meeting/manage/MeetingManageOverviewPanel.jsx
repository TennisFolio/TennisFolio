import AttendanceStatusSummary from '../shared/AttendanceStatusSummary';
import CapacityChips from '../shared/CapacityChips';
import { formatDate, formatTimeRange } from '../shared/meetingAttendanceUtils';

function MeetingManageOverviewPanel({
  meeting,
  attendances,
  editDisabled,
  onCopyShareLink,
  onEditMeeting,
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
      {meeting.note && (
        <p className="meeting-note-box">{meeting.note}</p>
      )}
      <AttendanceStatusSummary attendances={attendances} />
      <div className="meeting-manage-actions">
        <button
          type="button"
          className="meeting-button full"
          onClick={onCopyShareLink}
        >
          공유 링크 복사
        </button>
        <button
          type="button"
          className="meeting-button full"
          disabled={editDisabled}
          aria-describedby={editDisabled ? 'meeting-edit-lock-message' : undefined}
          onClick={onEditMeeting}
        >
          모임 수정
        </button>
      </div>
      {editDisabled && (
        <p className="meeting-muted" id="meeting-edit-lock-message">
          대진표가 생성된 모임은 수정할 수 없습니다. 수정하려면 대진표를 먼저 삭제해 주세요.
        </p>
      )}
    </section>
  );
}

export default MeetingManageOverviewPanel;
