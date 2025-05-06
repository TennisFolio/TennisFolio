package com.tennisfolio.Tennisfolio.Tournament.service;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.categoryTournaments.CategoryTournamentsDTO;
import com.tennisfolio.Tennisfolio.api.leagueDetails.LeagueDetailsDTO;
import com.tennisfolio.Tennisfolio.api.tournamentInfo.TournamentInfoDTO;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentServiceImpl implements TournamentService{
    private final AbstractApiTemplate<List<CategoryTournamentsDTO>, List<Tournament>> categoryTournamentsApiTemplate;
    private final AbstractApiTemplate<TournamentInfoDTO, Tournament> tournamentInfoApiTemplate;
    private final AbstractApiTemplate<LeagueDetailsDTO, Tournament> leagueDetailsApiTemplate;
    private final CategoryRepository categoryRepository;
    private final TournamentRepository tournamentRepository;

    public TournamentServiceImpl(AbstractApiTemplate<List<CategoryTournamentsDTO>, List<Tournament>> categoryTournamentsApiTemplate, AbstractApiTemplate<TournamentInfoDTO, Tournament> tournamentInfoApiTemplate, AbstractApiTemplate<LeagueDetailsDTO, Tournament> leagueDetailsApiTemplate, CategoryRepository categoryRepository, TournamentRepository tournamentRepository) {
        this.categoryTournamentsApiTemplate = categoryTournamentsApiTemplate;
        this.tournamentInfoApiTemplate = tournamentInfoApiTemplate;
        this.leagueDetailsApiTemplate = leagueDetailsApiTemplate;
        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @Override
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

    @Override
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

    @Override
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
