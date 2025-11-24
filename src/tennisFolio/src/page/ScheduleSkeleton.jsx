import React from 'react';
import Skeleton from '../components/common/Skeleton';
import './Schedule.css';

/**
 * Schedule 페이지 전용 스켈레톤 컴포넌트
 */

// 필터 영역 스켈레톤
export const FilterSectionSkeleton = () => {
  return (
    <div className="filter-section">
      {/* 카테고리 탭 스켈레톤 */}
      <div className="category-tabs">
        <Skeleton
          width="100px"
          height="40px"
          borderRadius="20px"
          className="skeleton-category-tab"
        />
        <Skeleton
          width="100px"
          height="40px"
          borderRadius="20px"
          className="skeleton-category-tab"
        />
        <Skeleton
          width="120px"
          height="40px"
          borderRadius="20px"
          className="skeleton-category-tab"
        />
      </div>

      {/* 대회 목록 스켈레톤 */}
      <div className="tournament-list">
        <Skeleton
          width="100%"
          height="45px"
          borderRadius="8px"
          className="skeleton-tournament-item"
        />
        <Skeleton
          width="100%"
          height="45px"
          borderRadius="8px"
          className="skeleton-tournament-item"
        />
        <Skeleton
          width="95%"
          height="45px"
          borderRadius="8px"
          className="skeleton-tournament-item"
        />
        <Skeleton
          width="100%"
          height="45px"
          borderRadius="8px"
          className="skeleton-tournament-item"
        />
        <Skeleton
          width="90%"
          height="45px"
          borderRadius="8px"
          className="skeleton-tournament-item"
        />
        <Skeleton
          width="100%"
          height="45px"
          borderRadius="8px"
          className="skeleton-tournament-item"
        />
      </div>
    </div>
  );
};

// 경기 목록 스켈레톤
export const MatchesListSkeleton = () => {
  return (
    <div className="schedule-detail-section">
      {/* 헤더 스켈레톤 */}
      <div className="detail-header">
        <Skeleton width="200px" height="28px" borderRadius="4px" />
      </div>

      {/* 대회 그룹 스켈레톤 */}
      <div className="matches-list">
        {/* 대회 헤더 스켈레톤 */}
        <div className="season-group">
          <Skeleton width="100%" height="50px" borderRadius="8px" />
          
          {/* 경기 카드 스켈레톤들 */}
          {[1, 2, 3].map((index) => (
            <div key={index} className="match-card">
              {/* 경기 헤더 스켈레톤 */}
              <div className="match-header">
                <div className="match-header-left">
                  <Skeleton
                    width="60px"
                    height="20px"
                    borderRadius="4px"
                    className="skeleton-match-time"
                  />
                  <Skeleton
                    width="80px"
                    height="24px"
                    borderRadius="12px"
                    className="skeleton-match-status"
                  />
                </div>
                <Skeleton
                  width="100px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-match-round"
                />
              </div>

              {/* 플레이어 정보 스켈레톤 */}
              <div className="match-players">
                <div className="player" style={{ flex: 1 }}>
                  <Skeleton
                    width="100px"
                    height="20px"
                    borderRadius="4px"
                    className="skeleton-player-name"
                  />
                </div>
                <Skeleton
                  width="30px"
                  height="20px"
                  borderRadius="4px"
                  style={{ flexShrink: 0 }}
                  className="skeleton-score"
                />
                <Skeleton
                  width="20px"
                  height="20px"
                  borderRadius="4px"
                  style={{ flexShrink: 0 }}
                />
                <Skeleton
                  width="30px"
                  height="20px"
                  borderRadius="4px"
                  style={{ flexShrink: 0 }}
                  className="skeleton-score"
                />
                <div className="player" style={{ flex: 1 }}>
                  <Skeleton
                    width="100px"
                    height="20px"
                    borderRadius="4px"
                    className="skeleton-player-name"
                  />
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

