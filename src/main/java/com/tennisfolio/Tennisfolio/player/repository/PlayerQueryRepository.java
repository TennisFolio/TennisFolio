package com.tennisfolio.Tennisfolio.player.repository;

import com.tennisfolio.Tennisfolio.calendar.dto.MatchScheduleResponse;
import com.tennisfolio.Tennisfolio.player.dto.PlayerDetailResponse;
import com.tennisfolio.Tennisfolio.player.dto.PlayerMatchResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlayerQueryRepository {
    private final EntityManager em;

    public PlayerDetailResponse findPlayerDetail(@Param("playerId") Long playerId){
        List<PlayerDetailResponse> results =  em.createQuery("""
                SELECT new com.tennisfolio.Tennisfolio.player.dto.PlayerDetailResponse(
                    p.playerId,
                    p.rapidPlayerId,
                    p.playerName,
                    p.playerNameKr,
                    p.birth,
                    p.countryEntity.countryCode,
                    p.turnedPro,
                    p.weight,
                    p.plays,
                    p.height,
                    p.image,
                    p.gender,
                    pp.prizeCurrentAmount,
                    pp.prizeCurrentCurrency,
                    pp.prizeTotalAmount,
                    pp.prizeTotalCurrency,
                    r.rankingId,
                    r.curRank,
                    r.curPoints,
                    r.bestRank
                )
                FROM PlayerEntity p
                LEFT JOIN PlayerPrizeEntity pp
                       ON p.playerId = pp.playerEntity.playerId
                LEFT JOIN RankingEntity r
                       ON p.playerId = r.playerEntity.playerId
                      AND r.lastUpdate = (
                          SELECT MAX(r2.lastUpdate)
                          FROM RankingEntity r2
                          WHERE r2.playerEntity.playerId = :playerId
                      )
                WHERE p.id = :playerId
    """, PlayerDetailResponse.class)
                .setParameter("playerId", playerId)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);

    }

    public List<PlayerMatchResponse> findMatchesByPlayerAndYear(@Param("playerId") Long playerId,
                                                         @Param("year") String year) {
        return em.createQuery("""
            SELECT new com.tennisfolio.Tennisfolio.player.dto.PlayerMatchResponse(
                c.categoryId,
                c.categoryName,
                t.tournamentId,
                t.tournamentName,
                s.seasonId,
                s.seasonName,
                r.roundId,
                r.name,
                m.matchId,
                m.rapidMatchId,
                hp.playerId,
                hp.playerName,
                hp.playerNameKr,
                m.homeSet.set1,
                m.homeSet.set2,
                m.homeSet.set3,
                m.homeSet.set4,
                m.homeSet.set5,
                m.homeSet.set1Tie,
                m.homeSet.set2Tie,
                m.homeSet.set3Tie,
                m.homeSet.set4Tie,
                m.homeSet.set5Tie,
                ap.playerId,
                ap.playerName,
                ap.playerNameKr,
                m.awaySet.set1,
                m.awaySet.set2,
                m.awaySet.set3,
                m.awaySet.set4,
                m.awaySet.set5,
                m.awaySet.set1Tie,
                m.awaySet.set2Tie,
                m.awaySet.set3Tie,
                m.awaySet.set4Tie,
                m.awaySet.set5Tie,
                m.winner,
                m.startTimestamp
            )
            FROM MatchEntity m
            JOIN m.roundEntity r
            JOIN r.seasonEntity s
            JOIN s.tournamentEntity t
            JOIN t.categoryEntity c
            JOIN m.homePlayer hp
            JOIN m.awayPlayer ap
            WHERE (hp.playerId = :playerId OR ap.playerId = :playerId)
              AND m.startTimestamp LIKE CONCAT(:year, '%')
            ORDER BY m.startTimestamp DESC
            """, PlayerMatchResponse.class)
                .setParameter("playerId", playerId)
                .setParameter("year", year)
                .setMaxResults(10)
                .getResultList();
    }
}
