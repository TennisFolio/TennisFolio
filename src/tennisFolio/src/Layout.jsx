import './Layout.css';
import Navigation from './Navigation.jsx';
import { useNavigate } from 'react-router-dom';
import logo from './assets/tennisFolio_logo.png';
import Footer from './Footer.jsx';
import LoadingMask from './components/common/LoadingMask.jsx';
import PlayerDetailModal from './components/common/PlayerDetailModal.jsx';

function Layout({ children }) {
  const navigate = useNavigate();

  return (
    <div className="layout">
      <header className="header">
        <div className="header-inner">
          <button
            type="button"
            className="logo"
            onClick={() => navigate('/')}
            aria-label="TennisFolio 홈"
          >
            <img className="logo_img" src={logo} alt="TennisFolio" />
          </button>
          <Navigation />
        </div>
      </header>

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
