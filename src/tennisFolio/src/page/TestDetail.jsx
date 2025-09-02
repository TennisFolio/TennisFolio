import React from 'react';
import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import IntroRenderer from '../components/testDetail/IntroRenderer';
import { clearTestResult } from '../store/testSlice';
import { setCurrentTest } from '../store/testSlice';

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

function TestDetail() {
  const { category } = useParams();
  const dispatch = useDispatch();
  const currentTest = useSelector((state) => state.test.currentTest);
  const navigate = useNavigate();

  const [questionList, setQuestionList] = useState({});
  useEffect(() => {
    // 기존 테스트 결과 redux 초기화
    dispatch(clearTestResult());

    const loadTestData = async () => {
      // 테스트 데이터 가져오기
      const testData = await getTestData(category);

      if (!testData) {
        alert('해당 테스트는 없습니다.');
        navigate('/test');
        return;
      }

      console.log('currentTest', currentTest);
      if (!currentTest || currentTest === null || currentTest === undefined) {
        const testInfo = {
          testCategoryId: `${category}-test`,
          url: testData.info.mainUrl,
          title: testData.info.mainTitle,
          testCategoryName: testData.info.mainTitle,
          description: testData.info.description,
          testType: testData.info.testType,
          testData: testData,
          image: testData.info.image,
        };
        dispatch(setCurrentTest(testInfo));
      }

      // JSON 파일에서 질문 데이터 설정 (구조 변환)
      const formattedQuestions = testData.questions.map((question, index) => ({
        order: index + 1,
        question: question.question,
        testOption: question.testOption.map((option, optIndex) => ({
          optionId: optIndex + 1,
          optionText: option.content,
          target: option.target,
        })),
      }));
      setQuestionList(formattedQuestions);
    };

    loadTestData();
  }, [category, currentTest, dispatch, navigate]);

  return (
    <div>
      <IntroRenderer currentTest={currentTest} questionList={questionList} />
    </div>
  );
}

export default TestDetail;
