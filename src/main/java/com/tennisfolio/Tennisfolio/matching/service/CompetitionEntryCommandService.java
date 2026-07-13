package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionEntryRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompetitionEntryCommandService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionEntryRepository competitionEntryRepository;
    private final CompetitionAdminAuthorizationService competitionAdminAuthorizationService;

    public CompetitionEntryCommandService(
            CompetitionRepository competitionRepository,
            CompetitionEntryRepository competitionEntryRepository,
            CompetitionAdminAuthorizationService competitionAdminAuthorizationService
    ) {
        this.competitionRepository = competitionRepository;
        this.competitionEntryRepository = competitionEntryRepository;
        this.competitionAdminAuthorizationService = competitionAdminAuthorizationService;
    }

    public Map<String, CompetitionEntry> createCompetitionEntries(Competition competition) {
        return createCompetitionEntries(competition, null);
    }

    public Map<String, CompetitionEntry> createCompetitionEntries(
            Competition competition,
            CompetitionCreateRequest request
    ) {
        List<CompetitionEntry> entries = new ArrayList<>();

        for (int i = 1; i <= competition.getMaleCount(); i++) {
            String defaultName = "M" + i;
            entries.add(new CompetitionEntry(
                    competition,
                    normalizeInitialPlayerName(playerNameAt(request == null ? null : request.getMalePlayerNames(), i), defaultName),
                    CompetitionEntry.Gender.MALE
            ));
        }
        for (int i = 1; i <= competition.getFemaleCount(); i++) {
            String defaultName = "F" + i;
            entries.add(new CompetitionEntry(
                    competition,
                    normalizeInitialPlayerName(playerNameAt(request == null ? null : request.getFemalePlayerNames(), i), defaultName),
                    CompetitionEntry.Gender.FEMALE
            ));
        }

        List<CompetitionEntry> savedEntries = competitionEntryRepository.saveAll(entries);
        Map<String, CompetitionEntry> entryMap = new HashMap<>();
        for (int i = 0; i < savedEntries.size(); i++) {
            CompetitionEntry entry = savedEntries.get(i);
            String defaultName = i < competition.getMaleCount()
                    ? "M" + (i + 1)
                    : "F" + (i - competition.getMaleCount() + 1);
            entryMap.put(defaultName, entry);
        }
        return entryMap;
    }

    @Transactional
    public CompetitionEntryResponse createCompetitionEntry(
            String publicId,
            String adminToken,
            CompetitionEntryCreateRequest request
    ) {
        competitionAdminAuthorizationService.validateAdminToken(publicId, adminToken);
        return createCompetitionEntry(publicId, null, adminToken, request);
    }

    @Transactional
    public CompetitionEntryResponse createCompetitionEntry(
            String publicId,
            Long currentUserId,
            String adminToken,
            CompetitionEntryCreateRequest request
    ) {
        Competition competition = competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        competitionAdminAuthorizationService.validateManagementAccess(competition, currentUserId, adminToken);
        validateClubSession(competition);

        String playerName = normalizePlayerName(request.getPlayerName());
        CompetitionEntry.Gender gender = resolveGender(request.getGender());

        CompetitionEntry entry = competitionEntryRepository.save(new CompetitionEntry(competition, playerName, gender));
        if (gender == CompetitionEntry.Gender.MALE) {
            competition.incrementMaleCount();
        } else {
            competition.incrementFemaleCount();
        }

        return CompetitionEntryResponse.from(entry);
    }

    @Transactional
    public CompetitionEntryResponse updateCompetitionEntry(
            String publicId,
            Long entryId,
            String adminToken,
            CompetitionEntryUpdateRequest request
    ) {
        competitionAdminAuthorizationService.validateAdminToken(publicId, adminToken);
        return updateCompetitionEntry(publicId, entryId, null, adminToken, request);
    }

    @Transactional
    public CompetitionEntryResponse updateCompetitionEntry(
            String publicId,
            Long entryId,
            Long currentUserId,
            String adminToken,
            CompetitionEntryUpdateRequest request
    ) {
        if (request.getPlayerName() == null && request.getGender() == null && request.getStatus() == null) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }

        Competition competition = competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        competitionAdminAuthorizationService.validateManagementAccess(competition, currentUserId, adminToken);
        CompetitionEntry entry = competitionEntryRepository
                .findByIdAndCompetitionId(entryId, competition.getId())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        if (request.getPlayerName() != null) {
            entry.updatePlayerName(normalizePlayerName(request.getPlayerName()));
        }
        if (request.getGender() != null) {
            entry.updateGender(resolveGender(request.getGender()));
        }
        if (request.getStatus() != null) {
            CompetitionEntry.EntryStatus status = resolveEntryStatus(request.getStatus());
            entry.updateStatus(status);
        }

        return CompetitionEntryResponse.from(entry);
    }

    private String normalizePlayerName(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        String normalizedPlayerName = playerName.trim();
        if (normalizedPlayerName.length() > 9) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        return normalizedPlayerName;
    }

    private String playerNameAt(List<String> playerNames, int oneBasedIndex) {
        if (playerNames == null || playerNames.size() < oneBasedIndex) {
            return null;
        }
        return playerNames.get(oneBasedIndex - 1);
    }

    private String normalizeInitialPlayerName(String playerName, String defaultPlayerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return defaultPlayerName;
        }
        return normalizePlayerName(playerName);
    }

    private CompetitionEntry.Gender resolveGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        try {
            return CompetitionEntry.Gender.valueOf(gender.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
    }

    private CompetitionEntry.EntryStatus resolveEntryStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        try {
            return CompetitionEntry.EntryStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
    }

    private void validateClubSession(Competition competition) {
        if (competition.getMode() != Competition.CompetitionMode.CLUB_SESSION) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
    }

}
