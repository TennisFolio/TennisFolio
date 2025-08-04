package com.tennisfolio.Tennisfolio.Tournament.application;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.category.application.CategoryService;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryEntity;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments.CategoryTournamentsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails.LeagueDetailsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TournamentSyncService {
    private final StrategyApiTemplate<List<CategoryTournamentsDTO>, List<Tournament>> categoryTournamentsApiTemplate;
    private final StrategyApiTemplate<TournamentInfoDTO, Tournament> tournamentInfoApiTemplate;
    private final StrategyApiTemplate<LeagueDetailsDTO, Tournament> leagueDetailsApiTemplate;
    private final CategoryService categoryService;
    private final TournamentRepository tournamentRepository;
    private final TransactionTemplate transactionTemplate;


    public TournamentSyncService(StrategyApiTemplate<List<CategoryTournamentsDTO>,
                                 List<Tournament>> categoryTournamentsApiTemplate,
                                 StrategyApiTemplate<TournamentInfoDTO, Tournament> tournamentInfoApiTemplate,
                                 StrategyApiTemplate<LeagueDetailsDTO, Tournament> leagueDetailsApiTemplate,
                                 CategoryService categoryService,
                                 TournamentRepository tournamentRepository,
                                 PlatformTransactionManager transactionManager) {
        this.categoryTournamentsApiTemplate = categoryTournamentsApiTemplate;
        this.tournamentInfoApiTemplate = tournamentInfoApiTemplate;
        this.leagueDetailsApiTemplate = leagueDetailsApiTemplate;
        this.categoryService = categoryService;
        this.tournamentRepository = tournamentRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
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
                // api 조회
                .map(rapidId -> categoryTournamentsApiTemplate.execute(rapidId))
                .flatMap(List::stream)
                .filter(tournament -> !existingKeys.contains(tournament.getRapidTournamentId()))
                .forEach(tournament -> {
                    tournamentRepository.collect(tournament);
                    tournamentRepository.flushWhenFull();
                });


        // 남은 데이터 저장
        tournamentRepository.flushAll();
    }

    public void saveTournamentDetail(){

        List<Tournament> findTournaments = tournamentRepository.findAll();
        findTournaments
                .stream()
                .map(tournament -> {
                    try{
                        Tournament tournamentInfo = tournamentInfoApiTemplate.execute(tournament.getRapidTournamentId());
                        Tournament leagueDetails = leagueDetailsApiTemplate.execute(tournament.getRapidTournamentId());
                        if(tournamentInfo != null){
                            tournamentInfo.mergeTournament(leagueDetails);
                            tournamentRepository.collect(tournamentInfo);
                            tournamentRepository.flushWhenFull();
                        }
                        return tournamentInfo;
                    }catch(Exception e){
                        e.printStackTrace();
                        System.out.println(tournament.getRapidTournamentId());
                    }

                    return null;
                })
                .toList();

        tournamentRepository.flushAll();


    }
}
