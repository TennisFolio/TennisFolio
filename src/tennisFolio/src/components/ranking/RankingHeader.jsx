import React from 'react'
import './RankingHeader.css';
import dayjs from 'dayjs';
function RankingHeader({lastUpdated}) {
  return (
    <h1 className="ranking-title">
        ATP 단식 랭킹 <br/>({dayjs(lastUpdated).format('YYYY.MM.DD')})
    </h1>
  )
}

export default RankingHeader