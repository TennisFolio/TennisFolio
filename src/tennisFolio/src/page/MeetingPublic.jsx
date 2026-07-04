import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { getPublicMeeting, upsertAttendance } from '../utils/meetingApi';
import { getCurrentUser } from '../utils/authApi';
import MeetingPublicAttendancePanel from '../components/meeting/public/MeetingPublicAttendancePanel';
import MeetingPublicEntryScreen from '../components/meeting/public/MeetingPublicEntryScreen';
import MeetingPublicOverviewPanel from '../components/meeting/public/MeetingPublicOverviewPanel';
import MeetingRosterSections from '../components/meeting/shared/MeetingRosterSections';
import {
  emptyAttendance,
  findCurrentUserAttendance,
  getAttendanceForm,
  getCurrentUserForm,
  groupAttendances,
  normalizeAttendances,
} from '../components/meeting/shared/meetingAttendanceUtils';
import {
  findRememberedAttendance,
  forgetRememberedAttendance,
  rememberAttendance,
} from '../components/meeting/public/meetingAttendanceStorage';
import './Meeting.css';
import MeetingManage from './MeetingManage';
import MeetingToast from './MeetingToast';

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
    return (
      <MeetingPublicEntryScreen
        meeting={meeting}
        attendances={attendances}
        form={form}
        isNameLocked={isCurrentUserNameLocked}
        notice={notice}
        onCloseNotice={() => setNotice(null)}
        onFieldChange={updateField}
        onStatusSelect={handleStatusClick}
        onAttendanceSelect={selectAttendance}
      />
    );
  }

  return (
    <main className="meeting-page">
      <MeetingPublicOverviewPanel
        meeting={meeting}
        attendances={attendances}
        onCopyShareLink={handleCopyShareLink}
        onOpenCompetition={() => navigate(`/competitions/${meeting.competitionPublicId}`)}
      />

      <MeetingPublicAttendancePanel
        form={form}
        isNameLocked={isCurrentUserNameLocked}
        onFieldChange={updateField}
        onSaveProfile={handleSaveProfile}
        onEnterAsDifferentParticipant={handleEnterAsDifferentParticipant}
        onStatusSelect={handleStatusClick}
      />

      <MeetingToast notice={notice} onClose={() => setNotice(null)} />

      <MeetingRosterSections
        groupedAttendances={groupedAttendances}
        meeting={meeting}
      />
    </main>
  );
}

export default MeetingPublic;
