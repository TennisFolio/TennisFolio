package com.tennisfolio.Tennisfolio.test.service;

import com.tennisfolio.Tennisfolio.common.TestType;
import com.tennisfolio.Tennisfolio.test.response.TestCategoryResponse;
import com.tennisfolio.Tennisfolio.test.response.TestQuestionResponse;
import com.tennisfolio.Tennisfolio.test.response.PlayerTestResultResponse;
import com.tennisfolio.Tennisfolio.test.response.TestResultResponse;

import java.util.List;
import java.util.Map;

public interface TestService {
    List<TestCategoryResponse> getTestList();
    TestCategoryResponse getTest(TestType testType);
    List<TestQuestionResponse> getTestQuestion(TestType testType);
    TestResultResponse getResult(List<Long> answerList, TestType testType);
    TestResultResponse getTestResultByQuery(TestType testType, String query);
}
