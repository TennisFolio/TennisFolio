package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.domain.GameMatch;
import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import com.tennisfolio.Tennisfolio.matching.domain.ScheduleResult;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionStat;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionStatRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CompetitionStatService {

    private final CompetitionStatRepository competitionStatRepository;

    public CompetitionStatService(CompetitionStatRepository competitionStatRepository) {
        this.competitionStatRepository = competitionStatRepository;
    }

    public CompetitionStat createCompetitionStat(
            Competition competition,
            ScheduleResult result,
            Map<String, CompetitionEntry> entriesByPlayerName
    ) {
        CompetitionStat stat = new CompetitionStat(competition);

        Map<String, Integer> playerGameCount = new HashMap<>();
        for (String playerId : entriesByPlayerName.keySet()) {
            playerGameCount.put(playerId, 0);
        }

        for (GameMatch match : result.matches) {
            stat.incrementTotalGames();
            incrementMatchTypeCount(stat, match.type);

            for (GamePlayer player : match.teamA) {
                playerGameCount.merge(player.id, 1, Integer::sum);
            }
            for (GamePlayer player : match.teamB) {
                playerGameCount.merge(player.id, 1, Integer::sum);
            }
        }

        int maxGames = playerGameCount.values().stream().max(Integer::compareTo).orElse(0);
        int minGames = playerGameCount.values().stream().min(Integer::compareTo).orElse(0);
        stat.updateGameStatistics(maxGames, minGames);

        return competitionStatRepository.save(stat);
    }

    private void incrementMatchTypeCount(CompetitionStat stat, MatchType matchType) {
        switch (matchType) {
            case MIXED -> stat.incrementMixedCount();
            case MALE -> stat.incrementMaleCount();
            case FEMALE -> stat.incrementFemaleCount();
            case RANDOM_M3F1 -> stat.incrementRandomM3F1Count();
            case RANDOM_M1F3 -> stat.incrementRandomM1F3Count();
        }
    }
}
