import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { deleteMeeting, getMyMeetings } from '../utils/meetingApi';
import './Meeting.css';
import MeetingToast from './MeetingToast';

function formatDateTime(value) {
  return value ? value.replace('T', ' ').slice(0, 16) : '';
}

function formatCount(value) {
  return Number.isFinite(Number(value)) ? Number(value) : 0;
}

function copyText(value) {
  if (navigator.clipboard) {
    return navigator.clipboard.writeText(value);
  }
  return Promise.resolve();
}

function Meetings() {
  const navigate = useNavigate();
  const [meetings, setMeetings] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [notice, setNotice] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let cancelled = false;

    getMyMeetings()
      .then((response) => {
        if (!cancelled) {
          setMeetings(response.data.data || []);
          setErrorMessage('');
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setErrorMessage(
            error.response?.status === 401
              ? '로그인이 필요합니다.'
              : '모임 목록을 불러오지 못했습니다.',
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
  }, []);

  const showNotice = (type, message) => {
    setNotice({ type, message });
  };

  const handleShare = async (publicId) => {
    await copyText(`${window.location.origin}/meetings/${publicId}`);
    showNotice('success', '공유 링크를 복사했습니다.');
  };

  const handleDelete = async (meeting) => {
    const confirmed = window.confirm('모임을 삭제할까요?');
    if (!confirmed) {
      return;
    }

    await deleteMeeting(meeting.publicId);
    setMeetings((current) =>
      current.filter((item) => item.publicId !== meeting.publicId),
    );
    showNotice('success', '모임을 삭제했습니다.');
  };

  return (
    <main className="meeting-page">
      <header className="meeting-header">
        <div className="meeting-header-row">
          <div>
            <h1>내 모임</h1>
            <p>참석 체크 링크를 만들고 경기표 생성까지 이어갑니다.</p>
          </div>
          <button
            type="button"
            className="meeting-button primary"
            onClick={() => navigate('/meetings/new')}
          >
            모임 만들기
          </button>
        </div>
      </header>

      {isLoading && <p className="meeting-state">불러오는 중입니다.</p>}
      {!isLoading && errorMessage && (
        <p className="meeting-state meeting-error">{errorMessage}</p>
      )}
      {!isLoading && !errorMessage && meetings.length === 0 && (
        <section className="meeting-state">
          아직 만든 모임이 없습니다. 첫 모임을 만들고 참석 링크를 공유해보세요.
        </section>
      )}

      {!isLoading && !errorMessage && meetings.length > 0 && (
        <section className="meeting-list" aria-label="내 모임 목록">
          {meetings.map((meeting) => (
            <article className="meeting-card" key={meeting.publicId}>
              <div className="meeting-card-title-row">
                <div>
                  <h2>{meeting.title}</h2>
                  <span className="meeting-card-status">
                    {meeting.competitionCreated ? '경기표 생성됨' : meeting.status}
                  </span>
                </div>
                <button
                  type="button"
                  className="meeting-button danger small"
                  onClick={() => handleDelete(meeting)}
                >
                  삭제
                </button>
              </div>

              <div className="meeting-card-meta">
                <span className="meeting-chip">{formatDateTime(meeting.startAt)}</span>
                <span className="meeting-chip">{formatDateTime(meeting.endAt)}</span>
                <span className="meeting-chip">
                  {formatCount(meeting.courtCount)}코트
                </span>
                <span className="meeting-chip">
                  {formatCount(meeting.totalGames)}경기
                </span>
              </div>

              <div className="meeting-chip-row">
                <span className="meeting-chip ok">
                  참석 {formatCount(meeting.attendingCount)}
                </span>
                <span className="meeting-chip warning">
                  미정 {formatCount(meeting.maybeCount)}
                </span>
                <span className="meeting-chip danger">
                  불참 {formatCount(meeting.notAttendingCount)}
                </span>
              </div>

              <div className="meeting-card-actions">
                <button
                  type="button"
                  className="meeting-button primary"
                  onClick={() => navigate(`/meetings/${meeting.publicId}/manage`)}
                >
                  관리
                </button>
                <button
                  type="button"
                  className="meeting-button"
                  onClick={() => handleShare(meeting.publicId)}
                >
                  공유
                </button>
              </div>
            </article>
          ))}
        </section>
      )}
      <MeetingToast notice={notice} onClose={() => setNotice(null)} />
    </main>
  );
}

export default Meetings;
