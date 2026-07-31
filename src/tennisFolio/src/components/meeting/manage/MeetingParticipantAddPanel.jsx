import { useState } from 'react';
import AttendanceStatusOptions from '../shared/AttendanceStatusOptions';

const emptyGuest = {
  participantName: '',
  gender: 'MALE',
  attendanceStatus: 'ATTENDING',
  clubSkillTierId: '',
};

function MeetingParticipantAddPanel({
  isClubMeeting,
  members,
  skillTiers,
  query,
  onQueryChange,
  onSubmit,
  onClose,
  isSubmitting,
  disabled,
}) {
  const [participantType, setParticipantType] = useState('CLUB_MEMBER');
  const [selectedMember, setSelectedMember] = useState(null);
  const [guest, setGuest] = useState(emptyGuest);
  const isGuest = !isClubMeeting || participantType === 'GUEST';

  const submit = async (event) => {
    event.preventDefault();
    const payload = isGuest
      ? {
          participantName: guest.participantName.trim(),
          gender: guest.gender,
          attendanceStatus: guest.attendanceStatus,
          ...(isClubMeeting && {
            clubSkillTierId: guest.clubSkillTierId ? Number(guest.clubSkillTierId) : null,
          }),
        }
      : selectedMember
        ? {
            clubMemberId: selectedMember.id,
            attendanceStatus: guest.attendanceStatus,
          }
        : null;

    if (!payload) {
      return;
    }

    const added = await onSubmit(payload);
    if (!added) {
      return;
    }
    if (isGuest) {
      setGuest(emptyGuest);
    } else {
      setSelectedMember(null);
    }
  };

  return (
    <section
      className="meeting-panel meeting-participant-add-panel"
      role="dialog"
      aria-modal="true"
      aria-labelledby="meeting-participant-add-title"
      onClick={(event) => event.stopPropagation()}
    >
      <div className="meeting-participant-add-head">
        <div>
          <h2 id="meeting-participant-add-title">참가자 추가</h2>
          <p>참석, 대기, 불참 상태로 바로 등록할 수 있습니다.</p>
        </div>
        <button
          type="button"
          className="meeting-participant-sheet-close"
          onClick={onClose}
          disabled={isSubmitting}
          aria-label="참가자 추가 닫기"
        >
          ×
        </button>
      </div>

      {isClubMeeting && (
        <div className="meeting-participant-type-tabs" role="tablist" aria-label="참가자 유형">
          <button
            type="button"
            className={`meeting-button ${participantType === 'CLUB_MEMBER' ? 'primary' : ''}`}
            onClick={() => setParticipantType('CLUB_MEMBER')}
            disabled={disabled || isSubmitting}
          >
            클럽원
          </button>
          <button
            type="button"
            className={`meeting-button ${participantType === 'GUEST' ? 'primary' : ''}`}
            onClick={() => setParticipantType('GUEST')}
            disabled={disabled || isSubmitting}
          >
            게스트
          </button>
        </div>
      )}

      <form className="meeting-participant-add-form" onSubmit={submit}>
        {isGuest ? (
          <>
            <label className="meeting-field">
              <span>이름</span>
              <input
                value={guest.participantName}
                onChange={(event) =>
                  setGuest((current) => ({ ...current, participantName: event.target.value }))
                }
                placeholder="이름 입력"
                maxLength={30}
                disabled={disabled || isSubmitting}
                required
              />
            </label>
            <div className="meeting-participant-choice-field">
              <span>성별</span>
              <div className="meeting-participant-gender-options" aria-label="성별">
                <button
                  type="button"
                  className={`meeting-participant-gender-option male ${
                    guest.gender === 'MALE' ? 'selected' : ''
                  }`}
                  onClick={() => setGuest((current) => ({ ...current, gender: 'MALE' }))}
                  disabled={disabled || isSubmitting}
                >
                  남성
                </button>
                <button
                  type="button"
                  className={`meeting-participant-gender-option female ${
                    guest.gender === 'FEMALE' ? 'selected' : ''
                  }`}
                  onClick={() => setGuest((current) => ({ ...current, gender: 'FEMALE' }))}
                  disabled={disabled || isSubmitting}
                >
                  여성
                </button>
              </div>
            </div>
            {isClubMeeting && (
              <label className="meeting-field">
                <span>등급</span>
                <select
                  value={guest.clubSkillTierId}
                  onChange={(event) =>
                    setGuest((current) => ({ ...current, clubSkillTierId: event.target.value }))
                  }
                  disabled={disabled || isSubmitting}
                >
                  <option value="">선택 안 함</option>
                  {skillTiers.map((skillTier) => (
                    <option key={skillTier.id} value={skillTier.id}>
                      {skillTier.name}
                    </option>
                  ))}
                </select>
              </label>
            )}
          </>
        ) : (
          <>
            <label className="meeting-field">
              <span>클럽원 검색</span>
              <input
                value={query}
                onChange={(event) => onQueryChange(event.target.value)}
                placeholder="이름으로 검색"
                maxLength={30}
                disabled={disabled || isSubmitting}
              />
            </label>
            <div className="meeting-participant-member-list" aria-label="클럽원 목록">
              {members.length === 0 ? (
                <p className="meeting-entry-empty">추가할 수 있는 클럽원이 없습니다.</p>
              ) : (
                members.map((member) => (
                  <button
                    type="button"
                    className={`meeting-participant-member ${
                      selectedMember?.id === member.id ? 'selected' : ''
                    }`}
                    key={member.id}
                    onClick={() => setSelectedMember(member)}
                    disabled={disabled || isSubmitting}
                  >
                    <span>
                      <strong>{member.name}</strong>
                      <small>{member.skillTierName || '등급 미정'}</small>
                    </span>
                    <em className={member.gender === 'FEMALE' ? 'female' : 'male'}>
                      {member.gender === 'FEMALE' ? '여성' : '남성'}
                    </em>
                  </button>
                ))
              )}
            </div>
          </>
        )}

        <div className="meeting-participant-choice-field">
          <span>참가 상태</span>
          <AttendanceStatusOptions
            selectedStatus={guest.attendanceStatus}
            onSelect={(attendanceStatus) =>
              setGuest((current) => ({ ...current, attendanceStatus }))
            }
            disabled={disabled || isSubmitting}
            buttonClassName="meeting-participant-status-option"
          />
        </div>
        <button
          className="meeting-participant-submit"
          type="submit"
          disabled={
            disabled ||
            isSubmitting ||
            (isGuest ? !guest.participantName.trim() : !selectedMember)
          }
        >
          {isSubmitting ? '추가 중...' : isGuest ? '게스트 추가' : '클럽원 추가'}
        </button>
      </form>
    </section>
  );
}

export default MeetingParticipantAddPanel;
