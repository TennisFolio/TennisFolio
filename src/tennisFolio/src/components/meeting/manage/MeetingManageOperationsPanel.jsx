import MeetingCompetitionPanel from './MeetingCompetitionPanel';

function MeetingManageOperationsPanel({
  meeting,
  onOpenCompetition,
  onCreateCompetition,
  onAskDeleteCompetition,
  sameGenderDoublesOnly,
  onSameGenderDoublesOnlyChange,
  isClubMeeting,
  skillBalancedSchedule,
  onSkillBalancedScheduleChange,
  sameGenderDoublesOnlyUnavailable,
  sameGenderDoublesOnlyUnavailableReason,
  skillBalancedScheduleUnavailable,
  skillBalancedScheduleUnavailableReason,
  attendanceStatusCounts,
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
        skillBalancedScheduleUnavailable={skillBalancedScheduleUnavailable}
        skillBalancedScheduleUnavailableReason={skillBalancedScheduleUnavailableReason}
        attendanceStatusCounts={attendanceStatusCounts}
      />
    </section>
  );
}

export default MeetingManageOperationsPanel;
