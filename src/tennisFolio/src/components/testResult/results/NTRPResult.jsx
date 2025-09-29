import React from 'react';
import './ntrpResult.css';
import { base_url } from '@/constants';
function NTRPResult({ renderResultInfo }) {
  const { label, description, image } = renderResultInfo;
  return (
    <div className="ntrp-result-container">
      <h2 className="result-title">당신의 NTRP는?</h2>
      <div className="ntrp-card">
        <img src={`${base_url}${image}`} className="ntrp-image" />
        <div className="ntrp-info">
          <h3 className="ntrp-name">{label}</h3>
          <p className="ntrp-description">{description}</p>
        </div>
      </div>
    </div>
  );
}

export default NTRPResult;
