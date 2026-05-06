package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
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
    public CompetitionEntryResponse updateCompetitionEntry(
            String publicId,
            Long entryId,
            String editToken,
            CompetitionEntryUpdateRequest request
    ) {
        if (request.getPlayerName() == null || request.getPlayerName().trim().isEmpty()) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        String playerName = request.getPlayerName().trim();
        if (playerName.length() > 9) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }

        Competition competition = competitionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        validateEditToken(competition, editToken);
        CompetitionEntry entry = competitionEntryRepository
                .findByIdAndCompetitionId(entryId, competition.getId())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        entry.updatePlayerName(playerName);

        return CompetitionEntryResponse.from(entry);
    }

    private void validateEditToken(Competition competition, String editToken) {
        if (editToken == null || !competition.getEditToken().equals(editToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid edit token");
        }
    }
}
