package com.tennisfolio.Tennisfolio.Tournament.repository;

import com.tennisfolio.Tennisfolio.calendar.dto.TournamentCalendarResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TournamentQueryRepository {
    private final EntityManager em;

    public List<TournamentCalendarResponse> findTournamentCalendar(String month){
        String startOfMonth = month + "01000000";
        String endOfMonth = month + "31999999";
        return em.createQuery("""
                SELECT new com.tennisfolio.Tennisfolio.calendar.dto.TournamentCalendarResponse(
                                       c.categoryId, c.categoryName,
                                       t.tournamentId, t.tournamentName,
                                       s.seasonId, s.seasonName, s.year,
                                       s.startTimestamp, s.endTimestamp
                                   )
                                   FROM SeasonEntity s
                                   JOIN s.tournamentEntity t
                                   JOIN t.categoryEntity c
                                   WHERE s.startTimestamp <= :endOfMonth
                                     AND s.endTimestamp >= :startOfMonth
                """, TournamentCalendarResponse.class)
                .setParameter("startOfMonth", startOfMonth)
                .setParameter("endOfMonth", endOfMonth)
                .getResultList();
    }
}
