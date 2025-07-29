package com.tennisfolio.Tennisfolio.season.repository;

import com.tennisfolio.Tennisfolio.infrastructure.repository.SeasonJpaRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SeasonRepositoryImpl implements SeasonRepository{
    private final SeasonJpaRepository seasonJpaRepository;

    public SeasonRepositoryImpl(SeasonJpaRepository seasonJpaRepository) {
        this.seasonJpaRepository = seasonJpaRepository;
    }

    @Override
    public List<Season> findAll() {
        return seasonJpaRepository.findAll().stream().map(SeasonEntity::toModel).toList();
    }
}
