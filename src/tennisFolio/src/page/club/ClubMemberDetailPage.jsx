import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ClubDashboardLayout from '../../components/club/dashboard/ClubDashboardLayout';
import MemberDetailForm from '../../components/club/member-management/MemberDetailForm';
import ClubAuthRequired from './ClubAuthRequired';
import { addClubMember, getClub, getClubManagementMember, updateClubMember, updateClubMemberStatus } from '../../utils/clubApi';
import { emptyMemberForm, errorMessage, memberPayload, normalizeClub, unwrapData } from './clubPageUtils';
import '../Club.css';

function memberForm(member) {
  return { ...emptyMemberForm, ...member, skillTierId: member.skillTierId ?? null, joinedAt: member.joinedAt ?? '', contactMemo: member.contactMemo ?? '', memo: member.memo ?? '' };
}

function ClubMemberDetailPage({ currentUser }) {
  const navigate = useNavigate();
  const { clubPublicId, memberId } = useParams();
  const isNew = memberId === 'new';
  const [club, setClub] = useState(null);
  const [form, setForm] = useState(emptyMemberForm);
  const [originalStatus, setOriginalStatus] = useState('ACTIVE');
  const [originalStatusDate, setOriginalStatusDate] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (!currentUser || !clubPublicId) return undefined;
    let cancelled = false;
    setIsLoading(true);
    const requests = [getClub(clubPublicId)];
    if (!isNew) requests.push(getClubManagementMember(clubPublicId, memberId));
    Promise.all(requests)
      .then(([clubResponse, memberResponse]) => {
        if (cancelled) return;
        setClub(normalizeClub(unwrapData(clubResponse, null)));
        if (memberResponse) {
          const member = unwrapData(memberResponse, null);
          setForm(memberForm(member));
          setOriginalStatus(member.memberStatus);
          setOriginalStatusDate(member.memberStatus === 'SUSPENDED' ? member.suspendedAt ?? '' : member.leftAt ?? '');
        }
      })
      .catch((requestError) => !cancelled && setError(errorMessage(requestError, '회원 정보를 불러오지 못했습니다.')))
      .finally(() => !cancelled && setIsLoading(false));
    return () => { cancelled = true; };
  }, [clubPublicId, currentUser, isNew, memberId]);

  const backToList = () => navigate(`/clubs/${clubPublicId}/dashboard/members`);
  const saveMember = async (event) => {
    event.preventDefault();
    const name = form.name.trim();
    if (!name) return;
    setIsSaving(true); setError('');
    try {
      if (isNew) {
        await addClubMember(clubPublicId, memberPayload(form, name));
      } else {
        await updateClubMember(clubPublicId, memberId, memberPayload(form, name));
        const statusDate = form.memberStatus === 'SUSPENDED' ? form.suspendedAt : form.leftAt;
        if (form.memberStatus !== originalStatus || (form.memberStatus !== 'ACTIVE' && statusDate !== originalStatusDate)) {
          await updateClubMemberStatus(clubPublicId, memberId, form.memberStatus, statusDate);
        }
      }
      backToList();
    } catch (requestError) {
      setError(errorMessage(requestError, '회원 정보를 저장하지 못했습니다.'));
    } finally { setIsSaving(false); }
  };

  if (!currentUser) return <ClubAuthRequired />;
  return <ClubDashboardLayout clubPublicId={clubPublicId} currentUser={currentUser} activeMenu="members">
    <header className="club-dashboard-heading"><div><h1>{isNew ? '회원 추가' : '회원 상세'}</h1><p>회원의 기본 정보와 활동 상태를 관리합니다.</p></div></header>
    {error && <p className="club-notice danger">{error}</p>}
    {isLoading ? <p className="club-notice static">회원 정보를 불러오는 중입니다.</p> : <MemberDetailForm form={form} skillTiers={club?.skillTiers ?? []} isNew={isNew} isSaving={isSaving} onChange={setForm} onSubmit={saveMember} onBack={backToList} />}
  </ClubDashboardLayout>;
}

export default ClubMemberDetailPage;
