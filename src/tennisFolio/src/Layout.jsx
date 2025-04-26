import React from 'react';
import './layout.css';
import Navigation from './Navigation.jsx';
function Layout({ children }) {
  return (
    <div className="layout">
      <header className="header">TennisFolio</header>

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