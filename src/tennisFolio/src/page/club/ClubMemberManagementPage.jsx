import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ClubDashboardLayout from '../../components/club/dashboard/ClubDashboardLayout';
import MemberManagementList from '../../components/club/member-management/MemberManagementList';
import ClubAuthRequired from './ClubAuthRequired';
import { getClubManagementMembers } from '../../utils/clubApi';
import { errorMessage, unwrapData } from './clubPageUtils';
import '../Club.css';

const MEMBER_PAGE_SIZE = 10;

function ClubMemberManagementPage({ currentUser }) {
  const navigate = useNavigate();
  const { clubPublicId } = useParams();
  const [allMembers, setAllMembers] = useState([]);
  const [memberStatus, setMemberStatus] = useState('ALL');
  const [keyword, setKeyword] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!currentUser || !clubPublicId) return undefined;
    setIsLoading(true);
    setError('');
    getClubManagementMembers(clubPublicId)
      .then((response) => setAllMembers(unwrapData(response, [])))
      .catch((requestError) => setError(errorMessage(requestError, '회원 목록을 불러오지 못했습니다.')))
      .finally(() => setIsLoading(false));
  }, [clubPublicId, currentUser]);

  const members = useMemo(() => allMembers.filter((member) => {
    const matchesStatus = memberStatus === 'ALL' || member.memberStatus === memberStatus;
    return matchesStatus && member.name.toLocaleLowerCase().includes(keyword.trim().toLocaleLowerCase());
  }), [allMembers, keyword, memberStatus]);
  const totalPages = Math.ceil(members.length / MEMBER_PAGE_SIZE);
  const pagedMembers = useMemo(
    () => members.slice(currentPage * MEMBER_PAGE_SIZE, (currentPage + 1) * MEMBER_PAGE_SIZE),
    [currentPage, members],
  );

  useEffect(() => {
    setCurrentPage(0);
  }, [keyword, memberStatus]);

  if (!currentUser) return <ClubAuthRequired />;

  return (
    <ClubDashboardLayout clubPublicId={clubPublicId} currentUser={currentUser} activeMenu="members">
      <header className="club-dashboard-heading club-member-management-heading">
        <div><h1>회원 관리</h1><p>회원 정보를 관리하고 활동 상태를 변경합니다.</p></div>
        <div className="club-member-management-actions">
          <button className="club-button" type="button" onClick={() => navigate(`/clubs/${clubPublicId}/dashboard/members/paste`)}>엑셀 붙여넣기 등록</button>
          <button className="club-button primary" type="button" onClick={() => navigate(`/clubs/${clubPublicId}/dashboard/members/new`)}>회원 추가</button>
        </div>
      </header>
      {error && <p className="club-notice danger">{error}</p>}
      <MemberManagementList
        members={pagedMembers} allMembers={allMembers} memberStatus={memberStatus} keyword={keyword} isLoading={isLoading}
        totalMemberCount={members.length} pageSize={MEMBER_PAGE_SIZE} currentPage={currentPage} totalPages={totalPages}
        onStatusChange={setMemberStatus} onKeywordChange={setKeyword} onPageChange={setCurrentPage}
        onOpenMember={(memberId) => navigate(`/clubs/${clubPublicId}/dashboard/members/${memberId}`)}
      />
    </ClubDashboardLayout>
  );
}

export default ClubMemberManagementPage;
