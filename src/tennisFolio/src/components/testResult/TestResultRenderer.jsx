import React from 'react'
import './testResultRenderer.css';
import PlayerResult from './PlayerResult';
import StringResult from './StringResult';
import RacketResult from './RacketResult';

import { useSelector, useDispatch  } from 'react-redux';

function TestResultRenderer({renderResultInfo}) {
    
    const currentTest = useSelector((state) => state.test.currentTest);
    if(!currentTest) return <div>테스트 정보가 없습니다.</div>;
    
    if(currentTest.url === "atpPlayer"){
      return <PlayerResult renderResultInfo={renderResultInfo} />
    }else if (currentTest.url === "string"){
      return <StringResult renderResultInfo={renderResultInfo} />
    }else if (currentTest.url === "racket"){
      return <RacketResult renderResultInfo={renderResultInfo} />
    }
}

export default TestResultRenderer