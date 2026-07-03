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
import MeetingConfirmModal from '../components/meeting/shared/MeetingConfirmModal';
import MeetingDangerZone from '../components/meeting/manage/MeetingDangerZone';
import MeetingManageOverviewPanel from '../components/meeting/manage/MeetingManageOverviewPanel';
import MeetingManageOperationsPanel from '../components/meeting/manage/MeetingManageOperationsPanel';
import MeetingOwnerAttendancePanel from '../components/meeting/manage/MeetingOwnerAttendancePanel';
import MeetingRosterSections from '../components/meeting/shared/MeetingRosterSections';
import {
  findCurrentUserAttendance,
  groupAttendances,
  normalizeAttendances,
} from '../components/meeting/shared/meetingAttendanceUtils';
import './Meeting.css';
import MeetingToast from './MeetingToast';

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
    () => findCurrentUserAttendance(currentUser, attendances),
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
        <MeetingManageOverviewPanel
          meeting={meeting}
          attendances={attendances}
          editDisabled={meetingEditDisabled}
          onCopyShareLink={handleCopyShareLink}
          onEditMeeting={() => navigate(`/meetings/${publicId}/edit`)}
        />

        <MeetingManageOperationsPanel
          meeting={meeting}
          onOpenCompetition={() => navigate(`/competitions/${meeting.competitionPublicId}`)}
          onCreateCompetition={handleCreateCompetition}
          onAskDeleteCompetition={() => setCompetitionDeleteRequested(true)}
          onChangeStatus={handleStatus}
        />

        <MeetingOwnerAttendancePanel
          ownerName={ownerName}
          ownerStatus={ownerStatus}
          onStatusSelect={handleOwnerAttendance}
        />

        <MeetingRosterSections
          groupedAttendances={groupedAttendances}
          meeting={meeting}
          emptyMessage={null}
          onAskDelete={setAttendeeToDelete}
        />
        <MeetingDangerZone onDeleteMeeting={handleDeleteMeeting} />
      </div>

      {attendeeToDelete && (
        <MeetingConfirmModal
          title={`${attendeeToDelete.participantName} 선수를 삭제하시겠습니까?`}
          description="삭제하면 이 모임의 참석 응답에서 제거됩니다."
          confirmLabel="삭제"
          onCancel={() => setAttendeeToDelete(null)}
          onConfirm={handleDeleteAttendance}
        />
      )}
      {competitionDeleteRequested && (
        <MeetingConfirmModal
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
