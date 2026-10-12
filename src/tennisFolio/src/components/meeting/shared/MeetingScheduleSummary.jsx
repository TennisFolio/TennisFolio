import {
  formatScheduleDate,
  formatTimeRange,
} from './meetingAttendanceUtils';

function MeetingScheduleSummary({ startAt, endAt }) {
  return (
    <div className="meeting-schedule" aria-label="모임 일정">
      <time className="meeting-schedule-date" dateTime={startAt}>
        {formatScheduleDate(startAt)}
      </time>
      <span className="meeting-schedule-time">
        {formatTimeRange(startAt, endAt)}
      </span>
    </div>
  );
}

export default MeetingScheduleSummary;
