const FILTERS = [
  { value: 'all', label: '전체' },
  { value: 'participated', label: '참여' },
  { value: 'not-participated', label: '미참여' },
];

function MemberParticipationPanel({ activeMemberCount, participation, memberFilter, onFilterChange, onPageChange }) {
  const members = participation?.members ?? {};
  const content = members.content ?? [];
  const participantCount = participation?.participantCount ?? 0;
  const totalPages = members.totalPages ?? 0;
  const page = members.page ?? 0;
  const filterCounts = {
    all: activeMemberCount ?? 0,
    participated: participantCount,
    'not-participated': Math.max((activeMemberCount ?? 0) - participantCount, 0),
  };
  const start = members.totalElements === 0 ? 0 : page * (members.size ?? 10) + 1;
  const end = Math.min((page + 1) * (members.size ?? 10), members.totalElements ?? 0);

  return (
    <article className="club-dashboard-card club-dashboard-member-panel">
      <header className="club-dashboard-section-header">
        <div>
          <h2>회원 참여 현황</h2>
          <p>선택한 달 모임에 한 번 이상 참석한 활성 회원 기준입니다.</p>
        </div>
      </header>
      <div className="club-dashboard-member-filters" aria-label="회원 참여 상태 필터">
        {FILTERS.map((filter) => (
          <button
            className={memberFilter === filter.value ? 'active' : ''}
            key={filter.value}
            type="button"
            onClick={() => onFilterChange(filter.value)}
          >
            {filter.label} {filterCounts[filter.value]}
          </button>
        ))}
      </div>
      {content.length === 0 ? (
        <p className="club-dashboard-empty">조건에 맞는 활성 회원이 없습니다.</p>
      ) : (
        <div className="club-dashboard-table-wrap">
          <table className="club-dashboard-table">
            <thead>
              <tr><th>회원</th><th>이번 달 참석</th><th>상태</th></tr>
            </thead>
            <tbody>
              {content.map((member) => {
                const attended = member.attendanceCount > 0;
                return (
                  <tr key={member.memberId}>
                    <td>{member.memberName}</td>
                    <td className={attended ? 'attendance' : ''}>{member.attendanceCount}회</td>
                    <td className={attended ? 'attendance' : 'not-attending'}>{attended ? '참여' : '미참여'}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
      {totalPages > 0 && (
        <div className="club-dashboard-member-paging">
          <span>{members.totalElements}명 중 {start}~{end}명</span>
          <nav aria-label="회원 목록 페이지">
            {Array.from({ length: totalPages }, (_, index) => (
              <button
                aria-current={page === index ? 'page' : undefined}
                className={page === index ? 'active' : ''}
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
    </article>
  );
}

export default MemberParticipationPanel;
