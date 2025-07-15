import React from 'react'
import { useDispatch, useSelector } from 'react-redux';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useParams } from 'react-router-dom';
import TestResultRenderer from '../components/testResult/TestResultRenderer';
import { setTestResult } from '../store/testSlice';
import { setCurrentTest } from '../store/testSlice';
import axios from 'axios';
import { base_server_url } from '../App';
import ShareButtonGroup from '../components/testResult/ShareButtonGroup';
import ResultButtonGroup from '../components/testResult/ResultButtonGroup';


function TestResult() {
  const dispatch = useDispatch();
  const testResult = useSelector((state) => state.test.testResult);
  const currentTest = useSelector((state) => state.test.currentTest);
  const navigate = useNavigate();
  const param = useParams();

  useEffect(() => { 

    if(param.query === undefined || param.query === null || param.query === ""){
      alert("잘못된 접근입니다!");
      navigate(`/test/${param.category}`);
      return;
    }
    if(!currentTest || currentTest === null || currentTest === undefined){
            axios.get(`${base_server_url}/api/test/${param.category}`)
            .then((res) => {
                if(res.data.code !== '0000'){
                    alert('해당 테스트는 없습니다.');
                    navigate('/test');
                    
                }
                dispatch(setCurrentTest(res.data.data));
            })
            .catch((err) => {
                alert('해당 테스트는 없습니다.');
                navigate('/test');
            })
        }

    if (!testResult) {
      const fetchResultData = async () => {
            axios.get(`${base_server_url}/api/test/${param.category}/result/${param.query}`)
            .then((res) => {
                if(res.data.code !== '0000'){
                  alert("해당 결과는 없습니다.");
                  return;
                }
                dispatch(setTestResult(res.data.data));  
            })
            .catch((err) => {
              console.error("Error fetching test result:", err);
              alert("테스트 결과를 불러오는 데 실패했습니다. 다시 시도해주세요.");
            })
        }
      fetchResultData();
    }

    
  },[])
  return (
    <>
    {testResult ? (
      <>
      <TestResultRenderer renderResultInfo={testResult} />
      <ShareButtonGroup />
      <ResultButtonGroup />
      </>
    ) : (
      <div style={{ textAlign: 'center', marginTop: '40px' }}>
        결과를 불러오는 중입니다...
      </div>
    )}
    
    </>
  )
}

export default TestResult