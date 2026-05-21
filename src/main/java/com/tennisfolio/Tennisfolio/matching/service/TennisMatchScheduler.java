package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.domain.GameMatch;
import com.tennisfolio.Tennisfolio.matching.domain.ScheduleResult;
import com.tennisfolio.Tennisfolio.matching.engine.CandidateGenerator;
import com.tennisfolio.Tennisfolio.matching.engine.ConstraintChecker;
import com.tennisfolio.Tennisfolio.matching.engine.ScoreCalculator;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.entity.GameEntry;
import com.tennisfolio.Tennisfolio.matching.service.club.ClubSessionNextGameGenerator;
import com.tennisfolio.Tennisfolio.matching.service.fixed.FixedScheduleGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TennisMatchScheduler {

    private final FixedScheduleGenerator fixedScheduleGenerator;
    private final ClubSessionNextGameGenerator clubSessionNextGameGenerator;

    public TennisMatchScheduler(
            ConstraintChecker constraintChecker,
            ScoreCalculator scoreCalculator,
            CandidateGenerator generator
    ) {
        this.fixedScheduleGenerator = new FixedScheduleGenerator(constraintChecker, scoreCalculator, generator);
        this.clubSessionNextGameGenerator = new ClubSessionNextGameGenerator(scoreCalculator, generator);
    }

    public ScheduleResult generateSchedule(int male, int female, int court, int rounds, long seed) {
        return fixedScheduleGenerator.generateSchedule(male, female, court, rounds, seed);
    }

    public GameMatch generateNextClubSessionGame(
            List<CompetitionEntry> candidateEntries,
            List<GameEntry> history,
            int court,
            int round,
            long seed
    ) {
        return clubSessionNextGameGenerator.generateNextGame(candidateEntries, history, court, round, seed);
    }
}
