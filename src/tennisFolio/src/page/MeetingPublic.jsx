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

function RosterPanel({ title, tone, attendees }) {
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
            </span>
          ))}
        </div>
      )}
    </section>
  );
}

function MeetingPublic() {
  const { publicId } = useParams();
  const [meeting, setMeeting] = useState(null);
  const [form, setForm] = useState(emptyAttendance);
  const [hasEntered, setHasEntered] = useState(() =>
    new URLSearchParams(window.location.search).has('entry'),
  );
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const attendances = useMemo(() => normalizeAttendances(meeting), [meeting]);
  const groupedAttendances = useMemo(
    () => groupAttendances(attendances),
    [attendances],
  );

  const loadMeeting = useCallback(
    () =>
      getPublicMeeting(publicId).then((response) => {
        setMeeting(response.data.data);
      }),
    [publicId],
  );

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

  const selectAttendance = (attendance) => {
    setForm({
      attendanceId: attendance.id || '',
      participantName: attendance.participantName,
      gender: attendance.gender,
      attendanceStatus: attendance.attendanceStatus,
    });
    setHasEntered(true);
    setMessage('선택한 응답을 아래에서 수정할 수 있습니다.');
    setErrorMessage('');
  };

  const saveAttendance = async (nextForm) => {
    if (!nextForm.participantName.trim()) {
      setErrorMessage('이름을 입력해주세요.');
      return false;
    }

    try {
      const response = await upsertAttendance(publicId, {
        attendanceId: nextForm.attendanceId || null,
        participantName: nextForm.participantName.trim(),
        gender: nextForm.gender,
        attendanceStatus: nextForm.attendanceStatus,
      });
      const savedAttendance = response.data.data;
      await loadMeeting();
      setForm({
        attendanceId: savedAttendance?.id ?? nextForm.attendanceId,
        participantName: savedAttendance?.participantName ?? nextForm.participantName.trim(),
        gender: savedAttendance?.gender ?? nextForm.gender,
        attendanceStatus: savedAttendance?.attendanceStatus ?? nextForm.attendanceStatus,
      });
      setHasEntered(true);
      setMessage('참석 상태를 저장했습니다.');
      setErrorMessage('');
      return true;
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || '참석 상태를 저장하지 못했습니다.',
      );
      return false;
    }
  };

  const handleSaveProfile = async () => {
    await saveAttendance(form);
  };

  const handleStatusClick = async (status) => {
    const nextForm = { ...form, attendanceStatus: status };
    setForm(nextForm);
    const saved = await saveAttendance(nextForm);
    if (!saved) {
      setForm(form);
    }
  };

  const renderEntryScreen = () => {
    const maleAttendances = attendances.filter(
      (attendance) => attendance.gender === 'MALE',
    );
    const femaleAttendances = attendances.filter(
      (attendance) => attendance.gender === 'FEMALE',
    );

    return (
      <main className="meeting-page">
        <section className="meeting-panel">
          <p className="meeting-muted">처음 입장</p>
          <h1>{meeting.title}</h1>
          <div className="meeting-card-meta">
            <span className="meeting-chip">{formatDate(meeting.startAt)}</span>
            <span className="meeting-chip">
              {formatTimeRange(meeting.startAt, meeting.endAt)}
            </span>
          </div>
          {meeting.note && <p className="meeting-note-box">{meeting.note}</p>}
          <p className="meeting-state-note">
            이미 응답했다면 아래 이름을 눌러 다시 입장하세요.
          </p>

          <div className="meeting-entry-name-groups">
            <section className="meeting-entry-name-group" aria-label="남자">
              <p className="meeting-muted">남자</p>
              <div className="meeting-attendance-list">
                {maleAttendances.map((attendance) => (
                  <button
                    type="button"
                    className="meeting-chip male"
                    key={attendance.id}
                    onClick={() => selectAttendance(attendance)}
                  >
                    {attendance.participantName}
                  </button>
                ))}
              </div>
            </section>
            <section className="meeting-entry-name-group" aria-label="여자">
              <p className="meeting-muted">여자</p>
              <div className="meeting-attendance-list">
                {femaleAttendances.map((attendance) => (
                  <button
                    type="button"
                    className="meeting-chip female"
                    key={attendance.id}
                    onClick={() => selectAttendance(attendance)}
                  >
                    {attendance.participantName}
                  </button>
                ))}
              </div>
            </section>
          </div>

          <div className="meeting-grid two">
            <label className="meeting-field">
              <span>이름</span>
              <input
                value={form.participantName}
                onChange={(event) => updateField('participantName', event.target.value)}
              />
            </label>
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
          </div>

          <div className="meeting-status-options">
            {Object.entries(statusLabels).map(([status, label]) => (
              <button
                type="button"
                className={`meeting-button full ${
                  form.attendanceStatus === status ? 'primary' : ''
                }`}
                key={status}
                onClick={() => handleStatusClick(status)}
              >
                {label}
              </button>
            ))}
          </div>

          <p className="meeting-state-note">
            참석 여부를 선택하면 참석 현황과 명단을 볼 수 있습니다.
          </p>
        </section>

        <div className="meeting-feedback-stack">
          {message && <p className="meeting-state meeting-success">{message}</p>}
          {errorMessage && <p className="meeting-state meeting-error">{errorMessage}</p>}
        </div>
      </main>
    );
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

  if (!hasEntered) {
    return renderEntryScreen();
  }

  return (
    <main className="meeting-page">
      <section className="meeting-panel">
        <div className="meeting-card-title-row">
          <div>
            <p className="meeting-muted">공개 참석 체크</p>
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
        {meeting.note && <p className="meeting-note-box">{meeting.note}</p>}
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
        <p className="meeting-state-note">
          이름이나 상태가 바뀌었다면 아래에서 선택해 수정할 수 있습니다.
        </p>
        <button type="button" className="meeting-button full" onClick={handleCopyShareLink}>
          공유 링크 복사
        </button>
      </section>

      <section className="meeting-panel" aria-label="내 정보">
        <p className="meeting-muted">내 정보</p>
        <div className="meeting-grid two">
          <label className="meeting-field">
            <span>이름</span>
            <input
              value={form.participantName}
              onChange={(event) => updateField('participantName', event.target.value)}
            />
          </label>
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
        </div>
        <button type="button" className="meeting-button full" onClick={handleSaveProfile}>
          정보 저장
        </button>
        <div className="meeting-status-row">
          <span className="meeting-muted">상태</span>
          <div className="meeting-status-options">
            {Object.entries(statusLabels).map(([status, label]) => (
              <button
                type="button"
                className={`meeting-button status-option ${
                  form.attendanceStatus === status ? 'primary' : ''
                }`}
                key={status}
                onClick={() => handleStatusClick(status)}
              >
                {label}
              </button>
            ))}
          </div>
        </div>
      </section>

      <div className="meeting-feedback-stack">
        {message && <p className="meeting-state meeting-success">{message}</p>}
        {errorMessage && <p className="meeting-state meeting-error">{errorMessage}</p>}
      </div>

      <RosterPanel
        title="남자 참석자"
        tone="ok"
        attendees={groupedAttendances.attendingMale}
      />
      <RosterPanel
        title="여자 참석자"
        tone="ok"
        attendees={groupedAttendances.attendingFemale}
      />
      <RosterPanel
        title="미정"
        tone="warning"
        attendees={groupedAttendances.maybe}
      />
      <RosterPanel
        title="불참"
        tone="danger"
        attendees={groupedAttendances.notAttending}
      />
    </main>
  );
}

export default MeetingPublic;
