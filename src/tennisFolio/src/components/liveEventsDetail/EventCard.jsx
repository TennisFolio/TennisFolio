import React from 'react';
import Flag from 'react-world-flags';
import { base_image_url } from '@/constants';
import SmartImage from '../main/SmartImage';
import { getSetWinner } from '@/tools/tools';
function EventCard({ event }) {
  if (!event) return null;

  return (
    <div key={event.rapidId} className="eventCard">
      <div className="tournamentHeader">
        <div className="tournamentName">{event.tournamentName}</div>
        <div className="roundName">{event.roundName}</div>
      </div>
      <div className="eventHeader">
        <div className="teamBlock">
          <SmartImage
            base_url={base_image_url}
            imageName={event.homePlayer.playerImage}
            fallbackText={event.homePlayer.name}
          />
          <div className="teamName">
            {event?.homePlayer?.playerName}({event?.homePlayer?.playerRanking})
          </div>
        </div>

        <div className="setScore">
          {event?.homeScore?.point} : {event?.awayScore?.point}
        </div>

        <div className="teamBlock">
          <SmartImage
            base_url={base_image_url}
            imageName={event.awayPlayer.playerImage}
            fallbackText={event.awayPlayer.name}
          />
          <div className="teamName">
            {event?.awayPlayer?.playerName}({event?.awayPlayer?.playerRanking})
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
                      {event.homePlayer.playerName}
                    </span>
                    <Flag
                      code={event.homePlayer.playerCountryAlpha}
                      style={{
                        width: '24px',
                        height: '16px',
                        verticalAlign: 'middle',
                        marginLeft: '4px',
                      }}
                    />
                  </td>
                  {event.homeScore.periodScore
                    .slice(0, setsToShow)
                    .map((score, index) => {
                      const winner = getSetWinner(event, index, setsToShow);
                      return (
                        <td
                          key={index}
                          style={{
                            backgroundColor:
                              winner === 'home' ? '#43cea2' : 'transparent',
                            color: winner === 'home' ? 'white' : 'inherit',
                            fontWeight: winner === 'home' ? 'bold' : 'normal',
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
                      {event.awayPlayer.playerName}
                    </span>
                    <Flag
                      code={event.awayPlayer.playerCountryAlpha}
                      style={{
                        width: '24px',
                        height: '16px',
                        verticalAlign: 'middle',
                        marginLeft: '4px',
                      }}
                    />
                  </td>
                  {event.awayScore.periodScore
                    .slice(0, setsToShow)
                    .map((score, index) => {
                      const winner = getSetWinner(event, index, setsToShow);
                      return (
                        <td
                          key={index}
                          style={{
                            backgroundColor:
                              winner === 'away' ? '#43cea2' : 'transparent',
                            color: winner === 'away' ? 'white' : 'inherit',
                            fontWeight: winner === 'away' ? 'bold' : 'normal',
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
    </div>
  );
}

export default EventCard;
