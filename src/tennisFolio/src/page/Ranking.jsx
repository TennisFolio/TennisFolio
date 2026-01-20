import React, { useState, useEffect, useRef, useCallback } from 'react';
import RankingTable from '../components/ranking/RankingTable';
import RankingHeader from '../components/ranking/RankingHeader';
import RankingSearch from '../components/ranking/RankingSearch';
import { apiRequest } from '../utils/apiClient';
import { RankingTableSkeleton } from './RankingSkeleton';
import { useParams } from 'react-router-dom';

import './ranking.css';

function Ranking() {
  const { category } = useParams();
  const [rankings, setRankings] = useState([]);
  const [page, setPage] = useState(0);
  const size = 40;
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isAllLoaded, setIsAllLoaded] = useState(false);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [hasError, setHasError] = useState(false);
  const [searchNameKeyword, setSearchNameKeyword] = useState(null);
  const [searchCountryCode, setSearchCountryCode] = useState(null);
  const observerTargetRef = useRef(null);

  // API 호출 함수
  const fetchRankings = useCallback(
    async (pageNum, nameKeyword, countryCode) => {
      const params = {
        page: pageNum,
        size,
        category: category?.toUpperCase() || 'ATP',
      };

      // 이름과 나라 조건을 모두 확인
      const hasName = nameKeyword && nameKeyword.trim();
      const hasCountry = countryCode && countryCode.trim();

      if (hasName) {
        params.name = nameKeyword.trim();
      }
      if (hasCountry) {
        params.country = countryCode.trim();
      }

      return await apiRequest.get('/api/ranking', params);
    },
    [size, category]
  );

  // 최초 데이터 로드 및 category 변경 시 재로드
  useEffect(() => {
    const fetchInitial = async () => {
      setIsInitialLoading(true);
      setRankings([]);
      setPage(0);
      setIsAllLoaded(false);
      setSearchNameKeyword(null);
      setSearchCountryCode(null);

      try {
        setHasError(false);
        const res = await fetchRankings(0, null, null);
        setRankings(res.data.data);
        setPage(1);
        if (res.data.data.length < size) setIsAllLoaded(true);
      } catch (error) {
        console.error('초기 데이터 조회 실패', error);
        setHasError(true);
        setIsAllLoaded(true); // 에러 발생 시 더 이상 로드하지 않도록 설정
      } finally {
        setIsInitialLoading(false);
      }
    };
    fetchInitial();
  }, [category, fetchRankings, size]);

  // 추가 데이터 로드
  const handleLoadMore = useCallback(async () => {
    if (isLoadingMore || isAllLoaded || hasError) return;

    setIsLoadingMore(true);
    try {
      setHasError(false);
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
      setHasError(true);
      setIsAllLoaded(true); // 에러 발생 시 더 이상 로드하지 않도록 설정
    } finally {
      setIsLoadingMore(false);
    }
  }, [
    page,
    isLoadingMore,
    isAllLoaded,
    hasError,
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
      setHasError(false);

      try {
        const res = await fetchRankings(0, nameKeyword, countryCode);
        setRankings(res.data.data);
        setPage(1);
        if (res.data.data.length < size) setIsAllLoaded(true);
      } catch (error) {
        console.error('검색 실패', error);
        setRankings([]);
        setHasError(true);
        setIsAllLoaded(true); // 에러 발생 시 더 이상 로드하지 않도록 설정
      } finally {
        setIsInitialLoading(false);
      }
    },
    [fetchRankings, size]
  );

  // Intersection Observer로 무한스크롤 구현
  useEffect(() => {
    if (isInitialLoading || isAllLoaded || hasError) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && !isLoadingMore && !hasError) {
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
  }, [isInitialLoading, isAllLoaded, isLoadingMore, hasError, handleLoadMore]);

  return (
    <div
      style={{
        width: '100%',
        boxSizing: 'border-box',
        overflowX: 'hidden',
        minHeight: '100%',
      }}
    >
      <RankingHeader
        lastUpdated={rankings[0]?.rankingLastUpdated}
        category={category}
      />
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
