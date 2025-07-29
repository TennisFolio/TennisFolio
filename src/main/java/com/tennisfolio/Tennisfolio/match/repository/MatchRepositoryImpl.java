package com.tennisfolio.Tennisfolio.match.repository;

import com.tennisfolio.Tennisfolio.infrastructure.repository.MatchJpaRepository;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MatchRepositoryImpl implements MatchRepository{
    private final MatchJpaRepository matchJpaRepository;

    public MatchRepositoryImpl(MatchJpaRepository matchJpaRepository) {
        this.matchJpaRepository = matchJpaRepository;
    }

    @Override
    public Match findByRapidMatchId(String rapidMatchId) {
        return matchJpaRepository.findByRapidMatchId(rapidMatchId).orElseThrow().toModel();
    }

    @Override
    public List<Match> findAll() {
        return matchJpaRepository.findAll().stream().map(MatchEntity::toModel).toList();
    }
}
