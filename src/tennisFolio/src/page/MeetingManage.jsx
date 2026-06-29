import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  createMeetingCompetition,
  deleteMeeting,
  deleteMeetingCompetition,
  getManagedMeeting,
  updateMeetingStatus,
} from '../utils/meetingApi';
import './Meeting.css';

function normalizeAttendances(meeting) {
  return meeting?.attendances || meeting?.attendanceResponses || [];
}

function MeetingManage() {
  const { publicId } = useParams();
  const navigate = useNavigate();
  const [meeting, setMeeting] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const attendances = useMemo(() => normalizeAttendances(meeting), [meeting]);

  const loadMeeting = useCallback(() =>
    getManagedMeeting(publicId).then((response) => {
      setMeeting(response.data.data);
    }), [publicId]);

  useEffect(() => {
    let cancelled = false;

    loadMeeting()
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
  }, [loadMeeting]);

  const handleStatus = async (status) => {
    const response = await updateMeetingStatus(publicId, status);
    setMeeting(response.data.data);
    setMessage(status === 'OPEN' ? '참석 체크를 다시 열었습니다.' : '참석 체크를 마감했습니다.');
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
    <main className="meeting-page">
      <header className="meeting-header">
        <div className="meeting-status-row">
          <div>
            <h1>{meeting.title}</h1>
            <p>{meeting.note || '모임장 관리 화면입니다.'}</p>
          </div>
          <span className="meeting-chip">{meeting.status}</span>
        </div>
        <div className="meeting-chip-row">
          <span className="meeting-chip">코트 {meeting.courtCount}</span>
          <span className="meeting-chip">경기 {meeting.totalGames}</span>
          <span className="meeting-chip">
            {meeting.competitionCreated ? '경기표 생성됨' : '경기표 없음'}
          </span>
        </div>
      </header>

      {message && <p className="meeting-state meeting-success">{message}</p>}
      {errorMessage && <p className="meeting-state meeting-error">{errorMessage}</p>}

      <section className="meeting-panel" aria-label="모임장 액션">
        <h2>관리</h2>
        <div className="meeting-action-row">
          {meeting.status === 'OPEN' ? (
            <button
              type="button"
              className="meeting-button"
              onClick={() => handleStatus('CLOSED')}
            >
              참석 마감
            </button>
          ) : (
            <button
              type="button"
              className="meeting-button"
              onClick={() => handleStatus('OPEN')}
            >
              다시 열기
            </button>
          )}
          {meeting.competitionCreated ? (
            <button
              type="button"
              className="meeting-button danger"
              onClick={handleDeleteCompetition}
            >
              경기표 삭제
            </button>
          ) : (
            <button
              type="button"
              className="meeting-button primary"
              onClick={handleCreateCompetition}
            >
              경기표 생성
            </button>
          )}
          <button
            type="button"
            className="meeting-button danger"
            onClick={handleDeleteMeeting}
          >
            모임 삭제
          </button>
        </div>
      </section>

      <section className="meeting-panel" aria-label="참석 응답">
        <h2>참석 응답</h2>
        {attendances.length === 0 ? (
          <p>아직 표시할 참석 응답이 없습니다.</p>
        ) : (
          <div className="meeting-attendance-list">
            {attendances.map((attendance) => (
              <span
                className={`meeting-chip ${
                  attendance.gender === 'FEMALE' ? 'female' : 'male'
                }`}
                key={attendance.id}
              >
                {attendance.participantName} · {attendance.attendanceStatus}
              </span>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}

export default MeetingManage;
