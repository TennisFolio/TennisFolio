import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ClubAccountSelfForm from '../../components/club/account-management/ClubAccountSelfForm';
import ClubDashboardLayout from '../../components/club/dashboard/ClubDashboardLayout';
import ClubAuthRequired from './ClubAuthRequired';
import { getMyClubAccount, leaveClub } from '../../utils/clubApi';
import { errorMessage, unwrapData } from './clubPageUtils';
import '../Club.css';

function ClubAccountPage({ currentUser }) {
  const navigate = useNavigate();
  const { clubPublicId } = useParams();
  const [account, setAccount] = useState(null);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isLeaving, setIsLeaving] = useState(false);

  useEffect(() => {
    if (!currentUser || !clubPublicId) return undefined;
    let cancelled = false;
    setIsLoading(true); setError('');
    getMyClubAccount(clubPublicId).then((response) => { if (!cancelled) setAccount(unwrapData(response, null)); }).catch((requestError) => !cancelled && setError(errorMessage(requestError, '클럽 계정 정보를 불러오지 못했습니다.'))).finally(() => !cancelled && setIsLoading(false));
    return () => { cancelled = true; };
  }, [clubPublicId, currentUser]);

  const leave = async () => { if (!window.confirm('이 클럽에서 탈퇴할까요?')) return; setIsLeaving(true); setError(''); try { await leaveClub(clubPublicId); navigate('/clubs'); } catch (requestError) { setError(errorMessage(requestError, '클럽에서 탈퇴하지 못했습니다.')); } finally { setIsLeaving(false); } };

  if (!currentUser) return <ClubAuthRequired />;
  return <ClubDashboardLayout clubPublicId={clubPublicId} currentUser={currentUser} activeMenu="account"><header className="club-dashboard-heading"><div><h1>클럽 계정</h1><p>Account 이름과 이 클럽의 소속 상태를 확인합니다.</p></div></header>{error && <p className="club-notice danger">{error}</p>}{isLoading ? <p className="club-notice static">클럽 계정 정보를 불러오는 중입니다.</p> : account && <ClubAccountSelfForm account={account} isLeaving={isLeaving} onLeave={leave} />}</ClubDashboardLayout>;
}

export default ClubAccountPage;
