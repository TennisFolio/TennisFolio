import React, { useState, useEffect } from 'react';
import './RankingSearch.css';
import { apiRequest } from '../../utils/apiClient';

function RankingSearch({ onSearch }) {
  const [nameKeyword, setNameKeyword] = useState('');
  const [selectedCountry, setSelectedCountry] = useState('');
  const [countries, setCountries] = useState([]);
  const [isLoadingCountries, setIsLoadingCountries] = useState(true);

  // 나라 리스트 로드
  useEffect(() => {
    const fetchCountries = async () => {
      try {
        const res = await apiRequest.get(
          '/api/ranking/country',
          {},
          { showLoading: false }
        );
        setCountries(res.data.data || []);
      } catch (error) {
        console.error('나라 리스트 조회 실패:', error);
        setCountries([]);
      } finally {
        setIsLoadingCountries(false);
      }
    };
    fetchCountries();
  }, []);

  const handleNameChange = (e) => {
    setNameKeyword(e.target.value);
  };

  const handleCountryChange = (e) => {
    setSelectedCountry(e.target.value);
  };

  const handleSearch = () => {
    const name = nameKeyword.trim();
    const country = selectedCountry;

    // 두 조건을 모두 전달 (둘 다 있을 수도, 하나만 있을 수도 있음)
    onSearch(name || null, country || null);
  };

  const handleClear = () => {
    setNameKeyword('');
    setSelectedCountry('');
    onSearch(null, null);
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <div className="ranking-search-container">
      <div className="ranking-search-box">
        <div className="search-input-group">
          <label htmlFor="name-search">이름 검색</label>
          <input
            id="name-search"
            type="text"
            className="search-input"
            placeholder="선수 이름을 입력하세요"
            value={nameKeyword}
            onChange={handleNameChange}
            onKeyPress={handleKeyPress}
          />
        </div>

        <div className="search-select-group">
          <label htmlFor="country-search">나라 검색</label>
          <select
            id="country-search"
            className="search-select"
            value={selectedCountry}
            onChange={handleCountryChange}
            disabled={isLoadingCountries}
          >
            <option value="">전체</option>
            {countries.map((country) => (
              <option key={country.countryCode} value={country.countryCode}>
                {country.countryName}
              </option>
            ))}
          </select>
        </div>

        <div className="search-button-group">
          <button className="search-button" onClick={handleSearch}>
            검색
          </button>
          <button className="clear-button" onClick={handleClear}>
            초기화
          </button>
        </div>
      </div>
    </div>
  );
}

export default RankingSearch;
