import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ClubAccountDetailForm from '../../components/club/account-management/ClubAccountDetailForm';
import ClubDashboardLayout from '../../components/club/dashboard/ClubDashboardLayout';
import ClubAuthRequired from './ClubAuthRequired';
import { getClubAccount, removeClubAccount, updateClubAccountRole } from '../../utils/clubApi';
import { errorMessage, unwrapData } from './clubPageUtils';
import '../Club.css';

function ClubAccountDetailPage({ currentUser }) {
  const navigate = useNavigate();
  const { clubPublicId, clubAccountId } = useParams();
  const [account, setAccount] = useState(null);
  const [role, setRole] = useState('ADMIN');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isRemoving, setIsRemoving] = useState(false);

  useEffect(() => {
    if (!currentUser || !clubPublicId || !clubAccountId) return undefined;
    let cancelled = false;
    setIsLoading(true); setError('');
    getClubAccount(clubPublicId, clubAccountId).then((response) => { if (!cancelled) { const data = unwrapData(response, null); setAccount(data); setRole(data?.role ?? 'ADMIN'); } }).catch((requestError) => !cancelled && setError(errorMessage(requestError, '클럽 계정 정보를 불러오지 못했습니다.'))).finally(() => !cancelled && setIsLoading(false));
    return () => { cancelled = true; };
  }, [clubAccountId, clubPublicId, currentUser]);

  const backToList = () => navigate(`/clubs/${clubPublicId}/dashboard/accounts`);
  const saveRole = async (event) => { event.preventDefault(); setIsSaving(true); setError(''); try { const response = await updateClubAccountRole(clubPublicId, clubAccountId, role); setAccount(unwrapData(response, account)); } catch (requestError) { setError(errorMessage(requestError, '역할을 변경하지 못했습니다.')); } finally { setIsSaving(false); } };
  const remove = async () => { if (!account || !window.confirm(`${account.displayName}님을 클럽에서 탈퇴시킬까요?`)) return; setIsRemoving(true); setError(''); try { await removeClubAccount(clubPublicId, clubAccountId); backToList(); } catch (requestError) { setError(errorMessage(requestError, '클럽 계정을 제거하지 못했습니다.')); } finally { setIsRemoving(false); } };

  if (!currentUser) return <ClubAuthRequired />;
  return <ClubDashboardLayout clubPublicId={clubPublicId} currentUser={currentUser} activeMenu="accounts"><header className="club-dashboard-heading"><div><h1>클럽 계정 상세</h1><p>역할을 변경하거나 클럽 계정을 제거할 수 있습니다.</p></div></header>{error && <p className="club-notice danger">{error}</p>}{isLoading ? <p className="club-notice static">클럽 계정 정보를 불러오는 중입니다.</p> : account && <ClubAccountDetailForm account={account} role={role} isSaving={isSaving} isRemoving={isRemoving} onRoleChange={setRole} onSubmit={saveRole} onRemove={remove} onBack={backToList} />}</ClubDashboardLayout>;
}

export default ClubAccountDetailPage;
