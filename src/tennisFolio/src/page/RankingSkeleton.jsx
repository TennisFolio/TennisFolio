import React from 'react';
import Skeleton from '../components/common/Skeleton';
import './ranking.css';
import '../components/ranking/RankingTable.css';

/**
 * Ranking 페이지 전용 스켈레톤 컴포넌트
 */

// 랭킹 테이블 스켈레톤
export const RankingTableSkeleton = () => {
  return (
    <div className="ranking-table-container">
      <table className="ranking-table">
        <thead>
          <tr>
            <th>랭킹</th>
            <th>국가</th>
            <th>선수 이름</th>
            <th>현재 포인트</th>
          </tr>
        </thead>
        <tbody>
          {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((index) => (
            <tr key={index}>
              <td>
                <Skeleton
                  width="40px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-ranking"
                />
              </td>
              <td>
                <Skeleton
                  width="32px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-flag"
                />
              </td>
              <td>
                <Skeleton
                  width="120px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-player-name"
                />
              </td>
              <td>
                <Skeleton
                  width="80px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-points"
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
