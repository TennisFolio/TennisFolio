// ✅ Layout.jsx (Navigation 내부 햄버거 고려, 중복 제거)
import React, { useEffect, useRef, useState } from 'react';
import './Layout.css';
import Navigation from './Navigation.jsx';
import { useNavigate } from 'react-router-dom';
import Footer from './Footer.jsx';

function Layout({ children }) {
  const navigate = useNavigate();
  const [showHeader, setShowHeader] = useState(true);
  const lastScrollY = useRef(window.scrollY);

  useEffect(() => {
    const handleScroll = () => {
      if (window.innerWidth > 768) return;

      const currentScrollY = window.scrollY;
      if (currentScrollY < lastScrollY.current) {
        // 스크롤 올림
        setShowHeader(true);
      } else if (currentScrollY > lastScrollY.current) {
        // 스크롤 내림
        setShowHeader(false);
      }
      lastScrollY.current = currentScrollY;
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <div className="layout">
      <header className={`header ${showHeader ? 'show' : 'hide'}`}>
        <div className="header-inner">
          <div className="logo" onClick={() => navigate('/')}>
            TennisFolio
          </div>
        </div>
      </header>

      <div className="main">
        <Navigation />

        <section className="contents">{children}</section>

        <aside className="ad" />
      </div>

      <footer className="footer">
        <div
          style={{
            backgroundColor: '#f8f8f8',
            padding: '20px',
            textAlign: 'center',
          }}
        >
          <h5>
            광고 및 후원 문의
            <br />
            Advertising and Sponsorshop Contact
          </h5>
          <p>tennisfolio1029@gmail.com</p>
          <p>
            TennisFolio
            <br />
            호스팅 서비스 : AWS
            <br />
          </p>

          <p>
            Disclaimer:<br></br> All content is provided for fun and
            entertainment purposes only. <br />※ Some of the data in this
            service is collected through the TennisApi provided by RapidAPI.
          </p>
          <p> TENNISFOLIO net All rights reserved. 2025 </p>
          <div>
            <a href="/privacy">개인정보 처리방침</a>
          </div>
        </div>
        © 2025 TennisFolio. All rights reserved.
      </footer>
    </div>
  );
}

export default Layout;
