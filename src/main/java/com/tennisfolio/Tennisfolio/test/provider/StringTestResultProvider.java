package com.tennisfolio.Tennisfolio.test.provider;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.TestType;
import com.tennisfolio.Tennisfolio.exception.ResultNotFoundException;
import com.tennisfolio.Tennisfolio.test.domain.model.TestString;
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
        TestString testString = testStringRepository.findById(resultId)
                .orElseThrow(() ->new ResultNotFoundException(ExceptionCode.NOT_FOUND));

        return new StringTestResultResponse(testString);
    }

    @Override
    public StringTestResultResponse getResultByQuery(String query) {
        TestString testString = testStringRepository.findByQuery(query)
                .orElseThrow(() ->new ResultNotFoundException(ExceptionCode.NOT_FOUND));
        return new StringTestResultResponse(testString);
    }
}
