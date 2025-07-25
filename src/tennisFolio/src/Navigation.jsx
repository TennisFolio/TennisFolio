import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
 
import './Navigation.css';

const NAV_ITEMS = [
  {
    title: '라이브',
    children: [
      { label: 'ATP', path: '/live/atp' },
      { label: 'WTA', path: '/live/wta' },
    ],
  },
  {
    title: '랭킹',
    children: [
      { label: '실시간', path: '/ranking' },
    ],
  },
  {
    title: '엔터테인먼트',
    children: [
      { label: '테스트', path: '/test' },
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