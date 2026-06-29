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
  const sheetRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (sheetRef.current && !sheetRef.current.contains(event.target)) {
        setSheetMode(null);
      }
    };

    if (sheetMode) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [sheetMode]);

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
    await logout();
    const currentCompetitionPublicId = getCurrentCompetitionPublicId(
      location.pathname
    );
    if (currentCompetitionPublicId) {
      clearCompetitionAdminToken(currentCompetitionPublicId);
      window.dispatchEvent(
        new CustomEvent('competition-admin-token-cleared', {
          detail: { publicId: currentCompetitionPublicId },
        })
      );
    }
    setSheetMode(null);
    onLogout?.();
  };

  const closeSheet = () => setSheetMode(null);

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
            {currentUser ? (
              <>
                <button
                  type="button"
                  className="auth-button header-my-competitions-button"
                  onClick={() => navigate('/me/competitions')}
                >
                  내 경기
                </button>
                <button
                  type="button"
                  className="auth-button header-my-meetings-button"
                  onClick={() => navigate('/meetings')}
                >
                  내 모임
                </button>
                <button
                  type="button"
                  className="auth-button auth-button-account"
                  aria-label="프로필"
                  onClick={() => setSheetMode('account')}
                >
                  <span className="header-profile-avatar" aria-hidden="true">
                    {getProfileInitial(currentUser)}
                  </span>
                </button>
              </>
            ) : (
              <button
                type="button"
                className="auth-button auth-button-login"
                onClick={() => loginWithProvider(default_oauth_provider)}
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
                onClick={() => {
                  setSheetMode(null);
                  navigate('/me/competitions');
                }}
              >
                내 경기 보기
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
