import React from 'react';
import Skeleton from '../components/common/Skeleton';
import '../components/testList/testCard.css';
import '../components/testList/testHeader.css';

/**
 * TestList 페이지 전용 스켈레톤 컴포넌트
 */

// 테스트 카드 그리드 스켈레톤
export const TestListSkeleton = () => {
  return (
    <div>
      {/* 헤더는 실제 컴포넌트가 표시되므로 스켈레톤 불필요 */}
      <div className="test-grid-container">
        {[1, 2, 3, 4].map((index) => (
          <div key={index} className="racket-test-container">
            <div className="racket-test-card skeleton-test-card">
              <div className="racket-test-content">
                <Skeleton
                  width="80%"
                  height="32px"
                  borderRadius="4px"
                  className="skeleton-test-title"
                />
                <div style={{ marginTop: '0.5rem', marginBottom: '1.5rem' }}>
                  <Skeleton
                    width="100%"
                    height="20px"
                    borderRadius="4px"
                    className="skeleton-test-description"
                  />
                  <Skeleton
                    width="70%"
                    height="20px"
                    borderRadius="4px"
                    className="skeleton-test-description"
                    style={{ marginTop: '0.5rem' }}
                  />
                </div>
                <Skeleton
                  width="80px"
                  height="28px"
                  borderRadius="20px"
                  className="skeleton-feature-tag"
                />
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

