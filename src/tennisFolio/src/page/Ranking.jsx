import React, { useState, useEffect } from 'react';
import RankingTable from '../components/ranking/RankingTable';
import RankingHeader from '../components/ranking/RankingHeader';
import { apiRequest } from '../utils/apiClient';
import { RankingTableSkeleton } from './RankingSkeleton';

import './ranking.css';

function Ranking() {
  const [rankings, setRankings] = useState([]);
  const [page, setPage] = useState(0);
  const size = 40;
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isAllLoaded, setIsAllLoaded] = useState(false);

  // 최초 데이터 로드
  useEffect(() => {
    const fetchInitial = async () => {
      try {
        const res = await apiRequest.get('/api/ranking', { page: 0, size });
        setRankings(res.data.data);
        setPage(1);
        if (res.data.data.length < size) setIsAllLoaded(true);
      } catch (error) {
        console.error('초기 데이터 조회 실패', error);
      } finally {
        setIsInitialLoading(false);
      }
    };
    fetchInitial();
  }, []);

  // 더보기 버튼 클릭 시
  const handleLoadMore = async () => {
    try {
      const res = await apiRequest.get('/api/ranking', { page, size });
      setRankings((prev) => [...prev, ...res.data.data]);
      setPage((prev) => prev + 1);
      if (res.data.data.length < size) setIsAllLoaded(true);
    } catch (error) {
      console.error('추가 데이터 조회 실패', error);
    }
  };

  return (
    <div>
      <RankingHeader lastUpdated={rankings[0]?.rankingLastUpdated} />
      {isInitialLoading ? (
        <RankingTableSkeleton />
      ) : (
        <>
          <RankingTable rankings={rankings} />
          {!isAllLoaded && (
            <button className="load-more-button" onClick={handleLoadMore}>
              더 보기
            </button>
          )}
        </>
      )}
    </div>
  );
}

export default Ranking;
