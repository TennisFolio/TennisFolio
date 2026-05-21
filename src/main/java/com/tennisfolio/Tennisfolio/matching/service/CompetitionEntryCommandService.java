package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionEntryRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompetitionEntryCommandService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionEntryRepository competitionEntryRepository;

    public CompetitionEntryCommandService(
            CompetitionRepository competitionRepository,
            CompetitionEntryRepository competitionEntryRepository
    ) {
        this.competitionRepository = competitionRepository;
        this.competitionEntryRepository = competitionEntryRepository;
    }

    public Map<String, CompetitionEntry> createCompetitionEntries(Competition competition) {
        List<CompetitionEntry> entries = new ArrayList<>();

        for (int i = 1; i <= competition.getMaleCount(); i++) {
            entries.add(new CompetitionEntry(competition, "M" + i, CompetitionEntry.Gender.MALE));
        }
        for (int i = 1; i <= competition.getFemaleCount(); i++) {
            entries.add(new CompetitionEntry(competition, "F" + i, CompetitionEntry.Gender.FEMALE));
        }

        List<CompetitionEntry> savedEntries = competitionEntryRepository.saveAll(entries);
        Map<String, CompetitionEntry> entryMap = new HashMap<>();
        for (CompetitionEntry entry : savedEntries) {
            entryMap.put(entry.getPlayerName(), entry);
        }
        return entryMap;
    }

    @Transactional
    public CompetitionEntryResponse createCompetitionEntry(
            String publicId,
            String editToken,
            CompetitionEntryCreateRequest request
    ) {
        Competition competition = competitionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        validateEditToken(competition, editToken);
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
            String editToken,
            CompetitionEntryUpdateRequest request
    ) {
        if (request.getPlayerName() == null && request.getGender() == null && request.getStatus() == null) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }

        Competition competition = competitionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        validateEditToken(competition, editToken);
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

    private void validateEditToken(Competition competition, String editToken) {
        if (editToken == null || !competition.getEditToken().equals(editToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid edit token");
        }
    }
}
