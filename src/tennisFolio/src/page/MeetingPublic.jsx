import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import {
  createMeetingCompetitionWithOptions,
  deleteMeetingCompetition,
  getPublicMeeting,
} from '../utils/meetingApi';
import MeetingConfirmModal from '../components/meeting/shared/MeetingConfirmModal';
import MeetingManageOperationsPanel from '../components/meeting/manage/MeetingManageOperationsPanel';
import MeetingPublicOverviewPanel from '../components/meeting/public/MeetingPublicOverviewPanel';
import MeetingRosterSections from '../components/meeting/shared/MeetingRosterSections';
import {
  groupAttendances,
  normalizeAttendances,
} from '../components/meeting/shared/meetingAttendanceUtils';
import { getSameGenderDoublesOnlyUnavailableReason } from '../hooks/competitionCreateFormConfig';
import './Meeting.css';
import MeetingToast from './MeetingToast';

function MeetingPublic() {
  const { publicId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const initialNotice = location.state?.meetingNotice || null;
  const [meeting, setMeeting] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [notice, setNotice] = useState(initialNotice);
  const [errorMessage, setErrorMessage] = useState('');
  const [sameGenderDoublesOnly, setSameGenderDoublesOnly] = useState(false);
  const [competitionDeleteRequested, setCompetitionDeleteRequested] = useState(false);

  const attendances = useMemo(() => normalizeAttendances(meeting), [meeting]);
  const groupedAttendances = useMemo(
    () => groupAttendances(attendances),
    [attendances],
  );
  const attendingGenderCounts = useMemo(
    () =>
      attendances.reduce(
        (counts, attendance) => {
          if (attendance.attendanceStatus !== 'ATTENDING') {
            return counts;
          }
          if (attendance.gender === 'MALE') {
            return { ...counts, maleCount: counts.maleCount + 1 };
          }
          if (attendance.gender === 'FEMALE') {
            return { ...counts, femaleCount: counts.femaleCount + 1 };
          }
          return counts;
        },
        { maleCount: 0, femaleCount: 0 },
      ),
    [attendances],
  );
  const sameGenderDoublesOnlyUnavailableReason = useMemo(
    () =>
      getSameGenderDoublesOnlyUnavailableReason({
        maleCount: attendingGenderCounts.maleCount,
        femaleCount: attendingGenderCounts.femaleCount,
        totalGames: meeting?.totalGames ?? 0,
      }),
    [attendingGenderCounts, meeting?.totalGames],
  );
  const sameGenderDoublesOnlyUnavailable =
    sameGenderDoublesOnly && Boolean(sameGenderDoublesOnlyUnavailableReason);

  const loadMeeting = useCallback(
    () => getPublicMeeting(publicId).then((response) => setMeeting(response.data.data)),
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

  const showNotice = (type, message) => {
    setNotice({ type, message });
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

  const handleCreateCompetition = async () => {
    if (sameGenderDoublesOnlyUnavailable) {
      showNotice('error', sameGenderDoublesOnlyUnavailableReason);
      return;
    }

    try {
      const response = await createMeetingCompetitionWithOptions(publicId, {
        sameGenderDoublesOnly,
      });
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
    try {
      await deleteMeetingCompetition(publicId);
      await loadMeeting();
      showNotice('success', '연결된 대진표를 삭제했습니다.');
      setCompetitionDeleteRequested(false);
    } catch (error) {
      showNotice('error', error.response?.data?.message || '대진표를 삭제하지 못했습니다.');
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
      <MeetingPublicOverviewPanel
        meeting={meeting}
        attendances={attendances}
        onCopyShareLink={handleCopyShareLink}
        onOpenCompetition={() => navigate(`/competitions/${meeting.competitionPublicId}`)}
        canManage={meeting.ownedByCurrentUser === true}
        onManageMeeting={() => navigate(`/meetings/${publicId}/manage`)}
      />

      {meeting.ownedByCurrentUser === true && (
        <MeetingManageOperationsPanel
          meeting={meeting}
          onOpenCompetition={() => navigate(`/competitions/${meeting.competitionPublicId}`)}
          onCreateCompetition={handleCreateCompetition}
          onAskDeleteCompetition={() => setCompetitionDeleteRequested(true)}
          sameGenderDoublesOnly={sameGenderDoublesOnly}
          onSameGenderDoublesOnlyChange={setSameGenderDoublesOnly}
          isClubMeeting={false}
          skillBalancedSchedule={false}
          onSkillBalancedScheduleChange={() => {}}
          sameGenderDoublesOnlyUnavailable={sameGenderDoublesOnlyUnavailable}
          sameGenderDoublesOnlyUnavailableReason={sameGenderDoublesOnlyUnavailableReason}
          skillBalancedScheduleUnavailable={false}
          skillBalancedScheduleUnavailableReason=""
        />
      )}

      <MeetingToast notice={notice} onClose={() => setNotice(null)} />

      <MeetingRosterSections
        groupedAttendances={groupedAttendances}
        meeting={meeting}
      />

      {competitionDeleteRequested && (
        <MeetingConfirmModal
          title="생성된 대진표를 삭제하시겠습니까?"
          description="삭제하면 참석자 명단은 유지되고 대진표 연결만 해제됩니다."
          confirmLabel="대진표 삭제"
          onCancel={() => setCompetitionDeleteRequested(false)}
          onConfirm={handleDeleteCompetition}
        />
      )}
    </main>
  );
}

export default MeetingPublic;
