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
    children: [{ label: 'ATP', path: '/ranking' }],
  },
  {
    title: '경기일정',
    children: [{ label: '대회 일정', path: '/schedule' }],
  },
  {
    title: '엔터테인먼트',
    children: [{ label: '테스트', path: '/test' }],
  },
];

function Navigation({ sidebarVisible, setSidebarVisible }) {
  const navigate = useNavigate();
  const [openSections, setOpenSections] = useState(new Set());
  const [hoveredSection, setHoveredSection] = useState(null);
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
    setSidebarVisible(false);
    setHoveredSection(null);
    navigate(path);
  };

  // 사이드바 외부 클릭 시 닫기
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        sidebarVisible &&
        sidebarRef.current &&
        !sidebarRef.current.contains(event.target) &&
        hamburgerRef.current &&
        !hamburgerRef.current.contains(event.target)
      ) {
        setSidebarVisible(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [sidebarVisible, setSidebarVisible]);

  return (
    <>
      {/* 데스크톱 네비게이션 */}
      <nav className="desktop-nav">
        {NAV_ITEMS.map((section, index) => (
          <div
            key={index}
            className="nav-item"
            onMouseEnter={() => setHoveredSection(index)}
            onMouseLeave={() => setHoveredSection(null)}
          >
            <span className="nav-item-title">{section.title}</span>
            <div
              className={`dropdown ${hoveredSection === index ? 'show' : ''}`}
            >
              {section.children.map((item, i) => (
                <div
                  key={i}
                  className="dropdown-item"
                  onClick={() => handleNavigate(item.path)}
                >
                  {item.label}
                </div>
              ))}
            </div>
          </div>
        ))}
      </nav>

      {/* 모바일 햄버거 버튼 */}
      <button
        ref={hamburgerRef}
        className="hamburger"
        onClick={() => setSidebarVisible(!sidebarVisible)}
      >
        ☰
      </button>

      {/* 모바일 사이드바 */}
      <aside
        ref={sidebarRef}
        className={`sidebar ${sidebarVisible ? 'show' : ''}`}
      >
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
