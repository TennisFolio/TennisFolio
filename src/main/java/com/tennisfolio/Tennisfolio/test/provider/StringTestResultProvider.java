package com.tennisfolio.Tennisfolio.test.provider;

import com.tennisfolio.Tennisfolio.common.TestType;
import com.tennisfolio.Tennisfolio.test.domain.TestString;
import com.tennisfolio.Tennisfolio.test.repository.TestStringRepository;
import com.tennisfolio.Tennisfolio.test.response.StringTestResultResponse;
import org.springframework.stereotype.Component;

@Component
public class StringTestResultProvider implements TestResultProvider{

    private final TestStringRepository testStringRepository;

    public StringTestResultProvider(TestStringRepository testStringRepository) {
        this.testStringRepository = testStringRepository;
    }

    @Override
    public boolean supports(TestType type) {
        return type == TestType.STRING;
    }

    @Override
    public StringTestResultResponse getResult(Long resultId) {
        TestString testString = testStringRepository.findById(resultId).get();

        return new StringTestResultResponse(testString);
    }
}
