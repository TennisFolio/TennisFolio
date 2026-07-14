function MeetingParticipantFields({
  name,
  gender,
  onNameChange,
  onGenderChange,
  nameReadOnly = false,
  genderDisabled = false,
  showGender = true,
}) {
  const content = (
    <>
      <label className="meeting-field">
        <span>이름</span>
        <input
          value={name}
          readOnly={nameReadOnly}
          onChange={(event) => onNameChange?.(event.target.value)}
        />
      </label>
      {showGender && (
        <label className="meeting-field">
          <span>성별</span>
          <select
            value={gender}
            disabled={genderDisabled}
            onChange={(event) => onGenderChange?.(event.target.value)}
          >
            <option value="MALE">남성</option>
            <option value="FEMALE">여성</option>
          </select>
        </label>
      )}
    </>
  );

  if (!showGender) {
    return content;
  }

  return <div className="meeting-grid two">{content}</div>;
}

export default MeetingParticipantFields;
