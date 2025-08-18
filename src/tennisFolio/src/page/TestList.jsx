import React from 'react';
import { useEffect, useState } from 'react';
import TestHeader from '../components/testList/TestHeader';
import TestItemList from '../components/testList/TestItemList';
import { apiRequest } from '../utils/apiClient';

function TestList() {
  const [testList, setTestList] = useState([]);
  useEffect(() => {
    apiRequest
      .get('/api/test')
      .then((res) => setTestList(res.data.data))
      .catch((err) => console.log(err));
  }, []);

  return (
    <div>
      <TestHeader />
      <TestItemList testList={testList} />
    </div>
  );
}

export default TestList;
