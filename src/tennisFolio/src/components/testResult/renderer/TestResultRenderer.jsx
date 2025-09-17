import React from 'react';
import './testResultRenderer.css';
import PlayerResult from '../results/PlayerResult';
import StringResult from '../results/StringResult';
import RacketResult from '../results/RacketResult';
import NTRPResult from '../results/NTRPResult';
import { useSelector } from 'react-redux';

function TestResultRenderer({ renderResultInfo }) {
  const currentTest = useSelector((state) => state.test.currentTest);
  if (!currentTest) return <div>테스트 정보가 없습니다.</div>;

  if (currentTest.url === 'atpPlayer') {
    return <PlayerResult renderResultInfo={renderResultInfo} />;
  } else if (currentTest.url === 'string') {
    return <StringResult renderResultInfo={renderResultInfo} />;
  } else if (currentTest.url === 'racket') {
    return <RacketResult renderResultInfo={renderResultInfo} />;
  } else if (currentTest.url === 'ntrp') {
    return <NTRPResult renderResultInfo={renderResultInfo} />;
  }
}

export default TestResultRenderer;
