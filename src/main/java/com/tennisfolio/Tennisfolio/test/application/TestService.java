package com.tennisfolio.Tennisfolio.test.application;

import com.tennisfolio.Tennisfolio.common.TestType;
import com.tennisfolio.Tennisfolio.test.response.TestCategoryResponse;
import com.tennisfolio.Tennisfolio.test.response.TestQuestionResponse;
import com.tennisfolio.Tennisfolio.test.response.TestResultResponse;

import java.util.List;

public interface TestService {
    List<TestCategoryResponse> getTestList();
    TestCategoryResponse getTest(TestType testType);
    List<TestQuestionResponse> getTestQuestion(TestType testType);
    TestResultResponse getResult(List<Long> answerList, TestType testType);
    TestResultResponse getTestResultByQuery(TestType testType, String query);
}
