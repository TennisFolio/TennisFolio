import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getCurrentUser } from '../utils/authApi';
import {
  createMeetingCompetition,
  deleteAttendance,
  deleteMeeting,
  deleteMeetingCompetition,
  getPublicMeeting,
  upsertAttendance,
  updateMeetingStatus,
} from '../utils/meetingApi';
import './Meeting.css';
import MeetingToast from './MeetingToast';

const statusLabels = {
  ATTENDING: '참석',
  WAITING: '대기',
  NOT_ATTENDING: '불참',
};

function normalizeAttendances(meeting) {
  return meeting?.attendances || meeting?.attendanceResponses || [];
}

function countByStatus(attendances, status) {
  return attendances.filter((attendance) => attendance.attendanceStatus === status).length;
}

function countByStatusAndGender(attendances, status, gender) {
  return attendances.filter(
    (attendance) =>
      attendance.attendanceStatus === status && attendance.gender === gender,
  ).length;
}

function getCapacityChips(meeting, attendances) {
  const attendingCount = countByStatus(attendances, 'ATTENDING');
  const maleCount = countByStatusAndGender(attendances, 'ATTENDING', 'MALE');
  const femaleCount = countByStatusAndGender(attendances, 'ATTENDING', 'FEMALE');

  if (meeting.maxParticipants) {
    return [`정원 ${attendingCount}/${meeting.maxParticipants}`];
  }

  const capacityChips = [];

  if (meeting.maxMaleParticipants) {
    capacityChips.push(`남성 ${maleCount}/${meeting.maxMaleParticipants}`);
  }

  if (meeting.maxFemaleParticipants) {
    capacityChips.push(`여성 ${femaleCount}/${meeting.maxFemaleParticipants}`);
  }

  return capacityChips.length > 0 ? capacityChips : ['정원 제한 없음'];
}

function formatDate(startAt) {
  if (!startAt) {
    return '-';
  }
  const date = new Date(startAt);
  return `${date.getMonth() + 1}월 ${date.getDate()}일`;
}

function formatTimeRange(startAt, endAt) {
  if (!startAt || !endAt) {
    return '-';
  }
  const formatTime = (value) =>
    new Date(value).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });
  return `${formatTime(startAt)}-${formatTime(endAt)}`;
}

function findOwnerAttendance(attendances, currentUser) {
  const nickName = currentUser?.nickName?.trim();
  if (!nickName) {
    return null;
  }
  return (
    attendances.find((attendance) => attendance.participantName === nickName) ||
    null
  );
}

function groupAttendances(attendances) {
  return {
    attendingMale: attendances.filter(
      (attendance) =>
        attendance.attendanceStatus === 'ATTENDING' && attendance.gender === 'MALE',
    ),
    attendingFemale: attendances.filter(
      (attendance) =>
        attendance.attendanceStatus === 'ATTENDING' && attendance.gender === 'FEMALE',
    ),
    waiting: attendances.filter(
      (attendance) => attendance.attendanceStatus === 'WAITING',
    ),
    notAttending: attendances.filter(
      (attendance) => attendance.attendanceStatus === 'NOT_ATTENDING',
    ),
  };
}

function isOwnerAttendance(attendance, meeting) {
  const ownerNickName = meeting?.ownerNickName?.trim();
  return Boolean(ownerNickName) && attendance.participantName === ownerNickName;
}

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

function RosterPanel({ title, tone, attendees, meeting, onAskDelete }) {
  return (
    <section className="meeting-panel meeting-roster-panel" aria-label={title}>
      <div className="meeting-roster-head">
        <h2>{title}</h2>
        <span className={`meeting-chip ${tone}`}>{attendees.length}명</span>
      </div>
      {attendees.length === 0 ? (
        null
      ) : (
        <div className="meeting-attendance-list">
          {attendees.map((attendance) => {
            const isOwner = isOwnerAttendance(attendance, meeting);

            return (
              <span
                className={`meeting-chip ${
                  attendance.gender === 'FEMALE' ? 'female' : 'male'
                }${isOwner ? ' owner' : ''}`}
                key={attendance.id}
              >
                {attendance.participantName}
                {isOwner && <OwnerTag />}
                {!isOwner && (
                  <button
                    type="button"
                    className="meeting-attendee-remove"
                    aria-label={`${attendance.participantName} 제거`}
                    onClick={() => onAskDelete(attendance)}
                  >
                    x
                  </button>
                )}
              </span>
            );
          })}
        </div>
      )}
    </section>
  );
}

function ConfirmModal({ title, description, confirmLabel, onCancel, onConfirm }) {
  return (
    <div className="meeting-confirm-backdrop">
      <div className="meeting-confirm-panel" role="alertdialog" aria-modal="true">
        <strong>{title}</strong>
        <p>{description}</p>
        <div className="meeting-confirm-actions">
          <button
            type="button"
            className="meeting-button"
            onClick={onCancel}
          >
            취소
          </button>
          <button
            type="button"
            className="meeting-button danger"
            onClick={onConfirm}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

function CapacityChips({ meeting, attendances }) {
  return (
    <div className="meeting-capacity-row" aria-label="정원">
      {getCapacityChips(meeting, attendances).map((label) => (
        <span className="meeting-chip" key={label}>
          {label}
        </span>
      ))}
    </div>
  );
}

function MeetingManage({ initialMeeting = null, initialNotice = null }) {
  const { publicId } = useParams();
  const navigate = useNavigate();
  const [meeting, setMeeting] = useState(initialMeeting);
  const [currentUser, setCurrentUser] = useState(null);
  const [ownerStatus, setOwnerStatus] = useState('ATTENDING');
  const [isLoading, setIsLoading] = useState(true);
  const [attendeeToDelete, setAttendeeToDelete] = useState(null);
  const [competitionDeleteRequested, setCompetitionDeleteRequested] =
    useState(false);
  const [notice, setNotice] = useState(initialNotice);
  const [errorMessage, setErrorMessage] = useState('');

  const attendances = useMemo(() => normalizeAttendances(meeting), [meeting]);
  const ownerAttendance = useMemo(
    () => findOwnerAttendance(attendances, currentUser),
    [attendances, currentUser],
  );
  const groupedAttendances = useMemo(
    () => groupAttendances(attendances),
    [attendances],
  );
  const ownerName = currentUser?.nickName?.trim() || '';
  const meetingEditDisabled = Boolean(meeting?.competitionCreated);

  const loadMeeting = useCallback(
    () =>
      getPublicMeeting(publicId).then((response) => {
        const nextMeeting = response.data.data;
        if (nextMeeting?.ownedByCurrentUser !== true) {
          throw new Error('FORBIDDEN_MEETING_OWNER');
        }
        setMeeting(nextMeeting);
      }),
    [publicId],
  );

  useEffect(() => {
    let cancelled = false;

    Promise.all([getPublicMeeting(publicId), getCurrentUser()])
      .then(([meetingResponse, userResponse]) => {
        if (cancelled) {
          return;
        }
        const nextMeeting = meetingResponse.data.data;
        if (nextMeeting?.ownedByCurrentUser !== true) {
          setMeeting(null);
          setErrorMessage('모임을 관리할 권한이 없습니다.');
          return;
        }
        setMeeting(nextMeeting);
        setCurrentUser(userResponse.data.data);
      })
      .catch((error) => {
        if (!cancelled) {
          setErrorMessage(
            error.response?.status === 401
              ? '로그인이 필요합니다.'
              : '관리 화면을 불러오지 못했습니다.',
          );
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [publicId]);

  useEffect(() => {
    if (ownerAttendance?.attendanceStatus) {
      setOwnerStatus(ownerAttendance.attendanceStatus);
    }
  }, [ownerAttendance]);

  const showNotice = (type, message) => {
    setNotice({ type, message });
  };

  const handleStatus = async (status) => {
    await updateMeetingStatus(publicId, status);
    await loadMeeting();
    showNotice('success', status === 'OPEN' ? '참석 체크를 다시 열었습니다.' : '참석 체크를 마감했습니다.');
  };

  const handleOwnerAttendance = async (status) => {
    if (!ownerName) {
      showNotice('error', '프로필 nickname을 먼저 설정해주세요.');
      return;
    }

    try {
      setOwnerStatus(status);
      await upsertAttendance(publicId, {
        attendanceId: ownerAttendance?.id,
        participantName: ownerName,
        gender: currentUser?.gender || ownerAttendance?.gender || 'MALE',
        attendanceStatus: status,
      });
      await loadMeeting();
      showNotice('success', '내 참석 상태를 저장했습니다.');
    } catch (error) {
      setOwnerStatus(ownerAttendance?.attendanceStatus || 'ATTENDING');
      showNotice(
        'error',
        error.response?.data?.message || '내 참석 상태를 저장하지 못했습니다.',
      );
    }
  };

  const handleCreateCompetition = async () => {
    try {
      const response = await createMeetingCompetition(publicId);
      showNotice('success', '대진표를 생성했습니다.');
      await loadMeeting();
      if (response.data.data?.publicId) {
        navigate(`/competitions/${response.data.data.publicId}`);
      }
    } catch (error) {
      showNotice('error', error.response?.data?.message || '대진표를 생성하지 못했습니다.');
    }
  };

  const handleDeleteCompetition = async () => {
    await deleteMeetingCompetition(publicId);
    await loadMeeting();
    showNotice('success', '연결된 대진표를 삭제했습니다.');
    setCompetitionDeleteRequested(false);
  };

  const handleCopyShareLink = async () => {
    const shareUrl = `${window.location.origin}/meetings/${publicId}`;
    try {
      await navigator.clipboard.writeText(shareUrl);
      showNotice('success', '공유 링크를 복사했습니다.');
    } catch {
      showNotice('error', `공유 링크를 복사하지 못했습니다. ${shareUrl}`);
    }
  };

  const handleDeleteAttendance = async () => {
    if (!attendeeToDelete) {
      return;
    }

    try {
      await deleteAttendance(publicId, attendeeToDelete.id);
      await loadMeeting();
      showNotice('success', `${attendeeToDelete.participantName} 참석 응답을 삭제했습니다.`);
      setAttendeeToDelete(null);
    } catch (error) {
      showNotice(
        'error',
        error.response?.data?.message || '참석 응답을 삭제하지 못했습니다.',
      );
    }
  };

  const handleDeleteMeeting = async () => {
    const confirmed = window.confirm('모임을 삭제할까요?');
    if (!confirmed) {
      return;
    }
    await deleteMeeting(publicId);
    navigate('/meetings');
  };

  if (isLoading) {
    return (
      <main className="meeting-page">
        <p className="meeting-state">불러오는 중입니다.</p>
      </main>
    );
  }

  if (!meeting) {
    return (
      <main className="meeting-page">
        <p className="meeting-state meeting-error">{errorMessage}</p>
      </main>
    );
  }

  return (
    <main className="meeting-page manage">
      <div className="meeting-manage-grid">
        <section className="meeting-panel">
          <div className="meeting-card-title-row">
            <div>
              <h1>{meeting.title}</h1>
            </div>
          </div>
          <div className="meeting-card-meta">
            <span className="meeting-chip">{formatDate(meeting.startAt)}</span>
            <span className="meeting-chip">
              {formatTimeRange(meeting.startAt, meeting.endAt)}
            </span>
            <span className="meeting-chip">{meeting.courtCount}코트</span>
            <span className="meeting-chip">{meeting.totalGames}경기</span>
          </div>
          <CapacityChips meeting={meeting} attendances={attendances} />
          {meeting.note && (
            <p className="meeting-note-box">{meeting.note}</p>
          )}
          <div className="meeting-chip-row">
            <span className="meeting-chip ok">
              참석 {countByStatus(attendances, 'ATTENDING')}
            </span>
            <span className="meeting-chip warning">
              대기 {countByStatus(attendances, 'WAITING')}
            </span>
            <span className="meeting-chip danger">
              불참 {countByStatus(attendances, 'NOT_ATTENDING')}
            </span>
          </div>
          <div className="meeting-manage-actions">
            <button
              type="button"
              className="meeting-button full"
              onClick={handleCopyShareLink}
            >
              공유 링크 복사
            </button>
            <button
              type="button"
              className="meeting-button full"
              disabled={meetingEditDisabled}
              aria-describedby={meetingEditDisabled ? 'meeting-edit-lock-message' : undefined}
              onClick={() => navigate(`/meetings/${publicId}/edit`)}
            >
              모임 수정
            </button>
          </div>
          {meetingEditDisabled && (
            <p className="meeting-muted" id="meeting-edit-lock-message">
              대진표가 생성된 모임은 수정할 수 없습니다. 수정하려면 대진표를 먼저 삭제해 주세요.
            </p>
          )}
        </section>

        <section className="meeting-panel">
          <div className="meeting-operation-primary">
            <div className="meeting-operation-head">
              <div>
                <h2>대진표 생성</h2>
                <p>
                  참석자 기준으로 대진표를 만들거나 생성된 대진표를 확인합니다.
                </p>
              </div>
            </div>
            {meeting.competitionCreated ? (
              <>
                {meeting.competitionPublicId && (
                  <button
                    type="button"
                    className="meeting-button primary full"
                    onClick={() => navigate(`/competitions/${meeting.competitionPublicId}`)}
                  >
                    대진표 보기
                  </button>
                )}
                <button
                  type="button"
                  className="meeting-button danger full"
                  onClick={() => setCompetitionDeleteRequested(true)}
                >
                  대진표 삭제
                </button>
              </>
            ) : (
              <button
                type="button"
                className="meeting-button primary full"
                onClick={handleCreateCompetition}
              >
                참석자로 대진표 생성
              </button>
            )}
          </div>
          <div
            className={`meeting-operation-status ${
              meeting.status === 'CLOSED' ? 'closed' : ''
            }`}
          >
            <div className="meeting-attendance-control-head">
              <div>
                <div className="meeting-attendance-control-title">
                  <span
                    className={`meeting-attendance-control-dot ${
                      meeting.status === 'CLOSED' ? 'closed' : ''
                    }`}
                  />
                  참석 체크
                </div>
                <p>
                  {meeting.status === 'OPEN'
                    ? '참가자가 응답할 수 있습니다. 마감하면 새 응답을 잠시 막습니다.'
                    : '참석 체크가 마감되어 새 응답은 받지 않습니다. 필요하면 다시 열 수 있습니다.'}
                </p>
              </div>
              <span
                className={`meeting-attendance-status-pill ${
                  meeting.status === 'OPEN' ? 'ok' : 'danger'
                }`}
              >
                {meeting.status === 'OPEN' ? '열림' : '마감'}
              </span>
            </div>
            <div className="meeting-attendance-control-actions">
              {meeting.status === 'OPEN' ? (
                <>
                  <button
                    type="button"
                    className="meeting-button primary"
                    aria-pressed="true"
                  >
                    열어두기
                  </button>
                  <button
                    type="button"
                    className="meeting-button"
                    onClick={() => handleStatus('CLOSED')}
                  >
                    마감하기
                  </button>
                </>
              ) : (
                <>
                  <button
                    type="button"
                    className="meeting-button"
                    onClick={() => handleStatus('OPEN')}
                  >
                    다시 열기
                  </button>
                  <button
                    type="button"
                    className="meeting-button danger"
                    aria-pressed="true"
                  >
                    마감 유지
                  </button>
                </>
              )}
            </div>
          </div>
        </section>

        <section className="meeting-panel">
          <label className="meeting-field">
            <span>이름</span>
            <input value={ownerName} readOnly />
          </label>
          <div className="meeting-status-row">
            <span className="meeting-muted">상태</span>
            <div className="meeting-status-options">
              {Object.entries(statusLabels).map(([status, label]) => (
                <button
                  type="button"
                  className={`meeting-button status-option ${
                    ownerStatus === status ? 'primary' : ''
                  }`}
                  key={status}
                  onClick={() => handleOwnerAttendance(status)}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>
        </section>

        <RosterPanel
          title="남자 참석자"
          tone="ok"
          attendees={groupedAttendances.attendingMale}
          meeting={meeting}
          onAskDelete={setAttendeeToDelete}
        />
        <RosterPanel
          title="여자 참석자"
          tone="ok"
          attendees={groupedAttendances.attendingFemale}
          meeting={meeting}
          onAskDelete={setAttendeeToDelete}
        />
        <RosterPanel
          title="대기"
          tone="warning"
          attendees={groupedAttendances.waiting}
          meeting={meeting}
          onAskDelete={setAttendeeToDelete}
        />
        <RosterPanel
          title="불참"
          tone="danger"
          attendees={groupedAttendances.notAttending}
          meeting={meeting}
          onAskDelete={setAttendeeToDelete}
        />
        <section className="meeting-panel meeting-danger-zone">
          <div>
            <h2>모임 삭제</h2>
          </div>
          <button
            type="button"
            className="meeting-button danger"
            onClick={handleDeleteMeeting}
          >
            삭제
          </button>
        </section>
      </div>

      {attendeeToDelete && (
        <ConfirmModal
          title={`${attendeeToDelete.participantName} 선수를 삭제하시겠습니까?`}
          description="삭제하면 이 모임의 참석 응답에서 제거됩니다."
          confirmLabel="삭제"
          onCancel={() => setAttendeeToDelete(null)}
          onConfirm={handleDeleteAttendance}
        />
      )}
      {competitionDeleteRequested && (
        <ConfirmModal
          title="생성된 대진표를 삭제하시겠습니까?"
          description="삭제하면 참석자 명단은 유지되고 대진표 연결만 해제됩니다."
          confirmLabel="대진표 삭제"
          onCancel={() => setCompetitionDeleteRequested(false)}
          onConfirm={handleDeleteCompetition}
        />
      )}
      <MeetingToast notice={notice} onClose={() => setNotice(null)} />
    </main>
  );
}

export default MeetingManage;
