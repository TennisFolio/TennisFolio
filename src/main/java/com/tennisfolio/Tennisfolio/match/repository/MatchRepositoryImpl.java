package com.tennisfolio.Tennisfolio.match.repository;

import com.tennisfolio.Tennisfolio.infrastructure.repository.MatchJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class MatchRepositoryImpl implements MatchRepository{
    private final MatchJpaRepository matchJpaRepository;
    private final BufferedBatchSaver<MatchEntity> bufferedBatchSaver;
    public MatchRepositoryImpl(MatchJpaRepository matchJpaRepository, TransactionTemplate transactionTemplate) {
        this.matchJpaRepository = matchJpaRepository;
        this.bufferedBatchSaver = new BufferedBatchSaver<>(matchJpaRepository, 1000, transactionTemplate);
    }

    @Override
    public Optional<Match> findByRapidMatchId(String rapidMatchId) {
        return matchJpaRepository.findByRapidMatchId(rapidMatchId).map(MatchEntity::toModel);
    }

    @Override
    public List<Match> findAll() {
        return matchJpaRepository.findAll().stream().map(MatchEntity::toModel).toList();
    }

    @Override
    public Set<String> findAllRapidIds() {
        return matchJpaRepository.findAllRapidMatchIds();
    }

    @Override
    public List<Match> collect(Match match) {

        return bufferedBatchSaver.collect(MatchEntity.fromModel(match)).stream().map(MatchEntity::toModel).toList();
    }

    @Override
    public List<Match> collect(List<Match> matches) {
        List<MatchEntity> matchEntities = matches.stream().map(MatchEntity::fromModel).toList();
        return bufferedBatchSaver.collect(matchEntities).stream().map(MatchEntity::toModel).toList();
    }

    @Override
    public boolean flushWhenFull() {
        return bufferedBatchSaver.flushWhenFull();
    }

    @Override
    public boolean flushAll() {
        return bufferedBatchSaver.flushAll();
    }
}
