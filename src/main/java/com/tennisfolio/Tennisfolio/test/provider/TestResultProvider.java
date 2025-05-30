package com.tennisfolio.Tennisfolio.test.provider;

import com.tennisfolio.Tennisfolio.common.TestType;
import com.tennisfolio.Tennisfolio.test.response.TestResultResponse;

public interface TestResultProvider {
    boolean supports(TestType type);
    TestResultResponse getResult(Long resultId);
}
