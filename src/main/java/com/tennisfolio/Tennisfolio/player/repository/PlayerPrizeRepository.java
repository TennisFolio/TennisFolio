package com.tennisfolio.Tennisfolio.player.repository;

import com.tennisfolio.Tennisfolio.player.domain.PlayerPrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerPrizeRepository extends JpaRepository<PlayerPrize, Long> {
}
