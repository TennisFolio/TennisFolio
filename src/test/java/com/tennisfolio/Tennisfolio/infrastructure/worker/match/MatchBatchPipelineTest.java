package com.tennisfolio.Tennisfolio.infrastructure.worker.match;

import com.tennisfolio.Tennisfolio.fixtures.MatchFixtures;
import com.tennisfolio.Tennisfolio.fixtures.PlayerFixtures;
import com.tennisfolio.Tennisfolio.fixtures.RoundFixtures;
import com.tennisfolio.Tennisfolio.fixtures.SeasonFixtures;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.mock.FakeMatchRepository;
import com.tennisfolio.Tennisfolio.mock.FakeRoundRepository;
import com.tennisfolio.Tennisfolio.mock.FakeSeasonRepository;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MatchBatchPipelineTest {

    private final SeasonRepository seasonRepository = new FakeSeasonRepository();
    private final RoundRepository roundRepository = new FakeRoundRepository();
    @Mock
    private PlayerProvider playerProvider;
    private final MatchRepository matchRepository = new FakeMatchRepository();

    private MatchBatchPipeline matchBatchPipeline;

    @BeforeEach
    public void setUp(){
        matchBatchPipeline = new MatchBatchPipeline(seasonRepository, roundRepository, playerProvider, matchRepository);
        when(playerProvider.provide("275923")).thenReturn(PlayerFixtures.alcaraz());
        when(playerProvider.provide("14486")).thenReturn(PlayerFixtures.nadal());
    }

    @Test
    void enrich_and_save_should_work() {

        // 1) Season
        Season season = SeasonFixtures.wimbledonMen2025();
        seasonRepository.save(season);

        // 2) Round
        Round round = RoundFixtures.wimbledonMen2025Final();
        roundRepository.save(round);

        // 3) Player
        Player home = PlayerFixtures.alcaraz();
        Player away = PlayerFixtures.nadal();
        playerProvider.provide(home.getRapidPlayerId());
        playerProvider.provide(away.getRapidPlayerId());

        // 4) Match 원본 (PK는 null)
        Match match = MatchFixtures.wimbledonMen2025FinalMatch();

        List<Match> batch = List.of(match);

        // WHEN enrich
        List<Match> enriched = matchBatchPipeline.enrich(batch);

        // THEN enrich 검증
        assertEquals(1, enriched.size());
        Match em = enriched.get(0);

        assertEquals("63966", em.getSeason().getRapidSeasonId());
        assertEquals(29L, em.getRound().getRound());
        assertEquals("275923", em.getHomePlayer().getRapidPlayerId());
        assertEquals("14486", em.getAwayPlayer().getRapidPlayerId());

        // WHEN save
        matchBatchPipeline.save(enriched);

        // THEN: save됨
        assertEquals(1, matchRepository.findAll().stream().count());
        assertTrue(matchRepository.findByRapidMatchId("1").isPresent());

        Long assignedId = enriched.get(0).getMatchId();
        assertNotNull(assignedId);

        // 동일 rapidMatchId 다시 저장 시
        matchBatchPipeline.save(enriched);

        // THEN: 중복 저장 안 됨
        assertEquals(1, matchRepository.findAll().stream().count());
    }

}
