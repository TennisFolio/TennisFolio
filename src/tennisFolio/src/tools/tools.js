export function arrayShuffler(array) {
  // 원본 배열을 복사하여 읽기 전용 배열 문제 해결
  const shuffledArray = [...array];
  let currentIndex = shuffledArray.length,
    temporaryValue,
    randomIndex;

  while (0 !== currentIndex) {
    randomIndex = Math.floor(Math.random() * currentIndex);
    currentIndex -= 1;

    temporaryValue = shuffledArray[currentIndex];
    shuffledArray[currentIndex] = shuffledArray[randomIndex];
    shuffledArray[randomIndex] = temporaryValue;
  }

  return shuffledArray;
}

/**
 * 테니스 세트의 승자를 판단하는 함수
 * @param {Object} event - 경기 이벤트 객체
 * @param {number} setIndex - 세트 인덱스 (0부터 시작)
 * @returns {string|null} - 'home', 'away', 또는 null
 */
export function getSetWinner(event, setIndex) {
  const homeScore = event.homeScore?.periodScore[setIndex];
  const awayScore = event.awayScore?.periodScore[setIndex];

  // 점수가 없으면 시작되지 않은 세트
  if (homeScore === 0 && awayScore === 0) {
    return null;
  }

  // 테니스 세트 승리 조건:
  // 1. 6점 이상을 먼저 획득
  // 2. 상대방과 최소 2점 차이
  // 예: 6-0, 6-1, 6-2, 6-3, 6-4, 7-5, 7-6 등

  const scoreDiff = Math.abs(homeScore - awayScore);
  const maxScore = Math.max(homeScore, awayScore);

  // 세트가 완료되었는지 확인
  const isSetCompleted = maxScore >= 6 && scoreDiff >= 2;

  if (!isSetCompleted) {
    return null; // 세트가 아직 진행 중 (예: 6-5, 6-6)
  }

  // 세트가 완료되었으면 승자 반환
  if (homeScore > awayScore) return 'home';
  if (awayScore > homeScore) return 'away';
  return null;
}
