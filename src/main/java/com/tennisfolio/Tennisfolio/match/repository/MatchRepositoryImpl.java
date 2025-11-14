package com.tennisfolio.Tennisfolio.match.repository;

import com.tennisfolio.Tennisfolio.category.repository.CategoryEntity;
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
        return matchJpaRepository.findByRapidMatchId(rapidMatchId).map(MatchEntity::toModelBaseOnly);
    }

    @Override
    public Match save(Match match) {
        return matchJpaRepository.save(MatchEntity.fromModel(match)).toModel();
    }

    @Override
    public void saveAll(List<Match> matches) {
        List<MatchEntity> entities = matches.stream().map(MatchEntity::fromModel).toList();
        matchJpaRepository.saveAll(entities);
    }

    @Override
    public List<Match> findAll() {
        return matchJpaRepository.findAll().stream().map(MatchEntity::toModelBaseOnly).toList();
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

    @Override
    public void flush() {
        matchJpaRepository.flush();
    }

    @Override
    public void updateMatch(Match match) {
        matchJpaRepository.updateMatch(
                match.getRapidMatchId(),
                match.getHomeSeed(),
                match.getAwaySeed(),
                match.getHomeScore(),
                match.getAwayScore(),

                match.getHomeSet().getSet1(),
                match.getHomeSet().getSet2(),
                match.getHomeSet().getSet3(),
                match.getHomeSet().getSet4(),
                match.getHomeSet().getSet5(),

                match.getHomeSet().getSet1Tie(),
                match.getHomeSet().getSet2Tie(),
                match.getHomeSet().getSet3Tie(),
                match.getHomeSet().getSet4Tie(),
                match.getHomeSet().getSet5Tie(),

                match.getAwaySet().getSet1(),
                match.getAwaySet().getSet2(),
                match.getAwaySet().getSet3(),
                match.getAwaySet().getSet4(),
                match.getAwaySet().getSet5(),

                match.getAwaySet().getSet1Tie(),
                match.getAwaySet().getSet2Tie(),
                match.getAwaySet().getSet3Tie(),
                match.getAwaySet().getSet4Tie(),
                match.getAwaySet().getSet5Tie(),

                match.getPeriodSet().getSet1(),
                match.getPeriodSet().getSet2(),
                match.getPeriodSet().getSet3(),
                match.getPeriodSet().getSet4(),
                match.getPeriodSet().getSet5(),

                match.getStartTimestamp(),
                match.getWinner(),
                match.getStatus()
        );
    }
}
