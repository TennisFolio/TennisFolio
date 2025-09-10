export function calculateTestResult(answerList, testData) {
  // 각 결과(target)별 점수 계산
  const targetScores = {};

  // 모든 결과의 target들을 초기화
  testData.results.forEach((result) => {
    targetScores[result.target] = 0;
  });

  // 각 답변에 대해 점수 계산
  answerList.forEach((selectedOptionId, questionIndex) => {
    if (selectedOptionId && testData.questions[questionIndex]) {
      const question = testData.questions[questionIndex];
      const selectedOption = question.testOption[selectedOptionId - 1]; // optionId는 1부터 시작

      if (selectedOption && selectedOption.target) {
        // 선택된 옵션의 target 배열에 있는 각 결과에 점수 추가
        selectedOption.target.forEach((targetId) => {
          if (Object.prototype.hasOwnProperty.call(targetScores, targetId)) {
            targetScores[targetId] += 1;
          }
        });
      }
    }
  });

  // 가장 높은 점수를 받은 결과 찾기
  let maxScore = 0;
  let bestTarget = null;

  Object.entries(targetScores).forEach(([targetId, score]) => {
    if (score > maxScore) {
      maxScore = score;
      bestTarget = targetId;
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
