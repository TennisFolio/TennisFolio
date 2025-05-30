package com.tennisfolio.Tennisfolio.test.provider;

import com.tennisfolio.Tennisfolio.common.TestType;
import com.tennisfolio.Tennisfolio.test.domain.TestRacket;
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
        TestRacket testRacket = testRacketRepository.findById(resultId).get();

        return new RacketTestResultResponse(testRacket);
    }
}
