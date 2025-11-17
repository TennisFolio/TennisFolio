import { useState, useEffect, useMemo, Fragment } from 'react';
import Calendar from 'react-calendar';
import 'react-calendar/dist/Calendar.css';
import './Schedule.css';
import { apiRequest } from '../utils/apiClient';
import { base_server_url } from '@/constants';

// 임시 목 데이터 - 전체 대회 목록
const mockTournamentsResponse = {
  code: '0000',
  message: '성공',
  data: [
    {
      categoryId: 53,
      categoryName: 'ATP',
      tournamentId: 107,
      tournamentName: 'Almaty',
      seasonId: 32002,
      seasonName: 'ATP Almaty, Kazakhstan Men Singles 2025',
      year: '2025',
      startTimestamp: '20251011',
      endTimestamp: '20251020',
    },
    {
      categoryId: 53,
      categoryName: 'ATP',
      tournamentId: 108,
      tournamentName: 'Stockholm',
      seasonId: 32003,
      seasonName: 'ATP Stockholm, Sweden Men Singles 2025',
      year: '2025',
      startTimestamp: '20251011',
      endTimestamp: '20251019',
    },
    {
      categoryId: 62,
      categoryName: 'WTA',
      tournamentId: 2579,
      tournamentName: 'Ningbo, China',
      seasonId: 32004,
      seasonName: 'WTA Ningbo, China Women Singles 2025',
      year: '2025',
      startTimestamp: '20251011',
      endTimestamp: '20251019',
    },
    {
      categoryId: 62,
      categoryName: 'WTA',
      tournamentId: 2578,
      tournamentName: 'Osaka',
      seasonId: 32005,
      seasonName: 'WTA Osaka, Japan Women Singles 2025',
      year: '2025',
      startTimestamp: '20251012',
      endTimestamp: '20251019',
    },
    {
      categoryId: 61,
      categoryName: 'Exhibition',
      tournamentId: 8002,
      tournamentName: 'Six Kings Slam',
      seasonId: 35003,
      seasonName: 'Riyadh Exhibition 2025',
      year: '2025',
      startTimestamp: '20251015',
      endTimestamp: '20251018',
    },
  ],
};

// 임시 목 데이터 - 특정 날짜의 경기 목록
const mockMatchesResponse = {
  20251013: {
    code: '0000',
    message: '성공',
    data: [
      {
        matchId: 29003,
        rapidMatchId: '14875097',
        homeScore: 0,
        awayScore: 0,
        homePlayerId: 14709,
        homePlayerName: 'Bernard Tomic',
        homePlayerNameKr: '버나드 토믹',
        awayPlayerId: 14544,
        awayPlayerName: 'Corentin Moutet',
        awayPlayerNameKr: '코렌틴 무테',
        status: 'Not started',
        startTimestamp: '20251013230000',
        winner: null,
      },
      {
        matchId: 29004,
        rapidMatchId: '14871772',
        homeScore: 0,
        awayScore: 0,
        homePlayerId: 14548,
        homePlayerName: 'Miomir Kecmanovic',
        homePlayerNameKr: '미오미르 케크마노비치',
        awayPlayerId: 14538,
        awayPlayerName: 'Alexandre Muller',
        awayPlayerNameKr: '알렉상드르 뮐러',
        status: 'Not started',
        startTimestamp: '20251013200000',
        winner: null,
      },
      {
        matchId: 29005,
        rapidMatchId: '14871770',
        homeScore: 0,
        awayScore: 0,
        homePlayerId: 14529,
        homePlayerName: 'Tallon Griekspoor',
        homePlayerNameKr: '탈론 그릭스푸르',
        awayPlayerId: 14553,
        awayPlayerName: 'Jacob Fearnley',
        awayPlayerNameKr: '제이콥 펀리',
        status: 'Not started',
        startTimestamp: '20251013211000',
        winner: null,
      },
    ],
  },
  20251015: {
    code: '0000',
    message: '성공',
    data: [
      {
        matchId: 29012,
        rapidMatchId: '14871733',
        homeScore: 0,
        awayScore: 0,
        homePlayerId: 15334,
        homePlayerName: 'Xiyu Wang',
        homePlayerNameKr: null,
        awayPlayerId: 15306,
        awayPlayerName: 'Diana Shnaider',
        awayPlayerNameKr: null,
        status: 'Not started',
        startTimestamp: '20251015203000',
        winner: null,
      },
    ],
  },
};

// YYYYMMDD 형식의 문자열을 Date 객체로 변환
const parseTimestamp = (timestamp) => {
  const year = parseInt(timestamp.substring(0, 4));
  const month = parseInt(timestamp.substring(4, 6)) - 1; // 월은 0부터 시작
  const day = parseInt(timestamp.substring(6, 8));
  return new Date(year, month, day);
};

// Date 객체를 YYYYMMDD 형식으로 변환
const formatDateToTimestamp = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}${month}${day}`;
};

// Date 객체를 YYYYMM 형식으로 변환 (월별 조회용)
const formatDateToYearMonth = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  return `${year}${month}`;
};

const renderPlayerName = (nameKr, name) => {
  const source = (nameKr || name || '').trim();

  if (!source) {
    return '미정';
  }

  const parts = source
    .split('/')
    .map((part) => part.trim())
    .filter((part) => part.length > 0);

  if (parts.length <= 1) {
    return parts[0];
  }

  return parts.map((part, index) => (
    <Fragment key={`${part}-${index}`}>
      {part}
      {index !== parts.length - 1 && <br />}
    </Fragment>
  ));
};

// YYYYMMDDHHMMSS 형식을 HH:MM으로 변환
const formatTimeFromTimestamp = (timestamp) => {
  if (!timestamp || timestamp.length < 12) return '';
  const hour = timestamp.substring(8, 10);
  const minute = timestamp.substring(10, 12);
  return `${hour}:${minute}`;
};

// 대회 색상 배열 (명확하게 구분되는 색상)
const tournamentColors = [
  '#FF6B6B', // 빨강
  '#4ECDC4', // 청록
  '#FFD93D', // 노랑
  '#6BCF7F', // 초록
  '#A8E6CF', // 민트
  '#FF8B94', // 연분홍
  '#C7CEEA', // 연보라
  '#FFDAC1', // 피치
  '#B4A7D6', // 라벤더
  '#95E1D3', // 민트그린
  '#F38181', // 코랄
  '#AA96DA', // 퍼플
];

function Schedule() {
  const [selectedDate, setSelectedDate] = useState(new Date()); // 초기에는 오늘 날짜 선택
  const [activeStartDate, setActiveStartDate] = useState(new Date()); // 현재 보고 있는 달력의 월
  const [tournaments, setTournaments] = useState([]);
  const [matches, setMatches] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState(null); // 선택된 카테고리
  const [selectedSeasonId, setSelectedSeasonId] = useState(null); // 선택된 대회

  // 첫 번째 카테고리를 기본 선택으로 설정
  useEffect(() => {
    if (tournaments.length > 0 && selectedCategory === null) {
      const categories = [...new Set(tournaments.map((t) => t.categoryName))];
      if (categories.length > 0) {
        setSelectedCategory(categories[0]);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tournaments]);

  // 월별 대회 목록 로드
  useEffect(() => {
    const fetchTournaments = async () => {
      try {
        const yearMonth = formatDateToYearMonth(activeStartDate);
        const response = await apiRequest.get(
          `${base_server_url}/api/calendar?month=${yearMonth}`
        );

        if (response.data.code === '0000') {
          const tournamentsData = response.data.data.map((tournament) => ({
            ...tournament,
            startDate: parseTimestamp(tournament.startTimestamp),
            endDate: parseTimestamp(tournament.endTimestamp),
          }));
          setTournaments(tournamentsData);
        }
      } catch (error) {
        console.error('대회 목록 조회 실패:', error);
        // 에러 시 목 데이터 사용
        const tournamentsData = mockTournamentsResponse.data.map(
          (tournament) => ({
            ...tournament,
            startDate: parseTimestamp(tournament.startTimestamp),
            endDate: parseTimestamp(tournament.endTimestamp),
          })
        );
        setTournaments(tournamentsData);
      }
    };

    fetchTournaments();
  }, [activeStartDate]);

  // 월이 변경될 때 처리
  useEffect(() => {
    // 선택된 날짜의 달과 현재 보고 있는 달 비교
    const selectedMonth = selectedDate.getMonth();
    const selectedYear = selectedDate.getFullYear();
    const activeMonth = activeStartDate.getMonth();
    const activeYear = activeStartDate.getFullYear();

    // 선택된 날짜의 달과 현재 달이 같으면 아무것도 안 함 (날짜 클릭)
    if (selectedYear === activeYear && selectedMonth === activeMonth) {
      return;
    }

    // 다르면 화살표 클릭 - 경기 목록 초기화하고 1일 선택
    setMatches([]);

    const today = new Date();
    const isCurrentMonth =
      activeYear === today.getFullYear() && activeMonth === today.getMonth();

    if (isCurrentMonth) {
      // 현재 달이면 오늘 날짜 선택
      setSelectedDate(today);
    } else {
      // 다른 달이면 해당 달의 1일 선택
      const firstDayOfMonth = new Date(activeYear, activeMonth, 1);
      setSelectedDate(firstDayOfMonth);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeStartDate]);

  // 날짜가 특정 대회 기간에 속하는지 확인 (필터링 적용)
  const getTournamentsForDate = (date) => {
    let filtered = tournaments.filter(
      (tournament) => date >= tournament.startDate && date <= tournament.endDate
    );

    // 카테고리 필터링
    if (selectedCategory) {
      filtered = filtered.filter(
        (tournament) => tournament.categoryName === selectedCategory
      );
    }

    // 대회 필터링 (특정 대회만 보여주기)
    if (selectedSeasonId) {
      filtered = filtered.filter(
        (tournament) => tournament.seasonId === selectedSeasonId
      );
    }

    return filtered;
  };

  // 카테고리별로 대회 그룹화 (메모이제이션)
  const tournamentsByCategory = useMemo(() => {
    const grouped = {};
    if (!tournaments || tournaments.length === 0) {
      return grouped;
    }

    tournaments.forEach((tournament) => {
      if (tournament && tournament.categoryName) {
        if (!grouped[tournament.categoryName]) {
          grouped[tournament.categoryName] = [];
        }
        grouped[tournament.categoryName].push(tournament);
      }
    });
    return grouped;
  }, [tournaments]);

  // 현재 선택된 카테고리의 categoryId (메모이제이션)
  const selectedCategoryId = useMemo(() => {
    if (!selectedCategory || !tournamentsByCategory[selectedCategory]) {
      return null;
    }
    // 같은 카테고리의 첫 번째 토너먼트에서 categoryId 가져오기
    const firstTournament = tournamentsByCategory[selectedCategory][0];
    return firstTournament?.categoryId || null;
  }, [selectedCategory, tournamentsByCategory]);

  // 대회 ID와 색상 매핑 (같은 달에 색상 겹침 방지)
  const [tournamentColorMap, setTournamentColorMap] = useState({});

  // 대회 목록이 변경될 때 색상 재할당
  useEffect(() => {
    if (tournaments.length === 0) return;

    const newColorMap = {};
    let colorIndex = 0;

    // 대회 ID 순으로 정렬
    const sortedTournaments = [...tournaments].sort((a, b) => {
      const aKey = a.seasonId ?? a.tournamentId;
      const bKey = b.seasonId ?? b.tournamentId;
      return aKey - bKey;
    });

    sortedTournaments.forEach((tournament) => {
      const mapKey = tournament.seasonId ?? tournament.tournamentId;
      if (!newColorMap[mapKey]) {
        newColorMap[mapKey] =
          tournamentColors[colorIndex % tournamentColors.length];
        colorIndex += 1;
      }
    });

    setTournamentColorMap(newColorMap);
  }, [tournaments]);

  // 대회 ID로 색상 할당
  const getTournamentColor = (key) => {
    return tournamentColorMap[key] || tournamentColors[0];
  };

  // 타일에 클래스명 추가 (대회 기간 표시)
  const tileClassName = ({ date, view }) => {
    if (view !== 'month') return null;

    const dateTournaments = getTournamentsForDate(date);
    if (dateTournaments.length === 0) return null;

    return 'tournament-date';
  };

  // 타일 내용 (대회 선 표시)
  const tileContent = ({ date, view }) => {
    if (view !== 'month') return null;

    const dateTournaments = getTournamentsForDate(date);
    if (dateTournaments.length === 0) return null;

    const uniqueTournaments = Array.from(
      new Map(
        dateTournaments.map((tournament) => [
          tournament.seasonId ?? tournament.tournamentId,
          tournament,
        ])
      ).values()
    );

    return (
      <div className="tournament-lines">
        {uniqueTournaments
          .sort((a, b) => a.startTimestamp.localeCompare(b.startTimestamp))
          .map((tournament, index) => {
            const isStart =
              date.toDateString() === tournament.startDate.toDateString();
            const isEnd =
              date.toDateString() === tournament.endDate.toDateString();
            const isMiddle =
              date > tournament.startDate && date < tournament.endDate;

            let lineClass = 'tournament-line';
            if (isStart && isEnd) {
              // 하루짜리 대회
              lineClass += ' tournament-line-single';
            } else if (isStart) {
              lineClass += ' tournament-line-start';
            } else if (isEnd) {
              lineClass += ' tournament-line-end';
            } else if (isMiddle) {
              lineClass += ' tournament-line-middle';
            }

            return (
              <div
                key={`${
                  tournament.seasonId ?? tournament.tournamentId
                }-${index}`}
                className={lineClass}
                style={{
                  backgroundColor: getTournamentColor(
                    tournament.seasonId ?? tournament.tournamentId
                  ),
                }}
                title={tournament.seasonName}
              />
            );
          })}
      </div>
    );
  };

  // 날짜 클릭 핸들러
  const handleDateClick = async (date) => {
    setSelectedDate(date);

    // 선택된 날짜의 경기 목록 가져오기
    try {
      const dateKey = formatDateToTimestamp(date);
      let apiUrl = `${base_server_url}/api/calendar/detail?date=${dateKey}`;

      // seasonId가 선택되어 있으면 함께 전송
      if (selectedSeasonId) {
        apiUrl += `&seasonId=${selectedSeasonId}`;
      } else if (selectedCategoryId) {
        // seasonId가 선택되지 않았고, 선택된 카테고리가 있으면 categoryId 전송
        apiUrl += `&categoryId=${selectedCategoryId}`;
      }

      const response = await apiRequest.get(apiUrl);

      if (response.data.code === '0000') {
        setMatches(response.data.data);
      } else {
        setMatches([]);
      }
    } catch (error) {
      console.error('경기 목록 조회 실패:', error);
      // 에러 시 목 데이터 사용
      const dateKey = formatDateToTimestamp(date);
      const matchesData = mockMatchesResponse[dateKey];
      if (matchesData && matchesData.code === '0000') {
        setMatches(matchesData.data);
      } else {
        setMatches([]);
      }
    }
  };

  // 선택된 날짜가 변경될 때 경기 목록 가져오기
  useEffect(() => {
    if (!selectedDate || !selectedCategory) return;

    const fetchMatchesForDate = async () => {
      try {
        const dateKey = formatDateToTimestamp(selectedDate);
        let apiUrl = `${base_server_url}/api/calendar/detail?date=${dateKey}`;

        // seasonId가 선택되어 있으면 함께 전송
        if (selectedSeasonId) {
          apiUrl += `&seasonId=${selectedSeasonId}`;
        } else if (selectedCategoryId) {
          // seasonId가 선택되지 않았고, 선택된 카테고리가 있으면 categoryId 전송
          apiUrl += `&categoryId=${selectedCategoryId}`;
        }

        const response = await apiRequest.get(apiUrl);

        if (response.data.code === '0000') {
          setMatches(response.data.data);
        }
      } catch (error) {
        console.error('경기 목록 조회 실패:', error);
        // 에러 시 목 데이터 사용
        const dateKey = formatDateToTimestamp(selectedDate);
        const matchesData = mockMatchesResponse[dateKey];
        if (matchesData && matchesData.code === '0000') {
          setMatches(matchesData.data);
        }
      }
    };

    fetchMatchesForDate();
  }, [selectedDate, selectedSeasonId, selectedCategory, selectedCategoryId]);

  // 달력의 월이 변경될 때 호출
  const handleActiveStartDateChange = ({ activeStartDate }) => {
    setActiveStartDate(activeStartDate);
  };

  return (
    <div className="schedule-page">
      {/* 필터 영역 */}
      <div className="filter-section">
        <div className="category-tabs">
          {Object.keys(tournamentsByCategory).map((category) => (
            <button
              key={category}
              className={`category-tab ${
                selectedCategory === category ? 'active' : ''
              }`}
              onClick={() => {
                // 이미 선택된 카테고리를 다시 클릭하면 해제
                if (selectedCategory === category) {
                  const categories = Object.keys(tournamentsByCategory);
                  if (categories.length > 0) {
                    setSelectedCategory(categories[0]);
                  }
                } else {
                  setSelectedCategory(category);
                  setSelectedSeasonId(null);
                }
              }}
            >
              {category}
            </button>
          ))}
        </div>

        {/* 선택된 카테고리의 대회 목록 */}
        {selectedCategory && tournamentsByCategory[selectedCategory] && (
          <div className="tournament-list">
            {tournamentsByCategory[selectedCategory].map((tournament) => (
              <button
                key={tournament.seasonId}
                className={`tournament-item ${
                  selectedSeasonId === tournament.seasonId ? 'active' : ''
                }`}
                onClick={() => {
                  // 이미 선택된 아이템을 다시 클릭하면 해제
                  if (selectedSeasonId === tournament.seasonId) {
                    setSelectedSeasonId(null);
                  } else {
                    setSelectedSeasonId(tournament.seasonId);
                  }
                }}
              >
                {tournament.seasonName}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* 달력 영역 */}
      <div className="calendar-section">
        <Calendar
          value={selectedDate}
          onClickDay={handleDateClick}
          tileClassName={tileClassName}
          tileContent={tileContent}
          locale="ko-KR"
          formatDay={(locale, date) => date.getDate().toString()}
          onActiveStartDateChange={handleActiveStartDateChange}
          calendarType="gregory"
        />
      </div>

      {/* 일정 상세 영역 */}
      <div className="schedule-detail-section">
        {selectedDate ? (
          <>
            <div className="detail-header">
              <h2>
                {selectedDate.toLocaleDateString('ko-KR', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                })}
              </h2>
            </div>

            <div className="matches-list">
              {matches.length > 0 ? (
                (() => {
                  // seasonId로 경기 그룹화
                  const groupedMatches = matches.reduce((acc, match) => {
                    const seasonId = match.seasonId;
                    if (!acc[seasonId]) {
                      acc[seasonId] = [];
                    }
                    acc[seasonId].push(match);
                    return acc;
                  }, {});

                  return Object.entries(groupedMatches).map(
                    ([seasonId, seasonMatches]) => {
                      // 해당 seasonId의 대회 찾기
                      const tournament = tournaments.find(
                        (t) => t.seasonId === parseInt(seasonId)
                      );
                      const colorKey = tournament
                        ? tournament.seasonId ?? tournament.tournamentId
                        : null;
                      const tournamentColor = colorKey
                        ? getTournamentColor(colorKey)
                        : '#667eea';
                      return (
                        <div key={seasonId} className="season-group">
                          <div
                            className="tournament-info"
                            style={{
                              background: tournamentColor,
                            }}
                          >
                            <h3>{tournament?.seasonName || '대회 정보'}</h3>
                          </div>
                          {seasonMatches.map((match) => {
                            const winner =
                              match.winner === '1'
                                ? 'home'
                                : match.winner === '2'
                                ? 'away'
                                : null;
                            return (
                              <div key={match.matchId} className="match-card">
                                <div className="match-header">
                                  <div className="match-header-left">
                                    <div className="match-time">
                                      {formatTimeFromTimestamp(
                                        match.startTimestamp
                                      )}
                                    </div>
                                    <div className="match-status">
                                      {match.status}
                                    </div>
                                  </div>
                                  <div className="match-header-right">
                                    {match.roundNameKr}
                                  </div>
                                </div>
                                <div className="match-players">
                                  <div
                                    className={`player ${
                                      winner === 'home' ? 'winner' : ''
                                    }`}
                                  >
                                    {winner === 'home' && (
                                      <img
                                        src="/images/ico_winner.png"
                                        className="winner-icon home"
                                        alt="winner"
                                      />
                                    )}
                                    <span>
                                      {renderPlayerName(
                                        match.homePlayerNameKr,
                                        match.homePlayerName
                                      )}
                                    </span>
                                  </div>
                                  <div className="score">{match.homeScore}</div>
                                  <div className="vs">vs</div>
                                  <div className="score">{match.awayScore}</div>
                                  <div
                                    className={`player ${
                                      winner === 'away' ? 'winner' : ''
                                    }`}
                                  >
                                    {winner === 'away' && (
                                      <img
                                        src="/images/ico_winner.png"
                                        className="winner-icon away"
                                        alt="winner"
                                      />
                                    )}
                                    <span>
                                      {renderPlayerName(
                                        match.awayPlayerNameKr,
                                        match.awayPlayerName
                                      )}
                                    </span>
                                  </div>
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      );
                    }
                  );
                })()
              ) : (
                <div className="no-matches">
                  이 날짜에 예정된 경기가 없습니다.
                </div>
              )}
            </div>
          </>
        ) : (
          <div className="no-matches">
            날짜를 선택하면 경기 일정을 확인할 수 있습니다.
          </div>
        )}
      </div>
    </div>
  );
}

export default Schedule;
