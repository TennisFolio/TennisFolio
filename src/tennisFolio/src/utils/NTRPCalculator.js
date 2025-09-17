export function calculateNTRPResult(answerList, testData) {
  let totalScore = 0;
  let validAnswerCount = 0;

  // 각 답변에 대해 점수 합산
  answerList.forEach((selectedOptionId, questionIndex) => {
    if (selectedOptionId && testData.questions[questionIndex]) {
      const question = testData.questions[questionIndex];
      const selectedOption = question.testOption[selectedOptionId - 1]; // optionId는 1부터 시작

      if (selectedOption && selectedOption.score) {
        totalScore += selectedOption.score;
        validAnswerCount++;
      }
    }
  });

  // 평균 점수 계산 (0으로 나누기 방지)
  if (validAnswerCount === 0) {
    return testData.results[0]; // 기본값으로 첫 번째 결과 반환
  }

  const averageScore = totalScore / validAnswerCount;

  // 0.5 단위로 반올림
  const roundedScore = Math.round(averageScore * 2) / 2;

  // 가장 가까운 target 찾기
  let bestTarget = null;
  let minDifference = Infinity;

  testData.results.forEach((result) => {
    const targetValue = parseFloat(result.target);
    const difference = Math.abs(targetValue - roundedScore);

    if (difference < minDifference) {
      minDifference = difference;
      bestTarget = result.target;
    }
  });

  // 해당 결과 정보 반환
  const result = testData.results.find((r) => r.target === bestTarget);

  if (!result) {
    // 기본값으로 첫 번째 결과 반환
    return testData.results[0];
  }

  return result;
}
