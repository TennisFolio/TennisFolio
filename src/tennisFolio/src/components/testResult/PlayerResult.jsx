import React from 'react'
import './playerResult.css';
function PlayerResult({renderResultInfo}) {
  return (
        <div className="test-result-container">
      <h2 className="result-title">당신에게 어울리는 선수는</h2>
      <div className="player-card">
        <img
          src={renderResultInfo.image}
          alt={renderResultInfo.playerName}
          className="player-image"
        />
        <div className="player-info">
          <h3 className="player-kor-name">{renderResultInfo.name}</h3>
          <p className="player-eng-name">{renderResultInfo.player.playerName}</p>
          <p className="player-description">{renderResultInfo.description}</p>
        </div>
      </div>
    </div>
  )
}

export default PlayerResult