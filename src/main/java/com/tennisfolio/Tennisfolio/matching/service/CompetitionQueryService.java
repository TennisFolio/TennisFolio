package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionDetailResponse;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionStat;
import com.tennisfolio.Tennisfolio.matching.entity.GameEntry;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionStatRepository;
import com.tennisfolio.Tennisfolio.matching.repository.GameEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompetitionQueryService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionStatRepository competitionStatRepository;
    private final GameEntryRepository gameEntryRepository;

    public CompetitionQueryService(
            CompetitionRepository competitionRepository,
            CompetitionStatRepository competitionStatRepository,
            GameEntryRepository gameEntryRepository
    ) {
        this.competitionRepository = competitionRepository;
        this.competitionStatRepository = competitionStatRepository;
        this.gameEntryRepository = gameEntryRepository;
    }

    @Transactional(readOnly = true)
    public CompetitionDetailResponse getCompetition(String publicId) {
        Competition competition = competitionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        CompetitionStat stat = competitionStatRepository.findByCompetitionId(competition.getId()).orElse(null);
        List<GameEntry> gameEntries = gameEntryRepository.findScheduleEntriesByCompetitionId(competition.getId());

        return CompetitionDetailResponse.from(competition, stat, gameEntries);
    }
}
