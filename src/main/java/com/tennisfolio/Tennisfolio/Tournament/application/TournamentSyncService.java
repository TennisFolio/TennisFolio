package com.tennisfolio.Tennisfolio.Tournament.application;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.infrastructure.repository.TournamentJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments.CategoryTournamentsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails.LeagueDetailsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;
import com.tennisfolio.Tennisfolio.category.domain.model.Category;
import com.tennisfolio.Tennisfolio.infrastructure.repository.CategoryJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentSyncService {
    private final StrategyApiTemplate<List<CategoryTournamentsDTO>, List<Tournament>> categoryTournamentsApiTemplate;
    private final StrategyApiTemplate<TournamentInfoDTO, Tournament> tournamentInfoApiTemplate;
    private final StrategyApiTemplate<LeagueDetailsDTO, Tournament> leagueDetailsApiTemplate;
    private final CategoryJpaRepository categoryJpaRepository;
    private final TournamentRepository tournamentRepository;

    public TournamentSyncService(StrategyApiTemplate<List<CategoryTournamentsDTO>,
                                 List<Tournament>> categoryTournamentsApiTemplate,
                                 StrategyApiTemplate<TournamentInfoDTO, Tournament> tournamentInfoApiTemplate,
                                 StrategyApiTemplate<LeagueDetailsDTO, Tournament> leagueDetailsApiTemplate,
                                 CategoryJpaRepository categoryJpaRepository,
                                 TournamentRepository tournamentRepository) {
        this.categoryTournamentsApiTemplate = categoryTournamentsApiTemplate;
        this.tournamentInfoApiTemplate = tournamentInfoApiTemplate;
        this.leagueDetailsApiTemplate = leagueDetailsApiTemplate;
        this.categoryJpaRepository = categoryJpaRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @Transactional
    public List<Tournament> saveTournamentList() {
        List<String> categoryRapidIdList = categoryJpaRepository.findAll().stream().map(Category::getRapidCategoryId).collect(Collectors.toList());

        // 카테고리별 토너먼트 리스트
        List<Tournament> categoryTournamentList = categoryRapidIdList
                .stream()
                .map(rapidId -> categoryTournamentsApiTemplate.execute(rapidId))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        List<Tournament> tournaments = categoryTournamentList
                .stream()
                .map(tournament -> {
                    Tournament tournamentInfo = tournamentInfoApiTemplate.execute(tournament.getRapidTournamentId());
                    Tournament leagueDetails = leagueDetailsApiTemplate.execute(tournament.getRapidTournamentId());
                    tournamentInfo.mergeTournament(leagueDetails);
                    tournamentRepository.bufferedSave(tournamentInfo);
                    return tournamentInfo;
                }).toList();

        tournamentRepository.flush();

        return tournaments;

    }
}
