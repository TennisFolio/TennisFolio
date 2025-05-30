import React from 'react'
import './testIntro.css';

function TestIntro({currentTest, setMode}) {

   const handleStart = () => {
    setMode('question');
  };

  return (
    <div className="test-detail">
      <h1 className="test-title">{currentTest?.testCategoryName}</h1>
      <img src={currentTest?.image} alt={currentTest?.testCategoryName} className="test-image" />
      <p className="test-description">{currentTest?.description}</p>
      <button className="start-button" onClick={handleStart}>퀴즈 시작하기</button>
    </div>
  );
}

export default TestIntro