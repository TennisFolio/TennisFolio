package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
@Component
public class CategoryTournamentsEntitySaver implements EntitySaver<List<Tournament>> {

    private final TournamentRepository tournamentRepository;

    public CategoryTournamentsEntitySaver(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public List<Tournament> save(List<Tournament> entity) {
        // 저장할 tournament id
        List<String> ids = entity.stream()
                .map(Tournament::getRapidTournamentId)
                .collect(Collectors.toList());

        Map<String,Tournament> existingMap = tournamentRepository.findByRapidTournamentIds(ids).stream()
                .collect(Collectors.toMap(Tournament::getRapidTournamentId, Function.identity()));

        List<Tournament> toSave = entity.stream().map(
                incoming -> {
                    Tournament exist = existingMap.get(incoming.getRapidTournamentId());
                    if(exist != null){
                        return null;
                    }
                    return incoming;
                }).collect(Collectors.toList());

        return tournamentRepository.saveAll(toSave);
    }
}
