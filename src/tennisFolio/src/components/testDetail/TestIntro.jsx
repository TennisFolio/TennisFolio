import React from 'react'
import './testIntro.css';
import { base_url } from '../../App';
function TestIntro({currentTest, setMode}) {

   const handleStart = () => {
    setMode('question');
  };

  return (
    <div className="test-detail">
      <h1 className="test-title">{currentTest?.testCategoryName}</h1>
      <img src={`${base_url}${currentTest?.image}`} alt={currentTest?.testCategoryName} className="test-image" />
      <p className="test-description">{currentTest?.description}</p>
      <button className="start-button" onClick={handleStart}>테스트 시작하기</button>
    </div>
  );
}

export default TestIntro