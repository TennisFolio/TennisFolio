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
 * @returns {'home'|'away'|null}
 */
export function getSetWinner(event, setIndex) {
  const homeScore = event.homeScore?.periodScore[setIndex];
  const awayScore = event.awayScore?.periodScore[setIndex];

  if (homeScore == null || awayScore == null) return null;
  if (homeScore === 0 && awayScore === 0) return null;

  const scoreDiff = Math.abs(homeScore - awayScore);
  const maxScore = Math.max(homeScore, awayScore);
  const minScore = Math.min(homeScore, awayScore);

  const isNormalWin = maxScore >= 6 && scoreDiff >= 2;
  const isTiebreakWin = maxScore === 7 && minScore === 6;

  const isSetCompleted = isNormalWin || isTiebreakWin;
  if (!isSetCompleted) return null;

  return homeScore > awayScore ? 'home' : 'away';
}
