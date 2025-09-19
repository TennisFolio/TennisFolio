package com.tennisfolio.Tennisfolio.Tournament.application;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.category.application.CategoryService;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryEntity;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments.CategoryTournamentsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails.LeagueDetailsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TournamentSyncService {
    private final ApiWorker apiWorker;
    private final CategoryService categoryService;
    private final TournamentRepository tournamentRepository;


    public TournamentSyncService(
            ApiWorker apiWorker,
            CategoryService categoryService,
            TournamentRepository tournamentRepository) {
        this.apiWorker = apiWorker;
        this.categoryService = categoryService;
        this.tournamentRepository = tournamentRepository;
    }


    public void saveTournamentList() {
        // 중복되는 category 제외
        List<String> notInIds = List.of("-100", "-101");

        // DB에 저장된 category 조회
        List<String> categoryRapidIdList = categoryService.getByRapidCategoryIdNotIn(notInIds)
                .stream()
                .map(Category::getRapidCategoryId)
                .collect(Collectors.toList());

        // 중복 제거를 위한 apiId 조회
        Set<String> existingKeys = tournamentRepository.findAllRapidIds();

        // 카테고리별 토너먼트 리스트
        categoryRapidIdList
                .stream()
                .map(rapidId -> apiWorker.<List<Tournament>, List<Tournament>>process(RapidApi.CATEGORYTOURNAMENTS, rapidId))
                .flatMap(List::stream)
                .filter(tournament -> !existingKeys.contains(tournament.getRapidTournamentId()))
                .forEach(tournament -> {
                    tournamentRepository.collect(tournament);
                    tournamentRepository.flushWhenFull();
                });


        // 남은 데이터 저장
        tournamentRepository.flushAll();
    }

    public void saveTournamentInfo(){
        List<Tournament> findTournaments = tournamentRepository.findAll();

        findTournaments
                .stream()
                .map(this::updateTournamentInfo)
                .filter(Objects::nonNull)
                .toList();

        tournamentRepository.flushAll();
    }

    private Tournament updateTournamentInfo(Tournament tournament){
        String rapidId = tournament.getRapidTournamentId();
        Tournament tournamentInfo = tournament;

        if (tournament.needsTournamentInfo()) {
            Tournament fetched = apiWorker.process(RapidApi.TOURNAMENTINFO, rapidId);
            if(fetched != null){
                tournamentInfo = fetched;
                tournamentRepository.collect(tournamentInfo);
                tournamentRepository.flushWhenFull();
                return tournamentInfo;
            }else{
                log.warn("tournamentInfoApiTemplate returned null for rapidId={}", rapidId);
            }

        }
        return tournament;
    }

    public void saveLeagueDetails(){
        List<Tournament> findTournaments = tournamentRepository.findAllWithPlayers();

        findTournaments
                .stream()
                .map(this::updateLeagueDetails)
                .filter(Objects::nonNull)
                .toList();

        tournamentRepository.flushAll();
    }

    private Tournament updateLeagueDetails(Tournament tournament){
        String rapidId = tournament.getRapidTournamentId();

        if (tournament.needsLeagueDetails()) {
            Tournament fetched = apiWorker.process(RapidApi.LEAGUEDETAILS, rapidId);
            if(fetched != null){
                tournamentRepository.collect(fetched);
                tournamentRepository.flushWhenFull();
                return fetched;
            }else{
                log.warn("leagueDetailsApiTemplate returned null for rapidId={}", rapidId);

            }
        }
        return tournament;

    }

}
