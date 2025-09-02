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
