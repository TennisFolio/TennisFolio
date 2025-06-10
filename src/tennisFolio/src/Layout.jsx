import React from 'react';
import './Layout.css';
import Navigation from './Navigation.jsx';
import { useNavigate } from 'react-router-dom';
import Footer from './Footer.jsx';
function Layout({ children }) {
  const navigate = useNavigate();
  return (
    <div className="layout">
      <header className="header" onClick={() => navigate("/")}>TennisFolio</header>

      <div className="main">
          <Navigation />
        <section className="contents">
          {children}
          <Footer />
        </section>

        <aside className="ad">
          
        </aside>
      </div>

      <footer className="footer">© 2025 TennisFolio. All rights reserved.</footer>
    </div>
  );
}

export default Layout;