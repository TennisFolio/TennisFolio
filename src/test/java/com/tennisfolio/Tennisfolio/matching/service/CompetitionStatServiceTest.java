package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.domain.GameMatch;
import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import com.tennisfolio.Tennisfolio.matching.domain.ScheduleResult;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionStat;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionStatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.clubSessionCompetition;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionStatServiceTest {

    @Mock
    private CompetitionStatRepository competitionStatRepository;

    private CompetitionStatService service;

    @BeforeEach
    void setUp() {
        service = new CompetitionStatService(competitionStatRepository);
    }

    @Test
    void createCompetitionStat_countsBySchedulerPlayerIdWhenEntryNamesAreDuplicated() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        ScheduleResult result = new ScheduleResult();
        result.matches.add(new GameMatch(
                1,
                1,
                MatchType.RANDOM_M3F1,
                List.of(
                        new GamePlayer("M1", GamePlayer.Gender.MALE),
                        new GamePlayer("M2", GamePlayer.Gender.MALE)
                ),
                List.of(
                        new GamePlayer("M3", GamePlayer.Gender.MALE),
                        new GamePlayer("F1", GamePlayer.Gender.FEMALE)
                )
        ));
        Map<String, CompetitionEntry> entriesByPlayerName = Map.of(
                "M1", entry(1L, competition, "민수", CompetitionEntry.Gender.MALE),
                "M2", entry(2L, competition, "민수", CompetitionEntry.Gender.MALE),
                "M3", entry(3L, competition, "민수", CompetitionEntry.Gender.MALE),
                "F1", entry(4L, competition, "민수", CompetitionEntry.Gender.FEMALE)
        );

        when(competitionStatRepository.save(any(CompetitionStat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompetitionStat stat = service.createCompetitionStat(competition, result, entriesByPlayerName);

        assertEquals(1, stat.getTotalGames());
        assertEquals(1, stat.getRandomM3F1Count());
        assertEquals(1, stat.getMaxGames());
        assertEquals(1, stat.getMinGames());
    }
}
