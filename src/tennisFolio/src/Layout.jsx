import { useEffect, useRef, useState } from 'react';
import './Layout.css';
import { useNavigate } from 'react-router-dom';
import logo from './assets/tennisFolio_logo.png';
import Footer from './Footer.jsx';
import LoadingMask from './components/common/LoadingMask.jsx';
import PlayerDetailModal from './components/common/PlayerDetailModal.jsx';
import { default_oauth_provider } from './constants/urls.js';
import { loginWithProvider, logout } from './utils/authApi';

function Layout({ children, currentUser, onLogout }) {
  const navigate = useNavigate();
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

  const handleLogout = async () => {
    await logout();
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
              <button
                type="button"
                className="auth-button auth-button-account"
                onClick={() => setSheetMode('account')}
              >
                내 계정
              </button>
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
              <h2 id="login-sheet-title">내 계정</h2>
              <button
                type="button"
                className="login-sheet-close"
                onClick={closeSheet}
              >
                닫기
              </button>
            </div>

            <div className="account-sheet-content">
              <div className="account-email">
                <span>로그인한 이메일</span>
                <strong>{currentUser.email}</strong>
              </div>
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
