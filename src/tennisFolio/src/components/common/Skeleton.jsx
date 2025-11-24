import React from 'react';
import './Skeleton.css';

/**
 * 범용 스켈레톤 컴포넌트
 * @param {string} width - 너비 (예: '100%', '200px', '50%')
 * @param {string} height - 높이 (예: '20px', '100%')
 * @param {string} borderRadius - 둥근 모서리 (예: '8px', '50%')
 * @param {string} className - 추가 CSS 클래스
 * @param {object} style - 인라인 스타일
 */
const Skeleton = ({
  width = '100%',
  height = '20px',
  borderRadius = '4px',
  className = '',
  style = {},
}) => {
  return (
    <div
      className={`skeleton ${className}`}
      style={{
        width,
        height,
        borderRadius,
        ...style,
      }}
    />
  );
};

export default Skeleton;

