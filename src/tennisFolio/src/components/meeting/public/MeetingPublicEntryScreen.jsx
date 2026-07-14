import MeetingToast from '../../../page/MeetingToast';
import AttendanceChip from '../shared/AttendanceChip';
import AttendanceStatusOptions from '../shared/AttendanceStatusOptions';
import CapacityChips from '../shared/CapacityChips';
import MeetingParticipantFields from '../shared/MeetingParticipantFields';
import { formatDate, formatTimeRange } from '../shared/meetingAttendanceUtils';

function MeetingPublicEntryScreen({
  meeting,
  attendances,
  form,
  isNameLocked,
  notice,
  onCloseNotice,
  onFieldChange,
  onStatusSelect,
  onAttendanceSelect,
}) {
  const entryGroups = [
    {
      title: '참석',
      attendances: attendances.filter(
        (attendance) => attendance.attendanceStatus === 'ATTENDING',
      ),
    },
    {
      title: '대기',
      attendances: attendances.filter(
        (attendance) => attendance.attendanceStatus === 'WAITING',
      ),
    },
    {
      title: '불참',
      attendances: attendances.filter(
        (attendance) => attendance.attendanceStatus === 'NOT_ATTENDING',
      ),
    },
  ];

  return (
    <main className="meeting-page">
      <section className="meeting-panel">
        <h1>{meeting.title}</h1>
        <div className="meeting-card-meta">
          <span className="meeting-chip">{formatDate(meeting.startAt)}</span>
          <span className="meeting-chip">
            {formatTimeRange(meeting.startAt, meeting.endAt)}
          </span>
          <span className="meeting-chip">{meeting.courtCount}코트</span>
          <span className="meeting-chip">{meeting.totalGames}경기</span>
        </div>
        <CapacityChips meeting={meeting} attendances={attendances} />
        {meeting.note && <p className="meeting-note-box">{meeting.note}</p>}

        <div className="meeting-entry-form">
          <MeetingParticipantFields
            name={form.participantName}
            gender={form.gender}
            nameReadOnly={isNameLocked}
            genderDisabled={isNameLocked}
            onNameChange={(value) => onFieldChange('participantName', value)}
            onGenderChange={(value) => onFieldChange('gender', value)}
          />

          <AttendanceStatusOptions
            selectedStatus={form.attendanceStatus}
            buttonClassName="meeting-button full"
            onSelect={onStatusSelect}
          />
        </div>

        {meeting.clubMeeting && (
          <p className="meeting-state-note">
            이름과 성별이 클럽원 정보와 일치하면 클럽원으로 표시됩니다.
          </p>
        )}

        <p className="meeting-state-note">
          참석 여부를 선택하면 참석 현황과 명단을 볼 수 있습니다.
        </p>

        <div className="meeting-entry-name-groups">
          {entryGroups.map((group) => (
            <section
              className="meeting-entry-name-group"
              aria-label={group.title}
              key={group.title}
            >
              <p className="meeting-muted">{group.title}</p>
              {group.attendances.length === 0 ? (
                <p className="meeting-entry-empty">아직 없습니다.</p>
              ) : (
                <div className="meeting-attendance-list">
                  {group.attendances.map((attendance) => (
                    <AttendanceChip
                      attendance={attendance}
                      key={attendance.id}
                      meeting={meeting}
                      asButton={!isNameLocked}
                      onSelect={onAttendanceSelect}
                    />
                  ))}
                </div>
              )}
            </section>
          ))}
        </div>

        <p className="meeting-state-note">
          이미 응답했다면 아래 명단에서 이름을 선택해 모임에 입장하세요.
        </p>
      </section>

      <MeetingToast notice={notice} onClose={onCloseNotice} />
    </main>
  );
}

export default MeetingPublicEntryScreen;
