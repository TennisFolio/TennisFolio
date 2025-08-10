import React from 'react';
import { useEffect, useState } from 'react';
import TestHeader from '../components/testList/TestHeader';
import TestItemList from '../components/testList/TestItemList';
import axios from 'axios';
import { base_server_url } from '@/constants';

function TestList() {
  const [testList, setTestList] = useState([]);
  useEffect(() => {
    axios
      .get(`${base_server_url}/api/test`)
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
