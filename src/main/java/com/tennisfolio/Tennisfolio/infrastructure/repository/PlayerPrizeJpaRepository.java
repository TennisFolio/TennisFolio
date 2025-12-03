package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.prize.repository.PlayerPrizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerPrizeJpaRepository extends JpaRepository<PlayerPrizeEntity, Long> {

    @Query("""
            SELECT pp 
            FROM PlayerPrizeEntity pp
            JOIN FETCH playerEntity p
            """)
    List<PlayerPrizeEntity> findAll();
}
