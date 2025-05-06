package com.tennisfolio.Tennisfolio.api.categoryTournaments;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.api.base.SaveStrategy;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
@Component
public class CategoryTournamentsSaveStrategy implements SaveStrategy<List<Tournament>> {

    private final TournamentRepository tournamentRepository;

    public CategoryTournamentsSaveStrategy(TournamentRepository tournamentRepository) {
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
                        incoming.setTournamentId(exist.getTournamentId());
                    }
                    return incoming;
                }).collect(Collectors.toList());

        return tournamentRepository.saveAll(toSave);
    }
}
