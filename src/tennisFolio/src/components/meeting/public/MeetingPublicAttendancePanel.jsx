import MeetingParticipantFields from '../shared/MeetingParticipantFields';

function MeetingPublicAttendancePanel({
  form,
  isNameLocked,
  onFieldChange,
  onSaveProfile,
  onEnterAsDifferentParticipant,
}) {
  return (
    <section className="meeting-panel">
      <MeetingParticipantFields
        name={form.participantName}
        gender={form.gender}
        nameReadOnly={isNameLocked}
        genderDisabled={isNameLocked}
        onNameChange={(value) => onFieldChange('participantName', value)}
        onGenderChange={(value) => onFieldChange('gender', value)}
      />
      <button type="button" className="meeting-button full" onClick={onSaveProfile}>
        정보 저장
      </button>
      {!isNameLocked && (
        <button
          type="button"
          className="meeting-button full"
          onClick={onEnterAsDifferentParticipant}
        >
          다른 이름으로 입장
        </button>
      )}
    </section>
  );
}

export default MeetingPublicAttendancePanel;
