import React from 'react'
import { useState } from 'react';
import { useEffect } from 'react';
import axios from 'axios';
import RankingTable from '../components/ranking/RankingTable'; 
import RankingHeader from '../components/ranking/RankingHeader';
import { base_server_url } from '../App';
import './ranking.css';

function Ranking() {
  const [rankings, setRankings] = useState([]);
  const [visibleCount, setVisibleCount] = useState(20);
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isAllLoaded, setIsAllLoaded] = useState(false);
  const [isLive, setIsLive] = useState(false);
  useEffect(() => {
    const fetchInitial = async () =>{
      try{
        const res = await axios.get(`${base_server_url}/api/ranking`, {
          params:{
            type: 'init'
          }
          });

          if(res.data.code !== '0000'){
            console.error('초기 데이터 조회 실패', res.data.message);
            return;
          }
        setRankings(res.data.data);
      }catch(error){
        console.error('초기 데이터 조회 실패', error);
      }finally{
        setIsInitialLoading(false);
      }
    };
    
    fetchInitial();
  },[]);

  const handleLoadMore = async () => {
    try{
      const res = await axios.get(`${base_server_url}/api/ranking`, {
        params:{
          type: 'all'
        }
      });
      setRankings(res.data);
      setVisibleCount(res.data.length);
      setIsAllLoaded(true);
    }catch(error){
      console.error('전체 데이터 조회 실패', error);
    }
  };

  if(isInitialLoading){
    return <div>Loading...</div>;
  }
  return (
    <div>
      <RankingHeader lastUpdated = {rankings[0].rankingLastUpdated}/>
      {isLive && (
        <RankingTable rankings = {rankings}/>
      )}
      {!isLive && (
        <RankingTable rankings = {rankings}/>
      )}
      
      {!isAllLoaded && (
        <button className= "load-more-button" onClick={handleLoadMore}>더 보기</button>
      )}
    </div>
  )
}

export default Ranking