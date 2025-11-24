import React from 'react';
import Skeleton from '../components/common/Skeleton';
import '../components/main/LiveEvents.css';

/**
 * LiveEvents 페이지 전용 스켈레톤 컴포넌트
 */

// 라이브 이벤트 카드 하나의 스켈레톤 (재사용 가능)
export const LiveEventCardSkeleton = () => {
  return (
    <div className="eventCard">
      {/* 대회 헤더 스켈레톤 */}
      <div className="tournamentHeader">
        <div>
          <Skeleton
            width="200px"
            height="28px"
            borderRadius="4px"
            className="skeleton-tournament-name"
          />
        </div>
        <div style={{ marginTop: '4px' }}>
          <Skeleton
            width="150px"
            height="22px"
            borderRadius="4px"
            className="skeleton-round-name"
          />
        </div>
      </div>

      {/* 선수 정보 및 점수 스켈레톤 */}
      <div className="eventHeader">
        <div className="teamBlock">
          <Skeleton
            width="80px"
            height="80px"
            borderRadius="50%"
            className="skeleton-player-image"
          />
          <Skeleton
            width="100px"
            height="20px"
            borderRadius="4px"
            className="skeleton-team-name"
            style={{ marginTop: '8px' }}
          />
        </div>

        <Skeleton
          width="60px"
          height="32px"
          borderRadius="4px"
          className="skeleton-set-score"
        />

        <div className="teamBlock">
          <Skeleton
            width="80px"
            height="80px"
            borderRadius="50%"
            className="skeleton-player-image"
          />
          <Skeleton
            width="100px"
            height="20px"
            borderRadius="4px"
            className="skeleton-team-name"
            style={{ marginTop: '8px' }}
          />
        </div>
      </div>

      {/* 세트 점수 테이블 스켈레톤 */}
      <div className="eventTable">
        <table>
          <thead>
            <tr>
              <th></th>
              <th>SET1</th>
              <th>SET2</th>
              <th>SET3</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <Skeleton
                  width="100px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-player-name"
                />
              </td>
              <td>
                <Skeleton
                  width="30px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-set-score-cell"
                />
              </td>
              <td>
                <Skeleton
                  width="30px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-set-score-cell"
                />
              </td>
              <td>
                <Skeleton
                  width="30px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-set-score-cell"
                />
              </td>
            </tr>
            <tr>
              <td>
                <Skeleton
                  width="100px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-player-name"
                />
              </td>
              <td>
                <Skeleton
                  width="30px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-set-score-cell"
                />
              </td>
              <td>
                <Skeleton
                  width="30px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-set-score-cell"
                />
              </td>
              <td>
                <Skeleton
                  width="30px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-set-score-cell"
                />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      {/* 버튼 스켈레톤 */}
      <Skeleton
        width="120px"
        height="40px"
        borderRadius="24px"
        className="skeleton-event-button"
        style={{ margin: '16px auto 0' }}
      />
    </div>
  );
};

// 라이브 이벤트 카드 스켈레톤
export const LiveEventsSkeleton = () => {
  return (
    <div className="live-events">
      {/* 헤더 */}
      <div className="live-title">
        <div>지금 펼쳐지는 경기, 실시간으로 함께하세요!</div>
      </div>

      <div className="events-grid">
        {[1, 2, 3].map((index) => (
          <LiveEventCardSkeleton key={index} />
        ))}
      </div>
    </div>
  );
};
