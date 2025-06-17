// ✅ Layout.jsx (Navigation 내부 햄버거 고려, 중복 제거)
import React from 'react';
import './Layout.css';
import Navigation from './Navigation.jsx';
import { useNavigate } from 'react-router-dom';
import Footer from './Footer.jsx';

function Layout({ children }) {
  const navigate = useNavigate();

  return (
    <div className="layout">
      <header className="header">
        <div className="header-inner">
          <div className="logo" onClick={() => navigate("/")}>TennisFolio</div>
        </div>
      </header>

      <div className="main">
        <Navigation />

        <section className="contents">
          {children}
          <Footer />
        </section>

        <aside className="ad" />
      </div>

      <footer className="footer">© 2025 TennisFolio. All rights reserved.</footer>
    </div>
  );
}

export default Layout;