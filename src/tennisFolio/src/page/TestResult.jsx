import React from 'react'
import { useDispatch, useSelector } from 'react-redux';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useParams } from 'react-router-dom';
import TestResultRenderer from '../components/testResult/TestResultRenderer';
function TestResult() {
  const dispatch = useDispatch();
  const testResult = useSelector((state) => state.test.testResult);
  const navigate = useNavigate();
  const param = useParams();

  useEffect(() => { 
    console.log(`${param.category}`);
    console.log(param.query);
    if(param.query === undefined || param.query === null || param.query === ""){
      alert("잘못된 접근입니다.");
      navigate(`/test/${param.category}`);
      return;
    }
    if (!testResult) {
      alert("테스트를 먼저 완료해주세요.");
      navigate(`/test/${param.category}`);
    }
    // 여기에 결과를 처리하는 로직을 추가할 수 있습니다.
    // 예를 들어, 결과를 서버에 저장하거나 UI에 표시하는 등의 작업을 할 수 있습니다.
    console.log("Test Result:", testResult);
  },[])
  return (
    <TestResultRenderer renderResultInfo ={testResult}/>
  )
}

export default TestResult