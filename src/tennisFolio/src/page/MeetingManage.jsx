import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getCurrentUser } from '../utils/authApi';
import {
  createMeetingCompetitionWithOptions,
  addManagedParticipant,
  deleteAttendance,
  deleteMeetingCompetition,
  getPublicMeeting,
  upsertAttendance,
  updateMeetingStatus,
} from '../utils/meetingApi';
import {
  createClubMeetingCompetitionWithOptions,
  deleteClubMeetingCompetition,
  getClubMembers,
  getClubMeeting,
  updateClubMeetingStatus,
} from '../utils/clubApi';
import MeetingConfirmModal from '../components/meeting/shared/MeetingConfirmModal';
import MeetingManageOverviewPanel from '../components/meeting/manage/MeetingManageOverviewPanel';
import MeetingManageOperationsPanel from '../components/meeting/manage/MeetingManageOperationsPanel';
import MeetingOwnerAttendancePanel from '../components/meeting/manage/MeetingOwnerAttendancePanel';
import MeetingParticipantAddPanel from '../components/meeting/manage/MeetingParticipantAddPanel';
import MeetingRosterSections from '../components/meeting/shared/MeetingRosterSections';
import {
  findCurrentUserAttendance,
  groupAttendances,
  normalizeAttendances,
} from '../components/meeting/shared/meetingAttendanceUtils';
import { getSameGenderDoublesOnlyUnavailableReason } from '../hooks/competitionCreateFormConfig';
import './Meeting.css';
import MeetingToast from './MeetingToast';

function MeetingManage({ initialMeeting = null, initialNotice = null }) {
  const { clubPublicId, publicId } = useParams();
  const navigate = useNavigate();
  const [meeting, setMeeting] = useState(initialMeeting);
  const [currentUser, setCurrentUser] = useState(null);
  const [ownerStatus, setOwnerStatus] = useState('ATTENDING');
  const [isLoading, setIsLoading] = useState(true);
  const [attendeeToDelete, setAttendeeToDelete] = useState(null);
  const [competitionDeleteRequested, setCompetitionDeleteRequested] =
    useState(false);
  const [sameGenderDoublesOnly, setSameGenderDoublesOnly] = useState(false);
  const [notice, setNotice] = useState(initialNotice);
  const [errorMessage, setErrorMessage] = useState('');
  const [clubMembers, setClubMembers] = useState([]);
  const [memberQuery, setMemberQuery] = useState('');
  const [isParticipantSubmitting, setIsParticipantSubmitting] = useState(false);
  const [participantAddOpen, setParticipantAddOpen] = useState(false);

  const attendances = useMemo(() => normalizeAttendances(meeting), [meeting]);
  const ownerAttendance = useMemo(
    () => findCurrentUserAttendance(currentUser, meeting, attendances),
    [attendances, currentUser, meeting],
  );
  const groupedAttendances = useMemo(
    () => groupAttendances(attendances),
    [attendances],
  );
  const ownerName = currentUser?.nickName?.trim() || '';
  const meetingEditDisabled = Boolean(meeting?.competitionCreated);
  const registeredClubMemberIds = useMemo(
    () => new Set(attendances.map((attendance) => attendance.clubMemberId).filter(Boolean)),
    [attendances],
  );
  const selectableClubMembers = useMemo(
    () => clubMembers.filter((member) => !registeredClubMemberIds.has(member.id)),
    [clubMembers, registeredClubMemberIds],
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
    () => {
      const request = clubPublicId
        ? getClubMeeting(clubPublicId, publicId)
        : getPublicMeeting(publicId);

      return request.then((response) => {
        const nextMeeting = response.data.data;
        if (!clubPublicId && nextMeeting?.ownedByCurrentUser !== true) {
          throw new Error('FORBIDDEN_MEETING_OWNER');
        }
        setMeeting(nextMeeting);
      });
    },
    [clubPublicId, publicId],
  );

  useEffect(() => {
    let cancelled = false;

    const meetingRequest = clubPublicId
      ? getClubMeeting(clubPublicId, publicId)
      : getPublicMeeting(publicId);

    Promise.all([meetingRequest, getCurrentUser()])
      .then(([meetingResponse, userResponse]) => {
        if (cancelled) {
          return;
        }
        const nextMeeting = meetingResponse.data.data;
        if (!clubPublicId && nextMeeting?.ownedByCurrentUser !== true) {
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
  }, [clubPublicId, publicId]);

  useEffect(() => {
    if (!clubPublicId) {
      setClubMembers([]);
      return undefined;
    }

    let cancelled = false;
    getClubMembers(clubPublicId, memberQuery.trim())
      .then((response) => {
        if (!cancelled) {
          setClubMembers(response.data.data || []);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setClubMembers([]);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [clubPublicId, memberQuery]);

  useEffect(() => {
    if (ownerAttendance?.attendanceStatus) {
      setOwnerStatus(ownerAttendance.attendanceStatus);
    }
  }, [ownerAttendance]);

  const showNotice = (type, message) => {
    setNotice({ type, message });
  };

  const handleStatus = async (status) => {
    if (clubPublicId) {
      await updateClubMeetingStatus(clubPublicId, publicId, status);
    } else {
      await updateMeetingStatus(publicId, status);
    }
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

  const isGuestPromotion = (participant) => {
    if (!participant.clubMemberId) {
      return false;
    }

    const selectedMember = clubMembers.find((member) => member.id === participant.clubMemberId);
    return Boolean(
      selectedMember &&
        attendances.some(
          (attendance) =>
            attendance.participantType === 'GUEST' &&
            attendance.participantName === selectedMember.name &&
            attendance.gender === selectedMember.gender,
        ),
    );
  };

  const handleAddParticipant = async (participant) => {
    try {
      setIsParticipantSubmitting(true);
      const promotedGuest = isGuestPromotion(participant);
      await addManagedParticipant(publicId, participant);
      await loadMeeting();
      showNotice(
        'success',
        promotedGuest
          ? '기존 게스트 참가자를 클럽원으로 전환했어요.'
          : '참가자를 추가했습니다.',
      );
      return true;
    } catch (error) {
      showNotice(
        'error',
        error.response?.data?.message || '참가자를 추가하지 못했습니다.',
      );
      return false;
    } finally {
      setIsParticipantSubmitting(false);
    }
  };

  const handleCreateCompetition = async () => {
    if (sameGenderDoublesOnlyUnavailable) {
      showNotice('error', sameGenderDoublesOnlyUnavailableReason);
      return;
    }

    try {
      const response = clubPublicId
        ? await createClubMeetingCompetitionWithOptions(clubPublicId, publicId, {
            sameGenderDoublesOnly,
          })
        : await createMeetingCompetitionWithOptions(publicId, {
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
    if (clubPublicId) {
      await deleteClubMeetingCompetition(clubPublicId, publicId);
    } else {
      await deleteMeetingCompetition(publicId);
    }
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
          onEditMeeting={() =>
            navigate(
              clubPublicId
                ? `/clubs/${clubPublicId}/meetings/${publicId}/edit`
                : `/meetings/${publicId}/edit`,
            )
          }
        />

        <MeetingManageOperationsPanel
          meeting={meeting}
          onOpenCompetition={() => navigate(`/competitions/${meeting.competitionPublicId}`)}
          onCreateCompetition={handleCreateCompetition}
          onAskDeleteCompetition={() => setCompetitionDeleteRequested(true)}
          onChangeStatus={handleStatus}
          sameGenderDoublesOnly={sameGenderDoublesOnly}
          onSameGenderDoublesOnlyChange={setSameGenderDoublesOnly}
          sameGenderDoublesOnlyUnavailable={sameGenderDoublesOnlyUnavailable}
          sameGenderDoublesOnlyUnavailableReason={sameGenderDoublesOnlyUnavailableReason}
        />

        <MeetingOwnerAttendancePanel
          ownerName={ownerName}
          ownerStatus={ownerStatus}
          onStatusSelect={handleOwnerAttendance}
        />

        <button
          className="meeting-participant-add-trigger"
          type="button"
          onClick={() => setParticipantAddOpen(true)}
          disabled={meetingEditDisabled || meeting.status !== 'OPEN'}
        >
          참가자 추가
        </button>

        <MeetingRosterSections
          groupedAttendances={groupedAttendances}
          meeting={meeting}
          emptyMessage={null}
          onAskDelete={setAttendeeToDelete}
        />
      </div>

      {participantAddOpen && (
        <div
          className="meeting-participant-sheet-backdrop"
          role="presentation"
          onClick={() => !isParticipantSubmitting && setParticipantAddOpen(false)}
        >
          <MeetingParticipantAddPanel
            isClubMeeting={Boolean(clubPublicId)}
            members={selectableClubMembers}
            query={memberQuery}
            onQueryChange={setMemberQuery}
            onSubmit={async (participant) => {
              const added = await handleAddParticipant(participant);
              if (added) {
                setParticipantAddOpen(false);
              }
              return added;
            }}
            onClose={() => setParticipantAddOpen(false)}
            isSubmitting={isParticipantSubmitting}
            disabled={meetingEditDisabled || meeting.status !== 'OPEN'}
          />
        </div>
      )}

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
