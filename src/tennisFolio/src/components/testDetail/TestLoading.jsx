import React from 'react';
import { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { setTestResult } from '../../store/testSlice';
import loadingAnimation from '../../assets/loading-animation.json';
import Lottie from 'lottie-react';
import { calculateTestResult } from '../../utils/testCalculator.js';
import { calculateNTRPResult } from '../../utils/NTRPCalculator.js';

// 테스트 데이터를 동적으로 가져오는 함수
const getTestData = async (category) => {
  try {
    const testData = await import(`../../assets/testAssets/${category}.json`);
    return testData.default;
  } catch (error) {
    console.error(`Failed to load test data for category: ${category}`, error);
    return null;
  }
};

function TestLoading({ answerList }) {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { category } = useParams();

  const loadingTime = 1000; //ms

  useEffect(() => {
    const loadAndCalculateResult = async () => {
      const testData = await getTestData(category);

      if (!testData) {
        console.error(`테스트 데이터를 찾을 수 없습니다: ${category}`);
        navigate('/test');
        return;
      }

      const result =
        category === 'ntrp'
          ? calculateNTRPResult(answerList, testData)
          : calculateTestResult(answerList, testData);
      dispatch(setTestResult(result));
    };

    loadAndCalculateResult();
  }, [answerList, category, dispatch, navigate]);

  const testResult = useSelector((state) => state.test.testResult);
  useEffect(() => {
    if (!testResult) return;
    let timeout = setTimeout(() => {
      navigate(`/test/${category}/result/${testResult.query}`);
    }, loadingTime);
    return () => {
      clearTimeout(timeout);
    };
  }, [testResult, loadingTime, category, navigate]);

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
      }}
    >
      <Lottie
        animationData={loadingAnimation}
        loop={true}
        autoplay={true}
        style={{
          width: 120,
          height: 120,
          marginBottom: '1rem',
          filter: 'sepia(1) hue-rotate(40deg) saturate(1.8) brightness(1.2)',
        }}
      />
    </div>
  );
}

export default TestLoading;
