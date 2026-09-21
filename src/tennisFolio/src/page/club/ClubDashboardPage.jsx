import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import DashboardKpiCards from '../../components/club/dashboard/DashboardKpiCards';
import MemberParticipationPanel from '../../components/club/dashboard/MemberParticipationPanel';
import MonthlyMeetingsPanel from '../../components/club/dashboard/MonthlyMeetingsPanel';
import ClubMessage from '../../components/club/ClubMessage';
import ClubState from '../../components/club/ClubState';
import { getClubDashboard, getMyClubs } from '../../utils/clubApi';
import ClubAuthRequired from './ClubAuthRequired';
import { errorMessage, normalizeClub, unwrapData } from './clubPageUtils';
import '../Club.css';

function currentMonthValue() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

function parseMonthValue(value) {
  if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(value ?? '')) {
    return null;
  }

  const [year, month] = value.split('-').map(Number);
  return { year, month, value };
}

function shiftMonth({ year, month }, delta) {
  const date = new Date(year, month - 1 + delta, 1);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
}

function ClubDashboardPage({ currentUser }) {
  const navigate = useNavigate();
  const { clubPublicId } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const [clubs, setClubs] = useState([]);
  const [dashboard, setDashboard] = useState(null);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isClubSwitcherOpen, setIsClubSwitcherOpen] = useState(false);
  const [memberFilter, setMemberFilter] = useState('all');
  const [memberPage, setMemberPage] = useState(0);
  const clubSwitcherRef = useRef(null);
  const selectedMonth = parseMonthValue(searchParams.get('month')) ?? parseMonthValue(currentMonthValue());
  const currentMonth = currentMonthValue();
  const isNextMonthDisabled = selectedMonth.value >= currentMonth;

  const selectedClub = useMemo(
    () => clubs.find((club) => club.publicId === clubPublicId),
    [clubPublicId, clubs],
  );

  useEffect(() => {
    if (!currentUser) {
      return undefined;
    }

    let cancelled = false;
    getMyClubs()
      .then((clubsResponse) => {
        if (!cancelled) {
          setClubs(
            unwrapData(clubsResponse, [])
              .map(normalizeClub)
              .filter((club) => club?.admin === true || club?.role === 'ADMIN'),
          );
        }
      })
      .catch((requestError) => {
        if (!cancelled) {
          setError(errorMessage(requestError, '운영 클럽 목록을 불러오지 못했습니다.'));
        }
      });

    return () => {
      cancelled = true;
    };
  }, [currentUser]);

  useEffect(() => {
    if (!currentUser || !clubPublicId) {
      return undefined;
    }

    let cancelled = false;
    setIsLoading(true);
    setError('');

    getClubDashboard(clubPublicId, {
      year: selectedMonth.year,
      month: selectedMonth.month,
      memberFilter,
      page: memberPage,
    })
      .then((response) => {
        if (!cancelled) {
          setDashboard(unwrapData(response, null));
        }
      })
      .catch((requestError) => {
        if (!cancelled) {
          setError(errorMessage(requestError, '대시보드 정보를 불러오지 못했습니다.'));
          setDashboard(null);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [clubPublicId, currentUser, memberFilter, memberPage, selectedMonth.month, selectedMonth.year]);

  useEffect(() => {
    const closeClubSwitcher = (event) => {
      if (!clubSwitcherRef.current?.contains(event.target)) {
        setIsClubSwitcherOpen(false);
      }
    };

    document.addEventListener('mousedown', closeClubSwitcher);
    return () => document.removeEventListener('mousedown', closeClubSwitcher);
  }, []);

  if (!currentUser) {
    return <ClubAuthRequired />;
  }

  const changeMonth = (delta) => {
    setMemberFilter('all');
    setMemberPage(0);
    setSearchParams({ month: shiftMonth(selectedMonth, delta) });
  };

  const changeMemberFilter = (filter) => {
    if (filter !== memberFilter) {
      setMemberFilter(filter);
      setMemberPage(0);
    }
  };

  if (!isLoading && !dashboard) {
    return (
      <main className="club-dashboard-page">
        <ClubState eyebrow="BACKOFFICE" title="대시보드를 불러올 수 없습니다">
          <ClubMessage error={error} />
          <button className="club-button full" type="button" onClick={() => navigate('/clubs')}>
            클럽 목록
          </button>
        </ClubState>
      </main>
    );
  }

  return (
    <main className="club-dashboard-page">
      <aside className="club-dashboard-sidebar">
        <div className="club-dashboard-brand">Tennis<span>Folio</span></div>
        <span className="club-dashboard-nav-label">BACKOFFICE</span>
        <button className="club-dashboard-nav active" type="button">대시보드</button>
        <span className="club-dashboard-nav muted">멤버 현황 <small>준비 중</small></span>
        <span className="club-dashboard-nav muted">모임 활동 <small>준비 중</small></span>
        <span className="club-dashboard-nav muted">참여 현황 <small>준비 중</small></span>
      </aside>

      <div className="club-dashboard-content">
        <header className="club-dashboard-topbar">
          <div className="club-dashboard-club-switcher" ref={clubSwitcherRef}>
            <button
              className="club-dashboard-club-switcher-trigger"
              type="button"
              aria-haspopup="menu"
              aria-expanded={isClubSwitcherOpen}
              onClick={() => setIsClubSwitcherOpen((isOpen) => !isOpen)}
            >
              <strong>{selectedClub?.name ?? '클럽 선택'}</strong>
              <i aria-hidden="true">⌄</i>
            </button>
            {isClubSwitcherOpen && (
              <div className="club-dashboard-club-switcher-menu" role="menu" aria-label="운영 클럽 선택">
                {clubs.map((club) => (
                  <button
                    key={club.publicId}
                    type="button"
                    role="menuitemradio"
                    aria-selected={club.publicId === clubPublicId}
                    onClick={() => {
                      setIsClubSwitcherOpen(false);
                      navigate(`/clubs/${club.publicId}/dashboard?month=${selectedMonth.value}`);
                    }}
                  >
                    {club.name}
                  </button>
                ))}
              </div>
            )}
          </div>
          <span>클럽 ADMIN · {currentUser.nickname ?? currentUser.name ?? '운영자'}</span>
        </header>

        {isLoading ? (
          <p className="club-notice static">대시보드를 불러오는 중입니다.</p>
        ) : (
          <>
            <header className="club-dashboard-heading">
              <div>
                <h1>월별 운영 현황</h1>
                <p>선택한 달의 모임과 회원 참여를 확인합니다.</p>
              </div>
              <div className="club-dashboard-month-nav" aria-label="기준 월 이동">
                <button type="button" aria-label="이전 달" onClick={() => changeMonth(-1)}>‹</button>
                <strong>{selectedMonth.year}년 {selectedMonth.month}월</strong>
                <button type="button" aria-label="다음 달" disabled={isNextMonthDisabled} onClick={() => changeMonth(1)}>›</button>
              </div>
            </header>
            <ClubMessage error={error} />
            <DashboardKpiCards dashboard={dashboard} />
            <section className="club-dashboard-details">
              <MemberParticipationPanel
                activeMemberCount={dashboard.activeMemberCount}
                participation={dashboard.memberParticipation}
                memberFilter={memberFilter}
                onFilterChange={changeMemberFilter}
                onPageChange={setMemberPage}
              />
              <MonthlyMeetingsPanel
                meetings={dashboard.meetings ?? []}
                year={dashboard.period?.year ?? selectedMonth.year}
                month={dashboard.period?.month ?? selectedMonth.month}
              />
            </section>
          </>
        )}
      </div>
    </main>
  );
}

export default ClubDashboardPage;
