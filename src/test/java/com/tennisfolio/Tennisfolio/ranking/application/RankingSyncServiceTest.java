package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.common.RankingCategory;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageService;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakePlayerRepository;
import com.tennisfolio.Tennisfolio.mock.FakeRankingRepository;
import com.tennisfolio.Tennisfolio.mock.atpRanking.FakeAtpRankingApiTemplate;
import com.tennisfolio.Tennisfolio.mock.atpRanking.FakeAtpRankingEntityMapper;
import com.tennisfolio.Tennisfolio.mock.teamDetails.FakeTeamDetailsApiTemplate;
import com.tennisfolio.Tennisfolio.mock.teamDetails.FakeTeamDetailsEntityMapper;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingSyncServiceTest {

    @InjectMocks
    private RankingSyncService rankingSyncService;

    @Mock
    private ApiWorker apiWorker;

    @Mock
    private RankingRepository rankingRepository;

    @Mock
    private PlayerProvider playerProvider;

    /**
     * 이미 최신 데이터라면 저장하지 않는다
     */
    @Test
    void 이미_최신_데이터면_저장하지_않는다() {

        // given
        Ranking ranking = mock(Ranking.class);

        when(ranking.getLastUpdate()).thenReturn("20250301");

        when(apiWorker.process(RapidApi.WTARANKINGS))
                .thenReturn(List.of(ranking));

        when(rankingRepository.findTopLastUpdateByCategoryOrderByLastUpdateDesc(RankingCategory.WTA))
                .thenReturn(Optional.of("20250301"));

        // when
        rankingSyncService.saveWtaRanking();

        // then
        verify(rankingRepository, never()).collect(anyList());
        verify(rankingRepository, never()).flushAll();
    }


    /**
     * 이전 랭킹 데이터가 없다면 그대로 저장한다
     */
    @Test
    void 이전_랭킹이_없으면_그대로_저장한다() {

        // given
        Ranking ranking = mock(Ranking.class);
        Player player = mock(Player.class);

        when(ranking.getLastUpdate()).thenReturn("20250302");
        when(ranking.getPlayer()).thenReturn(player);

        when(player.getRapidPlayerId()).thenReturn("1");
        when(player.getPlayerId()).thenReturn(1L);

        when(apiWorker.process(RapidApi.WTARANKINGS))
                .thenReturn(List.of(ranking));

        when(rankingRepository.findTopLastUpdateByCategoryOrderByLastUpdateDesc(RankingCategory.WTA))
                .thenReturn(Optional.empty());

        when(playerProvider.provide("1")).thenReturn(player);

        // when
        rankingSyncService.saveWtaRanking();

        // then
        verify(rankingRepository).collect(anyList());
        verify(rankingRepository).flushAll();
    }


    /**
     * 이전 랭킹이 있다면 previousPoints를 적용한다
     */
    @Test
    void 이전_랭킹이_있으면_previousPoints를_적용한다() {

        // given
        Ranking ranking = mock(Ranking.class);
        Ranking beforeRanking = mock(Ranking.class);
        Player player = mock(Player.class);

        when(ranking.getLastUpdate()).thenReturn("20250302");
        when(ranking.getPlayer()).thenReturn(player);

        when(player.getRapidPlayerId()).thenReturn("1");
        when(player.getPlayerId()).thenReturn(1L);

        when(apiWorker.process(RapidApi.WTARANKINGS))
                .thenReturn(List.of(ranking));

        when(rankingRepository.findTopLastUpdateByCategoryOrderByLastUpdateDesc(RankingCategory.WTA))
                .thenReturn(Optional.of("20250301"));

        when(rankingRepository.findByLastUpdateAndCategory("20250301", RankingCategory.WTA))
                .thenReturn(List.of(beforeRanking));

        when(beforeRanking.getPlayer()).thenReturn(player);
        when(beforeRanking.getCurPoints()).thenReturn(2000L);

        when(playerProvider.provide("1")).thenReturn(player);

        // when
        rankingSyncService.saveWtaRanking();

        // then
        verify(ranking).applyPreviousPoints(2000L);
        verify(rankingRepository).collect(anyList());
        verify(rankingRepository).flushAll();
    }
}
