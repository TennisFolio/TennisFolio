import React from 'react'
import './rankingTable.css';
import Flag from 'react-world-flags';
function RankingTable({rankings}) {
  console.log(rankings);
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
          {rankings.map((rank, index) => (
            <tr key={index}>
              <td> 
                <span style={{ fontSize: '16px', fontWeight: 'bold' }}>
                  {rank.curRanking}
                </span>
                {rank.gapRanking !== 0 && (
                  <span
                    style={{
                      fontSize: '12px',
                      color: rank.gapRanking < 0 ? 'red' : 'blue',
                      marginLeft: '5px'
                    }}
                    >
                    {rank.gapRanking < 0 ? '▲' : '▼'} {Math.abs(rank.gapRanking)}
                  </span>)}
              </td>
              <td><Flag code={rank.player.country} style={{ width: '24px', height: '16px' }}/></td>
              <td><span className="player-name">{rank.player.playerName}</span></td>
              <td> 
                <span style={{ fontSize: '16px', fontWeight: 'bold' }}>
                  {rank.curPoints}  
                </span>
                {rank.gapPoints !== 0 && (
                  <span
                      style={{
                        fontSize: '12px',
                        color: rank.gapPoints < 0 ? 'red' : 'blue',
                        marginLeft: '5px'
                      }}
                    >
                    {rank.gapPoints < 0 ? '▲' : '▼'} {Math.abs(rank.gapPoints)}
                  </span>
              )}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default RankingTable