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
 * @param {number} setsToShow - 표시할 총 세트 수
 * @returns {string|null} - 'home', 'away', 또는 null
 */
export function getSetWinner(event, setIndex, setsToShow) {
  const homeScore = event.homeScore?.periodScore[setIndex];
  const awayScore = event.awayScore?.periodScore[setIndex];

  // 해당 세트가 끝났는지 확인 (다음 세트에 점수가 있을 때만)
  const isSetCompleted =
    setIndex < setsToShow - 1
      ? event.homeScore?.periodScore[setIndex + 1] !== 0 ||
        event.awayScore?.periodScore[setIndex + 1] !== 0
      : false; // 마지막 세트는 완료 여부를 알 수 없으므로 색칠하지 않음

  if (!isSetCompleted || (homeScore === 0 && awayScore === 0)) {
    return null; // 세트가 아직 끝나지 않았거나 시작되지 않음
  }

  if (homeScore > awayScore) return 'home';
  if (awayScore > homeScore) return 'away';
  return null; // 동점 (일반적으로 발생하지 않음)
}
