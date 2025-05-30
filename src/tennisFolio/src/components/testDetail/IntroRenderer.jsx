import React from 'react'
import { useState } from 'react';
import TestIntro from './TestIntro';
import TestLoading from './TestLoading';
import TestQuestion from './TestQuestion';

function IntroRenderer({currentTest, questionList}) {
    const [answerList, setAnswerList] = useState([]);
    const [mode, setMode] = useState('intro'); // intro, question, result
    if(mode ==="intro"){
        return <TestIntro currentTest ={currentTest} setMode={setMode}/>
    }else if(mode === "question"){
        return (<TestQuestion
                    setMode={setMode}
                    currentTest={currentTest}
                    questionList={questionList}
                    answerList={answerList}
                    setAnswerList={setAnswerList}/>
        )
    }else if(mode === 'loading'){
        return <TestLoading currentTest={currentTest} answerList={answerList}/>
    }else{
        return <div> 잘못된 페이지입니다!</div>
    }
}

export default IntroRenderer