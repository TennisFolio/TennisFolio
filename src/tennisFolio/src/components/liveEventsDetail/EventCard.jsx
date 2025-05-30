import React from 'react'
import { useState } from 'react'
import { useLocation } from 'react-router-dom';
import Flag from 'react-world-flags';
function EventCard({event}) {
    const location = useLocation();
     if (!event) return null;
     
  return (
      <div key = {event.rapidId} className="eventCard">
              <div className="tournamentHeader">
                <div className="tournamentName">
                {event.tournamentName}
                </div>
                <div className="roundName">
                  {event.roundName}
                </div>
              </div>
              <div className="eventHeader">
                <div className="teamBlock">
                  <img src={event?.homePlayer?.playerImage} className="playerImg" />
                  <div className="teamName">{event?.homePlayer?.playerName}({event?.homePlayer?.playerRanking})</div>
                </div>

                <div className="setScore">{event?.homeScore?.current} : {event?.awayScore?.current}</div>

                <div className="teamBlock">
                  <img src={event?.awayPlayer?.playerImage} className="playerImg" />
                  <div className="teamName">{event?.awayPlayer?.playerName}({event?.awayPlayer?.playerRanking})</div>
                </div>
              </div>

              <div className="pointScore">{event?.homeScore?.point} : {event?.awayScore?.point}</div>

              <div className="eventTable">
                <table>
                  <thead>
                    <tr>
                      <th></th>
                      <th>SET1</th>
                      <th>SET2</th>
                      <th>SET3</th>
                      <th>SET4</th>
                      <th>SET5</th>
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
                                                  style={{ width: '24px', height: '16px', verticalAlign: 'middle', marginLeft: '4px' }}
                                                />
                                            </td>
                                            {event.homeScore.periodScore.map((score, index) => (
                                              <td key={index}>{score}</td>
                                            ))}
                                          </tr>
                                          <tr>
                                            <td>
                                              <span style={{ verticalAlign: 'middle' }}>
                                                  {event.awayPlayer.playerName}
                                                </span>
                                                <Flag
                                                  code={event.awayPlayer.playerCountryAlpha}
                                                  style={{ width: '24px', height: '16px', verticalAlign: 'middle', marginLeft: '4px' }}
                                                />
                                            </td>
                                            {event.awayScore.periodScore.map((score, index) => (
                                              <td key={index}>{score}</td>
                                            ))}
                    </tr>
                  </tbody>
                </table>
              </div>
              
         </div>
  )
}

export default EventCard