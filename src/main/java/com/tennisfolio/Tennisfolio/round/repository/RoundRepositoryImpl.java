package com.tennisfolio.Tennisfolio.round.repository;

import com.tennisfolio.Tennisfolio.infrastructure.repository.RoundJpaRepository;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class RoundRepositoryImpl implements RoundRepository{
    private final RoundJpaRepository roundJpaRepository;

    public RoundRepositoryImpl(RoundJpaRepository roundJpaRepository) {
        this.roundJpaRepository = roundJpaRepository;
    }

    @Override
    public List<Round> findAll() {
        return roundJpaRepository.findAll().stream().map(RoundEntity::toModel).toList();
    }
}
