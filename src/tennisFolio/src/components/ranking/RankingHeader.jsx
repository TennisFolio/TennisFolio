import React from 'react'
import './rankingHeader.css';
import dayjs from 'dayjs';
function RankingHeader({lastUpdated}) {
  return (
    <h3 className="ranking-header">
        ATP 단식 랭킹 (기준 : {dayjs(lastUpdated).format('YYYY.MM.DD')})
    </h3>
  )
}

export default RankingHeader