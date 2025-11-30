import React from 'react';
import './RankingTable.css';
import Flag from 'react-world-flags';
import SmartImage from '../main/SmartImage';
import { base_image_url } from '@/constants';

// YYYYMMDD 형식의 생년월일을 받아서 만 나이 계산
function calculateAge(birth) {
  if (!birth || birth.length !== 8) return null;

  const year = parseInt(birth.substring(0, 4), 10);
  const month = parseInt(birth.substring(4, 6), 10) - 1; // 0-based month
  const day = parseInt(birth.substring(6, 8), 10);

  const birthDate = new Date(year, month, day);
  const today = new Date();

  let age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();

  if (
    monthDiff < 0 ||
    (monthDiff === 0 && today.getDate() < birthDate.getDate())
  ) {
    age--;
  }

  return age;
}

function RankingTable({ rankings }) {
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
          {rankings.map((rank, index) => {
            const age = calculateAge(rank.player?.birth);
            const gapPointsDisplay =
              rank.gapPoints === 0
                ? '-'
                : rank.gapPoints > 0
                ? `+${rank.gapPoints}`
                : `${rank.gapPoints}`;

            return (
              <tr key={index}>
                <td className="ranking-cell">
                  <span className="ranking-number">{rank.curRanking}</span>
                </td>
                <td className="player-cell">
                  <div className="player-info">
                    <div
                      className={`player-image-container ${
                        rank.gapRanking !== 0
                          ? rank.gapRanking < 0
                            ? 'gap-up'
                            : 'gap-down'
                          : ''
                      }`}
                    >
                      {rank.gapRanking !== 0 && (
                        <span
                          className={`gap-ranking ${
                            rank.gapRanking < 0 ? 'up' : 'down'
                          }`}
                        >
                          {rank.gapRanking < 0 ? '▲' : '▼'}{' '}
                          {Math.abs(rank.gapRanking)}
                        </span>
                      )}
                      <SmartImage
                        base_url={base_image_url}
                        imageName={rank.player?.image}
                        fallbackText={
                          rank.player?.playerNameKr || rank.player?.playerName
                        }
                        forceDisableMSW={true}
                      />
                    </div>
                    <div className="player-flag-container">
                      <Flag
                        code={rank.player?.country}
                        className="player-flag"
                      />
                    </div>
                    <span className="player-name">
                      {rank.player?.playerNameKr || rank.player?.playerName}
                    </span>
                  </div>
                </td>
                <td className="age-cell">{age !== null ? age : '-'}</td>
                <td className="points-cell">
                  <span className="points-number">
                    {rank.curPoints?.toLocaleString()}
                  </span>
                </td>
                <td
                  className={`gap-points-cell ${
                    rank.gapPoints > 0 ? 'up' : rank.gapPoints < 0 ? 'down' : ''
                  }`}
                >
                  {gapPointsDisplay}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

export default RankingTable;
