import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
 
import './Navigation.css';

const NAV_ITEMS = [
  {
    title: '선수',
    children: [
      { label: 'ATP', path: '/players/atp' },
      { label: 'WTA', path: '/players/wta' },
    ],
  },
  {
    title: '랭킹',
    children: [
      { label: '실시간', path: '/ranking' },
      { label: '연도별', path: '/ranking/yearly' },
    ],
  },
  {
    title: '테스트',
    children: [
      { label: '라켓테스트', path: '/test/racketTest' },
      { label: '스트링테스트', path: '/test/tournament' },
      { label: 'ATP선수테스트', path: '/test/ATPPlayer' },
    ],
  },
];

function Navigation() {
  const navigate = useNavigate();
  const [visible, setVisible] = useState(false);
  const [openSection, setOpenSection] = useState(null);

  const toggleSection = (index) => {
    setOpenSection(openSection === index ? null : index);
  };

  const handleNavigate = (path) => {
    setVisible(false);
    navigate(path);
  }

  return (
    <>
    <button className="hamburger" onClick={() => setVisible(!visible)}>
       ☰
    </button>
    <aside className={`sidebar ${visible ? 'show' : ''}`}>
      {NAV_ITEMS.map((section, index) => (
        <div key={index} className="nav-section">
          <h5 className="nav-title" onClick={() => toggleSection(index)}>
            {section.title}
            <span className="arrow">{openSection === index ? '▲' : '▼'}</span>
          </h5>
          <ul className={`nav-sublist ${openSection === index ? 'open' : ''}`}>
            {section.children.map((item, i) => (
              <li key={i} onClick={() => handleNavigate (item.path)}>
                {item.label}
              </li>
            ))}
          </ul>
        </div>
      ))}
    </aside>
    </>
  );
}

export default Navigation;