import React from 'react';
import './racketResult.css';
import { base_url } from '@/constants';
function RacketResult({ renderResultInfo }) {
  return (
    <div className="racket-result-container">
      <h2 className="result-title">당신에게 어울리는 테니스 라켓은</h2>
      <div className="racket-card">
        <img
          src={`${base_url}${renderResultInfo.image}`}
          alt={renderResultInfo.modelName}
          className="racket-image"
        />
        <div className="racket-info">
          <p className="racket-name">{renderResultInfo.modelName}</p>
          <p className="racket-brand">{renderResultInfo.brand}</p>
          <p className="racket-description">{renderResultInfo.description}</p>
        </div>
      </div>
    </div>
  );
}

export default RacketResult;
