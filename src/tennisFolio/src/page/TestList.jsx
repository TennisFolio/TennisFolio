import React, { useState, useEffect } from 'react';
import TestHeader from '../components/testList/TestHeader';
import TestCard from '../components/testList/TestCard';
import '../components/testList/testCard.css';

function TestList() {
  const [testDataList, setTestDataList] = useState([]);

  useEffect(() => {
    const loadAllTests = async () => {
      const testCategories = ['racket', 'string', 'atpPlayer'];
      const loadedTests = [];

      for (const category of testCategories) {
        try {
          const testData = await import(
            `../assets/testAssets/${category}.json`
          );
          loadedTests.push(testData.default);
        } catch (error) {
          console.error(`Failed to load ${category} test data:`, error);
        }
      }

      setTestDataList(loadedTests);
    };

    loadAllTests();
  }, []);

  return (
    <div>
      <TestHeader />
      <div className="test-grid-container">
        {testDataList.map((testData, index) => (
          <TestCard key={testData.info.mainUrl} testData={testData} />
        ))}
      </div>
    </div>
  );
}

export default TestList;
