import React from 'react';
import { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { apiRequest } from '../utils/apiClient';

import IntroRenderer from '../components/testDetail/IntroRenderer';
import { clearTestResult } from '../store/testSlice';
import { setCurrentTest } from '../store/testSlice';

function TestDetail() {
  const { category } = useParams();
  const dispatch = useDispatch();
  const currentTest = useSelector((state) => state.test.currentTest);
  const navigate = useNavigate();

  const [questionList, setQuestionList] = useState({});
  useEffect(() => {
    // 기존 테스트 결과 redux 초기화
    dispatch(clearTestResult());

    if (!currentTest || currentTest === null || currentTest === undefined) {
      apiRequest
        .get(`/api/test/${category}`)
        .then((res) => {
          if (res.data.code !== '0000') {
            alert('해당 테스트는 없습니다.');
            navigate('/test');
          }
          dispatch(setCurrentTest(res.data.data));
        })
        .catch((err) => {
          alert('해당 테스트는 없습니다.');
          navigate('/test');
        });
    }

    const fetchQuestionData = async () => {
      apiRequest
        .get(`/api/test/${category}/questions`)
        .then((res) => {
          if (res.data.code !== '0000') {
            return;
          }
          setQuestionList(res.data.data);
        })
        .catch((err) => {});
    };
    fetchQuestionData();
  }, []);

  return (
    <div>
      <IntroRenderer currentTest={currentTest} questionList={questionList} />
    </div>
  );
}

export default TestDetail;
