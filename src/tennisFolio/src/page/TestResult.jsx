import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useParams } from 'react-router-dom';
import TestResultRenderer from '../components/testResult/renderer/TestResultRenderer';
import { setTestResult } from '../store/testSlice';
import { setCurrentTest } from '../store/testSlice';
import { calculateTestResult } from '../utils/testCalculator';

import ShareButtonGroup from '../components/testResult/buttons/ShareButtonGroup';
import ResultButtonGroup from '../components/testResult/buttons/ResultButtonGroup';

// 테스트 데이터를 동적으로 가져오는 함수
const getTestData = async (category) => {
  try {
    const testData = await import(`../assets/testAssets/${category}.json`);
    return testData.default;
  } catch (error) {
    console.error(`Failed to load test data for category: ${category}`, error);
    return null;
  }
};

function TestResult() {
  const dispatch = useDispatch();
  const testResult = useSelector((state) => state.test.testResult);
  const currentTest = useSelector((state) => state.test.currentTest);
  const answerList = useSelector((state) => state.test.answerList);
  const navigate = useNavigate();
  const param = useParams();

  useEffect(() => {
    if (
      param.query === undefined ||
      param.query === null ||
      param.query === ''
    ) {
      alert('잘못된 접근입니다.');
      navigate(`/test/${param.category}`);
      return;
    }

    const loadTestData = async () => {
      // 테스트 데이터 가져오기
      const testData = await getTestData(param.category);

      if (!testData) {
        alert('해당 테스트는 없습니다.');
        navigate('/test');
        return;
      }

      // currentTest가 없으면 설정
      if (!currentTest || currentTest === null || currentTest === undefined) {
        const testInfo = {
          testCategoryId: `${param.category}-test`,
          url: testData.info.mainUrl,
          title: testData.info.mainTitle,
          testCategoryName: testData.info.mainTitle,
          description: testData.info.description,
          testType: testData.info.testType,
          testData: testData,
        };
        dispatch(setCurrentTest(testInfo));
      }

      // testResult가 없으면 로컬에서 계산
      if (!testResult && answerList && answerList.length > 0) {
        const calculatedResult = calculateTestResult(answerList, testData);
        dispatch(setTestResult(calculatedResult));
      }
    };

    loadTestData();
  }, [
    answerList,
    currentTest,
    dispatch,
    navigate,
    param.category,
    param.query,
    testResult,
  ]);
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
  );
}

export default TestResult;
