import { genderLabel } from './clubUtils';
import MeetingListSection from '../meeting/list/MeetingListSection';

function formatMeetingMonth(monthValue) {
  const [year, month] = monthValue.split('-').map(Number);
  return `${year}년 ${month}월`;
}

function ClubDetailView({
  club,
  isAdmin,
  activeTab,
  members,
  sortedMembers,
  visibleMeetings,
  selectedMeetingMonth,
  memberQuery,
  isMembersLoading,
  isMeetingsLoading,
  onChangeTab,
  onPreviousMeetingMonth,
  onNextMeetingMonth,
  onChangeMemberQuery,
  onEditClub,
  onOpenDashboard,
  onAddMeeting,
  onOpenMeeting,
  onOpenMemberManagement,
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
            <button className="club-button full accent" type="button" onClick={onOpenDashboard}>
              대시보드
            </button>
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
            <div className="club-meeting-actions">
              <div className="club-dashboard-month-nav" aria-label="모임 월 이동">
                <button type="button" aria-label="이전 달" onClick={onPreviousMeetingMonth}>
                  ‹
                </button>
                <strong>{formatMeetingMonth(selectedMeetingMonth)}</strong>
                <button type="button" aria-label="다음 달" onClick={onNextMeetingMonth}>
                  ›
                </button>
              </div>
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
          </div>
          {isMeetingsLoading && (
            <div className="club-scroll-list club-meeting-list">
              <article className="club-row">
                <p className="club-row-sub">모임 목록을 불러오는 중입니다.</p>
              </article>
            </div>
          )}
          {!isMeetingsLoading && visibleMeetings.length === 0 && (
            <div className="club-scroll-list club-meeting-list">
              <article className="club-row">
                <p className="club-row-sub">
                  {formatMeetingMonth(selectedMeetingMonth)}에 등록된 모임이 없습니다.
                </p>
              </article>
            </div>
          )}
          {!isMeetingsLoading && visibleMeetings.length > 0 && (
            <MeetingListSection
              meetings={visibleMeetings}
              hasMoreMeetings={false}
              onLoadMore={() => {}}
              onOpen={(meetingPublicId) => {
                const meeting = visibleMeetings.find(({ publicId }) => publicId === meetingPublicId);
                if (meeting) {
                  onOpenMeeting(meeting);
                }
              }}
            />
          )}
        </section>
      )}

      {activeTab === 'members' && (
        <section className="club-section">
          <div className="club-section-title">
            <h2>클럽원</h2>
            {isAdmin ? (
              <button className="club-button small accent" type="button" onClick={onOpenMemberManagement}>
                회원 관리
              </button>
            ) : (
              <span>{members.length}명</span>
            )}
          </div>
          <input
            className="club-search"
            value={memberQuery}
            onChange={(event) => onChangeMemberQuery(event.target.value)}
            placeholder="이름 검색"
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
                      {member.skillTierName ?? '미정'}
                    </span>
                  </span>
                  <span
                    className={`club-chip ${
                      member.gender === 'FEMALE' ? 'female' : 'male'
                    }`}
                  >
                    {genderLabel(member.gender)}
                  </span>
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
