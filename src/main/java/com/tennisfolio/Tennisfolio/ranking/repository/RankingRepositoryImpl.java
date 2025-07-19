package com.tennisfolio.Tennisfolio.ranking.repository;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.repository.RankingJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RankingRepositoryImpl implements RankingRepository{
    private final RankingJpaRepository rankingJpaRepository;

    private final BufferedBatchSaver<Ranking> rankingBufferedBatchSaver;


    public RankingRepositoryImpl(RankingJpaRepository rankingJpaRepository) {
        this.rankingJpaRepository = rankingJpaRepository;
        this.rankingBufferedBatchSaver = new BufferedBatchSaver<>(rankingJpaRepository, 500);
    }

    @Override
    public Ranking getById(Long id) {
        return rankingJpaRepository.findById(id).orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    @Override
    public Ranking save(Ranking ranking) {
        return rankingJpaRepository.save(ranking);
    }

    @Override
    public List<Ranking> saveAll(List<Ranking> rankings) {
        return rankingJpaRepository.saveAll(rankings);
    }

    @Override
    public List<Ranking> bufferedSave(Ranking ranking) {
        return rankingBufferedBatchSaver.collect(ranking);
    }

    @Override
    public List<Ranking> bufferedSaveAll(List<Ranking> rankings) {
        return rankingBufferedBatchSaver.collect(rankings);
    }

    @Override
    public List<Ranking> flush() {
        return rankingBufferedBatchSaver.flush();
    }


    @Override
    public List<Ranking> findLatestRankings() {
        return rankingJpaRepository.findLatestRankings();
    }

    @Override
    public List<Ranking> findLatestRankings(Pageable pageable) {
        return rankingJpaRepository.findLatestRankings(pageable);
    }

    @Override
    public List<Ranking> findByLastUpdate(String lastUpdate) {
        return rankingJpaRepository.findByLastUpdate(lastUpdate);
    }
}
