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

function ParticipantBadge({ attendance, meeting }) {
  if (
    !meeting?.clubMeeting
  ) {
    return null;
  }

  const label = attendance.clubSkillTierName || '등급 미정';

  return (
    <span className="meeting-participant-badge">
      {label}
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
      <ParticipantBadge attendance={attendance} meeting={meeting} />
      {isOwner && <OwnerTag />}
      {!isOwner && onRemove && (
        <button
          type="button"
          className="meeting-attendee-remove"
          aria-label={`${attendance.participantName} 제거`}
          onClick={(event) => {
            event.stopPropagation();
            onRemove(attendance);
          }}
          onKeyDown={(event) => event.stopPropagation()}
        >
          x
        </button>
      )}
    </>
  );

  if (!asButton || isOwner || !onSelect) {
    return (
      <span className={className} key={attendance.id}>
        {content}
      </span>
    );
  }

  return (
    <div
      role="button"
      tabIndex={0}
      className={className}
      key={attendance.id}
      onClick={() => onSelect(attendance)}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          onSelect(attendance);
        }
      }}
    >
      {content}
    </div>
  );
}

export default AttendanceChip;
