package com.tennisfolio.Tennisfolio.Tournament.application;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments.CategoryTournamentsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails.LeagueDetailsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;
import com.tennisfolio.Tennisfolio.category.domain.model.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentSyncService {
    private final StrategyApiTemplate<List<CategoryTournamentsDTO>, List<Tournament>> categoryTournamentsApiTemplate;
    private final StrategyApiTemplate<TournamentInfoDTO, Tournament> tournamentInfoApiTemplate;
    private final StrategyApiTemplate<LeagueDetailsDTO, Tournament> leagueDetailsApiTemplate;
    private final CategoryRepository categoryRepository;
    private final TournamentRepository tournamentRepository;

    public TournamentSyncService(StrategyApiTemplate<List<CategoryTournamentsDTO>, List<Tournament>> categoryTournamentsApiTemplate, StrategyApiTemplate<TournamentInfoDTO, Tournament> tournamentInfoApiTemplate, StrategyApiTemplate<LeagueDetailsDTO, Tournament> leagueDetailsApiTemplate, CategoryRepository categoryRepository, TournamentRepository tournamentRepository) {
        this.categoryTournamentsApiTemplate = categoryTournamentsApiTemplate;
        this.tournamentInfoApiTemplate = tournamentInfoApiTemplate;
        this.leagueDetailsApiTemplate = leagueDetailsApiTemplate;
        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
    }


    public List<Tournament> saveTournamentList() {
        List<String> categoryRapidIdList = categoryRepository.findAll().stream().map(Category::getRapidCategoryId).collect(Collectors.toList());

        categoryRapidIdList
                .stream()
                .map(rapidId -> categoryTournamentsApiTemplate.execute(rapidId))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return categoryRapidIdList
                .stream()
                .map(rapidId -> tournamentInfoApiTemplate.execute(rapidId))
                .collect(Collectors.toList());

    }

    public List<Tournament> saveTournamentInfo() {

        List<String> tournamentRapidIdList = tournamentRepository.findAll()
                .stream()
                .map(Tournament::getRapidTournamentId)
                .collect(Collectors.toList());

        return tournamentRapidIdList
                .stream()
                .map(rapidId -> tournamentInfoApiTemplate.execute(rapidId))
                .collect(Collectors.toList());
    }

    public List<Tournament> saveLeagueDetails() {
        List<String> tournamentRapidIdList = tournamentRepository.findAll()
                .stream()
                .map(Tournament::getRapidTournamentId)
                .collect(Collectors.toList());

        return tournamentRapidIdList
                .stream()
                .map(rapidId -> leagueDetailsApiTemplate.execute(rapidId))
                .collect(Collectors.toList());
    }
}
