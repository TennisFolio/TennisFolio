import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ClubListView from '../../components/club/ClubListView';
import ClubMessage from '../../components/club/ClubMessage';
import { getMyClubs } from '../../utils/clubApi';
import ClubAuthRequired from './ClubAuthRequired';
import { errorMessage, normalizeClub, unwrapData } from './clubPageUtils';
import '../Club.css';

function ClubListPage({ currentUser }) {
  const navigate = useNavigate();
  const [clubs, setClubs] = useState([]);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!currentUser) {
      return;
    }

    let cancelled = false;
    setIsLoading(true);
    setError('');

    getMyClubs()
      .then((response) => {
        if (!cancelled) {
          setClubs(unwrapData(response, []).map(normalizeClub).filter(Boolean));
        }
      })
      .catch((requestError) => {
        if (!cancelled) {
          setError(errorMessage(requestError, '클럽 목록을 불러오지 못했습니다.'));
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
  }, [currentUser]);

  if (!currentUser) {
    return <ClubAuthRequired />;
  }

  return (
    <main className="club-mobile-page">
      <ClubMessage error={error} />
      <ClubListView
        clubs={clubs}
        isLoading={isLoading}
        onCreateClub={() => navigate('/clubs/new')}
        onOpenClub={(club) => navigate(`/clubs/${club.publicId}`)}
      />
    </main>
  );
}

export default ClubListPage;
