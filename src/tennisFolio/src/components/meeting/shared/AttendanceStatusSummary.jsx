import { countByStatus } from './meetingAttendanceUtils';

function AttendanceStatusSummary({ attendances }) {
  return (
    <div className="meeting-chip-row">
      <span className="meeting-chip ok">
        참석 {countByStatus(attendances, 'ATTENDING')}
      </span>
      <span className="meeting-chip warning">
        대기 {countByStatus(attendances, 'WAITING')}
      </span>
      <span className="meeting-chip danger">
        불참 {countByStatus(attendances, 'NOT_ATTENDING')}
      </span>
    </div>
  );
}

export default AttendanceStatusSummary;
