package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import com.tennisfolio.Tennisfolio.matching.domain.ScheduleResult;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionUpdateResponse;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.service.fixed.SameGenderScheduleTargetCalculator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CompetitionCommandService {
    private static final int MAX_PLAYER_COUNT = 40;
    private static final int MAX_COURT_COUNT = 10;
    private static final int MAX_ROUNDS = 20;
    private static final EnumSet<MatchType> SAME_GENDER_DOUBLES_TYPES = EnumSet.of(
            MatchType.MALE,
            MatchType.FEMALE
    );

    private final TennisMatchScheduler scheduler;
    private final CompetitionService competitionService;
    private final CompetitionEntryCommandService competitionEntryCommandService;
    private final GameService gameService;
    private final CompetitionStatService competitionStatService;
    private final CompetitionAdminTokenService competitionAdminTokenService;
    private final SameGenderScheduleTargetCalculator sameGenderTargetCalculator = new SameGenderScheduleTargetCalculator();

    public CompetitionCommandService(
            TennisMatchScheduler scheduler,
            CompetitionService competitionService,
            CompetitionEntryCommandService competitionEntryCommandService,
            GameService gameService,
            CompetitionStatService competitionStatService,
            CompetitionAdminTokenService competitionAdminTokenService
    ) {
        this.scheduler = scheduler;
        this.competitionService = competitionService;
        this.competitionEntryCommandService = competitionEntryCommandService;
        this.gameService = gameService;
        this.competitionStatService = competitionStatService;
        this.competitionAdminTokenService = competitionAdminTokenService;
    }

    @Transactional
    public CompetitionCreateResponse createCompetition(CompetitionCreateRequest request) {
        Competition.CompetitionMode mode = resolveMode(request.getMode());
        validateRequest(request, mode);

        int rounds = mode == Competition.CompetitionMode.CLUB_SESSION
                ? 1
                : calculateRounds(request.getTotalGames(), request.getCourtCount());
        long seed = request.getSeed() != null
                ? request.getSeed()
                : ThreadLocalRandom.current().nextLong(1, 10000);

        Competition competition = competitionService.createCompetition(request, rounds, seed);

        int scheduleGames = mode == Competition.CompetitionMode.CLUB_SESSION ? rounds : request.getTotalGames();
        ScheduleResult result = generateSchedule(request, mode, scheduleGames, seed);

        Map<String, CompetitionEntry> entriesByPlayerName = competitionEntryCommandService.createCompetitionEntries(competition, request);
        gameService.saveSchedule(competition, result, entriesByPlayerName);
        competitionStatService.createCompetitionStat(competition, result, entriesByPlayerName);

        String competitionAdminToken = competitionAdminTokenService.createToken(competition.getPublicId());
        return CompetitionCreateResponse.from(competition, competitionAdminToken);
    }

    private ScheduleResult generateSchedule(
            CompetitionCreateRequest request,
            Competition.CompetitionMode mode,
            int scheduleGames,
            long seed
    ) {
        if (mode == Competition.CompetitionMode.FIXED_SCHEDULE && request.isSameGenderDoublesOnly()) {
            return scheduler.generateSchedule(
                    request.getMaleCount(),
                    request.getFemaleCount(),
                    request.getCourtCount(),
                    scheduleGames,
                    seed,
                    SAME_GENDER_DOUBLES_TYPES
            );
        }

        return scheduler.generateSchedule(
                request.getMaleCount(),
                request.getFemaleCount(),
                request.getCourtCount(),
                scheduleGames,
                seed
        );
    }

    @Transactional
    public CompetitionUpdateResponse updateCompetition(
            String publicId,
            CompetitionUpdateRequest request,
            String adminToken
    ) {
        Competition competition = competitionService.updateCompetitionName(publicId, request.getName(), adminToken);
        return CompetitionUpdateResponse.from(competition);
    }

    private void validateRequest(CompetitionCreateRequest request, Competition.CompetitionMode mode) {

        if (request.getMaleCount() < 0 || request.getFemaleCount() < 0) {
            throw new IllegalArgumentException("maleCount and femaleCount must be non-negative");
        }
        if (request.getCourtCount() <= 0) {
            throw new IllegalArgumentException("courtCount must be greater than 0");
        }
        if (request.getCourtCount() > MAX_COURT_COUNT) {
            throw new IllegalArgumentException("courtCount must be " + MAX_COURT_COUNT + " or less");
        }
        if (mode == Competition.CompetitionMode.FIXED_SCHEDULE) {
            if (request.getTotalGames() <= 0) {
                throw new IllegalArgumentException("totalGames must be greater than 0");
            }
            if (request.getTotalGames() > request.getCourtCount() * MAX_ROUNDS) {
                throw new IllegalArgumentException("totalGames must be courtCount * " + MAX_ROUNDS + " or less");
            }
        }

        int playerCount = request.getMaleCount() + request.getFemaleCount();

        if (playerCount < 4) {
            throw new IllegalArgumentException("at least 4 players are required");
        }
        if (playerCount > MAX_PLAYER_COUNT) {
            throw new IllegalArgumentException("player count must be " + MAX_PLAYER_COUNT + " or less");
        }
        if (playerCount < request.getCourtCount() * 4) {
            throw new IllegalArgumentException("player count must be at least courtCount * 4");
        }

        if (mode == Competition.CompetitionMode.FIXED_SCHEDULE && request.isSameGenderDoublesOnly()) {
            validateSameGenderDoublesOnly(request);
        }
    }

    private void validateSameGenderDoublesOnly(CompetitionCreateRequest request) {
        if (isIncludedGenderBelowDoublesMinimum(request)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sameGenderDoublesOnly requires each included gender to have at least 4 players"
            );
        }

        if (sameGenderTargetCalculator.calculate(
                request.getMaleCount(),
                request.getFemaleCount(),
                request.getTotalGames()
        ).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sameGenderDoublesOnly cannot allocate same-gender game counts for the requested player distribution"
            );
        }
    }

    private boolean isIncludedGenderBelowDoublesMinimum(CompetitionCreateRequest request) {
        return isIncludedButBelowDoublesMinimum(request.getMaleCount())
                || isIncludedButBelowDoublesMinimum(request.getFemaleCount());
    }

    private boolean isIncludedButBelowDoublesMinimum(int playerCount) {
        return playerCount > 0 && playerCount < 4;
    }

    private Competition.CompetitionMode resolveMode(String mode) {
        if (mode == null || mode.trim().isEmpty()) {
            return Competition.CompetitionMode.FIXED_SCHEDULE;
        }
        return Competition.CompetitionMode.valueOf(mode.trim().toUpperCase());
    }

    private int calculateRounds(int totalGames, int courtCount) {
        return (int) Math.ceil((double) totalGames / courtCount);
    }
}
