import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { default_oauth_provider } from '@/constants';
import { loginWithProvider } from '../utils/authApi';
import {
  deleteMeeting,
  getMyMeetings,
  isAuthenticationRequiredError,
} from '../utils/meetingApi';
import MeetingAuthRequired from '../components/meeting/list/MeetingAuthRequired';
import MeetingListHeader from '../components/meeting/list/MeetingListHeader';
import MeetingListSection from '../components/meeting/list/MeetingListSection';
import MeetingListStates from '../components/meeting/list/MeetingListStates';
import './Meeting.css';
import MeetingToast from './MeetingToast';

const INITIAL_VISIBLE_COUNT = 5;
const LOAD_MORE_COUNT = 5;

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
  const [authRequired, setAuthRequired] = useState(false);
  const [visibleCount, setVisibleCount] = useState(INITIAL_VISIBLE_COUNT);

  useEffect(() => {
    let cancelled = false;

    getMyMeetings()
      .then((response) => {
        if (!cancelled) {
          setMeetings(response.data.data || []);
          setVisibleCount(INITIAL_VISIBLE_COUNT);
          setErrorMessage('');
          setAuthRequired(false);
        }
      })
      .catch((error) => {
        if (!cancelled) {
          const requiresLogin = isAuthenticationRequiredError(error);
          setAuthRequired(requiresLogin);
          setErrorMessage(
            requiresLogin ? '' : '모임 관리 목록을 불러오지 못했습니다.'
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
      current.filter((item) => item.publicId !== meeting.publicId)
    );
    showNotice('success', '모임을 삭제했습니다.');
  };

  const visibleMeetings = meetings.slice(0, visibleCount);
  const hasMoreMeetings = visibleCount < meetings.length;
  const showMeetingCount = !isLoading && !errorMessage && !authRequired;

  if (!isLoading && authRequired) {
    return (
      <MeetingAuthRequired
        onLogin={() => loginWithProvider(default_oauth_provider)}
      />
    );
  }

  return (
    <main className="meeting-page">
      <MeetingListHeader
        count={meetings.length}
        showCount={showMeetingCount}
        onCreate={() => navigate('/meetings/new')}
      />

      <MeetingListStates
        errorMessage={errorMessage}
        isEmpty={!errorMessage && meetings.length === 0}
        isLoading={isLoading}
      />

      {!isLoading && !errorMessage && meetings.length > 0 && (
        <MeetingListSection
          meetings={visibleMeetings}
          hasMoreMeetings={hasMoreMeetings}
          onDelete={handleDelete}
          onLoadMore={() =>
            setVisibleCount((current) => current + LOAD_MORE_COUNT)
          }
          onManage={(publicId) => navigate(`/meetings/${publicId}`)}
          onShare={handleShare}
        />
      )}
      <MeetingToast notice={notice} onClose={() => setNotice(null)} />
    </main>
  );
}

export default Meetings;
