import AttendanceStatusOptions from '../shared/AttendanceStatusOptions';
import MeetingParticipantFields from '../shared/MeetingParticipantFields';

function MeetingOwnerAttendancePanel({ ownerName, ownerStatus, onStatusSelect }) {
  return (
    <section className="meeting-panel">
      <MeetingParticipantFields
        name={ownerName}
        nameReadOnly
        showGender={false}
      />
      <div className="meeting-status-row">
        <span className="meeting-muted">상태</span>
        <AttendanceStatusOptions
          selectedStatus={ownerStatus}
          onSelect={onStatusSelect}
        />
      </div>
    </section>
  );
}

export default MeetingOwnerAttendancePanel;
