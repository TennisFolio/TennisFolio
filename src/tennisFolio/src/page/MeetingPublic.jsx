import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { getPublicMeeting } from '../utils/meetingApi';
import MeetingPublicOverviewPanel from '../components/meeting/public/MeetingPublicOverviewPanel';
import MeetingRosterSections from '../components/meeting/shared/MeetingRosterSections';
import {
  groupAttendances,
  normalizeAttendances,
} from '../components/meeting/shared/meetingAttendanceUtils';
import './Meeting.css';
import MeetingManage from './MeetingManage';
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

  const attendances = useMemo(() => normalizeAttendances(meeting), [meeting]);
  const groupedAttendances = useMemo(
    () => groupAttendances(attendances),
    [attendances],
  );

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

  return (
    <main className="meeting-page">
      <MeetingPublicOverviewPanel
        meeting={meeting}
        attendances={attendances}
        onCopyShareLink={handleCopyShareLink}
        onOpenCompetition={() => navigate(`/competitions/${meeting.competitionPublicId}`)}
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
