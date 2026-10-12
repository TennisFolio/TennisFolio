import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPublicMeetings } from '../utils/meetingApi';
import MeetingListHeader from '../components/meeting/list/MeetingListHeader';
import MeetingListSection from '../components/meeting/list/MeetingListSection';
import MeetingListStates from '../components/meeting/list/MeetingListStates';
import './Meeting.css';

const INITIAL_VISIBLE_COUNT = 5;
const LOAD_MORE_COUNT = 5;

function Meetings() {
  const navigate = useNavigate();
  const [meetings, setMeetings] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [visibleCount, setVisibleCount] = useState(INITIAL_VISIBLE_COUNT);

  useEffect(() => {
    let cancelled = false;

    getPublicMeetings()
      .then((response) => {
        if (!cancelled) {
          setMeetings(response.data.data || []);
          setVisibleCount(INITIAL_VISIBLE_COUNT);
          setErrorMessage('');
        }
      })
      .catch(() => {
        if (!cancelled) {
          setErrorMessage('모임 목록을 불러오지 못했습니다.');
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

  const visibleMeetings = meetings.slice(0, visibleCount);
  const hasMoreMeetings = visibleCount < meetings.length;
  const showMeetingCount = !isLoading && !errorMessage;

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
          onLoadMore={() =>
            setVisibleCount((current) => current + LOAD_MORE_COUNT)
          }
          onOpen={(publicId) => navigate(`/meetings/${publicId}`)}
        />
      )}
    </main>
  );
}

export default Meetings;
