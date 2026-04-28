package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.springframework.stereotype.Service;

@Service
public class CompetitionService {

    private final CompetitionRepository competitionRepository;

    public CompetitionService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    public Competition createCompetition(CompetitionCreateRequest request, int rounds, long seed) {
        return competitionRepository.save(new Competition(
                request.getCompetitionName(),
                request.getMaleCount(),
                request.getFemaleCount(),
                request.getCourtCount(),
                rounds,
                seed
        ));
    }
}
