import { isOwnerAttendance } from './meetingAttendanceUtils';

function OwnerTag() {
  return (
    <span className="meeting-owner-icon" role="img" aria-label="모임장" title="모임장">
      <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <path d="M5 17h14l1-10-5 4-3-6-3 6-5-4 1 10Z" />
        <path d="M5 20h14" />
      </svg>
    </span>
  );
}

function AttendanceChip({
  attendance,
  meeting,
  asButton = false,
  onSelect,
  onRemove,
}) {
  const isOwner = isOwnerAttendance(attendance, meeting);
  const className = `meeting-chip ${
    attendance.gender === 'FEMALE' ? 'female' : 'male'
  }${isOwner ? ' owner' : ''}`;
  const content = (
    <>
      {attendance.participantName}
      {isOwner && <OwnerTag />}
      {!isOwner && onRemove && (
        <button
          type="button"
          className="meeting-attendee-remove"
          aria-label={`${attendance.participantName} 제거`}
          onClick={() => onRemove(attendance)}
        >
          x
        </button>
      )}
    </>
  );

  if (!asButton || isOwner) {
    return (
      <span className={className} key={attendance.id}>
        {content}
      </span>
    );
  }

  return (
    <button
      type="button"
      className={className}
      key={attendance.id}
      onClick={() => onSelect(attendance)}
    >
      {content}
    </button>
  );
}

export default AttendanceChip;
