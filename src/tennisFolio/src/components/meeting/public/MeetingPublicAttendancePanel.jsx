import AttendanceStatusOptions from '../shared/AttendanceStatusOptions';
import MeetingParticipantFields from '../shared/MeetingParticipantFields';

function MeetingPublicAttendancePanel({
  form,
  isNameLocked,
  onFieldChange,
  onSaveProfile,
  onEnterAsDifferentParticipant,
  onStatusSelect,
}) {
  return (
    <section className="meeting-panel">
      <MeetingParticipantFields
        name={form.participantName}
        gender={form.gender}
        nameReadOnly={isNameLocked}
        onNameChange={(value) => onFieldChange('participantName', value)}
        onGenderChange={(value) => onFieldChange('gender', value)}
      />
      <button type="button" className="meeting-button full" onClick={onSaveProfile}>
        정보 저장
      </button>
      <button
        type="button"
        className="meeting-button full"
        onClick={onEnterAsDifferentParticipant}
      >
        다른 이름으로 입장
      </button>
      <div className="meeting-status-row">
        <span className="meeting-muted">상태</span>
        <AttendanceStatusOptions
          selectedStatus={form.attendanceStatus}
          onSelect={onStatusSelect}
        />
      </div>
    </section>
  );
}

export default MeetingPublicAttendancePanel;
