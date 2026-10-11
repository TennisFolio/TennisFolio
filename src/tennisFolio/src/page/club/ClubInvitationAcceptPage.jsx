import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { default_oauth_provider } from '../../constants/urls';
import ClubInvitationAcceptView from '../../components/club/account-management/ClubInvitationAcceptView';
import ClubState from '../../components/club/ClubState';
import { acceptClubAccountInvitation, previewClubAccountInvitation } from '../../utils/clubApi';
import { loginWithProvider } from '../../utils/authApi';
import { errorMessage, unwrapData } from './clubPageUtils';
import '../Club.css';

function ClubInvitationAcceptPage({ currentUser }) {
  const navigate = useNavigate();
  const { token } = useParams();
  const [invitation, setInvitation] = useState(null);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!token) return undefined;
    let cancelled = false;
    setIsLoading(true); setError('');
    previewClubAccountInvitation(token).then((response) => { if (!cancelled) setInvitation(unwrapData(response, null)); }).catch((requestError) => !cancelled && setError(errorMessage(requestError, '초대 링크를 확인하지 못했습니다.'))).finally(() => !cancelled && setIsLoading(false));
    return () => { cancelled = true; };
  }, [token]);

  const login = () => {
    const returnUrl = `${window.location.pathname}${window.location.search}`;
    sessionStorage.setItem('tennisfolio:postLoginRedirect', returnUrl);
    localStorage.setItem('tennisfolio:postLoginRedirect', returnUrl);
    loginWithProvider(default_oauth_provider);
  };
  const accept = async (event) => { event.preventDefault(); setIsSubmitting(true); setError(''); try { await acceptClubAccountInvitation(token); navigate(`/clubs/${invitation.clubPublicId}/dashboard`, { replace: true }); } catch (requestError) { setError(errorMessage(requestError, '클럽 참여를 완료하지 못했습니다.')); } finally { setIsSubmitting(false); } };

  if (isLoading) return <main className="club-invitation-page"><p className="club-notice static">초대 링크를 확인하는 중입니다.</p></main>;
  if (error || !invitation) return <main className="club-invitation-page"><ClubState eyebrow="CLUB INVITATION" title="초대 링크를 사용할 수 없습니다" description={error || '유효하지 않거나 만료된 초대 링크입니다.'} /></main>;
  if (!currentUser) return <main className="club-invitation-page"><ClubState eyebrow="CLUB INVITATION" title="로그인이 필요합니다" description="로그인 후 현재 초대 링크로 돌아와 ADMIN 참여를 완료합니다."><button className="club-button primary full" type="button" onClick={login}>로그인하기</button></ClubState></main>;
  return <ClubInvitationAcceptView invitation={invitation} isSubmitting={isSubmitting} onSubmit={accept} />;
}

export default ClubInvitationAcceptPage;
