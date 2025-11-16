package com.tennisfolio.Tennisfolio.infrastructure.worker.match;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.worker.AbstractBatchPipeline;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerRepository;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MatchBatchPipeline extends AbstractBatchPipeline<Match> {
    private final SeasonRepository seasonRepository;
    private final RoundRepository roundRepository;
    private final PlayerProvider playerProvider;
    private final MatchRepository matchRepository;

    public MatchBatchPipeline(SeasonRepository seasonRepository, RoundRepository roundRepository, PlayerProvider playerProvider, MatchRepository matchRepository) {
        this.seasonRepository = seasonRepository;
        this.roundRepository = roundRepository;
        this.playerProvider = playerProvider;
        this.matchRepository = matchRepository;
    }

    @Override
    @Transactional(readOnly = true)
    protected List<Match> enrich(List<Match> batch) {

        // 1) 필요한 키 수집
        Set<String> rapidSeasonIds = batch.stream()
                .map(m -> m.getSeason() != null ? m.getSeason().getRapidSeasonId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        record RoundKey(String rapidSeasonId, Long roundNum) {}

        Set<RoundKey> roundKeys = batch.stream()
                .map(m -> {
                    if (m.getSeason() == null || m.getRound() == null) return null;
                    return new RoundKey(m.getSeason().getRapidSeasonId(), m.getRound().getRound());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        Set<String> playerRapidIds = batch.stream()
                .flatMap(m -> Stream.of(
                        m.getHomePlayer() != null ? m.getHomePlayer().getRapidPlayerId() : null,
                        m.getAwayPlayer() != null ? m.getAwayPlayer().getRapidPlayerId() : null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        // 2) 배치 조회: Season
        Map<String, Season> seasonMap = seasonRepository.findByRapidSeasonIdIn(rapidSeasonIds).stream()
                .collect(Collectors.toMap(Season::getRapidSeasonId, Function.identity()));



        // 3) 배치 조회: Round (Season+roundNum 묶음으로)
        Map<RoundKey, Round> roundMap = new HashMap<>();
        Map<String, Set<Long>> seasonToRoundNums = new HashMap<>();

        for (RoundKey key : roundKeys) {
            seasonToRoundNums.computeIfAbsent(key.rapidSeasonId, k -> new HashSet<>())
                    .add(key.roundNum);
        }
        for (var e : seasonToRoundNums.entrySet()) {
            Season season = seasonMap.get(e.getKey());
            if (season == null) continue;
            List<Round> rounds = roundRepository.findBySeasonAndRoundIn(season, e.getValue());
            for (Round r : rounds) {
                roundMap.put(new RoundKey(season.getRapidSeasonId(), r.getRound()), r);
            }
        }


        // 4) 배치 조회: Player (provider가 배치 제공 없으면 반복 조회/캐시)
        Map<String, Player> playerMap = playerRapidIds.stream()
                .collect(HashMap::new, (map, pid) -> {
                    try { map.put(pid, playerProvider.provide(pid)); }
                    catch (Exception ex) { /* 로깅 후 건너뛰기 */ }
                }, HashMap::putAll);


        // 5) 매치 엔리치: 관계 일관성 유지
        List<Match> enriched = new ArrayList<>(batch.size());
        for (Match m : batch) {
            var sRapid = m.getSeason() != null ? m.getSeason().getRapidSeasonId() : null;
            var rNum = m.getRound() != null ? m.getRound().getRound() : null;
            if (sRapid == null || rNum == null) continue;

            Season season = seasonMap.get(sRapid);
            Round round = roundMap.get(new RoundKey(sRapid, rNum));
            if (season == null || round == null) continue;

            Player home = m.getHomePlayer() != null ? playerMap.get(m.getHomePlayer().getRapidPlayerId()) : null;
            Player away = m.getAwayPlayer() != null ? playerMap.get(m.getAwayPlayer().getRapidPlayerId()) : null;
            if (home == null || away == null) continue;

            m.updatePlayer(home, away);
            m.updateRound(round);
            // 중복 필드가 있다면 명시적으로 맞추기
            // m.setSeason(round.getSeason());

            enriched.add(m);
        }

        return enriched;




//        batch.stream().forEach(p -> {
//           Season season = seasonRepository.findByRapidSeasonId(p.getSeason().getRapidSeasonId())
//                   .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
//
//           Round round = p.getRound();
//           Round findRound = roundRepository.findBySeasonAndRound(season, round.getRound())
//                   .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
//
//           Player homePlayer = playerProvider.provide(p.getHomePlayer().getRapidPlayerId());
//           Player awayPlayer = playerProvider.provide(p.getAwayPlayer().getRapidPlayerId());
//
//           p.updatePlayer(homePlayer, awayPlayer);
//           p.updateRound(findRound);
//
//        });

//        return batch;
    }

    @Override
    protected void save(List<Match> batchEntities) {
        List<String> rapidMatchIds = batchEntities.stream().map(p -> p.getRapidMatchId()).collect(Collectors.toList());
        Set<String> findRapidMatchIds = matchRepository.findRapidMatchIdByRapidMatchIds(rapidMatchIds);

        List<Match> savePossibleMatches = batchEntities
                .stream()
                .filter(p -> !findRapidMatchIds.contains(p.getRapidMatchId()))
                .collect(Collectors.toList());

        matchRepository.saveAll(savePossibleMatches);
    }
}
