import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { setCurrentTest } from '../../store/testSlice';
import './testCard.css';
import { base_url } from '@/constants';

function TestCard({ testData }) {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const handleStartTest = () => {
    const formattedTestData = {
      testCategoryId: `${testData.info.mainUrl}-test`,
      url: testData.info.mainUrl,
      title: testData.info.mainTitle,
      testCategoryName: testData.info.mainTitle,
      description: testData.info.description,
      testType: testData.info.testType,
      testData: testData,
      image: testData.info.image,
    };

    dispatch(setCurrentTest(formattedTestData));
    navigate(`/test/${testData.info.mainUrl}`);
  };

  // 질문 개수 계산
  const questionCount = testData.questions ? testData.questions.length : 0;

  return (
    <div className="racket-test-container">
      <div
        className="racket-test-card"
        onClick={handleStartTest}
        style={{
          backgroundImage: testData.info.image
            ? `url(${base_url}${testData.info.image})`
            : 'none',
        }}
      >
        <div className="racket-test-content">
          <h3 className="racket-test-title">{testData.info.mainTitle}</h3>
          <p className="racket-test-description">{testData.info.description}</p>
          <div className="racket-test-features">
            <span className="feature-tag">{questionCount}문항</span>
          </div>
        </div>
      </div>
    </div>
  );
}

export default TestCard;
