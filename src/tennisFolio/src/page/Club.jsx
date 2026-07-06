import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { default_oauth_provider } from '@/constants';
import { loginWithProvider } from '../utils/authApi';
import './Club.css';

const clubsSeed = [
  {
    publicId: 'seocho-tennis-crew',
    name: '서초 테니스 크루',
    description: '퇴근 후 복식 위주로 가볍게 치는 클럽입니다.',
    role: 'ADMIN',
    memberCount: 24,
  },
  {
    publicId: 'hangang-weekend-doubles',
    name: '한강 주말 복식',
    description: '주말 오전 복식 중심 클럽입니다.',
    role: 'MEMBER',
    memberCount: 31,
  },
];

const meetingsSeed = [
  {
    id: 1,
    title: '토요 복식 정모',
    summary: '7월 11일 · 참석 12 · 경기 대기',
    primaryAction: '관리',
  },
  {
    id: 2,
    title: '평일 야간 랠리',
    summary: '7월 14일 · 참석 8 · 마감',
    primaryAction: '관리',
  },
  {
    id: 3,
    title: '7월 첫째 주 정모',
    summary: '7월 4일 · 참석 16 · 경기 생성',
    primaryAction: '보기',
  },
  {
    id: 4,
    title: '6월 마지막 정모',
    summary: '6월 27일 · 참석 14 · 완료',
    primaryAction: '보기',
  },
];

const membersSeed = [
  { id: 1, name: '김서준', gender: 'MALE', skillNote: '중상급', role: 'ADMIN' },
  { id: 2, name: '박하리', gender: 'FEMALE', skillNote: 'A조', role: 'ADMIN' },
  { id: 3, name: '이도윤', gender: 'MALE', skillNote: '중급', role: 'MEMBER' },
  { id: 4, name: '정민아', gender: 'FEMALE', skillNote: '중상급', role: 'MEMBER' },
  { id: 5, name: '최지훈', gender: 'MALE', skillNote: '초중급', role: 'MEMBER' },
];

const emptyClubForm = {
  name: '',
  description: '',
};

function createPublicId(name) {
  const fallback = Date.now().toString(36);
  return `club-${name.trim().replace(/\s+/g, '-').toLowerCase() || fallback}`;
}

function roleLabel(role) {
  return role === 'ADMIN' ? '관리자' : '클럽원';
}

function genderLabel(gender) {
  return gender === 'FEMALE' ? '여' : '남';
}

function Club({ currentUser }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { publicId } = useParams();
  const [view, setView] = useState('list');
  const [activeTab, setActiveTab] = useState('meetings');
  const [clubs, setClubs] = useState(clubsSeed);
  const [members, setMembers] = useState(membersSeed);
  const [selectedClubId, setSelectedClubId] = useState(clubsSeed[0].publicId);
  const [clubForm, setClubForm] = useState({
    name: '서초 테니스 크루',
    description: '퇴근 후 복식 위주로 가볍게 치는 클럽입니다.',
  });
  const [memberQuery, setMemberQuery] = useState('');
  const [notice, setNotice] = useState('');

  useEffect(() => {
    if (location.pathname === '/clubs/new') {
      setView('new');
      return;
    }

    if (publicId) {
      setSelectedClubId(publicId);
      setView('detail');
      return;
    }

    setView('list');
  }, [location.pathname, publicId]);

  const selectedClub = useMemo(
    () => clubs.find((club) => club.publicId === selectedClubId) || clubs[0],
    [clubs, selectedClubId],
  );

  const filteredMembers = useMemo(() => {
    const keyword = memberQuery.trim().toLowerCase();
    if (!keyword) {
      return members;
    }

    return members.filter((member) =>
      `${member.name} ${member.skillNote}`.toLowerCase().includes(keyword),
    );
  }, [members, memberQuery]);

  const showNotice = (message) => {
    setNotice(message);
    window.setTimeout(() => setNotice(''), 2200);
  };

  const openClub = (club, tab = 'meetings') => {
    setSelectedClubId(club.publicId);
    setActiveTab(tab);
    navigate(`/clubs/${club.publicId}`);
  };

  const handleCreateClub = (event) => {
    event.preventDefault();
    const name = clubForm.name.trim();

    if (!name) {
      showNotice('클럽명을 입력해 주세요.');
      return;
    }

    const nextClub = {
      publicId: createPublicId(name),
      name,
      description: clubForm.description.trim(),
      role: 'ADMIN',
      memberCount: 1,
    };

    setClubs((current) => [nextClub, ...current]);
    setSelectedClubId(nextClub.publicId);
    setActiveTab('members');
    navigate(`/clubs/${nextClub.publicId}`);
    showNotice('클럽을 만들었습니다.');
  };

  const handleRemoveMember = (member) => {
    if (member.role === 'ADMIN') {
      showNotice('관리자는 이 화면에서 바로 삭제할 수 없습니다.');
      return;
    }

    setMembers((current) => current.filter((item) => item.id !== member.id));
    showNotice('클럽원 목록에서 삭제했습니다.');
  };

  if (!currentUser) {
    return (
      <main className="club-mobile-page">
        <section className="club-state">
          <span className="club-eyebrow">CLUB</span>
          <h1>로그인이 필요합니다.</h1>
          <p>클럽 목록과 클럽원 관리는 로그인한 사용자만 사용할 수 있습니다.</p>
          <button
            type="button"
            className="club-button primary full"
            onClick={() => loginWithProvider(default_oauth_provider)}
          >
            로그인하기
          </button>
        </section>
      </main>
    );
  }

  if (view === 'new') {
    return (
      <main className="club-mobile-page">
        <header className="club-page-head">
          <span className="club-eyebrow">NEW CLUB</span>
          <h1>새 클럽</h1>
          <p>클럽을 만들면 생성자는 자동으로 관리자가 됩니다.</p>
        </header>

        {notice && <p className="club-notice">{notice}</p>}

        <form className="club-stack" onSubmit={handleCreateClub}>
          <label className="club-field">
            <span>클럽명</span>
            <input
              value={clubForm.name}
              onChange={(event) =>
                setClubForm((current) => ({
                  ...current,
                  name: event.target.value,
                }))
              }
            />
          </label>
          <label className="club-field">
            <span>소개</span>
            <textarea
              value={clubForm.description}
              onChange={(event) =>
                setClubForm((current) => ({
                  ...current,
                  description: event.target.value,
                }))
              }
            />
          </label>
          <div className="club-notice static">
            공지사항, 회비 정책, 모임 기본 설정은 이번 범위에서 입력하지 않습니다. 추후 각각 별도 기능과 테이블로 확장합니다.
          </div>
          <button className="club-button primary full" type="submit">
            클럽 만들기
          </button>
          <button
            className="club-button full"
            type="button"
            onClick={() => {
              setClubForm(emptyClubForm);
              navigate('/clubs');
            }}
          >
            목록으로
          </button>
        </form>
      </main>
    );
  }

  if (view === 'detail' && selectedClub) {
    const isAdmin = selectedClub.role === 'ADMIN';

    return (
      <main className="club-mobile-page">
        <header className="club-page-head">
          <span className="club-eyebrow">{roleLabel(selectedClub.role)}</span>
          <h1>{selectedClub.name}</h1>
          <p>{selectedClub.description}</p>
          <div className="club-chips">
            <span className="club-chip">활성 클럽원 {selectedClub.memberCount}명</span>
          </div>
          {isAdmin && (
            <div className="club-page-actions">
              <button className="club-button full" type="button">
                클럽 정보 수정
              </button>
            </div>
          )}
        </header>

        {notice && <p className="club-notice">{notice}</p>}

        <div className="club-tabs">
          <button
            className={`club-button ${activeTab === 'meetings' ? 'primary' : ''}`}
            type="button"
            onClick={() => setActiveTab('meetings')}
          >
            모임
          </button>
          <button
            className={`club-button ${activeTab === 'members' ? 'primary' : ''}`}
            type="button"
            onClick={() => setActiveTab('members')}
          >
            클럽원
          </button>
        </div>

        {activeTab === 'meetings' && (
          <section className="club-section">
            <div className="club-section-title">
              <h2>모임</h2>
              {isAdmin && (
                <button className="club-button small accent" type="button">
                  모임 추가
                </button>
              )}
            </div>

            {meetingsSeed.map((meeting) => (
              <article className="club-row" key={meeting.id}>
                <div className="club-row-main">
                  <div>
                    <div className="club-row-title">{meeting.title}</div>
                    <p className="club-row-sub">{meeting.summary}</p>
                  </div>
                  <div className="club-row-actions">
                    <button className="club-button small" type="button">
                      {meeting.primaryAction}
                    </button>
                    {isAdmin && (
                      <button className="club-button small danger" type="button">
                        삭제
                      </button>
                    )}
                  </div>
                </div>
              </article>
            ))}
          </section>
        )}

        {activeTab === 'members' && (
          <section className="club-section">
            <div className="club-section-title">
              <h2>클럽원</h2>
              {isAdmin && (
                <button className="club-button small accent" type="button">
                  클럽원 추가
                </button>
              )}
            </div>
            <input
              className="club-search"
              value={memberQuery}
              onChange={(event) => setMemberQuery(event.target.value)}
              placeholder="이름, 실력, 메모 검색"
            />

            <div className="club-member-compact-list">
              {filteredMembers.map((member) => (
                <div className="club-member-compact-row" key={member.id}>
                  <span className="club-member-compact-main">
                    <span className="club-member-compact-name">{member.name}</span>
                    <span className="club-member-compact-sub">{member.skillNote}</span>
                  </span>
                  <span
                    className={`club-chip ${
                      member.gender === 'FEMALE' ? 'female' : 'male'
                    }`}
                  >
                    {genderLabel(member.gender)}
                  </span>
                  <span className={`club-chip ${member.role === 'ADMIN' ? 'admin' : ''}`}>
                    {roleLabel(member.role)}
                  </span>
                  {isAdmin && (
                    <button
                      className="club-button small danger"
                      type="button"
                      onClick={() => handleRemoveMember(member)}
                    >
                      삭제
                    </button>
                  )}
                </div>
              ))}
            </div>
          </section>
        )}

        <button
          className="club-button full"
          type="button"
          onClick={() => navigate('/clubs')}
        >
          클럽 목록
        </button>
      </main>
    );
  }

  return (
    <main className="club-mobile-page">
      <header className="club-page-head">
        <span className="club-eyebrow">CLUB</span>
        <h1>내 클럽</h1>
        <p>소속 클럽을 보고, 운영 중인 클럽에서 모임과 클럽원을 관리합니다.</p>
        <button
          className="club-button primary full"
          type="button"
          onClick={() => navigate('/clubs/new')}
        >
          클럽 만들기
        </button>
      </header>

      {notice && <p className="club-notice">{notice}</p>}

      <section className="club-section">
        <div className="club-section-title">
          <h2>클럽 목록</h2>
          <span>{clubs.length}개</span>
        </div>

        {clubs.map((club) => (
          <article className="club-card" key={club.publicId}>
            <div className="club-card-head">
              <div>
                <div className="club-card-title">{club.name}</div>
                <p className="club-card-copy">{club.description}</p>
              </div>
              <span className={`club-chip ${club.role === 'ADMIN' ? 'admin' : ''}`}>
                {roleLabel(club.role)}
              </span>
            </div>
            <div className="club-chips">
              <span className="club-chip">{club.memberCount}명</span>
            </div>
            <button
              className={`club-button full ${club.role === 'ADMIN' ? 'accent' : ''}`}
              type="button"
              onClick={() => openClub(club)}
            >
              {club.role === 'ADMIN' ? '관리' : '열기'}
            </button>
          </article>
        ))}
      </section>
    </main>
  );
}

export default Club;
