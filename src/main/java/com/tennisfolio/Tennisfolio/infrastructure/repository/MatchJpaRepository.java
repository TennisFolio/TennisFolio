package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MatchJpaRepository extends JpaRepository<MatchEntity, Long> {

    Optional<MatchEntity> findByRapidMatchId(@Param("rapidMatchId") String rapidMatchId);

    @Query("""
            SELECT m
            FROM MatchEntity m
            JOIN FETCH m.roundEntity r
            JOIN FETCH r.seasonEntity s
            JOIN FETCH s.tournamentEntity t
            JOIN FETCH t.categoryEntity c
            WHERE rapidMatchId = :rapidMatchId
            """)
    Optional<MatchEntity> findWithToOneByRapidMatchId(@Param("rapidMatchId") String rapidMatchId);

    @Query("""
            SELECT m
            FROM MatchEntity m
            JOIN FETCH m.roundEntity r
            JOIN FETCH r.seasonEntity s
            JOIN FETCH s.tournamentEntity t
            JOIN FETCH t.categoryEntity c
            JOIN FETCH m.homePlayer hp
            JOIN FETCH m.awayPlayer ap
            WHERE m.startTimestamp LIKE CONCAT(:year, '%')
            """)
    List<MatchEntity> findByYear(@Param("year") String year);

    @Query("SELECT m.rapidMatchId FROM MatchEntity m")
    Set<String> findAllRapidMatchIds();

    @Query("""
            SELECT m.rapidMatchId
            FROM MatchEntity m
            WHERE rapidMatchId IN :rapidMatchIds
            """)
    Set<String> findRapidMatchIdByRapidMatchIds(@Param("rapidMatchIds") Collection<String> rapidMatchIds);

    @Modifying
    @Query("""
UPDATE MatchEntity m
SET 
    m.homeSeed = :homeSeed,
    m.awaySeed = :awaySeed,

    m.homeScore = :homeScore,
    m.awayScore = :awayScore,

    m.homeSet.set1 = :homeSet1,
    m.homeSet.set2 = :homeSet2,
    m.homeSet.set3 = :homeSet3,
    m.homeSet.set4 = :homeSet4,
    m.homeSet.set5 = :homeSet5,

    m.homeSet.set1Tie = :homeTieSet1,
    m.homeSet.set2Tie = :homeTieSet2,
    m.homeSet.set3Tie = :homeTieSet3,
    m.homeSet.set4Tie = :homeTieSet4,
    m.homeSet.set5Tie = :homeTieSet5,

    m.awaySet.set1 = :awaySet1,
    m.awaySet.set2 = :awaySet2,
    m.awaySet.set3 = :awaySet3,
    m.awaySet.set4 = :awaySet4,
    m.awaySet.set5 = :awaySet5,

    m.awaySet.set1Tie = :awayTieSet1,
    m.awaySet.set2Tie = :awayTieSet2,
    m.awaySet.set3Tie = :awayTieSet3,
    m.awaySet.set4Tie = :awayTieSet4,
    m.awaySet.set5Tie = :awayTieSet5,

    m.periodSet.set1 = :periodSet1,
    m.periodSet.set2 = :periodSet2,
    m.periodSet.set3 = :periodSet3,
    m.periodSet.set4 = :periodSet4,
    m.periodSet.set5 = :periodSet5,

    m.startTimestamp = :startTimestamp,
    m.winner = :winner,
    m.status = :status,
    m.updateDt = CURRENT_TIMESTAMP 

WHERE m.rapidMatchId = :rapidMatchId
""")
    int updateMatch(
            @Param("rapidMatchId") String rapidMatchId,

            @Param("homeSeed") String homeSeed,
            @Param("awaySeed") String awaySeed,

            @Param("homeScore") Long homeScore,
            @Param("awayScore") Long awayScore,

            @Param("homeSet1") Long homeSet1,
            @Param("homeSet2") Long homeSet2,
            @Param("homeSet3") Long homeSet3,
            @Param("homeSet4") Long homeSet4,
            @Param("homeSet5") Long homeSet5,

            @Param("homeTieSet1") Long homeTieSet1,
            @Param("homeTieSet2") Long homeTieSet2,
            @Param("homeTieSet3") Long homeTieSet3,
            @Param("homeTieSet4") Long homeTieSet4,
            @Param("homeTieSet5") Long homeTieSet5,

            @Param("awaySet1") Long awaySet1,
            @Param("awaySet2") Long awaySet2,
            @Param("awaySet3") Long awaySet3,
            @Param("awaySet4") Long awaySet4,
            @Param("awaySet5") Long awaySet5,

            @Param("awayTieSet1") Long awayTieSet1,
            @Param("awayTieSet2") Long awayTieSet2,
            @Param("awayTieSet3") Long awayTieSet3,
            @Param("awayTieSet4") Long awayTieSet4,
            @Param("awayTieSet5") Long awayTieSet5,

            @Param("periodSet1") String periodSet1,
            @Param("periodSet2") String periodSet2,
            @Param("periodSet3") String periodSet3,
            @Param("periodSet4") String periodSet4,
            @Param("periodSet5") String periodSet5,

            @Param("startTimestamp") String startTimestamp,
            @Param("winner") String winner,
            @Param("status") String status
    );
}
