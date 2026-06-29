import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getCurrentUser } from '../utils/authApi';
import {
  createMeetingCompetition,
  deleteAttendance,
  deleteMeeting,
  deleteMeetingCompetition,
  getManagedMeeting,
  upsertAttendance,
  updateMeetingStatus,
} from '../utils/meetingApi';
import './Meeting.css';

const statusLabels = {
  ATTENDING: '참석',
  MAYBE: '미정',
  NOT_ATTENDING: '불참',
};

function normalizeAttendances(meeting) {
  return meeting?.attendances || meeting?.attendanceResponses || [];
}

function countByStatus(attendances, status) {
  return attendances.filter((attendance) => attendance.attendanceStatus === status).length;
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
    maybe: attendances.filter(
      (attendance) => attendance.attendanceStatus === 'MAYBE',
    ),
    notAttending: attendances.filter(
      (attendance) => attendance.attendanceStatus === 'NOT_ATTENDING',
    ),
  };
}

function RosterPanel({ title, tone, attendees, onAskDelete }) {
  return (
    <section className="meeting-panel meeting-roster-panel" aria-label={title}>
      <div className="meeting-roster-head">
        <h2>{title}</h2>
        <span className={`meeting-chip ${tone}`}>{attendees.length}명</span>
      </div>
      {attendees.length === 0 ? (
        <p>아직 표시할 참석자가 없습니다.</p>
      ) : (
        <div className="meeting-attendance-list">
          {attendees.map((attendance) => (
            <span
              className={`meeting-chip ${
                attendance.gender === 'FEMALE' ? 'female' : 'male'
              }`}
              key={attendance.id}
            >
              {attendance.participantName}
              <button
                type="button"
                className="meeting-attendee-remove"
                aria-label={`${attendance.participantName} 제거`}
                onClick={() => onAskDelete(attendance)}
              >
                x
              </button>
            </span>
          ))}
        </div>
      )}
    </section>
  );
}

function MeetingManage() {
  const { publicId } = useParams();
  const navigate = useNavigate();
  const [meeting, setMeeting] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [ownerStatus, setOwnerStatus] = useState('ATTENDING');
  const [isLoading, setIsLoading] = useState(true);
  const [attendeeToDelete, setAttendeeToDelete] = useState(null);
  const [message, setMessage] = useState('');
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

  const loadMeeting = useCallback(
    () =>
      getManagedMeeting(publicId).then((response) => {
        setMeeting(response.data.data);
      }),
    [publicId],
  );

  useEffect(() => {
    let cancelled = false;

    Promise.all([getManagedMeeting(publicId), getCurrentUser()])
      .then(([meetingResponse, userResponse]) => {
        if (cancelled) {
          return;
        }
        setMeeting(meetingResponse.data.data);
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

  const handleStatus = async (status) => {
    await updateMeetingStatus(publicId, status);
    await loadMeeting();
    setMessage(status === 'OPEN' ? '참석 체크를 다시 열었습니다.' : '참석 체크를 마감했습니다.');
  };

  const handleOwnerAttendance = async (status) => {
    if (!ownerName) {
      setErrorMessage('프로필 nickname을 먼저 설정해주세요.');
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
      setMessage('내 참석 상태를 저장했습니다.');
      setErrorMessage('');
    } catch (error) {
      setOwnerStatus(ownerAttendance?.attendanceStatus || 'ATTENDING');
      setErrorMessage(
        error.response?.data?.message || '내 참석 상태를 저장하지 못했습니다.',
      );
    }
  };

  const handleCreateCompetition = async () => {
    try {
      const response = await createMeetingCompetition(publicId);
      setMessage('경기표를 생성했습니다.');
      await loadMeeting();
      if (response.data.data?.publicId) {
        navigate(`/competitions/${response.data.data.publicId}`);
      }
    } catch (error) {
      setErrorMessage(error.response?.data?.message || '경기표를 생성하지 못했습니다.');
    }
  };

  const handleDeleteCompetition = async () => {
    await deleteMeetingCompetition(publicId);
    await loadMeeting();
    setMessage('연결된 경기표를 삭제했습니다.');
  };

  const handleCopyShareLink = async () => {
    const shareUrl = `${window.location.origin}/meetings/${publicId}`;
    try {
      await navigator.clipboard.writeText(shareUrl);
      setMessage('공유 링크를 복사했습니다.');
      setErrorMessage('');
    } catch {
      setErrorMessage(`공유 링크를 복사하지 못했습니다. ${shareUrl}`);
    }
  };

  const handleDeleteAttendance = async () => {
    if (!attendeeToDelete) {
      return;
    }

    try {
      await deleteAttendance(publicId, attendeeToDelete.id);
      await loadMeeting();
      setMessage(`${attendeeToDelete.participantName} 참석 응답을 삭제했습니다.`);
      setAttendeeToDelete(null);
      setErrorMessage('');
    } catch (error) {
      setErrorMessage(
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
              <p className="meeting-muted">관리자 대시보드</p>
              <h1>{meeting.title}</h1>
            </div>
            <button
              type="button"
              className="meeting-button danger small"
              onClick={handleDeleteMeeting}
            >
              삭제
            </button>
          </div>
          <div className="meeting-card-meta">
            <span className="meeting-chip">{formatDate(meeting.startAt)}</span>
            <span className="meeting-chip">
              {formatTimeRange(meeting.startAt, meeting.endAt)}
            </span>
            <span className="meeting-chip">{meeting.courtCount}코트</span>
            <span className="meeting-chip">{meeting.totalGames}경기</span>
          </div>
          {meeting.note && (
            <p className="meeting-note-box">{meeting.note}</p>
          )}
          <div className="meeting-chip-row">
            <span className="meeting-chip ok">
              참석 {countByStatus(attendances, 'ATTENDING')}
            </span>
            <span className="meeting-chip warning">
              미정 {countByStatus(attendances, 'MAYBE')}
            </span>
            <span className="meeting-chip danger">
              불참 {countByStatus(attendances, 'NOT_ATTENDING')}
            </span>
          </div>
        </section>

        <section className="meeting-panel">
          <p className="meeting-muted">내 정보</p>
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

        <section className="meeting-panel">
          <p className="meeting-muted">운영 관리</p>
          <div className="meeting-grid">
            {meeting.competitionCreated ? (
              <button
                type="button"
                className="meeting-button danger full"
                onClick={handleDeleteCompetition}
              >
                경기표 삭제
              </button>
            ) : (
              <button
                type="button"
                className="meeting-button primary full"
                onClick={handleCreateCompetition}
              >
                참석자로 경기 생성
              </button>
            )}
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
              onClick={() => navigate(`/meetings/${publicId}/edit`)}
            >
              모임 수정
            </button>
            {meeting.status === 'OPEN' ? (
              <button
                type="button"
                className="meeting-button full"
                onClick={() => handleStatus('CLOSED')}
              >
                참석 마감
              </button>
            ) : (
              <button
                type="button"
                className="meeting-button full"
                onClick={() => handleStatus('OPEN')}
              >
                다시 열기
              </button>
            )}
          </div>
          <div className="meeting-feedback-stack">
            {message && <p className="meeting-state meeting-success">{message}</p>}
            {errorMessage && <p className="meeting-state meeting-error">{errorMessage}</p>}
          </div>
        </section>

        <RosterPanel
          title="남자 참석자"
          tone="ok"
          attendees={groupedAttendances.attendingMale}
          onAskDelete={setAttendeeToDelete}
        />
        <RosterPanel
          title="여자 참석자"
          tone="ok"
          attendees={groupedAttendances.attendingFemale}
          onAskDelete={setAttendeeToDelete}
        />
        <RosterPanel
          title="미정"
          tone="warning"
          attendees={groupedAttendances.maybe}
          onAskDelete={setAttendeeToDelete}
        />
        <RosterPanel
          title="불참"
          tone="danger"
          attendees={groupedAttendances.notAttending}
          onAskDelete={setAttendeeToDelete}
        />
      </div>

      {attendeeToDelete && (
        <div className="meeting-confirm-panel" role="alertdialog">
          <strong>{attendeeToDelete.participantName} 선수를 삭제하시겠습니까?</strong>
          <p>삭제하면 이 모임의 참석 응답에서 제거됩니다.</p>
          <div className="meeting-confirm-actions">
            <button
              type="button"
              className="meeting-button"
              onClick={() => setAttendeeToDelete(null)}
            >
              취소
            </button>
            <button
              type="button"
              className="meeting-button danger"
              onClick={handleDeleteAttendance}
            >
              삭제
            </button>
          </div>
        </div>
      )}
    </main>
  );
}

export default MeetingManage;
