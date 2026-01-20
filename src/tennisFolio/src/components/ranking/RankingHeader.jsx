import React from 'react';
import './RankingHeader.css';
import dayjs from 'dayjs';

function RankingHeader({ lastUpdated, category }) {
  const categoryName = category?.toUpperCase() || 'ATP';
  
  return (
    <div className="ranking-title">
      <div>
        최신 {categoryName} 순위를 확인하세요! <br />
        <span className="update-date">
          업데이트: {dayjs(lastUpdated).format('YYYY.MM.DD')}
        </span>
      </div>
    </div>
  );
}

export default RankingHeader;
