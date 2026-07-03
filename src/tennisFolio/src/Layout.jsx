import { useEffect, useRef, useState } from 'react';
import './Layout.css';
import { useLocation, useNavigate } from 'react-router-dom';
import logo from './assets/tennisFolio_logo.png';
import Footer from './Footer.jsx';
import LoadingMask from './components/common/LoadingMask.jsx';
import PlayerDetailModal from './components/common/PlayerDetailModal.jsx';
import { default_oauth_provider } from './constants/urls.js';
import { clearCompetitionAdminToken } from './utils/competitionEditToken';
import { loginWithProvider, logout } from './utils/authApi';

function getCurrentCompetitionPublicId(pathname) {
  const match = pathname.match(/^\/competitions\/([^/]+)/);
  return match ? decodeURIComponent(match[1]) : '';
}

function getProfileName(user) {
  const nickName = user?.nickName?.trim();
  return nickName || '프로필';
}

function getProfileInitial(user) {
  const name = getProfileName(user);
  return name.slice(0, 1).toUpperCase();
}

function Layout({ children, currentUser, onLogout }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [sheetMode, setSheetMode] = useState(null);
  const [isManageMenuOpen, setIsManageMenuOpen] = useState(false);
  const sheetRef = useRef(null);
  const manageMenuRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        isManageMenuOpen &&
        manageMenuRef.current &&
        !manageMenuRef.current.contains(event.target)
      ) {
        setIsManageMenuOpen(false);
      }

      if (sheetRef.current && !sheetRef.current.contains(event.target)) {
        setSheetMode(null);
      }
    };

    if (sheetMode || isManageMenuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [sheetMode, isManageMenuOpen]);

  useEffect(() => {
    if (!currentUser) {
      return;
    }

    const redirectAfterLogin =
      sessionStorage.getItem('tennisfolio:postLoginRedirect') ||
      localStorage.getItem('tennisfolio:postLoginRedirect');
    if (
      !redirectAfterLogin ||
      !redirectAfterLogin.startsWith('/') ||
      redirectAfterLogin.startsWith('//')
    ) {
      return;
    }

    sessionStorage.removeItem('tennisfolio:postLoginRedirect');
    localStorage.removeItem('tennisfolio:postLoginRedirect');
    navigate(redirectAfterLogin, { replace: true });
  }, [currentUser, navigate]);

  const handleLogout = async () => {
    const currentCompetitionPublicId = getCurrentCompetitionPublicId(
      location.pathname
    );

    try {
      await logout();
    } finally {
      if (currentCompetitionPublicId) {
        clearCompetitionAdminToken(currentCompetitionPublicId);
        window.dispatchEvent(
          new CustomEvent('competition-admin-token-cleared', {
            detail: { publicId: currentCompetitionPublicId },
          })
        );
      }
      setIsManageMenuOpen(false);
      setSheetMode(null);
      onLogout?.();
      navigate('/', { replace: true });
    }
  };

  const closeSheet = () => setSheetMode(null);

  const closeManageSurfaces = () => {
    setIsManageMenuOpen(false);
    setSheetMode(null);
  };

  const navigateToMyCompetitions = () => {
    closeManageSurfaces();
    navigate('/me/competitions');
  };

  const navigateToMeetings = () => {
    closeManageSurfaces();
    navigate('/meetings');
  };

  return (
    <div className="layout">
      <header className="header">
        <div className="header-inner">
          <button
            type="button"
            className="logo"
            onClick={() => navigate('/')}
            aria-label="TennisFolio home"
          >
            <img className="logo_img" src={logo} alt="TennisFolio" />
          </button>

          <div className="header-actions">
            <div className="header-manage-menu" ref={manageMenuRef}>
              <button
                type="button"
                className={`auth-button header-manage-menu-button${
                  isManageMenuOpen ? ' is-open' : ''
                }`}
                aria-label="관리 메뉴"
                aria-haspopup="menu"
                aria-expanded={isManageMenuOpen}
                onClick={() => setIsManageMenuOpen((isOpen) => !isOpen)}
              >
                <span className="header-manage-menu-icon" aria-hidden="true">
                  ≡
                </span>
              </button>

              {isManageMenuOpen && (
                <div
                  className="header-manage-popover"
                  role="menu"
                  aria-label="관리 메뉴"
                >
                  <button
                    type="button"
                    className="header-manage-popover-item header-my-competitions-button is-primary"
                    role="menuitem"
                    onClick={navigateToMyCompetitions}
                  >
                    경기 관리
                  </button>
                  <button
                    type="button"
                    className="header-manage-popover-item header-my-meetings-button"
                    role="menuitem"
                    onClick={navigateToMeetings}
                  >
                    모임 관리
                  </button>
                </div>
              )}
            </div>

            {currentUser ? (
              <button
                type="button"
                className="auth-button auth-button-account"
                aria-label="프로필"
                onClick={() => {
                  setIsManageMenuOpen(false);
                  setSheetMode('account');
                }}
              >
                <span className="header-profile-avatar" aria-hidden="true">
                  {getProfileInitial(currentUser)}
                </span>
              </button>
            ) : (
              <button
                type="button"
                className="auth-button auth-button-login"
                onClick={() => {
                  setIsManageMenuOpen(false);
                  loginWithProvider(default_oauth_provider);
                }}
              >
                로그인
              </button>
            )}
          </div>
        </div>
      </header>

      {sheetMode && (
        <div
          className="login-sheet-backdrop"
          role="presentation"
          onClick={closeSheet}
        >
          <div
            className="login-sheet"
            ref={sheetRef}
            role="dialog"
            aria-modal="true"
            aria-labelledby="login-sheet-title"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="login-sheet-header">
              <h2 id="login-sheet-title">프로필</h2>
              <button
                type="button"
                className="login-sheet-close"
                onClick={closeSheet}
              >
                닫기
              </button>
            </div>

            <div className="account-sheet-content">
              <div className="account-profile-summary">
                <div className="account-avatar" aria-hidden="true">
                  {getProfileInitial(currentUser)}
                </div>
                <div className="account-profile-copy">
                  <strong>{getProfileName(currentUser)}</strong>
                  <span>{currentUser.email}</span>
                </div>
              </div>
              <button
                type="button"
                className="account-my-competitions-button"
                onClick={navigateToMyCompetitions}
              >
                경기 관리
              </button>
              <button
                type="button"
                className="account-my-competitions-button"
                onClick={navigateToMeetings}
              >
                모임 관리
              </button>
              <button
                type="button"
                className="account-logout-button"
                onClick={handleLogout}
              >
                로그아웃
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="main">
        <section className="contents">{children}</section>
      </div>

      <Footer />

      <LoadingMask />
      <PlayerDetailModal />
    </div>
  );
}

export default Layout;
