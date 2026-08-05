import MeetingAttendanceControlPanel from './MeetingAttendanceControlPanel';
import MeetingCompetitionPanel from './MeetingCompetitionPanel';

function MeetingManageOperationsPanel({
  meeting,
  onOpenCompetition,
  onCreateCompetition,
  onAskDeleteCompetition,
  onChangeStatus,
  sameGenderDoublesOnly,
  onSameGenderDoublesOnlyChange,
  isClubMeeting,
  skillBalancedSchedule,
  onSkillBalancedScheduleChange,
  sameGenderDoublesOnlyUnavailable,
  sameGenderDoublesOnlyUnavailableReason,
}) {
  return (
    <section className="meeting-panel">
      <MeetingCompetitionPanel
        meeting={meeting}
        onOpenCompetition={onOpenCompetition}
        onCreateCompetition={onCreateCompetition}
        onAskDeleteCompetition={onAskDeleteCompetition}
        sameGenderDoublesOnly={sameGenderDoublesOnly}
        onSameGenderDoublesOnlyChange={onSameGenderDoublesOnlyChange}
        isClubMeeting={isClubMeeting}
        skillBalancedSchedule={skillBalancedSchedule}
        onSkillBalancedScheduleChange={onSkillBalancedScheduleChange}
        sameGenderDoublesOnlyUnavailable={sameGenderDoublesOnlyUnavailable}
        sameGenderDoublesOnlyUnavailableReason={sameGenderDoublesOnlyUnavailableReason}
      />
      <MeetingAttendanceControlPanel
        status={meeting.status}
        onChangeStatus={onChangeStatus}
      />
    </section>
  );
}

export default MeetingManageOperationsPanel;
