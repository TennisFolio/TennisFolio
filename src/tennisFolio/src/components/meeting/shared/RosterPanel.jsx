import AttendanceChip from './AttendanceChip';

function RosterPanel({
  title,
  tone,
  attendees,
  meeting,
  emptyMessage = '아직 표시할 참석자가 없습니다.',
  onAskDelete,
}) {
  return (
    <section className="meeting-panel meeting-roster-panel" aria-label={title}>
      <div className="meeting-roster-head">
        <h2>{title}</h2>
        <span className={`meeting-chip ${tone}`}>{attendees.length}명</span>
      </div>
      {attendees.length === 0 ? (
        emptyMessage === null ? null : <p>{emptyMessage}</p>
      ) : (
        <div className="meeting-attendance-list">
          {attendees.map((attendance) => (
            <AttendanceChip
              attendance={attendance}
              key={attendance.id}
              meeting={meeting}
              onRemove={onAskDelete}
            />
          ))}
        </div>
      )}
    </section>
  );
}

export default RosterPanel;
