import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyClubs } from '../../../utils/clubApi';
import { errorMessage, normalizeClub, unwrapData } from '../../../page/club/clubPageUtils';

function ClubDashboardLayout({ clubPublicId, currentUser, activeMenu, children }) {
  const navigate = useNavigate();
  const [clubs, setClubs] = useState([]);
  const [error, setError] = useState('');
  const [isClubSwitcherOpen, setIsClubSwitcherOpen] = useState(false);
  const clubSwitcherRef = useRef(null);
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
      .then((response) => {
        if (!cancelled) {
          setClubs(
            unwrapData(response, [])
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
    const closeClubSwitcher = (event) => {
      if (!clubSwitcherRef.current?.contains(event.target)) {
        setIsClubSwitcherOpen(false);
      }
    };

    document.addEventListener('mousedown', closeClubSwitcher);
    return () => document.removeEventListener('mousedown', closeClubSwitcher);
  }, []);

  return (
    <main className="club-dashboard-page">
      <aside className="club-dashboard-sidebar">
        <div className="club-dashboard-brand">Tennis<span>Folio</span></div>
        <span className="club-dashboard-nav-label">BACKOFFICE</span>
        <button
          className={`club-dashboard-nav ${activeMenu === 'dashboard' ? 'active' : ''}`}
          type="button"
          onClick={() => navigate(`/clubs/${clubPublicId}/dashboard`)}
        >
          대시보드
        </button>
        <button
          className={`club-dashboard-nav ${activeMenu === 'members' ? 'active' : ''}`}
          type="button"
          onClick={() => navigate(`/clubs/${clubPublicId}/dashboard/members`)}
        >
          회원 관리
        </button>
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
                      navigate(`/clubs/${club.publicId}/dashboard/${activeMenu === 'members' ? 'members' : ''}`.replace(/\/$/, ''));
                    }}
                  >
                    {club.name}
                  </button>
                ))}
              </div>
            )}
          </div>
          <span>클럽 ADMIN · {currentUser?.nickname ?? currentUser?.name ?? '운영자'}</span>
        </header>
        {error && <p className="club-notice danger">{error}</p>}
        {children}
      </div>
    </main>
  );
}

export default ClubDashboardLayout;
