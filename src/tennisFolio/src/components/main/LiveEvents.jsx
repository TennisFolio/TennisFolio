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
        <div>지금 펼쳐지는 경기, 실시간으로 함께하세요!</div>
      </div>
      {liveEvents && liveEvents.length === 0 ? (
        <div className="no-events-message">현재 진행 중인 경기가 없습니다.</div>
      ) : (
        <div className="events-grid">
          {liveEvents &&
            liveEvents.map((event) => (
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
                              .map((score, index) => (
                                <td key={index}>{score}</td>
                              ))}
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
                              .map((score, index) => (
                                <td key={index}>{score}</td>
                              ))}
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
