import { useEffect, useState } from 'react';
import AttendanceStatusOptions from '../shared/AttendanceStatusOptions';

const toForm = (attendance) => ({
  participantName: attendance.participantName || '',
  gender: attendance.gender || 'MALE',
  attendanceStatus: attendance.attendanceStatus || 'ATTENDING',
  clubSkillTierId: attendance.clubSkillTierId ? String(attendance.clubSkillTierId) : '',
});

function MeetingParticipantEditPanel({
  attendance,
  isClubMeeting,
  skillTiers,
  onSubmit,
  onClose,
  isSubmitting,
  disabled,
}) {
  const [participant, setParticipant] = useState(() => toForm(attendance));
  const isClubMember = attendance.participantType === 'CLUB_MEMBER';
  const hasUnavailableSkillTier =
    !isClubMember &&
    participant.clubSkillTierId &&
    !skillTiers.some((skillTier) => String(skillTier.id) === participant.clubSkillTierId);

  useEffect(() => {
    setParticipant(toForm(attendance));
  }, [attendance]);

  const submit = async (event) => {
    event.preventDefault();
    const saved = await onSubmit(
      isClubMember
        ? { attendanceStatus: participant.attendanceStatus }
        : {
            participantName: participant.participantName.trim(),
            gender: participant.gender,
            attendanceStatus: participant.attendanceStatus,
            ...(isClubMeeting && {
              clubSkillTierId: participant.clubSkillTierId
                ? Number(participant.clubSkillTierId)
                : null,
            }),
          },
    );
    if (saved) {
      onClose();
    }
  };

  return (
    <section
      className="meeting-panel meeting-participant-add-panel"
      role="dialog"
      aria-modal="true"
      aria-labelledby="meeting-participant-edit-title"
      onClick={(event) => event.stopPropagation()}
    >
      <div className="meeting-participant-add-head">
        <div>
          <h2 id="meeting-participant-edit-title">참가자 수정</h2>
          <p>{isClubMember ? '클럽원은 참가 상태만 변경할 수 있습니다.' : '참가자 정보를 수정할 수 있습니다.'}</p>
        </div>
        <button
          type="button"
          className="meeting-participant-sheet-close"
          onClick={onClose}
          disabled={isSubmitting}
          aria-label="참가자 수정 닫기"
        >
          ×
        </button>
      </div>

      <form className="meeting-participant-add-form" onSubmit={submit}>
        <label className="meeting-field">
          <span>이름</span>
          <input
            value={participant.participantName}
            onChange={(event) =>
              setParticipant((current) => ({ ...current, participantName: event.target.value }))
            }
            maxLength={30}
            disabled={disabled || isSubmitting || isClubMember}
            required={!isClubMember}
          />
        </label>

        <div className="meeting-participant-choice-field">
          <span>성별</span>
          <div className="meeting-participant-gender-options" aria-label="성별">
            {['MALE', 'FEMALE'].map((gender) => (
              <button
                type="button"
                className={`meeting-participant-gender-option ${gender === 'FEMALE' ? 'female' : 'male'} ${
                  participant.gender === gender ? 'selected' : ''
                }`}
                key={gender}
                onClick={() => setParticipant((current) => ({ ...current, gender }))}
                disabled={disabled || isSubmitting || isClubMember}
              >
                {gender === 'FEMALE' ? '여성' : '남성'}
              </button>
            ))}
          </div>
        </div>

        {isClubMeeting && (isClubMember ? (
          <div className="meeting-field">
            <span>등급</span>
            <p className="meeting-participant-readonly">
              {attendance.clubSkillTierName || '등급 미정'}
            </p>
          </div>
        ) : (
          <label className="meeting-field">
            <span>등급</span>
            <select
              value={participant.clubSkillTierId}
              onChange={(event) =>
                setParticipant((current) => ({ ...current, clubSkillTierId: event.target.value }))
              }
              disabled={disabled || isSubmitting}
            >
              <option value="">선택 안 함</option>
              {hasUnavailableSkillTier && (
                <option value={participant.clubSkillTierId}>등급 미정</option>
              )}
              {skillTiers.map((skillTier) => (
                <option key={skillTier.id} value={skillTier.id}>
                  {skillTier.name}
                </option>
              ))}
            </select>
          </label>
        ))}

        <div className="meeting-participant-choice-field">
          <span>참가 상태</span>
          <AttendanceStatusOptions
            selectedStatus={participant.attendanceStatus}
            onSelect={(attendanceStatus) =>
              setParticipant((current) => ({ ...current, attendanceStatus }))
            }
            disabled={disabled || isSubmitting}
            buttonClassName="meeting-participant-status-option"
          />
        </div>
        <button
          className="meeting-participant-submit"
          type="submit"
          disabled={disabled || isSubmitting || (!isClubMember && !participant.participantName.trim())}
        >
          {isSubmitting ? '저장 중...' : '수정 완료'}
        </button>
      </form>
    </section>
  );
}

export default MeetingParticipantEditPanel;
