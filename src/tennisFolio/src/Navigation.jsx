import { useNavigate } from 'react-router-dom';

import './Navigation.css';

const NAV_ITEMS = [{ label: '경기 만들기', path: '/' }];

function Navigation() {
  const navigate = useNavigate();

  return (
    <nav className="app-nav" aria-label="주요 메뉴">
      {NAV_ITEMS.map((item) => (
        <button
          key={item.path}
          type="button"
          className="app-nav-item"
          onClick={() => navigate(item.path)}
        >
          {item.label}
        </button>
      ))}
    </nav>
  );
}

export default Navigation;
