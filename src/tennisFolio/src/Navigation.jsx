import React, { useState, useEffect, useRef } from 'react';
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
    children: [{ label: '실시간', path: '/ranking' }],
  },
  {
    title: '엔터테인먼트',
    children: [{ label: '테스트', path: '/test' }],
  },
];

function Navigation() {
  const navigate = useNavigate();
  const [visible, setVisible] = useState(false);
  const [openSections, setOpenSections] = useState(new Set());
  const sidebarRef = useRef(null);
  const hamburgerRef = useRef(null);

  const toggleSection = (index) => {
    setOpenSections((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(index)) {
        newSet.delete(index);
      } else {
        newSet.add(index);
      }
      return newSet;
    });
  };

  const handleNavigate = (path) => {
    setVisible(false);
    navigate(path);
  };

  // 사이드바 외부 클릭 시 닫기
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        visible &&
        sidebarRef.current &&
        !sidebarRef.current.contains(event.target) &&
        hamburgerRef.current &&
        !hamburgerRef.current.contains(event.target)
      ) {
        setVisible(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [visible]);

  return (
    <>
      <button
        ref={hamburgerRef}
        className="hamburger"
        onClick={() => setVisible(!visible)}
      >
        ☰
      </button>
      <aside ref={sidebarRef} className={`sidebar ${visible ? 'show' : ''}`}>
        {NAV_ITEMS.map((section, index) => (
          <div key={index} className="nav-section">
            <h5 className="nav-title" onClick={() => toggleSection(index)}>
              {section.title}
              <span className="arrow">
                {openSections.has(index) ? '▲' : '▼'}
              </span>
            </h5>
            <ul
              className={`nav-sublist ${openSections.has(index) ? 'open' : ''}`}
            >
              {section.children.map((item, i) => (
                <li key={i} onClick={() => handleNavigate(item.path)}>
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
