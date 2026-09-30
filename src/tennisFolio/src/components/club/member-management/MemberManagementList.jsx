import { genderLabel, roleLabel } from '../clubUtils';

const statusLabels = {
  ACTIVE: '활동',
  SUSPENDED: '휴면',
  WITHDRAWN: '탈퇴',
};

function MemberManagementList({ members, allMembers, memberStatus, keyword, isLoading, totalMemberCount, pageSize, currentPage, totalPages, onStatusChange, onKeywordChange, onPageChange, onOpenMember }) {
  const counts = allMembers.reduce((result, member) => ({
    ...result,
    [member.memberStatus]: (result[member.memberStatus] ?? 0) + 1,
  }), { ACTIVE: 0, SUSPENDED: 0, WITHDRAWN: 0 });
  const filterOptions = [
    ['ALL', '전체', allMembers.length],
    ['ACTIVE', '활동', counts.ACTIVE],
    ['SUSPENDED', '휴면', counts.SUSPENDED],
    ['WITHDRAWN', '탈퇴', counts.WITHDRAWN],
  ];

  return (
    <>
      <section className="club-member-status-summary" aria-label="회원 상태 요약">
        {filterOptions.slice(1).map(([status, label, count]) => (
          <article className={`club-dashboard-card club-member-summary ${status.toLowerCase()}`} key={status}>
            <span className="club-dashboard-label">{label} 회원</span>
            <strong className="club-dashboard-number">{count}명</strong>
          </article>
        ))}
      </section>

      <section className="club-dashboard-card club-member-management-panel">
        <header className="club-dashboard-section-header">
          <h2>회원 목록</h2>
          <p>{memberStatus === 'ALL' ? '전체 회원' : `${statusLabels[memberStatus]} 회원`}을 표시합니다.</p>
        </header>
        <div className="club-member-management-filters">
          <input
            className="club-member-management-search"
            type="search"
            value={keyword}
            onChange={(event) => onKeywordChange(event.target.value)}
            placeholder="이름으로 검색"
          />
          {filterOptions.map(([status, label, count]) => (
            <button
              className={memberStatus === status ? 'active' : ''}
              key={status}
              type="button"
              onClick={() => onStatusChange(status)}
            >
              {label} {count}
            </button>
          ))}
        </div>
        <div className="club-dashboard-table-wrap">
          <table className="club-dashboard-table club-member-management-table">
            <thead><tr><th>이름</th><th>성별</th><th>티어</th><th>역할</th><th>상태</th><th aria-label="작업" /></tr></thead>
            <tbody>
              {isLoading && <tr><td colSpan="6">회원 목록을 불러오는 중입니다.</td></tr>}
              {!isLoading && members.length === 0 && <tr><td colSpan="6">표시할 회원이 없습니다.</td></tr>}
              {!isLoading && members.map((member) => (
                <tr key={member.id}>
                  <td>{member.name}</td>
                  <td><span className={`club-chip ${member.gender === 'FEMALE' ? 'female' : 'male'}`}>{genderLabel(member.gender)}</span></td>
                  <td>{member.skillTierName ?? '미정'}</td>
                  <td>{roleLabel(member.role)}</td>
                  <td><span className={`club-member-status ${member.memberStatus.toLowerCase()}`}>{statusLabels[member.memberStatus]}</span></td>
                  <td><button className="club-button small" type="button" onClick={() => onOpenMember(member.id)}>{member.memberStatus === 'WITHDRAWN' ? '복구' : '수정'}</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {totalPages > 1 && (
          <div className="club-dashboard-member-paging">
            <span>{totalMemberCount}명 중 {currentPage * pageSize + 1}~{Math.min((currentPage + 1) * pageSize, totalMemberCount)}명</span>
            <nav aria-label="회원 목록 페이지">
              {Array.from({ length: totalPages }, (_, index) => (
                <button
                  aria-current={currentPage === index ? 'page' : undefined}
                  className={currentPage === index ? 'active' : ''}
                  key={index}
                  type="button"
                  onClick={() => onPageChange(index)}
                >
                  {index + 1}
                </button>
              ))}
            </nav>
          </div>
        )}
      </section>
    </>
  );
}

export default MemberManagementList;
