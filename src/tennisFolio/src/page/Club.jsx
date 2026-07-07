import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { default_oauth_provider } from '@/constants';
import { loginWithProvider } from '../utils/authApi';
import {
  addClubMember,
  createClub,
  deleteClub,
  deleteClubMember,
  getClub,
  getClubMembers,
  getMyClubs,
  updateClub,
  updateClubMember,
} from '../utils/clubApi';
import './Club.css';

const emptyClubForm = {
  name: '',
  description: '',
};

const emptyMemberForm = {
  name: '',
  gender: 'MALE',
  role: 'MEMBER',
  skillNote: '',
  contactMemo: '',
  memo: '',
};

function unwrapData(response, fallback) {
  return response?.data?.data ?? fallback;
}

function normalizeClub(club) {
  if (!club) {
    return null;
  }

  return {
    ...club,
    role: club.role ?? club.currentUserRole,
    memberCount: club.memberCount ?? club.activeMemberCount ?? 0,
  };
}

function roleLabel(role) {
  return role === 'ADMIN' ? '관리자' : '클럽원';
}

function genderLabel(gender) {
  return gender === 'FEMALE' ? '여성' : '남성';
}

function sortClubMembers(members = []) {
  const creator = members
    .filter((member) => member.role === 'ADMIN')
    .sort((first, second) => {
      const firstHasUser = first.userId == null ? 1 : 0;
      const secondHasUser = second.userId == null ? 1 : 0;

      if (firstHasUser !== secondHasUser) {
        return firstHasUser - secondHasUser;
      }

      return Number(first.id ?? 0) - Number(second.id ?? 0);
    })[0];

  const creatorId = creator?.id;

  return [...members].sort((first, second) => {
    if (first.id === creatorId && second.id !== creatorId) {
      return -1;
    }
    if (second.id === creatorId && first.id !== creatorId) {
      return 1;
    }

    const roleRank = { ADMIN: 0, MEMBER: 1 };
    const firstRank = roleRank[first.role] ?? 2;
    const secondRank = roleRank[second.role] ?? 2;

    if (firstRank !== secondRank) {
      return firstRank - secondRank;
    }

    return Number(first.id ?? 0) - Number(second.id ?? 0);
  });
}

function errorMessage(error, fallback) {
  const status = error?.response?.status;
  const serverMessage = error?.response?.data?.message;

  if (serverMessage) {
    return serverMessage;
  }

  if (status === 401) {
    return '로그인이 필요합니다.';
  }
  if (status === 403) {
    return '관리자만 실행할 수 있습니다.';
  }
  if (status === 409) {
    return '현재 상태에서는 실행할 수 없습니다.';
  }

  return fallback;
}

function Club({ currentUser }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { publicId, memberId } = useParams();
  const [view, setView] = useState('list');
  const [activeTab, setActiveTab] = useState('meetings');
  const [clubs, setClubs] = useState([]);
  const [members, setMembers] = useState([]);
  const [selectedClub, setSelectedClub] = useState(null);
  const [clubForm, setClubForm] = useState(emptyClubForm);
  const [memberForm, setMemberForm] = useState(emptyMemberForm);
  const [memberQuery, setMemberQuery] = useState('');
  const [memberFormSourceId, setMemberFormSourceId] = useState(null);
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isMembersLoading, setIsMembersLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (location.pathname === '/clubs/new') {
      setView('new');
      return;
    }

    if (publicId && location.pathname.endsWith('/edit') && !memberId) {
      setView('club-edit');
      return;
    }

    if (publicId && location.pathname.endsWith('/members/new')) {
      setView('member-new');
      return;
    }

    if (publicId && memberId && location.pathname.endsWith('/edit')) {
      setView('member-edit');
      return;
    }

    if (publicId) {
      setView('detail');
      return;
    }

    setView('list');
  }, [location.pathname, memberId, publicId]);

  const isAdmin = selectedClub?.admin === true || selectedClub?.role === 'ADMIN';

  const showNotice = (message) => {
    setNotice(message);
    window.setTimeout(() => setNotice(''), 2200);
  };

  const loadClubs = async () => {
    setIsLoading(true);
    setError('');

    try {
      const response = await getMyClubs();
      const nextClubs = unwrapData(response, []).map(normalizeClub).filter(Boolean);
      setClubs(nextClubs);
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽 목록을 불러오지 못했습니다.'));
    } finally {
      setIsLoading(false);
    }
  };

  const loadClubDetail = async (clubPublicId) => {
    setIsLoading(true);
    setError('');
    setSelectedClub(null);
    setMembers([]);

    try {
      const response = await getClub(clubPublicId);
      const nextClub = normalizeClub(unwrapData(response, null));
      setSelectedClub(nextClub);
      setClubForm({
        name: nextClub?.name ?? '',
        description: nextClub?.description ?? '',
      });
    } catch (requestError) {
      setSelectedClub(null);
      setError(errorMessage(requestError, '클럽 정보를 불러오지 못했습니다.'));
    } finally {
      setIsLoading(false);
    }
  };

  const loadMembers = async (clubPublicId, keyword = '') => {
    setIsMembersLoading(true);

    try {
      const response = await getClubMembers(clubPublicId, keyword);
      setMembers(unwrapData(response, []));
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽원 목록을 불러오지 못했습니다.'));
    } finally {
      setIsMembersLoading(false);
    }
  };

  useEffect(() => {
    if (!currentUser || view !== 'list') {
      return;
    }

    loadClubs();
  }, [currentUser, view]);

  useEffect(() => {
    if (
      !currentUser ||
      !['detail', 'club-edit', 'member-new', 'member-edit'].includes(view) ||
      !publicId
    ) {
      return;
    }

    loadClubDetail(publicId);
  }, [currentUser, view, publicId]);

  useEffect(() => {
    if (
      !currentUser ||
      !['detail', 'member-edit'].includes(view) ||
      !publicId
    ) {
      return;
    }

    const timeoutId = window.setTimeout(() => {
      loadMembers(publicId, view === 'member-edit' ? '' : memberQuery.trim());
    }, 250);

    return () => window.clearTimeout(timeoutId);
  }, [currentUser, view, publicId, memberQuery]);

  const editingMember = useMemo(
    () => members.find((member) => String(member.id) === String(memberId)) ?? null,
    [memberId, members],
  );

  const sortedMembers = useMemo(() => sortClubMembers(members), [members]);

  useEffect(() => {
    setError('');

    if (view === 'member-new') {
      setMemberForm(emptyMemberForm);
      setMemberFormSourceId(null);
      return;
    }

    if (view !== 'member-edit') {
      setMemberFormSourceId(null);
    }
  }, [location.pathname, view]);

  useEffect(() => {
    if (!editingMember || memberFormSourceId === editingMember.id) {
      return;
    }

    setMemberForm({
      name: editingMember.name ?? '',
      gender: editingMember.gender ?? 'MALE',
      role: editingMember.role ?? 'MEMBER',
      skillNote: editingMember.skillNote ?? '',
      contactMemo: editingMember.contactMemo ?? '',
      memo: editingMember.memo ?? '',
    });
    setMemberFormSourceId(editingMember.id);
  }, [editingMember, memberFormSourceId]);

  const openClub = (club, tab = 'meetings') => {
    setActiveTab(tab);
    navigate(`/clubs/${club.publicId}`);
  };

  const handleCreateClub = async (event) => {
    event.preventDefault();
    const name = clubForm.name.trim();

    if (!name) {
      showNotice('클럽명을 입력해 주세요.');
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      const response = await createClub({
        name,
        description: clubForm.description.trim(),
      });
      const nextPublicId = unwrapData(response, {})?.publicId;

      setClubForm(emptyClubForm);
      setActiveTab('members');
      navigate(nextPublicId ? `/clubs/${nextPublicId}` : '/clubs');
      showNotice('클럽을 만들었습니다.');
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽을 만들지 못했습니다.'));
    } finally {
      setIsSaving(false);
    }
  };

  const handleUpdateClub = async (event) => {
    event.preventDefault();

    if (!selectedClub) {
      return;
    }

    const name = clubForm.name.trim();
    if (!name) {
      showNotice('클럽명을 입력해 주세요.');
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      await updateClub(selectedClub.publicId, {
        name,
        description: clubForm.description.trim(),
      });
      navigate(`/clubs/${selectedClub.publicId}`);
      showNotice('클럽 정보를 수정했습니다.');
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽 정보를 수정하지 못했습니다.'));
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteClub = async () => {
    if (!selectedClub || !window.confirm('클럽을 삭제할까요?')) {
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      await deleteClub(selectedClub.publicId);
      navigate('/clubs');
      showNotice('클럽을 삭제했습니다.');
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽을 삭제하지 못했습니다.'));
    } finally {
      setIsSaving(false);
    }
  };

  const handleAddMember = async (event) => {
    event.preventDefault();

    if (!selectedClub) {
      return;
    }

    const name = memberForm.name.trim();
    if (!name) {
      showNotice('클럽원 이름을 입력해 주세요.');
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      await addClubMember(selectedClub.publicId, {
        ...memberForm,
        name,
        skillNote: memberForm.skillNote.trim() || null,
        contactMemo: memberForm.contactMemo.trim() || null,
        memo: memberForm.memo.trim() || null,
      });
      setMemberForm(emptyMemberForm);
      setActiveTab('members');
      navigate(`/clubs/${selectedClub.publicId}`);
      showNotice('클럽원을 추가했습니다.');
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽원을 추가하지 못했습니다.'));
    } finally {
      setIsSaving(false);
    }
  };

  const handleUpdateMember = async (event) => {
    event.preventDefault();

    if (!selectedClub || !memberId) {
      return;
    }

    const name = memberForm.name.trim();
    if (!name) {
      showNotice('클럽원 이름을 입력해 주세요.');
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      await updateClubMember(selectedClub.publicId, memberId, {
        ...memberForm,
        name,
        skillNote: memberForm.skillNote.trim() || null,
        contactMemo: memberForm.contactMemo.trim() || null,
        memo: memberForm.memo.trim() || null,
      });
      setMemberForm(emptyMemberForm);
      setMemberFormSourceId(null);
      setActiveTab('members');
      navigate(`/clubs/${selectedClub.publicId}`);
      showNotice('클럽원 정보를 수정했습니다.');
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽원 정보를 수정하지 못했습니다.'));
    } finally {
      setIsSaving(false);
    }
  };

  const handleRemoveMember = async (member) => {
    if (!selectedClub || !window.confirm(`${member.name} 님을 삭제할까요?`)) {
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      await deleteClubMember(selectedClub.publicId, member.id);
      await Promise.all([
        loadMembers(selectedClub.publicId, memberQuery.trim()),
        loadClubDetail(selectedClub.publicId),
      ]);
      showNotice('클럽원 목록에서 삭제했습니다.');
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽원을 삭제하지 못했습니다.'));
    } finally {
      setIsSaving(false);
    }
  };

  if (!currentUser) {
    return (
      <main className="club-mobile-page">
        <section className="club-state">
          <span className="club-eyebrow">CLUB</span>
          <h1>로그인이 필요합니다</h1>
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

  const renderMessage = () => (
    <>
      {notice && <p className="club-notice">{notice}</p>}
      {error && <p className="club-notice danger">{error}</p>}
    </>
  );

  const renderClubEditForm = () => (
    <form className="club-stack" onSubmit={handleUpdateClub}>
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
      <button
        className="club-button primary full"
        type="submit"
        disabled={isSaving}
      >
        저장
      </button>
      <button
        className="club-button full"
        type="button"
        onClick={() => navigate(selectedClub ? `/clubs/${selectedClub.publicId}` : '/clubs')}
      >
        목록으로
      </button>
      <div className="club-danger-zone">
        <button
          className="club-button danger full"
          type="button"
          onClick={handleDeleteClub}
          disabled={isSaving}
        >
          클럽 삭제
        </button>
      </div>
    </form>
  );

  const renderMemberForm = () => (
    <form
      className="club-stack club-member-form"
      onSubmit={view === 'member-edit' ? handleUpdateMember : handleAddMember}
    >
      <label className="club-field">
        <span>이름</span>
        <input
          value={memberForm.name}
          onChange={(event) =>
            setMemberForm((current) => ({ ...current, name: event.target.value }))
          }
        />
      </label>
      <div className="club-form-grid">
        <label className="club-field">
          <span>성별</span>
          <select
            value={memberForm.gender}
            onChange={(event) =>
              setMemberForm((current) => ({
                ...current,
                gender: event.target.value,
              }))
            }
          >
            <option value="MALE">남성</option>
            <option value="FEMALE">여성</option>
          </select>
        </label>
        <label className="club-field">
          <span>역할</span>
          <select
            value={memberForm.role}
            onChange={(event) =>
              setMemberForm((current) => ({ ...current, role: event.target.value }))
            }
          >
            <option value="MEMBER">클럽원</option>
            <option value="ADMIN">관리자</option>
          </select>
        </label>
      </div>
      <label className="club-field">
        <span>실력 메모</span>
        <input
          value={memberForm.skillNote}
          onChange={(event) =>
            setMemberForm((current) => ({
              ...current,
              skillNote: event.target.value,
            }))
          }
        />
      </label>
      <label className="club-field">
        <span>연락 메모</span>
        <input
          value={memberForm.contactMemo}
          onChange={(event) =>
            setMemberForm((current) => ({
              ...current,
              contactMemo: event.target.value,
            }))
          }
        />
      </label>
      <label className="club-field">
        <span>비고</span>
        <input
          value={memberForm.memo}
          onChange={(event) =>
            setMemberForm((current) => ({ ...current, memo: event.target.value }))
          }
        />
      </label>
      <button className="club-button primary full" type="submit" disabled={isSaving}>
        {view === 'member-edit' ? '클럽원 수정' : '클럽원 추가'}
      </button>
      <button
        className="club-button full"
        type="button"
        onClick={() => {
          setMemberForm(emptyMemberForm);
          setMemberFormSourceId(null);
          setActiveTab('members');
          navigate(selectedClub ? `/clubs/${selectedClub.publicId}` : '/clubs');
        }}
      >
        목록으로
      </button>
      {view === 'member-edit' && editingMember && (
        <div className="club-danger-zone">
          <button
            className="club-button danger full"
            type="button"
            onClick={() => handleRemoveMember(editingMember)}
            disabled={isSaving}
          >
            클럽원 삭제
          </button>
        </div>
      )}
    </form>
  );

  if (view === 'new') {
    return (
      <main className="club-mobile-page">
        <header className="club-page-head">
          <span className="club-eyebrow">NEW CLUB</span>
          <h1>새 클럽</h1>
          <p>클럽을 만들면 생성자는 자동으로 관리자가 됩니다.</p>
        </header>

        {renderMessage()}

        <form className="club-stack" onSubmit={handleCreateClub}>
          <label className="club-field">
            <span>클럽명</span>
            <input
              value={clubForm.name}
              onChange={(event) =>
                setClubForm((current) => ({ ...current, name: event.target.value }))
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
          <button className="club-button primary full" type="submit" disabled={isSaving}>
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

  if (view === 'member-new' || view === 'member-edit') {
    const isEdit = view === 'member-edit';

    return (
      <main className="club-mobile-page">
        {isLoading && <p className="club-notice static">클럽 정보를 불러오는 중입니다.</p>}
        {!isLoading && !selectedClub && (
          <section className="club-state">
            <span className="club-eyebrow">CLUB MEMBER</span>
            <h1>클럽을 찾을 수 없습니다</h1>
            {renderMessage()}
            <button
              className="club-button full"
              type="button"
              onClick={() => navigate('/clubs')}
            >
              클럽 목록
            </button>
          </section>
        )}
        {selectedClub && !isAdmin && (
          <section className="club-state">
            <span className="club-eyebrow">{selectedClub.name}</span>
            <h1>관리자만 사용할 수 있습니다</h1>
            <p>클럽원 추가와 수정은 클럽 관리자에게만 열려 있습니다.</p>
            <button
              className="club-button full"
              type="button"
              onClick={() => {
                setActiveTab('members');
                navigate(`/clubs/${selectedClub.publicId}`);
              }}
            >
              클럽원 목록
            </button>
          </section>
        )}
        {selectedClub && isAdmin && (
          <>
            <header className="club-page-head">
              <span className="club-eyebrow">{selectedClub.name}</span>
              <h1>{isEdit ? '클럽원 수정' : '클럽원 추가'}</h1>
              <p>
                {isEdit
                  ? '클럽원 이름, 성별, 역할과 운영 메모를 수정합니다.'
                  : '관리자가 직접 운영 명단을 추가합니다. userId는 없어도 됩니다.'}
              </p>
            </header>

            {renderMessage()}

            {isEdit && isMembersLoading && (
              <p className="club-notice static">클럽원 정보를 불러오는 중입니다.</p>
            )}
            {isEdit && !isMembersLoading && !editingMember && (
              <section className="club-state">
                <span className="club-eyebrow">MEMBER</span>
                <h1>클럽원을 찾을 수 없습니다</h1>
                <button
                  className="club-button full"
                  type="button"
                  onClick={() => {
                    setActiveTab('members');
                    navigate(`/clubs/${selectedClub.publicId}`);
                  }}
                >
                  클럽원 목록
                </button>
              </section>
            )}
            {(!isEdit || editingMember) && (
              <>
                {renderMemberForm()}
                <div className="club-notice static">
                  같은 클럽의 활성 클럽원 이름은 중복 저장하지 않습니다.
                </div>
              </>
            )}
          </>
        )}
      </main>
    );
  }

  if (view === 'club-edit') {
    return (
      <main className="club-mobile-page">
        {isLoading && <p className="club-notice static">클럽 정보를 불러오는 중입니다.</p>}
        {!isLoading && !selectedClub && (
          <section className="club-state">
            <span className="club-eyebrow">CLUB</span>
            <h1>클럽을 찾을 수 없습니다</h1>
            {renderMessage()}
            <button
              className="club-button full"
              type="button"
              onClick={() => navigate('/clubs')}
            >
              클럽 목록
            </button>
          </section>
        )}
        {selectedClub && !isAdmin && (
          <section className="club-state">
            <span className="club-eyebrow">{selectedClub.name}</span>
            <h1>관리자만 사용할 수 있습니다</h1>
            <p>클럽 정보 수정은 클럽 관리자에게만 열려 있습니다.</p>
            <button
              className="club-button full"
              type="button"
              onClick={() => navigate(`/clubs/${selectedClub.publicId}`)}
            >
              클럽 상세
            </button>
          </section>
        )}
        {selectedClub && isAdmin && (
          <>
            <header className="club-page-head">
              <span className="club-eyebrow">클럽 정보 수정</span>
              <h1>{selectedClub.name}</h1>
              <p>클럽명과 소개를 수정합니다.</p>
            </header>

            {renderMessage()}
            {renderClubEditForm()}
          </>
        )}
      </main>
    );
  }

  if (view === 'detail') {
    return (
      <main className="club-mobile-page">
        {isLoading && <p className="club-notice static">클럽 정보를 불러오는 중입니다.</p>}
        {!isLoading && !selectedClub && (
          <section className="club-state">
            <span className="club-eyebrow">CLUB</span>
            <h1>클럽을 찾을 수 없습니다</h1>
            {renderMessage()}
            <button
              className="club-button full"
              type="button"
              onClick={() => navigate('/clubs')}
            >
              클럽 목록
            </button>
          </section>
        )}
        {selectedClub && (
          <>
            <header className="club-page-head club-detail-head">
              <h1>{selectedClub.name}</h1>
              <p className="club-description">{selectedClub.description}</p>
              <div className="club-chips">
                <span className="club-chip">클럽원 {selectedClub.memberCount}명</span>
              </div>
              {isAdmin && (
                <div className="club-page-actions">
                  <button
                    className="club-button full"
                    type="button"
                    onClick={() => navigate(`/clubs/${selectedClub.publicId}/edit`)}
                  >
                    클럽 정보 수정
                  </button>
                </div>
              )}
            </header>

            {renderMessage()}

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
                </div>
                <div className="club-scroll-list club-meeting-list">
                  <article className="club-row">
                    <p className="club-row-sub">
                      아직 등록된 모임이 없습니다.
                    </p>
                  </article>
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
                      onClick={() => navigate(`/clubs/${selectedClub.publicId}/members/new`)}
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
                  onChange={(event) => setMemberQuery(event.target.value)}
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
                  {!isMembersLoading && sortedMembers.map((member) => (
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
                        className={`club-chip ${
                          member.role === 'ADMIN' ? 'admin' : ''
                        }`}
                      >
                        {roleLabel(member.role)}
                      </span>
                      {isAdmin && (
                        <div className="club-row-actions">
                          <button
                            className="club-button small"
                            type="button"
                            onClick={() =>
                              navigate(
                                `/clubs/${selectedClub.publicId}/members/${member.id}/edit`,
                              )
                            }
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

            <button
              className="club-button full"
              type="button"
              onClick={() => navigate('/clubs')}
            >
              클럽 목록
            </button>
          </>
        )}
      </main>
    );
  }

  return (
    <main className="club-mobile-page">
      <header className="club-page-head club-list-head">
        <h1>내 클럽</h1>
        <p>소속 클럽을 보고, 운영 중인 클럽에서 모임과 클럽원을 관리합니다.</p>
        <button
          className="club-button primary full"
          type="button"
          onClick={() => {
            setClubForm(emptyClubForm);
            navigate('/clubs/new');
          }}
        >
          클럽 만들기
        </button>
      </header>

      {renderMessage()}

      <section className="club-section">
        <div className="club-section-title">
          <h2>클럽 목록</h2>
        </div>

        {isLoading && <p className="club-notice static">클럽 목록을 불러오는 중입니다.</p>}
        {!isLoading && clubs.length === 0 && (
          <article className="club-card">
            <div className="club-card-title">아직 소속된 클럽이 없습니다.</div>
            <p className="club-card-copy">첫 클럽을 만들고 명단을 관리해 보세요.</p>
          </article>
        )}
        {clubs.map((club) => (
          <article className="club-card" key={club.publicId}>
            <div className="club-card-head">
              <div>
                <div className="club-card-title">{club.name}</div>
                <p className="club-card-copy club-description">{club.description}</p>
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
