import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { selectMSWActive } from '../../store/mswSlice';
import './MSWToggle.css';

function MSWToggle() {
  const isMSWActive = useSelector(selectMSWActive);

  // Store에서 MSW 상태를 가져와서 로컬 상태와 동기화
  const [isActive, setIsActive] = useState(isMSWActive);

  useEffect(() => {
    setIsActive(isMSWActive);
  }, [isMSWActive]);

  const handleToggle = async () => {
    if (typeof window !== 'undefined' && window.MSW) {
      try {
        if (isActive) {
          await window.MSW.stop();
          console.log('🔴 MSW 비활성화 - 실제 API 사용');
          // Store는 window.MSW.stop()에서 자동 업데이트됨
        } else {
          await window.MSW.start();
          console.log('🟢 MSW 활성화 - 목 데이터 사용');
          // Store는 window.MSW.start()에서 자동 업데이트됨
        }

        // 데이터 재요청을 위한 이벤트 발생
        window.dispatchEvent(
          new CustomEvent('mswToggled', { detail: { active: !isActive } })
        );
      } catch (error) {
        console.error('MSW 토글 실패:', error);
      }
    }
  };

  // 개발 모드가 아니면 렌더링하지 않음
  if (import.meta.env.MODE !== 'development') {
    return null;
  }

  return (
    <button
      onClick={handleToggle}
      className={`msw-toggle-btn ${isActive ? 'active' : 'inactive'}`}
      title={
        isActive
          ? 'MSW 활성화됨 (목 데이터 사용)'
          : 'MSW 비활성화됨 (실제 API 사용)'
      }
    >
      {isActive ? '🟢 MSW ON' : '🔴 MSW OFF'}
    </button>
  );
}

export default MSWToggle;
