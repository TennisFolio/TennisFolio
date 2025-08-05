package com.tennisfolio.Tennisfolio.round.repository;


import com.tennisfolio.Tennisfolio.infrastructure.repository.RoundJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class RoundRepositoryImpl implements RoundRepository{
    private final RoundJpaRepository roundJpaRepository;
    private final BufferedBatchSaver<RoundEntity> bufferedBatchSaver;

    public RoundRepositoryImpl(RoundJpaRepository roundJpaRepository, TransactionTemplate transactionTemplate) {
        this.roundJpaRepository = roundJpaRepository;
        this.bufferedBatchSaver = new BufferedBatchSaver<>(roundJpaRepository, 1000, transactionTemplate);
    }

    @Override
    public List<Round> findAll() {
        return roundJpaRepository.findAll().stream().map(RoundEntity::toModel).toList();
    }

    @Override
    public List<Round> collect(Round round) {
        return bufferedBatchSaver.collect(RoundEntity.fromModel(round)).stream().map(RoundEntity::toModel).toList();
    }

    @Override
    public Set<Pair<Season,String>> findAllSeasonRoundPairs() {
        return roundJpaRepository.findAllSeasonRoundPairs().stream()
                .map(pair -> Pair.of(pair.first.toModel(), pair.second))
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<Round> findBySeasonAndRoundAndSlug(Season season, Long round, String slug) {
        return roundJpaRepository.findBySeasonEntityAndRoundAndSlug(SeasonEntity.fromModel(season), round, slug).map(RoundEntity::toModel);
    }

    @Override
    public List<Round> collect(List<Round> rounds) {
        List<RoundEntity> entities = rounds.stream().map(RoundEntity::fromModel).toList();
        return bufferedBatchSaver.collect(entities).stream().map(RoundEntity::toModel).toList();
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
