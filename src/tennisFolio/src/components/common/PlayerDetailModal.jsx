import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  closePlayerDetail,
  selectPlayerDetail,
  setPlayerData,
  setLoading,
} from '../../store/playerDetailSlice';
import { apiRequest } from '../../utils/apiClient';
import SmartImage from '../main/SmartImage';
import { base_image_url } from '@/constants';
import Flag from 'react-world-flags';
import './PlayerDetailModal.css';

function PlayerDetailModal() {
  const dispatch = useDispatch();
  const { isOpen, playerId, playerData, isLoading } =
    useSelector(selectPlayerDetail);

  // playerId가 변경되면 API 호출
  useEffect(() => {
    if (isOpen && playerId) {
      const fetchPlayerDetail = async () => {
        try {
          dispatch(setLoading(true));
          const response = await apiRequest.get(`/api/player/${playerId}`);
          dispatch(setPlayerData(response.data.data));
        } catch (error) {
          console.error('선수 상세 정보 조회 실패', error);
          dispatch(setLoading(false));
        }
      };

      fetchPlayerDetail();
    }
  }, [isOpen, playerId, dispatch]);

  // 생년월일 포맷팅 (20030505 -> 2003.05.05)
  const formatBirth = (birth) => {
    if (!birth || birth.length !== 8) return '-';
    return `${birth.substring(0, 4)}.${birth.substring(4, 6)}.${birth.substring(
      6,
      8
    )}`;
  };

  // 상금 포맷팅 (숫자 + 통화)
  const formatPrize = (amount, currency) => {
    if (amount === null || amount === undefined || amount === 0) return '-';
    const formattedAmount = Number(amount).toLocaleString();
    return `${formattedAmount} ${currency || ''}`;
  };

  // 팝업 닫기
  const handleClose = () => {
    dispatch(closePlayerDetail());
  };

  // 배경 클릭 시 닫기
  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      handleClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div className="player-detail-modal-backdrop" onClick={handleBackdropClick}>
      <div className="player-detail-modal">
        <div className="player-detail-modal-header">
          <h2>선수 상세 정보</h2>
          <button
            className="player-detail-modal-close"
            onClick={handleClose}
            aria-label="닫기"
          >
            ×
          </button>
        </div>
        <div className="player-detail-modal-content">
          {isLoading ? (
            <div className="player-detail-loading">로딩 중...</div>
          ) : playerData ? (
            <>
              {/* 상단: 사진 + 기본 정보 */}
              <div className="player-detail-top">
                <div className="player-detail-image">
                  <SmartImage
                    base_url={base_image_url}
                    imageName={playerData.image}
                    fallbackText={
                      playerData.playerNameKr || playerData.playerName
                    }
                    forceDisableMSW={true}
                  />
                </div>
                <div className="player-detail-basic-info">
                  <div className="player-detail-flag-container">
                    <Flag
                      code={playerData.countryCode}
                      className="player-flag"
                    />
                  </div>
                  <h3 className="player-detail-name">
                    {playerData.playerNameKr || playerData.playerName}
                  </h3>
                  <div className="player-detail-ranking">
                    <span className="player-detail-label">ATP 랭킹</span>
                    <span className="player-detail-value ranking-value">
                      {playerData.curRanking || '-'}위
                    </span>
                  </div>
                  <div className="player-detail-info-row">
                    <span className="player-detail-label">생년월일</span>
                    <span className="player-detail-value">
                      {formatBirth(playerData.birth)}
                    </span>
                  </div>
                  <div className="player-detail-info-row">
                    <span className="player-detail-label">키/체중</span>
                    <span className="player-detail-value">
                      {playerData.height ? `${playerData.height}cm` : '-'} /{' '}
                      {playerData.weight ? `${playerData.weight}kg` : '-'}
                    </span>
                  </div>
                </div>
              </div>

              {/* 하단: 테니스 관련 정보 */}
              <div className="player-detail-bottom">
                <div className="player-detail-bottom-left">
                  <div className="player-detail-info-row">
                    <span className="player-detail-label">플레이 스타일</span>
                    <span className="player-detail-value">
                      {playerData.plays || '-'}
                    </span>
                  </div>
                  <div className="player-detail-info-row">
                    <span className="player-detail-label">데뷔날짜</span>
                    <span className="player-detail-value">
                      {playerData.turnedPro ? `${playerData.turnedPro}년` : '-'}
                    </span>
                  </div>
                  <div className="player-detail-info-row">
                    <span className="player-detail-label">커리어하이</span>
                    <span className="player-detail-value">
                      {playerData.bestRank ? `${playerData.bestRank}위` : '-'}
                    </span>
                  </div>
                </div>
                <div className="player-detail-bottom-right">
                  <div className="player-detail-info-row">
                    <span className="player-detail-label">현재 상금</span>
                    <span className="player-detail-value">
                      {formatPrize(
                        playerData.prizeCurrentAmount,
                        playerData.prizeCurrentCurrency
                      )}
                    </span>
                  </div>
                  <div className="player-detail-info-row">
                    <span className="player-detail-label">총 상금</span>
                    <span className="player-detail-value">
                      {formatPrize(
                        playerData.prizeTotalAmount,
                        playerData.prizeTotalCurrency
                      )}
                    </span>
                  </div>
                </div>
              </div>
            </>
          ) : (
            <div className="player-detail-error">
              데이터를 불러올 수 없습니다.
            </div>
          )}
        </div>
        <div className="player-detail-modal-footer">
          <button
            className="player-detail-modal-close-btn"
            onClick={handleClose}
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  );
}

export default PlayerDetailModal;
