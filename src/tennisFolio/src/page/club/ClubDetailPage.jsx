import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import ClubDetailView from '../../components/club/ClubDetailView';
import ClubMessage from '../../components/club/ClubMessage';
import ClubState from '../../components/club/ClubState';
import { sortClubMembers } from '../../components/club/clubUtils';
import { getClub, getClubMeetings, getClubMembers } from '../../utils/clubApi';
import ClubAuthRequired from './ClubAuthRequired';
import { errorMessage, normalizeClub, unwrapData } from './clubPageUtils';
import '../Club.css';

function ClubDetailPage({ currentUser }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { publicId } = useParams();
  const [activeTab, setActiveTab] = useState(location.state?.activeTab ?? 'meetings');
  const [selectedClub, setSelectedClub] = useState(null);
  const [members, setMembers] = useState([]);
  const [meetings, setMeetings] = useState([]);
  const [memberQuery, setMemberQuery] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isMembersLoading, setIsMembersLoading] = useState(false);
  const [isMeetingsLoading, setIsMeetingsLoading] = useState(false);

  const isAdmin = selectedClub?.admin === true || selectedClub?.role === 'ADMIN';
  const sortedMembers = useMemo(() => sortClubMembers(members), [members]);

  useEffect(() => {
    if (!currentUser || !publicId) {
      return;
    }

    let cancelled = false;
    setIsLoading(true);
    setError('');
    setSelectedClub(null);
    setMembers([]);
    setMeetings([]);

    getClub(publicId)
      .then((response) => {
        if (!cancelled) {
          setSelectedClub(normalizeClub(unwrapData(response, null)));
        }
      })
      .catch((requestError) => {
        if (!cancelled) {
          setError(errorMessage(requestError, '클럽 정보를 불러오지 못했습니다.'));
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
  }, [currentUser, publicId]);

  useEffect(() => {
    if (!currentUser || !publicId) {
      return undefined;
    }

    const timeoutId = window.setTimeout(() => {
      setIsMembersLoading(true);
      getClubMembers(publicId, memberQuery.trim())
        .then((response) => setMembers(unwrapData(response, [])))
        .catch((requestError) => {
          setError(errorMessage(requestError, '클럽원 목록을 불러오지 못했습니다.'));
        })
        .finally(() => setIsMembersLoading(false));
    }, 250);

    return () => window.clearTimeout(timeoutId);
  }, [currentUser, publicId, memberQuery]);

  useEffect(() => {
    if (!currentUser || !publicId || activeTab !== 'meetings') {
      return;
    }

    setIsMeetingsLoading(true);
    getClubMeetings(publicId)
      .then((response) => setMeetings(unwrapData(response, [])))
      .catch((requestError) => {
        setError(errorMessage(requestError, '모임 목록을 불러오지 못했습니다.'));
      })
      .finally(() => setIsMeetingsLoading(false));
  }, [activeTab, currentUser, publicId]);

  if (!currentUser) {
    return <ClubAuthRequired />;
  }

  return (
    <main className="club-mobile-page">
      {isLoading && <p className="club-notice static">클럽 정보를 불러오는 중입니다.</p>}
      {!isLoading && !selectedClub && (
        <ClubState eyebrow="CLUB" title="클럽을 찾을 수 없습니다">
          <ClubMessage error={error} />
          <button
            className="club-button full"
            type="button"
            onClick={() => navigate('/clubs')}
          >
            클럽 목록
          </button>
        </ClubState>
      )}
      {selectedClub && (
        <>
          <ClubMessage error={error} />
          <ClubDetailView
            club={selectedClub}
            isAdmin={isAdmin}
            activeTab={activeTab}
            members={members}
            sortedMembers={sortedMembers}
            meetings={meetings}
            memberQuery={memberQuery}
            isMembersLoading={isMembersLoading}
            isMeetingsLoading={isMeetingsLoading}
            onChangeTab={setActiveTab}
            onChangeMemberQuery={setMemberQuery}
            onEditClub={() => navigate(`/clubs/${selectedClub.publicId}/edit`)}
            onAddMeeting={() =>
              navigate(`/clubs/${selectedClub.publicId}/meetings/new`)
            }
            onOpenMeeting={(meeting) =>
              navigate(
                isAdmin
                  ? `/clubs/${selectedClub.publicId}/meetings/${meeting.publicId}`
                  : `/meetings/${meeting.publicId}`,
              )
            }
            onAddMember={() =>
              navigate(`/clubs/${selectedClub.publicId}/members/new`)
            }
            onEditMember={(member) =>
              navigate(`/clubs/${selectedClub.publicId}/members/${member.id}/edit`)
            }
            onBackToClubs={() => navigate('/clubs')}
          />
        </>
      )}
    </main>
  );
}

export default ClubDetailPage;
