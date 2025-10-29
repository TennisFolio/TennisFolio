package com.tennisfolio.Tennisfolio.match.repository;

import com.tennisfolio.Tennisfolio.calendar.dto.MatchScheduleResponse;
import com.tennisfolio.Tennisfolio.calendar.dto.TournamentCalendarResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MatchQueryRepository {
    private final EntityManager em;

    public List<MatchScheduleResponse> findMatchSchedule(String date, Long seasonId){

        return em.createQuery("""
                SELECT new com.tennisfolio.Tennisfolio.calendar.dto.MatchScheduleResponse(
                                      m.roundEntity.seasonEntity.seasonId,
                                      m.roundEntity.roundId,
                                      m.roundEntity.slug,
                                      m.matchId,
                                      m.rapidMatchId,
                                      m.homeScore,
                                      m.awayScore,
                                      m.homePlayer.playerId ,
                                      m.homePlayer.playerName,
                                      m.homePlayer.playerNameKr,
                                      m.awayPlayer.playerId,
                                      m.awayPlayer.playerName,
                                      m.awayPlayer.playerNameKr,
                                      m.status,
                                      m.startTimestamp,
                                      m.winner
                                   )
                                   FROM MatchEntity m
                                   WHERE FUNCTION('SUBSTRING', m.startTimestamp, 1, 8) = :date
                                   AND (:seasonId IS NULL OR m.roundEntity.seasonEntity.seasonId = :seasonId)
                """, MatchScheduleResponse.class)
                .setParameter("date", date)
                .setParameter("seasonId", seasonId)
                .getResultList();
    }
}
