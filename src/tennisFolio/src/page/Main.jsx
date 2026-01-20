import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { apiRequest } from '../utils/apiClient';
import { base_server_url, base_image_url } from '@/constants';
import { openPlayerDetail } from '../store/playerDetailSlice';
import SmartImage from '../components/main/SmartImage';
import Flag from 'react-world-flags';
import './Main.css';

function Main() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [liveEvents, setLiveEvents] = useState([]);
  const [rankings, setRankings] = useState([]);
  const [isLoadingLiveEvents, setIsLoadingLiveEvents] = useState(true);
  const [isLoadingRankings, setIsLoadingRankings] = useState(true);

  // 실시간 경기 데이터 가져오기
  useEffect(() => {
    const fetchLiveEvents = async () => {
      try {
        setIsLoadingLiveEvents(true);
        const response = await apiRequest.get(
          `${base_server_url}/api/liveEvents/summary`
        );
        if (response.data.code === '0000') {
          const data = response.data.data;
          // 배열인지 확인하고, 배열이 아니면 빈 배열로 설정
          if (Array.isArray(data)) {
            setLiveEvents(data);
          } else {
            console.warn('API 응답이 배열이 아닙니다:', data);
            setLiveEvents([]);
          }
        } else {
          setLiveEvents([]);
        }
      } catch (error) {
        console.error('실시간 경기 조회 실패:', error);
        setLiveEvents([]);
      } finally {
        setIsLoadingLiveEvents(false);
      }
    };

    fetchLiveEvents();
  }, []);

  // ATP 랭킹 TOP 10 가져오기
  useEffect(() => {
    const fetchRankings = async () => {
      try {
        setIsLoadingRankings(true);
        const response = await apiRequest.get('/api/ranking', {
          page: 0,
          size: 10,
          category: 'ATP',
        });
        if (response.data.code === '0000') {
          const data = response.data.data;
          // 배열인지 확인하고, 배열이 아니면 빈 배열로 설정
          if (Array.isArray(data)) {
            setRankings(data);
          } else {
            console.warn('API 응답이 배열이 아닙니다:', data);
            setRankings([]);
          }
        } else {
          setRankings([]);
        }
      } catch (error) {
        console.error('랭킹 조회 실패:', error);
        setRankings([]);
      } finally {
        setIsLoadingRankings(false);
      }
    };

    fetchRankings();
  }, []);

  // 경기 카드 클릭 핸들러
  const handleMatchClick = (matchId) => {
    if (matchId) {
      navigate(`/liveEvents/${matchId}`);
    }
  };

  // 선수 이름 클릭 핸들러
  const handlePlayerNameClick = (playerId) => {
    if (playerId) {
      dispatch(openPlayerDetail(playerId));
    }
  };

  return (
    <div className="main-page">
      <div className="main-container">
        {/* 실시간 경기 섹션 */}
        <div className="live-events-section">
          <div className="section-header">
            <h2>지금 펼쳐지는 경기</h2>
            <button
              className="more-button"
              onClick={() => navigate('/live/atp')}
            >
              더보기 →
            </button>
          </div>

          {isLoadingLiveEvents ? (
            <div className="loading-message">로딩 중...</div>
          ) : !Array.isArray(liveEvents) || liveEvents.length === 0 ? (
            <div className="empty-message">
              현재 진행 중인 경기가 없습니다.
              <br />
              다른 컨텐츠를 즐겨보세요!
            </div>
          ) : (
            <div className="matches-list">
              {Array.isArray(liveEvents) &&
                liveEvents.map((event, index) => {
                  const homeScore = event.homeScore || 0;
                  const awayScore = event.awayScore || 0;

                  // status가 "Xth set"이면 LIVE로 간주
                  const isLive = event.status?.toLowerCase().includes('set');

                  return (
                    <div
                      key={`${event.homePlayer?.playerRapidId}-${event.awayPlayer?.playerRapidId}-${index}`}
                      className="match-card"
                      onClick={() => handleMatchClick(event.rapidId)}
                    >
                      <div className="match-header">
                        <div className="match-header-left">
                          <div className="match-status">
                            {event.status || 'Not started'}
                          </div>
                        </div>
                        <div className="match-header-right">
                          {isLive && (
                            <div className="live-indicator">
                              <span className="live-dot"></span>
                              <span className="live-text">LIVE</span>
                            </div>
                          )}
                          {event.roundName}
                        </div>
                      </div>
                      <div className="match-players">
                        <div className="player">
                          <span
                            onClick={(e) => {
                              e.stopPropagation();
                              if (event.homePlayer?.playerId) {
                                handlePlayerNameClick(
                                  event.homePlayer.playerId
                                );
                              }
                            }}
                            style={{
                              cursor: event.homePlayer?.playerId
                                ? 'pointer'
                                : 'default',
                            }}
                          >
                            {event.homePlayer?.playerName || '미정'}
                          </span>
                          {event.homePlayer?.playerRanking && (
                            <span className="player-ranking">
                              {' '}
                              ({event.homePlayer.playerRanking})
                            </span>
                          )}
                        </div>
                        <div className="score">{homeScore}</div>
                        <div className="vs">vs</div>
                        <div className="score">{awayScore}</div>
                        <div className="player">
                          <span
                            onClick={(e) => {
                              e.stopPropagation();
                              if (event.awayPlayer?.playerId) {
                                handlePlayerNameClick(
                                  event.awayPlayer.playerId
                                );
                              }
                            }}
                            style={{
                              cursor: event.awayPlayer?.playerId
                                ? 'pointer'
                                : 'default',
                            }}
                          >
                            {event.awayPlayer?.playerName || '미정'}
                          </span>
                          {event.awayPlayer?.playerRanking && (
                            <span className="player-ranking">
                              {' '}
                              ({event.awayPlayer.playerRanking})
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })}
            </div>
          )}
        </div>

        {/* ATP 랭킹 TOP 10 섹션 */}
        <div className="ranking-section">
          <div className="section-header">
            <h2>ATP TOP 10</h2>
            <button
              className="more-button"
              onClick={() => navigate('/ranking/atp')}
            >
              전체 랭킹 →
            </button>
          </div>

          {isLoadingRankings ? (
            <div className="loading-message">로딩 중...</div>
          ) : !Array.isArray(rankings) || rankings.length === 0 ? (
            <div className="empty-message">랭킹 데이터가 없습니다.</div>
          ) : (
            <div className="ranking-list">
              {Array.isArray(rankings) &&
                rankings.map((rank, index) => {
                  const gapRankingDisplay =
                    rank.gapRanking !== 0
                      ? rank.gapRanking < 0
                        ? `▲ ${Math.abs(rank.gapRanking)}`
                        : `▼ ${rank.gapRanking}`
                      : '-';
                  const gapRankingClass =
                    rank.gapRanking !== 0
                      ? rank.gapRanking < 0
                        ? 'up'
                        : 'down'
                      : '';

                  return (
                    <div
                      key={index}
                      className="ranking-card"
                      onClick={() =>
                        handlePlayerNameClick(rank.player?.playerId)
                      }
                    >
                      <div className="ranking-number">{rank.curRanking}</div>
                      <div className="ranking-player-image">
                        <SmartImage
                          base_url={base_image_url}
                          imageName={rank.player?.image}
                          fallbackText={
                            rank.player?.playerNameKr || rank.player?.playerName
                          }
                          forceDisableMSW={true}
                          size="40px"
                        />
                      </div>
                      <div className="ranking-player-info">
                        <div className="ranking-player-name-wrapper">
                          <Flag
                            code={rank.player?.country}
                            className="ranking-player-flag"
                          />
                          <span className="ranking-player-name">
                            {rank.player?.playerNameKr ||
                              rank.player?.playerName}
                          </span>
                        </div>
                        <div className="ranking-player-meta">
                          <span className="ranking-points">
                            {rank.curPoints?.toLocaleString()} pts
                          </span>
                          <span className={`ranking-gap ${gapRankingClass}`}>
                            {gapRankingDisplay}
                          </span>
                        </div>
                      </div>
                    </div>
                  );
                })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Main;
