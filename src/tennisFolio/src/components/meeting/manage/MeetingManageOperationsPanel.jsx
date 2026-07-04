import MeetingAttendanceControlPanel from './MeetingAttendanceControlPanel';
import MeetingCompetitionPanel from './MeetingCompetitionPanel';

function MeetingManageOperationsPanel({
  meeting,
  onOpenCompetition,
  onCreateCompetition,
  onAskDeleteCompetition,
  onChangeStatus,
}) {
  return (
    <section className="meeting-panel">
      <MeetingCompetitionPanel
        meeting={meeting}
        onOpenCompetition={onOpenCompetition}
        onCreateCompetition={onCreateCompetition}
        onAskDeleteCompetition={onAskDeleteCompetition}
      />
      <MeetingAttendanceControlPanel
        status={meeting.status}
        onChangeStatus={onChangeStatus}
      />
    </section>
  );
}

export default MeetingManageOperationsPanel;
