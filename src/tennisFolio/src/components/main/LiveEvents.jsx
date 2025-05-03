import React from 'react'
import './liveEvents.css';
function LiveEvents({liveEvents}) {
    console.log(liveEvents);
  return (
    <div className="live-events">
        <h1>Live Events</h1>
        {liveEvents.map((event) => (
            <div key = {event.rapidId} className="eventCard">
              <div className="tournamentInfo">
                {event.tournamentName} - {event.roundName}
              </div>
              <div className="eventHeader">
                <div className="teamBlock">
                  <img src={event.homePlayer.playerImage} className="playerImg" />
                  <div className="teamName">{event.homePlayer.playerName}({event.homePlayer.playerRanking})</div>
                </div>

                <div className="setScore">{event.homeScore.current} : {event.awayScore.current}</div>

                <div className="teamBlock">
                  <img src={event.awayPlayer.playerImage} className="playerImg" />
                  <div className="teamName">{event.awayPlayer.playerName}({event.awayPlayer.playerRanking})</div>
                </div>
              </div>

              <div className="pointScore">{event.homeScore.point} : {event.awayScore.point}</div>

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
                      <td>{event.homePlayer.playerName}</td>
                      {event.homeScore.periodScore.map((score, index) => (
                        <td key={index}>{score}</td>
                      ))}
                    </tr>
                    <tr>
                      <td>{event.awayPlayer.playerName}</td>
                      {event.awayScore.periodScore.map((score, index) => (
                        <td key={index}>{score}</td>
                      ))}
                    </tr>
                  </tbody>
                </table>
              </div>
         </div>
        ))}
    </div>
    
  )
}

export default LiveEvents