import RosterPanel from './RosterPanel';

function MeetingRosterSections({
  groupedAttendances,
  meeting,
  emptyMessage,
  onAskDelete,
  onSelectAttendance,
}) {
  return (
    <>
      <RosterPanel
        title="남자 참석자"
        tone="ok"
        attendees={groupedAttendances.attendingMale}
        meeting={meeting}
        emptyMessage={emptyMessage}
        onAskDelete={onAskDelete}
        onSelectAttendance={onSelectAttendance}
      />
      <RosterPanel
        title="여자 참석자"
        tone="ok"
        attendees={groupedAttendances.attendingFemale}
        meeting={meeting}
        emptyMessage={emptyMessage}
        onAskDelete={onAskDelete}
        onSelectAttendance={onSelectAttendance}
      />
      <RosterPanel
        title="대기"
        tone="warning"
        attendees={groupedAttendances.waiting}
        meeting={meeting}
        emptyMessage={emptyMessage}
        onAskDelete={onAskDelete}
        onSelectAttendance={onSelectAttendance}
      />
      <RosterPanel
        title="불참"
        tone="danger"
        attendees={groupedAttendances.notAttending}
        meeting={meeting}
        emptyMessage={emptyMessage}
        onAskDelete={onAskDelete}
        onSelectAttendance={onSelectAttendance}
      />
    </>
  );
}

export default MeetingRosterSections;
