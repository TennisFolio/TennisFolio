import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ClubAccountManagementView from '../../components/club/account-management/ClubAccountManagementView';
import ClubDashboardLayout from '../../components/club/dashboard/ClubDashboardLayout';
import ClubState from '../../components/club/ClubState';
import ClubAuthRequired from './ClubAuthRequired';
import { closeClubAccountInvitation, getClubAccountInvitation, getClubAccounts, issueClubAccountInvitation } from '../../utils/clubApi';
import { errorMessage, unwrapData } from './clubPageUtils';
import '../Club.css';

function ClubAccountManagementPage({ currentUser }) {
  const navigate = useNavigate();
  const { clubPublicId } = useParams();
  const [accounts, setAccounts] = useState([]);
  const [invitation, setInvitation] = useState(null);
  const [invitationLink, setInvitationLink] = useState('');
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isIssuing, setIsIssuing] = useState(false);
  const invitationStorageKey = `tennisfolio:clubAccountInvitation:${clubPublicId}`;

  const load = async () => {
    const [accountsResponse, invitationResponse] = await Promise.all([
      getClubAccounts(clubPublicId),
      getClubAccountInvitation(clubPublicId).catch((requestError) => {
        if (requestError?.response?.status === 404) return null;
        throw requestError;
      }),
    ]);
    setAccounts(unwrapData(accountsResponse, []));
    setInvitation(invitationResponse ? unwrapData(invitationResponse, null) : { active: false, expiresAt: null });
    setInvitationLink(sessionStorage.getItem(invitationStorageKey) ?? '');
  };

  useEffect(() => {
    if (!currentUser || !clubPublicId) return undefined;
    let cancelled = false;
    setIsLoading(true); setError('');
    load().catch((requestError) => !cancelled && setError(errorMessage(requestError, '클럽 계정 정보를 불러오지 못했습니다.'))).finally(() => !cancelled && setIsLoading(false));
    return () => { cancelled = true; };
  }, [clubPublicId, currentUser]);

  const issueInvitation = async () => {
    setIsIssuing(true); setError(''); setFeedback('');
    try {
      const issued = unwrapData(await issueClubAccountInvitation(clubPublicId), null);
      setInvitation({ active: true, expiresAt: issued.expiresAt });
      const link = `${window.location.origin}/club-account-invitations/${issued.token}`;
      sessionStorage.setItem(invitationStorageKey, link);
      setInvitationLink(link);
      setFeedback('새 ADMIN 초대 링크를 만들었습니다.');
    } catch (requestError) { setError(errorMessage(requestError, '초대 링크를 만들지 못했습니다.')); } finally { setIsIssuing(false); }
  };

  const copyInvitation = async (link) => {
    if (!link) { setFeedback('현재 브라우저에서는 새 링크를 발급한 뒤 복사할 수 있습니다.'); return; }
    try { await navigator.clipboard.writeText(link); setFeedback('초대 링크를 복사했습니다.'); } catch { setFeedback(`초대 링크를 복사하지 못했습니다. ${link}`); }
  };

  const closeInvitation = async () => {
    if (!window.confirm('활성 초대 링크를 종료할까요?')) return;
    setError(''); setFeedback('');
    try { await closeClubAccountInvitation(clubPublicId); sessionStorage.removeItem(invitationStorageKey); setInvitation({ active: false, expiresAt: null }); setInvitationLink(''); setFeedback('초대 링크를 종료했습니다.'); } catch (requestError) { setError(errorMessage(requestError, '초대 링크를 종료하지 못했습니다.')); }
  };

  if (!currentUser) return <ClubAuthRequired />;
  return <ClubDashboardLayout clubPublicId={clubPublicId} currentUser={currentUser} activeMenu="accounts">
    {isLoading ? <p className="club-notice static">클럽 계정 정보를 불러오는 중입니다.</p> : error ? <ClubState eyebrow="CLUB ACCOUNTS" title="클럽 계정을 관리할 수 없습니다" description={error} /> : <ClubAccountManagementView accounts={accounts} invitation={invitation} invitationLink={invitationLink} feedback={feedback} isIssuing={isIssuing} onIssue={issueInvitation} onCopy={copyInvitation} onClose={closeInvitation} onOpenAccount={(clubAccountId) => navigate(`/clubs/${clubPublicId}/dashboard/accounts/${clubAccountId}`)} />}
  </ClubDashboardLayout>;
}

export default ClubAccountManagementPage;
