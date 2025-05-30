import React from 'react'
import { useEffect,useState } from 'react';
import './testQuestion.css';
import { arrayShuffler } from '../../tools/tools';
function TestQuestion({setMode, currentTest, questionList, answerList, setAnswerList}) {

  const [currentIndex, setCurrentIndex] = useState(0);

  const handleAnswerSelect = (questionOrder, optionId) => {
    const newAnswers = [...answerList];
    newAnswers[questionOrder - 1] = optionId;
    setAnswerList(newAnswers);

    if (currentIndex < questionList.length) {
      setCurrentIndex(currentIndex + 1);
    }
  };

  useEffect(() => {
    if( currentIndex === questionList.length) {
      setMode('loading');
    }
  },[currentIndex, questionList.length, setMode]);

  const currentQuestion = questionList[currentIndex];

  return (
    <div className="question-form-container">
      <div className="question-card">
        <h3 className="question-title">
          Q{currentQuestion?.order}. {currentQuestion?.question}
        </h3>
        <div className="options-group">
          {questionList[currentIndex]?.testOption &&
            arrayShuffler(currentQuestion?.testOption)?.map((opt) => (
            <button
              key={opt.optionId}
              className={`option-button ${answerList[currentQuestion.order - 1] === opt.optionId ? 'selected' : ''}`}
              onClick={() => handleAnswerSelect(currentQuestion.order, opt.optionId)}
            >
              {opt.optionText}
            </button>
          ))}
        </div>
      </div>

      <p className="progress-indicator">
        {currentIndex + 1} / {questionList.length}
      </p>
    </div>
  )
}

export default TestQuestion