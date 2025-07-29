package com.tennisfolio.Tennisfolio.ranking.repository;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.repository.RankingJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Repository
public class RankingRepositoryImpl implements RankingRepository{
    private final RankingJpaRepository rankingJpaRepository;

    private final BufferedBatchSaver<RankingEntity> bufferedBatchSaver;


    public RankingRepositoryImpl(RankingJpaRepository rankingJpaRepository, TransactionTemplate transactionTemplate) {
        this.rankingJpaRepository = rankingJpaRepository;
        this.bufferedBatchSaver = new BufferedBatchSaver<>(rankingJpaRepository, 500, transactionTemplate);
    }

    @Override
    public RankingEntity getById(Long id) {
        return rankingJpaRepository.findById(id).orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    @Override
    public RankingEntity save(RankingEntity rankingEntity) {
        return rankingJpaRepository.save(rankingEntity);
    }

    @Override
    public List<RankingEntity> saveAll(List<RankingEntity> rankingEntities) {
        return rankingJpaRepository.saveAll(rankingEntities);
    }

    @Override
    public List<RankingEntity> findLatestRankings() {
        return rankingJpaRepository.findLatestRankings();
    }

    @Override
    public List<RankingEntity> findLatestRankings(Pageable pageable) {
        return rankingJpaRepository.findLatestRankings(pageable);
    }

    @Override
    public List<RankingEntity> findByLastUpdate(String lastUpdate) {
        return rankingJpaRepository.findByLastUpdate(lastUpdate);
    }

    @Override
    public List<RankingEntity> collect(RankingEntity rankingEntity) {
        return bufferedBatchSaver.collect(rankingEntity);
    }

    @Override
    public List<RankingEntity> collect(List<RankingEntity> rankingEntities) {
        return bufferedBatchSaver.collect(rankingEntities);
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
