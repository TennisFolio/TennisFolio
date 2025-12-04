package com.tennisfolio.Tennisfolio.ranking.repository;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.RankingSearchCondition;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.repository.RankingJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import com.tennisfolio.Tennisfolio.player.domain.Country;
import com.tennisfolio.Tennisfolio.player.repository.CountryEntity;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class RankingRepositoryImpl implements RankingRepository{
    private final RankingJpaRepository rankingJpaRepository;

    private final BufferedBatchSaver<RankingEntity> bufferedBatchSaver;


    public RankingRepositoryImpl(RankingJpaRepository rankingJpaRepository, TransactionTemplate transactionTemplate) {
        this.rankingJpaRepository = rankingJpaRepository;
        this.bufferedBatchSaver = new BufferedBatchSaver<>(rankingJpaRepository, 500, transactionTemplate);
    }

    @Override
    public Optional<Ranking> getById(Long id) {
        return rankingJpaRepository.findById(id).map(RankingEntity::toModel);
    }

    @Override
    public Ranking save(Ranking ranking) {
        return rankingJpaRepository.save(RankingEntity.fromModel(ranking)).toModel();
    }

    @Override
    public List<Ranking> saveAll(List<Ranking> rankings) {
        List<RankingEntity> rankingEntities = rankings.stream()
                .map(RankingEntity::fromModel)
                .toList();

        return rankingJpaRepository.saveAll(rankingEntities).stream().map(RankingEntity::toModel).toList();
    }

    @Override
    public List<Ranking> findLatestRankings(Pageable pageable) {
        return rankingJpaRepository.findLatestRankings(pageable).stream().map(RankingEntity::toModel).toList();
    }

    @Override
    public List<Ranking> findLatestRankingsBefore(Pageable pageable) {
        return rankingJpaRepository.findLatestRankingsBefore(pageable).stream().map(RankingEntity::toModel).toList();
    }

    @Override
    public List<Ranking> findByLastUpdate(String lastUpdate) {
        return rankingJpaRepository.findByLastUpdate(lastUpdate).stream().map(RankingEntity::toModel).toList();
    }

    @Override
    public boolean existsByLastUpdate(String lastUpdate) {
        return rankingJpaRepository.existsByLastUpdate(lastUpdate);
    }

    @Override
    public List<Ranking> collect(Ranking rankings) {
        return bufferedBatchSaver.collect(RankingEntity.fromModel(rankings)).stream().map(RankingEntity::toModel).toList();
    }

    @Override
    public List<Ranking> collect(List<Ranking> rankings) {
        List<RankingEntity> rankingEntities = rankings.stream()
                .map(RankingEntity::fromModel)
                .toList();
        return bufferedBatchSaver.collect(rankingEntities).stream().map(RankingEntity::toModel).toList();
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
    public List<Country> getDistinctCountriesFromTopRankings() {
        return rankingJpaRepository.findDistinctCountriesFromTopRankings().stream().map(CountryEntity::toModel).toList();
    }

    @Override
    public Page<Ranking> search(Pageable pageable, RankingSearchCondition condition, String keyword) {
        Page<RankingEntity> result = rankingJpaRepository.search(pageable, condition, keyword);

        return result.map(RankingEntity::toModel);
    }
}
