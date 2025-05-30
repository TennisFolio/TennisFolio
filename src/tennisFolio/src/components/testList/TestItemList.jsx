import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { setCurrentTest } from '../../store/testSlice';
import './testItemList.css';

function TestItemList({testList}) {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  
  return (
    <div className="test-list-container">
      {testList.map((test) => (
        <div
          key={test.testCategoryId}
          className="test-item"
          onClick={() => {
            dispatch(setCurrentTest(test));
            navigate(`/test/${test.url}`)
          }}
        >
          {test.image && (
            <img
              src={test.image}
              alt={test.title}
              className="test-thumbnail"
            />
          )}
          <h3 className="test-title">{test.testCategoryName}</h3>
          <p className="test-description">{test.description}</p>
        </div>
      ))}
    </div>
  );
}

export default TestItemList;