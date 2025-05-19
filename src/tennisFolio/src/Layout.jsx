import React from 'react';
import './layout.css';
import Navigation from './Navigation.jsx';
import { useNavigate } from 'react-router-dom';
function Layout({ children }) {
  const navigate = useNavigate();
  return (
    <div className="layout">
      <header className="header" onClick={() => navigate("/")}>TennisFolio</header>

      <div className="main">
          <Navigation />
        <section className="contents">
          {children}
        </section>

        <aside className="ad">
          
        </aside>
      </div>

      <footer className="footer">© 2025 TennisFolio. All rights reserved.</footer>
    </div>
  );
}

export default Layout;