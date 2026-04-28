package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionEntryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompetitionEntryService {

    private final CompetitionEntryRepository competitionEntryRepository;

    public CompetitionEntryService(CompetitionEntryRepository competitionEntryRepository) {
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
}
