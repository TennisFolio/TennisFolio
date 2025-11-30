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
            <th>선수</th>
            <th>나이</th>
            <th>포인트</th>
            <th>+/-</th>
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
              <td className="player-cell">
                <div className="player-info" style={{ justifyContent: 'center' }}>
                  <Skeleton
                    width="40px"
                    height="40px"
                    borderRadius="50%"
                    className="skeleton-player-image"
                  />
                  <Skeleton
                    width="24px"
                    height="16px"
                    borderRadius="4px"
                    className="skeleton-flag"
                  />
                  <Skeleton
                    width="120px"
                    height="20px"
                    borderRadius="4px"
                    className="skeleton-player-name"
                  />
                </div>
              </td>
              <td>
                <Skeleton
                  width="30px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-age"
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
              <td>
                <Skeleton
                  width="50px"
                  height="20px"
                  borderRadius="4px"
                  className="skeleton-gap-points"
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
