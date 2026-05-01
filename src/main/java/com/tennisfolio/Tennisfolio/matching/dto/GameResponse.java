package com.tennisfolio.Tennisfolio.matching.dto;

import com.tennisfolio.Tennisfolio.matching.entity.Game;
import com.tennisfolio.Tennisfolio.matching.entity.GameEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class GameResponse {
    private Long gameId;
    private Integer round;
    private Integer court;
    private String matchType;
    private ScoreResponse score;
    private TeamResponse teamA;
    private TeamResponse teamB;

    @Getter
    @AllArgsConstructor
    public static class ScoreResponse {
        private Integer teamAScore;
        private Integer teamBScore;
        private Integer teamATiebreakScore;
        private Integer teamBTiebreakScore;

        private static ScoreResponse from(Game game) {
            return new ScoreResponse(
                    game.getTeamAScore(),
                    game.getTeamBScore(),
                    game.getTeamATiebreaKScore(),
                    game.getTeamBTiebreaKScore()
            );
        }
    }

    @Getter
    @AllArgsConstructor
    public static class TeamResponse {
        private List<GamePlayerResponse> players;
    }

    private static class Builder {
        private final Game game;
        private final List<GamePlayerResponse> teamAPlayers = new ArrayList<>();
        private final List<GamePlayerResponse> teamBPlayers = new ArrayList<>();

        public Builder(Game game) {
            this.game = game;
        }

        public Builder add(GameEntry gameEntry) {
            GamePlayerResponse player = GamePlayerResponse.from(gameEntry);
            if (gameEntry.getTeam() == GameEntry.Team.A) {
                teamAPlayers.add(player);
                return this;
            }
            teamBPlayers.add(player);
            return this;
        }

        public GameResponse build() {
            return new GameResponse(
                    game.getId(),
                    game.getRound(),
                    game.getCourt(),
                    game.getMatchType().name(),
                    ScoreResponse.from(game),
                    new TeamResponse(teamAPlayers),
                    new TeamResponse(teamBPlayers)
            );
        }
    }

    public static GameResponse from(Game game, List<GameEntry> gameEntries) {
        Builder builder = new Builder(game);
        for (GameEntry gameEntry : gameEntries) {
            builder.add(gameEntry);
        }
        return builder.build();
    }
}
