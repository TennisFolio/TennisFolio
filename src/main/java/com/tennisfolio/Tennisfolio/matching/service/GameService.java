package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.domain.GameMatch;
import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import com.tennisfolio.Tennisfolio.matching.domain.ScheduleResult;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.entity.Game;
import com.tennisfolio.Tennisfolio.matching.entity.GameEntry;
import com.tennisfolio.Tennisfolio.matching.repository.GameEntryRepository;
import com.tennisfolio.Tennisfolio.matching.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameEntryRepository gameEntryRepository;

    public GameService(GameRepository gameRepository, GameEntryRepository gameEntryRepository) {
        this.gameRepository = gameRepository;
        this.gameEntryRepository = gameEntryRepository;
    }

    public void saveSchedule(
            Competition competition,
            ScheduleResult result,
            Map<String, CompetitionEntry> entriesByPlayerName
    ) {
        List<Game> games = new ArrayList<>();
        for (GameMatch match : result.matches) {
            games.add(new Game(competition, match.round, match.court, toEntityMatchType(match.type)));
        }
        List<Game> savedGames = gameRepository.saveAll(games);

        List<GameEntry> gameEntries = new ArrayList<>();
        for (int i = 0; i < result.matches.size(); i++) {
            GameMatch match = result.matches.get(i);
            Game game = savedGames.get(i);

            addGameEntries(gameEntries, game, match.teamA, GameEntry.Team.A, entriesByPlayerName);
            addGameEntries(gameEntries, game, match.teamB, GameEntry.Team.B, entriesByPlayerName);
        }
        gameEntryRepository.saveAll(gameEntries);
    }

    private void addGameEntries(
            List<GameEntry> gameEntries,
            Game game,
            List<GamePlayer> players,
            GameEntry.Team team,
            Map<String, CompetitionEntry> entriesByPlayerName
    ) {
        for (int i = 0; i < players.size(); i++) {
            GamePlayer player = players.get(i);
            CompetitionEntry competitionEntry = entriesByPlayerName.get(player.id);
            if (competitionEntry == null) {
                throw new IllegalStateException("CompetitionEntry not found for player: " + player.id);
            }
            gameEntries.add(new GameEntry(game, competitionEntry, team, i + 1));
        }
    }

    private Game.MatchType toEntityMatchType(MatchType type) {
        return switch (type) {
            case MIXED -> Game.MatchType.MIXED;
            case MALE -> Game.MatchType.MALE;
            case FEMALE -> Game.MatchType.FEMALE;
            case RANDOM_M3F1 -> Game.MatchType.RANDOM_M3F1;
            case RANDOM_M1F3 -> Game.MatchType.RANDOM_M1F3;
        };
    }
}
