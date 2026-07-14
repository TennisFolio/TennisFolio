import {
  formatMeetingDate,
  genderLabel,
  meetingStateLabel,
  roleLabel,
} from './clubUtils';

function ClubDetailView({
  club,
  isAdmin,
  activeTab,
  members,
  sortedMembers,
  meetings,
  memberQuery,
  isMembersLoading,
  isMeetingsLoading,
  onChangeTab,
  onChangeMemberQuery,
  onEditClub,
  onAddMeeting,
  onOpenMeeting,
  onAddMember,
  onEditMember,
  onBackToClubs,
}) {
  return (
    <>
      <header className="club-page-head club-detail-head">
        <h1>{club.name}</h1>
        <p className="club-description">{club.description}</p>
        <div className="club-chips">
          <span className="club-chip">클럽원 {club.memberCount}명</span>
        </div>
        {isAdmin && (
          <div className="club-page-actions">
            <button className="club-button full" type="button" onClick={onEditClub}>
              클럽 정보 수정
            </button>
          </div>
        )}
      </header>

      <div className="club-tabs">
        <button
          className={`club-button ${activeTab === 'meetings' ? 'primary' : ''}`}
          type="button"
          onClick={() => onChangeTab('meetings')}
        >
          모임
        </button>
        <button
          className={`club-button ${activeTab === 'members' ? 'primary' : ''}`}
          type="button"
          onClick={() => onChangeTab('members')}
        >
          클럽원
        </button>
      </div>

      {activeTab === 'meetings' && (
        <section className="club-section">
          <div className="club-section-title">
            <h2>모임</h2>
            {isAdmin && (
              <button
                className="club-button small accent"
                type="button"
                onClick={onAddMeeting}
              >
                모임 추가
              </button>
            )}
          </div>
          <div className="club-scroll-list club-meeting-list">
            {isMeetingsLoading && (
              <article className="club-row">
                <p className="club-row-sub">모임 목록을 불러오는 중입니다.</p>
              </article>
            )}
            {!isMeetingsLoading && meetings.length === 0 && (
              <article className="club-row">
                <p className="club-row-sub">아직 등록된 모임이 없습니다.</p>
              </article>
            )}
            {!isMeetingsLoading &&
              meetings.map((meeting) => (
                <article className="club-row" key={meeting.publicId}>
                  <div className="club-row-main">
                    <div>
                      <div className="club-row-title">{meeting.title}</div>
                      <p className="club-row-sub">
                        {formatMeetingDate(meeting.startAt)} · 참석{' '}
                        {meeting.attendingCount ?? 0} · {meetingStateLabel(meeting)}
                      </p>
                    </div>
                    <div className="club-row-actions">
                      <button
                        className="club-button small"
                        type="button"
                        onClick={() => onOpenMeeting(meeting)}
                      >
                        {isAdmin ? '관리' : '보기'}
                      </button>
                    </div>
                  </div>
                </article>
              ))}
          </div>
        </section>
      )}

      {activeTab === 'members' && (
        <section className="club-section">
          <div className="club-section-title">
            <h2>클럽원</h2>
            {isAdmin ? (
              <button
                className="club-button small accent"
                type="button"
                onClick={onAddMember}
              >
                클럽원 추가
              </button>
            ) : (
              <span>{members.length}명</span>
            )}
          </div>
          <input
            className="club-search"
            value={memberQuery}
            onChange={(event) => onChangeMemberQuery(event.target.value)}
            placeholder="이름, 실력, 메모 검색"
          />

          <div className="club-member-compact-list club-scroll-list">
            {isMembersLoading && (
              <div className="club-member-compact-row">
                <span className="club-member-compact-main">
                  <span className="club-member-compact-name">
                    클럽원 목록을 불러오는 중입니다.
                  </span>
                </span>
              </div>
            )}
            {!isMembersLoading && members.length === 0 && (
              <div className="club-member-compact-row">
                <span className="club-member-compact-main">
                  <span className="club-member-compact-name">
                    표시할 클럽원이 없습니다.
                  </span>
                </span>
              </div>
            )}
            {!isMembersLoading &&
              sortedMembers.map((member) => (
                <div className="club-member-compact-row" key={member.id}>
                  <span className="club-member-compact-main">
                    <span className="club-member-compact-name">{member.name}</span>
                    <span className="club-member-compact-sub">
                      {member.skillNote || member.memo || '-'}
                    </span>
                  </span>
                  <span
                    className={`club-chip ${
                      member.gender === 'FEMALE' ? 'female' : 'male'
                    }`}
                  >
                    {genderLabel(member.gender)}
                  </span>
                  <span
                    className={`club-chip ${member.role === 'ADMIN' ? 'admin' : ''}`}
                  >
                    {roleLabel(member.role)}
                  </span>
                  {isAdmin && (
                    <div className="club-row-actions">
                      <button
                        className="club-button small"
                        type="button"
                        onClick={() => onEditMember(member)}
                      >
                        수정
                      </button>
                    </div>
                  )}
                </div>
              ))}
          </div>
        </section>
      )}

      <button className="club-button full" type="button" onClick={onBackToClubs}>
        클럽 목록
      </button>
    </>
  );
}

export default ClubDetailView;
