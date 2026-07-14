function MeetingDangerZone({ disabled = false, onDeleteMeeting }) {
  return (
    <section className="meeting-panel meeting-danger-zone">
      <div>
        <h2>모임 삭제</h2>
      </div>
      <button
        type="button"
        className="meeting-button danger"
        disabled={disabled}
        onClick={onDeleteMeeting}
      >
        삭제
      </button>
    </section>
  );
}

export default MeetingDangerZone;
