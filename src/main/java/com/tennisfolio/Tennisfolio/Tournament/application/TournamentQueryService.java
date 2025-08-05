package com.tennisfolio.Tennisfolio.Tournament.application;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TournamentQueryService {

    private final TournamentRepository tournamentRepository;

    public TournamentQueryService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    public List<Tournament> getByCategory(List<Category> categories){
        return tournamentRepository.findByCategoryIn(categories);
    }
}
