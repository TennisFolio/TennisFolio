package com.tennisfolio.Tennisfolio.round.repository;

import com.tennisfolio.Tennisfolio.round.domain.Round;

import java.util.List;

public interface RoundRepository {
    List<Round> findAll();
}
