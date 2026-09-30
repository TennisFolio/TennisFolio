import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyClubs } from '../../../utils/clubApi';
import { errorMessage, normalizeClub, unwrapData } from '../../../page/club/clubPageUtils';

function ClubDashboardLayout({ clubPublicId, currentUser, activeMenu, children }) {
  const navigate = useNavigate();
  const [clubs, setClubs] = useState([]);
  const [error, setError] = useState('');
  const [isClubSwitcherOpen, setIsClubSwitcherOpen] = useState(false);
  const [isDesktopNoticeVisible, setIsDesktopNoticeVisible] = useState(true);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
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
          <button
            className="club-dashboard-mobile-menu-trigger"
            type="button"
            aria-label="대시보드 메뉴"
            aria-expanded={isMobileMenuOpen}
            aria-controls="club-dashboard-mobile-menu"
            onClick={() => setIsMobileMenuOpen((isOpen) => !isOpen)}
          >
            <span aria-hidden="true">☰</span>
          </button>
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
        {isMobileMenuOpen && (
          <div className="club-dashboard-mobile-menu-backdrop" onClick={() => setIsMobileMenuOpen(false)}>
            <nav id="club-dashboard-mobile-menu" className="club-dashboard-mobile-menu" aria-label="대시보드 메뉴" onClick={(event) => event.stopPropagation()}>
              <header className="club-dashboard-mobile-menu-header">
                <strong>TennisFolio</strong>
                <button type="button" aria-label="대시보드 메뉴 닫기" onClick={() => setIsMobileMenuOpen(false)}>×</button>
              </header>
              <span className="club-dashboard-nav-label">BACKOFFICE</span>
              <button
                className={activeMenu === 'dashboard' ? 'active' : ''}
                type="button"
                onClick={() => {
                  setIsMobileMenuOpen(false);
                  navigate(`/clubs/${clubPublicId}/dashboard`);
                }}
              >
                대시보드
              </button>
              <button
                className={activeMenu === 'members' ? 'active' : ''}
                type="button"
                onClick={() => {
                  setIsMobileMenuOpen(false);
                  navigate(`/clubs/${clubPublicId}/dashboard/members`);
                }}
              >
                회원 관리
              </button>
              <span className="muted">모임 활동 <small>준비 중</small></span>
              <span className="muted">참여 현황 <small>준비 중</small></span>
            </nav>
          </div>
        )}
        {isDesktopNoticeVisible && (
          <aside className="club-dashboard-desktop-notice" role="status">
            <div>
              <strong>PC 사용을 권장합니다.</strong>
              <p>모바일에서도 조회할 수 있지만, 회원 관리와 일괄 등록은 PC에서 더 편리합니다.</p>
            </div>
            <button
              type="button"
              aria-label="PC 사용 권장 안내 닫기"
              onClick={() => setIsDesktopNoticeVisible(false)}
            >
              닫기
            </button>
          </aside>
        )}
        {error && <p className="club-notice danger">{error}</p>}
        {children}
      </div>
    </main>
  );
}

export default ClubDashboardLayout;
