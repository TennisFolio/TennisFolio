import React, { useState, useEffect, useRef, useCallback } from 'react';
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
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const observerTargetRef = useRef(null);

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

  // 추가 데이터 로드
  const handleLoadMore = useCallback(async () => {
    if (isLoadingMore || isAllLoaded) return;

    setIsLoadingMore(true);
    try {
      const res = await apiRequest.get('/api/ranking', { page, size });
      setRankings((prev) => [...prev, ...res.data.data]);
      setPage((prev) => prev + 1);
      if (res.data.data.length < size) setIsAllLoaded(true);
    } catch (error) {
      console.error('추가 데이터 조회 실패', error);
    } finally {
      setIsLoadingMore(false);
    }
  }, [page, size, isLoadingMore, isAllLoaded]);

  // Intersection Observer로 무한스크롤 구현
  useEffect(() => {
    if (isInitialLoading || isAllLoaded) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && !isLoadingMore) {
          handleLoadMore();
        }
      },
      { threshold: 0.1 }
    );

    const currentTarget = observerTargetRef.current;
    if (currentTarget) {
      observer.observe(currentTarget);
    }

    return () => {
      if (currentTarget) {
        observer.unobserve(currentTarget);
      }
    };
  }, [isInitialLoading, isAllLoaded, isLoadingMore, handleLoadMore]);

  return (
    <div>
      <RankingHeader lastUpdated={rankings[0]?.rankingLastUpdated} />
      {isInitialLoading ? (
        <RankingTableSkeleton />
      ) : (
        <>
          <RankingTable rankings={rankings} />
          {!isAllLoaded && (
            <div
              ref={observerTargetRef}
              style={{ height: '20px', margin: '20px 0' }}
            >
              {isLoadingMore && (
                <div style={{ textAlign: 'center', color: '#666' }}>
                  로딩 중...
                </div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default Ranking;
