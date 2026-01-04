import React, { useState, useEffect, useRef, useCallback } from 'react';
import RankingTable from '../components/ranking/RankingTable';
import RankingHeader from '../components/ranking/RankingHeader';
import RankingSearch from '../components/ranking/RankingSearch';
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
  const [searchNameKeyword, setSearchNameKeyword] = useState(null);
  const [searchCountryCode, setSearchCountryCode] = useState(null);
  const observerTargetRef = useRef(null);

  // API 호출 함수
  const fetchRankings = useCallback(
    async (pageNum, nameKeyword, countryCode) => {
      const params = { page: pageNum, size };

      // 이름과 나라 조건을 모두 확인
      const hasName = nameKeyword && nameKeyword.trim();
      const hasCountry = countryCode && countryCode.trim();

      if (hasName && hasCountry) {
        // 두 조건 모두 있을 때: 이름 조건을 기본으로 하고 나라 코드를 추가 파라미터로 전달
        params.condition = 'NAME';
        params.keyword = nameKeyword;
        params.countryCode = countryCode;
      } else if (hasName) {
        // 이름만 있을 때
        params.condition = 'NAME';
        params.keyword = nameKeyword;
      } else if (hasCountry) {
        // 나라만 있을 때
        params.condition = 'COUNTRY';
        params.keyword = countryCode;
      }
      // 둘 다 없으면 params에 조건 추가 안 함 (전체 조회)

      return await apiRequest.get('/api/ranking', params);
    },
    [size]
  );

  // 최초 데이터 로드
  useEffect(() => {
    const fetchInitial = async () => {
      try {
        const res = await fetchRankings(0, null, null);
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
  }, [fetchRankings]);

  // 추가 데이터 로드
  const handleLoadMore = useCallback(async () => {
    if (isLoadingMore || isAllLoaded) return;

    setIsLoadingMore(true);
    try {
      const res = await fetchRankings(
        page,
        searchNameKeyword,
        searchCountryCode
      );
      setRankings((prev) => [...prev, ...res.data.data]);
      setPage((prev) => prev + 1);
      if (res.data.data.length < size) setIsAllLoaded(true);
    } catch (error) {
      console.error('추가 데이터 조회 실패', error);
    } finally {
      setIsLoadingMore(false);
    }
  }, [
    page,
    isLoadingMore,
    isAllLoaded,
    searchNameKeyword,
    searchCountryCode,
    fetchRankings,
  ]);

  // 검색 핸들러
  const handleSearch = useCallback(
    async (nameKeyword, countryCode) => {
      setIsInitialLoading(true);
      setSearchNameKeyword(nameKeyword);
      setSearchCountryCode(countryCode);
      setPage(0);
      setIsAllLoaded(false);

      try {
        const res = await fetchRankings(0, nameKeyword, countryCode);
        setRankings(res.data.data);
        setPage(1);
        if (res.data.data.length < size) setIsAllLoaded(true);
      } catch (error) {
        console.error('검색 실패', error);
        setRankings([]);
      } finally {
        setIsInitialLoading(false);
      }
    },
    [fetchRankings, size]
  );

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
    <div
      style={{
        width: '100%',
        boxSizing: 'border-box',
        overflowX: 'hidden',
        minHeight: '100%',
      }}
    >
      <RankingHeader lastUpdated={rankings[0]?.rankingLastUpdated} />
      <RankingSearch onSearch={handleSearch} />
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
