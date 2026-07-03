import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { getPublicMeeting, upsertAttendance } from '../utils/meetingApi';
import { getCurrentUser } from '../utils/authApi';
import './Meeting.css';
import MeetingManage from './MeetingManage';
import MeetingToast from './MeetingToast';

const emptyAttendance = {
  attendanceId: '',
  participantName: '',
  gender: 'MALE',
  attendanceStatus: 'ATTENDING',
};

const statusLabels = {
  ATTENDING: '참석',
  WAITING: '대기',
  NOT_ATTENDING: '불참',
};

function normalizeAttendances(meeting) {
  return meeting?.attendances || meeting?.attendanceResponses || [];
}

function getRememberedAttendanceKey(publicId) {
  return `meetingPublic:${publicId}:attendance`;
}

function getAttendanceForm(attendance) {
  return {
    attendanceId: attendance.id || '',
    participantName: attendance.participantName,
    gender: attendance.gender,
    attendanceStatus: attendance.attendanceStatus,
  };
}

function forgetRememberedAttendance(publicId) {
  localStorage.removeItem(getRememberedAttendanceKey(publicId));
}

function rememberAttendance(publicId, attendance) {
  const attendanceId = attendance?.id || attendance?.attendanceId;

  if (!attendanceId) {
    return;
  }

  localStorage.setItem(
    getRememberedAttendanceKey(publicId),
    JSON.stringify({
      attendanceId,
      participantName: attendance.participantName,
    }),
  );
}

function findRememberedAttendance(publicId, attendances) {
  const rememberedValue = localStorage.getItem(getRememberedAttendanceKey(publicId));

  if (!rememberedValue) {
    return null;
  }

  try {
    const remembered = JSON.parse(rememberedValue);
    const attendance = attendances.find(
      (attendance) => attendance.id === remembered.attendanceId,
    );

    if (attendance) {
      return attendance;
    }
  } catch {
    // Invalid localStorage values are treated the same as stale attendance ids.
  }

  forgetRememberedAttendance(publicId);
  return null;
}

function findCurrentUserAttendance(currentUser, attendances) {
  const nickName = currentUser?.nickName?.trim();

  if (!nickName) {
    return null;
  }

  return (
    attendances.find((attendance) => attendance.participantName === nickName) ||
    null
  );
}

function getCurrentUserForm(currentUser) {
  const nickName = currentUser?.nickName?.trim();

  if (!nickName) {
    return null;
  }

  return {
    ...emptyAttendance,
    participantName: nickName,
    gender: currentUser.gender || emptyAttendance.gender,
  };
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

function AttendanceChip({ attendance, meeting, asButton = false, onSelect }) {
  const isOwner = isOwnerAttendance(attendance, meeting);
  const className = `meeting-chip ${
    attendance.gender === 'FEMALE' ? 'female' : 'male'
  }${isOwner ? ' owner' : ''}`;
  const content = (
    <>
      {attendance.participantName}
      {isOwner && <OwnerTag />}
    </>
  );

  if (!asButton || isOwner) {
    return (
      <span className={className} key={attendance.id}>
        {content}
      </span>
    );
  }

  return (
    <button
      type="button"
      className={className}
      key={attendance.id}
      onClick={() => onSelect(attendance)}
    >
      {content}
    </button>
  );
}

function RosterPanel({ title, tone, attendees, meeting }) {
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
            <AttendanceChip
              attendance={attendance}
              key={attendance.id}
              meeting={meeting}
            />
          ))}
        </div>
      )}
    </section>
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

function MeetingPublic() {
  const { publicId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const initialNotice = location.state?.meetingNotice || null;
  const [meeting, setMeeting] = useState(null);
  const [form, setForm] = useState(emptyAttendance);
  const [currentUser, setCurrentUser] = useState(null);
  const [hasEntered, setHasEntered] = useState(() =>
    new URLSearchParams(window.location.search).has('entry'),
  );
  const [isLoading, setIsLoading] = useState(true);
  const [notice, setNotice] = useState(initialNotice);
  const [errorMessage, setErrorMessage] = useState('');

  const attendances = useMemo(() => normalizeAttendances(meeting), [meeting]);
  const groupedAttendances = useMemo(
    () => groupAttendances(attendances),
    [attendances],
  );

  const loadMeeting = useCallback(
    () =>
      Promise.allSettled([getPublicMeeting(publicId), getCurrentUser()]).then(
        ([meetingResult, currentUserResult]) => {
          if (meetingResult.status === 'rejected') {
            throw meetingResult.reason;
          }

          const nextMeeting = meetingResult.value.data.data;
          const nextAttendances = normalizeAttendances(nextMeeting);
          const currentUser =
            currentUserResult.status === 'fulfilled'
              ? currentUserResult.value.data.data
              : null;
          setCurrentUser(currentUser);
          const rememberedAttendance = findRememberedAttendance(
            publicId,
            nextAttendances,
          );
          const currentUserAttendance = findCurrentUserAttendance(
            currentUser,
            nextAttendances,
          );
          const currentUserForm = getCurrentUserForm(currentUser);

          setMeeting(nextMeeting);

          if (currentUserAttendance) {
            setForm(getAttendanceForm(currentUserAttendance));
            rememberAttendance(publicId, currentUserAttendance);
            setHasEntered(true);
            return;
          }

          if (currentUserForm) {
            setForm((current) => ({
              ...currentUserForm,
              attendanceStatus: current.attendanceStatus,
            }));
            return;
          }

          if (rememberedAttendance) {
            setForm(getAttendanceForm(rememberedAttendance));
            setHasEntered(true);
          }
        },
      ),
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

  useEffect(() => {
    if (initialNotice) {
      navigate(`/meetings/${publicId}`, { replace: true, state: null });
    }
  }, [initialNotice, navigate, publicId]);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
    setNotice(null);
  };

  const showNotice = (type, message) => {
    setNotice({ type, message });
  };

  const currentUserName = currentUser?.nickName?.trim() || '';
  const isCurrentUserNameLocked = Boolean(currentUserName);

  const selectAttendance = (attendance) => {
    setForm(getAttendanceForm(attendance));
    rememberAttendance(publicId, attendance);
    setHasEntered(true);
    showNotice('success', '선택한 응답을 아래에서 수정할 수 있습니다.');
  };

  const saveAttendance = async (nextForm) => {
    const participantName = (
      isCurrentUserNameLocked ? currentUserName : nextForm.participantName
    ).trim();
    const ownerNickName = meeting?.ownerNickName?.trim();

    if (!participantName) {
      showNotice('error', '이름을 입력해주세요.');
      return false;
    }

    if (ownerNickName && participantName === ownerNickName) {
      showNotice('error', '모임장은 참석자로 선택할 수 없습니다.');
      return false;
    }

    try {
      const response = await upsertAttendance(publicId, {
        attendanceId: nextForm.attendanceId || null,
        participantName,
        gender: nextForm.gender,
        attendanceStatus: nextForm.attendanceStatus,
      });
      const savedAttendance = response.data.data;
      rememberAttendance(publicId, savedAttendance || nextForm);
      await loadMeeting();
      setForm({
        attendanceId: savedAttendance?.id ?? nextForm.attendanceId,
        participantName: savedAttendance?.participantName ?? participantName,
        gender: savedAttendance?.gender ?? nextForm.gender,
        attendanceStatus: savedAttendance?.attendanceStatus ?? nextForm.attendanceStatus,
      });
      setHasEntered(true);
      showNotice('success', '참석 상태를 저장했습니다.');
      return true;
    } catch (error) {
      showNotice(
        'error',
        error.response?.data?.message || '참석 상태를 저장하지 못했습니다.',
      );
      return false;
    }
  };

  const handleEnterAsDifferentParticipant = () => {
    forgetRememberedAttendance(publicId);
    setForm({
      ...emptyAttendance,
      participantName: isCurrentUserNameLocked ? currentUserName : '',
      gender: currentUser?.gender || emptyAttendance.gender,
    });
    setHasEntered(false);
    setNotice(null);
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
            <div className="meeting-grid two">
              <label className="meeting-field">
                <span>이름</span>
                <input
                  value={form.participantName}
                  readOnly={isCurrentUserNameLocked}
                  onChange={(event) =>
                    updateField('participantName', event.target.value)
                  }
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
          </div>

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
                        asButton={!isCurrentUserNameLocked}
                        onSelect={selectAttendance}
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

        <MeetingToast notice={notice} onClose={() => setNotice(null)} />
      </main>
    );
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

  if (meeting.ownedByCurrentUser === true) {
    return <MeetingManage initialMeeting={meeting} initialNotice={notice} />;
  }

  if (!hasEntered) {
    return renderEntryScreen();
  }

  return (
    <main className="meeting-page">
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
        {meeting.note && <p className="meeting-note-box">{meeting.note}</p>}
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
        <p className="meeting-state-note">
          이름이나 상태가 바뀌었다면 아래에서 선택해 수정할 수 있습니다.
        </p>
        <button type="button" className="meeting-button full" onClick={handleCopyShareLink}>
          공유 링크 복사
        </button>
        {meeting.competitionPublicId && (
          <button
            type="button"
            className="meeting-button primary full"
            onClick={() => navigate(`/competitions/${meeting.competitionPublicId}`)}
          >
            경기표 보기
          </button>
        )}
      </section>

      <section className="meeting-panel">
        <div className="meeting-grid two">
          <label className="meeting-field">
            <span>이름</span>
            <input
              value={form.participantName}
              readOnly={isCurrentUserNameLocked}
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
        <button
          type="button"
          className="meeting-button full"
          onClick={handleEnterAsDifferentParticipant}
        >
          다른 이름으로 입장
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

      <MeetingToast notice={notice} onClose={() => setNotice(null)} />

      <RosterPanel
        title="남자 참석자"
        tone="ok"
        attendees={groupedAttendances.attendingMale}
        meeting={meeting}
      />
      <RosterPanel
        title="여자 참석자"
        tone="ok"
        attendees={groupedAttendances.attendingFemale}
        meeting={meeting}
      />
      <RosterPanel
        title="대기"
        tone="warning"
        attendees={groupedAttendances.waiting}
        meeting={meeting}
      />
      <RosterPanel
        title="불참"
        tone="danger"
        attendees={groupedAttendances.notAttending}
        meeting={meeting}
      />
    </main>
  );
}

export default MeetingPublic;
