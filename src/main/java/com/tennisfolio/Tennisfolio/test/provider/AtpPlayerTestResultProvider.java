package com.tennisfolio.Tennisfolio.test.provider;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.TestType;
import com.tennisfolio.Tennisfolio.exception.ResultNotFoundException;
import com.tennisfolio.Tennisfolio.test.domain.model.TestPlayer;
import com.tennisfolio.Tennisfolio.test.repository.TestPlayerRepository;
import com.tennisfolio.Tennisfolio.test.response.PlayerTestResultResponse;
import org.springframework.stereotype.Component;

@Component
public class AtpPlayerTestResultProvider implements TestResultProvider{

    private final TestPlayerRepository testPlayerRepository;

    public AtpPlayerTestResultProvider(TestPlayerRepository testPlayerRepository) {
        this.testPlayerRepository = testPlayerRepository;
    }

    @Override
    public boolean supports(TestType type) {
        return type == TestType.ATPPLAYER;
    }

    @Override
    public PlayerTestResultResponse getResult(Long resultId) {
        TestPlayer testPlayer = testPlayerRepository.findById(resultId)
                .orElseThrow(() ->new ResultNotFoundException(ExceptionCode.NOT_FOUND));

        return new PlayerTestResultResponse(testPlayer);
    }

    @Override
    public PlayerTestResultResponse getResultByQuery(String query) {
        TestPlayer testPlayer = testPlayerRepository.findByQuery(query)
                .orElseThrow(() ->new ResultNotFoundException(ExceptionCode.NOT_FOUND));
        return new PlayerTestResultResponse(testPlayer);
    }
}
