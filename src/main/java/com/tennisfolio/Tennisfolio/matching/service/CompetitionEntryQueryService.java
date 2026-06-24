package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryResponse;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionEntryRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompetitionEntryQueryService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionEntryRepository competitionEntryRepository;

    public CompetitionEntryQueryService(
            CompetitionRepository competitionRepository,
            CompetitionEntryRepository competitionEntryRepository
    ) {
        this.competitionRepository = competitionRepository;
        this.competitionEntryRepository = competitionEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<CompetitionEntryResponse> getCompetitionEntries(String publicId) {
        Competition competition = competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        return competitionEntryRepository.findByCompetitionIdOrderByIdAsc(competition.getId())
                .stream()
                .map(CompetitionEntryResponse::from)
                .toList();
    }
}
