import React from 'react';
import './LiveEvents.css';
import { useNavigate } from 'react-router-dom';
import Flag from 'react-world-flags';
import { base_image_url } from '@/constants';
import SmartImage from './SmartImage';

function LiveEvents({ liveEvents }) {
  const navigate = useNavigate();

  const OnClickDetailButton = (event) => {
    navigate(`/liveEvents/${event.rapidId}`);
  };

  return (
    <div className="live-events">
      <div className="live-title">
        <div>
          {liveEvents && liveEvents.length === 0 ? (
            <>
              현재 진행 중인 경기가 없습니다.
              <br />
              다른 컨텐츠를 즐겨보세요!
            </>
          ) : (
            '지금 펼쳐지는 경기, 실시간으로 함께하세요!'
          )}
        </div>
      </div>

      {liveEvents && liveEvents.length === 0 && (
        <div className="alternative-content">
          <div className="content-card" onClick={() => navigate('/ranking')}>
            <div className="card-icon">🏆</div>
            <div className="card-content">
              <h3>실시간 랭킹</h3>
              <p>선수들의 최신 랭킹을 확인해보세요</p>
            </div>
          </div>

          <div className="content-card" onClick={() => navigate('/test')}>
            <div className="card-icon">🎾</div>
            <div className="card-content">
              <h3>테니스 테스트</h3>
              <p>나의 테니스 실력과 스타일을 알아보세요</p>
            </div>
          </div>

          {/* <div className="content-card" onClick={() => navigate('/live/atp')}>
            <div className="card-icon">📺</div>
            <div className="card-content">
              <h3>경기 일정</h3>
              <p>예정된 경기 일정을 미리 확인해보세요</p>
            </div>
          </div> */}
        </div>
      )}
      {liveEvents && liveEvents.length > 0 && (
        <div className="events-grid">
          {liveEvents.map((event) => (
            <div key={event.rapidId} className="eventCard">
              <div className="tournamentHeader">
                <div className="tournamentName">{event.tournamentName}</div>
                <div className="roundName">{event.roundName}</div>
              </div>

              <div className="eventHeader">
                <div className="teamBlock">
                  <SmartImage
                    base_url={base_image_url}
                    imageName={event.homePlayer?.playerImage}
                    fallbackText={event.homePlayer?.playerName}
                  />

                  <div className="teamName">
                    {event.homePlayer?.playerName}(
                    {event.homePlayer?.playerRanking})
                  </div>
                </div>

                <div className="setScore">
                  {event.homeScore?.point} : {event.awayScore?.point}
                </div>

                <div className="teamBlock">
                  <SmartImage
                    base_url={base_image_url}
                    imageName={event.awayPlayer?.playerImage}
                    fallbackText={event.awayPlayer?.playerName}
                  />
                  <div className="teamName">
                    {event.awayPlayer?.playerName}(
                    {event.awayPlayer?.playerRanking})
                  </div>
                </div>
              </div>

              <div className="eventTable">
                {(() => {
                  // 4세트 점수가 있는지 확인 (homeScore와 awayScore 모두 4세트가 0이 아니면 5세트 경기)
                  const isFiveSetMatch =
                    event.homeScore?.periodScore[3] !== 0 ||
                    event.awayScore?.periodScore[3] !== 0;

                  const setsToShow = isFiveSetMatch ? 5 : 3;

                  // 각 세트의 승자를 판단하는 함수
                  const getSetWinner = (setIndex) => {
                    const homeScore = event.homeScore?.periodScore[setIndex];
                    const awayScore = event.awayScore?.periodScore[setIndex];

                    // 해당 세트가 끝났는지 확인 (다음 세트에 점수가 있을 때만)
                    const isSetCompleted =
                      setIndex < setsToShow - 1
                        ? event.homeScore?.periodScore[setIndex + 1] !== 0 ||
                          event.awayScore?.periodScore[setIndex + 1] !== 0
                        : false; // 마지막 세트는 완료 여부를 알 수 없으므로 색칠하지 않음

                    if (
                      !isSetCompleted ||
                      (homeScore === 0 && awayScore === 0)
                    ) {
                      return null; // 세트가 아직 끝나지 않았거나 시작되지 않음
                    }

                    if (homeScore > awayScore) return 'home';
                    if (awayScore > homeScore) return 'away';
                    return null; // 동점 (일반적으로 발생하지 않음)
                  };

                  return (
                    <table>
                      <thead>
                        <tr>
                          <th></th>
                          {Array.from({ length: setsToShow }, (_, index) => (
                            <th key={index}>SET{index + 1}</th>
                          ))}
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td>
                            <span style={{ verticalAlign: 'middle' }}>
                              {event.homePlayer?.playerName}
                            </span>
                            <Flag
                              code={event.homePlayer?.playerCountryAlpha}
                              style={{
                                width: '24px',
                                height: '16px',
                                verticalAlign: 'middle',
                                marginLeft: '4px',
                              }}
                            />
                          </td>
                          {event.homeScore?.periodScore
                            .slice(0, setsToShow)
                            .map((score, index) => {
                              const winner = getSetWinner(index);
                              return (
                                <td
                                  key={index}
                                  style={{
                                    backgroundColor:
                                      winner === 'home'
                                        ? '#43cea2'
                                        : 'transparent',
                                    color:
                                      winner === 'home' ? 'white' : 'inherit',
                                    fontWeight:
                                      winner === 'home' ? 'bold' : 'normal',
                                  }}
                                >
                                  {score}
                                </td>
                              );
                            })}
                        </tr>
                        <tr>
                          <td>
                            <span style={{ verticalAlign: 'middle' }}>
                              {event.awayPlayer?.playerName}
                            </span>
                            <Flag
                              code={event.awayPlayer?.playerCountryAlpha}
                              style={{
                                width: '24px',
                                height: '16px',
                                verticalAlign: 'middle',
                                marginLeft: '4px',
                              }}
                            />
                          </td>
                          {event.awayScore?.periodScore
                            .slice(0, setsToShow)
                            .map((score, index) => {
                              const winner = getSetWinner(index);
                              return (
                                <td
                                  key={index}
                                  style={{
                                    backgroundColor:
                                      winner === 'away'
                                        ? '#43cea2'
                                        : 'transparent',
                                    color:
                                      winner === 'away' ? 'white' : 'inherit',
                                    fontWeight:
                                      winner === 'away' ? 'bold' : 'normal',
                                  }}
                                >
                                  {score}
                                </td>
                              );
                            })}
                        </tr>
                      </tbody>
                    </table>
                  );
                })()}
              </div>
              <button
                className="eventButton"
                onClick={() => OnClickDetailButton(event)}
              >
                채팅방 입장
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default LiveEvents;
