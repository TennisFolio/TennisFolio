import React from 'react';
import './stringResult.css';
import { base_url } from '@/constants';
function StringResult({ renderResultInfo }) {
  return (
    <div className="string-result-container">
      <h2 className="result-title">당신에게 어울리는 테니스 스트링은</h2>
      <div className="string-card">
        <img
          src={`${base_url}${renderResultInfo.image}`}
          alt={renderResultInfo.stringName}
          className="string-image"
        />
        <div className="string-info">
          <h3 className="string-name">{renderResultInfo.name}</h3>
          <p className="string-type">타입: {renderResultInfo.stringType}</p>
          <p className="string-description">{renderResultInfo.description}</p>
        </div>
      </div>
    </div>
  );
}

export default StringResult;
