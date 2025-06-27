package com.tennisfolio.Tennisfolio.test.provider;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.TestType;
import com.tennisfolio.Tennisfolio.exception.ResultNotFoundException;
import com.tennisfolio.Tennisfolio.test.domain.model.TestRacket;
import com.tennisfolio.Tennisfolio.test.repository.TestRacketRepository;
import com.tennisfolio.Tennisfolio.test.response.RacketTestResultResponse;
import org.springframework.stereotype.Component;

@Component
public class RacketTestResultProvider implements TestResultProvider{
    private final TestRacketRepository testRacketRepository;

    public RacketTestResultProvider(TestRacketRepository testRacketRepository) {
        this.testRacketRepository = testRacketRepository;
    }

    @Override
    public boolean supports(TestType type) {
        return type == TestType.RACKET;
    }

    @Override
    public RacketTestResultResponse getResult(Long resultId) {
        TestRacket testRacket = testRacketRepository.findById(resultId)
                .orElseThrow(() ->new ResultNotFoundException(ExceptionCode.NOT_FOUND));

        return new RacketTestResultResponse(testRacket);
    }

    @Override
    public RacketTestResultResponse getResultByQuery(String query) {
        TestRacket testRacket = testRacketRepository.findByQuery(query)
                .orElseThrow(() ->new ResultNotFoundException(ExceptionCode.NOT_FOUND));
        return new RacketTestResultResponse(testRacket);
    }
}
