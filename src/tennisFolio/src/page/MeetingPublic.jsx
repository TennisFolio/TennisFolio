import { useCallback, useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getPublicMeeting, upsertAttendance } from '../utils/meetingApi';
import './Meeting.css';

const emptyAttendance = {
  attendanceId: '',
  participantName: '',
  gender: 'MALE',
  attendanceStatus: 'ATTENDING',
};

function normalizeAttendances(meeting) {
  return meeting?.attendances || meeting?.attendanceResponses || [];
}

function countByStatus(attendances, status) {
  return attendances.filter((attendance) => attendance.attendanceStatus === status).length;
}

function MeetingPublic() {
  const { publicId } = useParams();
  const [meeting, setMeeting] = useState(null);
  const [form, setForm] = useState(emptyAttendance);
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const attendances = useMemo(() => normalizeAttendances(meeting), [meeting]);

  const loadMeeting = useCallback(() =>
    getPublicMeeting(publicId).then((response) => {
      setMeeting(response.data.data);
    }), [publicId]);

  useEffect(() => {
    let cancelled = false;

    loadMeeting()
      .catch(() => {
        if (!cancelled) {
          setErrorMessage('모임 정보를 불러오지 못했습니다.');
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

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
    setErrorMessage('');
    setMessage('');
  };

  const handleSubmit = async () => {
    if (!form.participantName.trim()) {
      setErrorMessage('이름을 입력해주세요.');
      return;
    }

    try {
      await upsertAttendance(publicId, {
        attendanceId: form.attendanceId || null,
        participantName: form.participantName.trim(),
        gender: form.gender,
        attendanceStatus: form.attendanceStatus,
      });
      await loadMeeting();
      setMessage('참석 상태를 저장했습니다.');
      setForm(emptyAttendance);
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || '참석 상태를 저장하지 못했습니다.',
      );
    }
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
        <h1>{meeting.title}</h1>
        <p>{meeting.note || '참석 가능 여부를 남겨주세요.'}</p>
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
      </header>

      {message && <p className="meeting-state meeting-success">{message}</p>}
      {errorMessage && <p className="meeting-state meeting-error">{errorMessage}</p>}

      <section className="meeting-panel" aria-label="참석 체크">
        <h2>내 참석 상태</h2>
        <label className="meeting-field">
          <span>이름</span>
          <input
            value={form.participantName}
            onChange={(event) => updateField('participantName', event.target.value)}
          />
        </label>
        <div className="meeting-grid two">
          <label className="meeting-field">
            <span>성별</span>
            <select
              value={form.gender}
              onChange={(event) => updateField('gender', event.target.value)}
            >
              <option value="MALE">남성</option>
              <option value="FEMALE">여성</option>
            </select>
          </label>
          <label className="meeting-field">
            <span>상태</span>
            <select
              value={form.attendanceStatus}
              onChange={(event) =>
                updateField('attendanceStatus', event.target.value)
              }
            >
              <option value="ATTENDING">참석</option>
              <option value="MAYBE">미정</option>
              <option value="NOT_ATTENDING">불참</option>
            </select>
          </label>
        </div>
        <button type="button" className="meeting-button primary full" onClick={handleSubmit}>
          참석 상태 저장
        </button>
      </section>

      <section className="meeting-panel" aria-label="참석자 현황">
        <h2>참석자 현황</h2>
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
                {attendance.participantName}
              </span>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}

export default MeetingPublic;
