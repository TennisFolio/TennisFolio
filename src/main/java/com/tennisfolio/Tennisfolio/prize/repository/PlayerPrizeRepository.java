package com.tennisfolio.Tennisfolio.prize.repository;

import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerPrizeRepository extends JpaRepository<PlayerPrize, Long> {
}
