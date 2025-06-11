import React from 'react'
import {useEffect,useState} from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { base_server_url } from '../../App';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { setTestResult } from '../../store/testSlice';
import loadingAnimation from '../../assets/loading-animation.json';
import Lottie from 'lottie-react';

function TestLoading({answerList}) {
  
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { category } = useParams();

  const loadingTime = 3700; //ms

  
  useEffect(() => {
    const fetchResult = async () => {
      console.log("answerList : ", answerList);
      console.log("base_server_url : ", base_server_url);
      axios.post(`https://tennisfolio.net/api/test/${category}/result`, answerList)
            .then((res) => {
                if(res.data.code !== '0000'){
                    console.error('테스트 데이터 조회 실패', res);
                    return;
                }
                
                dispatch(setTestResult(res.data.data));                
            })
    }
    fetchResult();

  },[])

  const testResult = useSelector((state) => state.test.testResult);  
  useEffect(() => {
    
    if (!testResult) return;
    let timeout = setTimeout(() => {
      
      navigate(`/test/${category}/result/${testResult.query}`);
    }, loadingTime);
    return () => {
      clearTimeout(timeout);
    }
  },[testResult, loadingTime]);

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      height: '100vh'
    }}>
      <Lottie 
        animationData={loadingAnimation}
        loop={true}
        autoplay={true}
        style={{height: 250, width: 250}} />
    </div>
  )
}

export default TestLoading