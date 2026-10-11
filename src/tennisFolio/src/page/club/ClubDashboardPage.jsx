import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import DashboardKpiCards from '../../components/club/dashboard/DashboardKpiCards';
import MemberParticipationPanel from '../../components/club/dashboard/MemberParticipationPanel';
import MonthlyMeetingsPanel from '../../components/club/dashboard/MonthlyMeetingsPanel';
import ClubDashboardLayout from '../../components/club/dashboard/ClubDashboardLayout';
import ClubMessage from '../../components/club/ClubMessage';
import ClubState from '../../components/club/ClubState';
import { getClubDashboard } from '../../utils/clubApi';
import ClubAuthRequired from './ClubAuthRequired';
import { errorMessage, unwrapData } from './clubPageUtils';
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
  const [dashboard, setDashboard] = useState(null);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [memberFilter, setMemberFilter] = useState('all');
  const [memberPage, setMemberPage] = useState(0);
  const selectedMonth = parseMonthValue(searchParams.get('month')) ?? parseMonthValue(currentMonthValue());
  const currentMonth = currentMonthValue();
  const isNextMonthDisabled = selectedMonth.value >= currentMonth;

  const loadDashboard = useCallback(() => {
    if (!currentUser || !clubPublicId) {
      return () => {};
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

  useEffect(() => loadDashboard(), [loadDashboard]);

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

  return (
    <ClubDashboardLayout clubPublicId={clubPublicId} currentUser={currentUser} activeMenu="dashboard">
        {isLoading ? (
          <p className="club-notice static">대시보드를 불러오는 중입니다.</p>
        ) : !dashboard ? (
          <div className="club-dashboard-error-state">
            <ClubState
              eyebrow="BACKOFFICE"
              title="대시보드를 불러올 수 없습니다"
              description={error || '잠시 후 다시 시도해 주세요.'}
            >
              <div className="club-dashboard-error-actions">
                <button className="club-button primary" type="button" onClick={loadDashboard}>
                  다시 시도
                </button>
                <button className="club-button" type="button" onClick={() => navigate('/clubs')}>
                  클럽 목록
                </button>
              </div>
            </ClubState>
          </div>
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
    </ClubDashboardLayout>
  );
}

export default ClubDashboardPage;
