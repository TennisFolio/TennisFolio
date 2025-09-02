import React from 'react';
import './testIntro.css';
import { base_url } from '@/constants';
function TestIntro({ currentTest, setMode }) {
  const { description, image, testCategoryName } = currentTest;
  const handleStart = () => {
    setMode('question');
  };

  return (
    <div className="test-detail">
      <h1 className="test-title">{testCategoryName}</h1>
      <img
        src={`${base_url}${image}`}
        alt={testCategoryName}
        className="test-image"
      />
      <p className="test-description">{description}</p>
      <button className="start-button" onClick={handleStart}>
        테스트 시작하기
      </button>
    </div>
  );
}

export default TestIntro;
