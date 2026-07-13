package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompetitionService {
    private static final int MAX_COMPETITION_NAME_LENGTH = 50;

    private final CompetitionRepository competitionRepository;
    private final CompetitionAdminAuthorizationService competitionAdminAuthorizationService;

    public CompetitionService(
            CompetitionRepository competitionRepository,
            CompetitionAdminAuthorizationService competitionAdminAuthorizationService
    ) {
        this.competitionRepository = competitionRepository;
        this.competitionAdminAuthorizationService = competitionAdminAuthorizationService;
    }

    public Competition createCompetition(CompetitionCreateRequest request, int rounds, long seed) {
        return createCompetition(request, rounds, seed, null);
    }

    public Competition createCompetition(
            CompetitionCreateRequest request,
            int rounds,
            long seed,
            Long ownerUserId
    ) {
        Competition.CompetitionMode mode = resolveMode(request.getMode());
        return competitionRepository.save(new Competition(
                request.getCompetitionName(),
                request.getMaleCount(),
                request.getFemaleCount(),
                request.getCourtCount(),
                rounds,
                seed,
                mode,
                ownerUserId
        ));
    }

    public Competition updateCompetitionName(String publicId, String name, Long currentUserId, String adminToken) {
        String normalizedName = normalizeName(name);
        Competition competition = findEditableCompetition(publicId, currentUserId, adminToken);
        competition.rename(normalizedName);
        return competition;
    }

    public Competition findEditableCompetition(String publicId, Long currentUserId, String adminToken) {
        Competition competition = competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found"));
        competitionAdminAuthorizationService.validateManagementAccess(competition, currentUserId, adminToken);
        return competition;
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("competition name is required");
        }

        String normalizedName = name.trim();
        if (normalizedName.length() > MAX_COMPETITION_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "competition name must be " + MAX_COMPETITION_NAME_LENGTH + " characters or less"
            );
        }
        return normalizedName;
    }

    private Competition.CompetitionMode resolveMode(String mode) {
        if (mode == null || mode.trim().isEmpty()) {
            return Competition.CompetitionMode.FIXED_SCHEDULE;
        }
        return Competition.CompetitionMode.valueOf(mode.trim().toUpperCase());
    }
}
