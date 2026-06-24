package com.tennisfolio.Tennisfolio.matching.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionStat;
import com.tennisfolio.Tennisfolio.matching.entity.GameEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class CompetitionDetailResponse {
    private String publicId;
    private String name;
    private Integer maleCount;
    private Integer femaleCount;
    private Integer courtCount;
    private Integer rounds;
    private String status;
    private String mode;
    private Boolean adminPasswordSet;
    private Boolean ownerUserIdSet;
    private Boolean ownedByCurrentUser;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private CompetitionStatResponse stat;
    private List<GameResponse> games;

    public static CompetitionDetailResponse from(
            Competition competition,
            CompetitionStat stat,
            List<GameEntry> gameEntries
    ) {
        return from(competition, stat, gameEntries, null);
    }

    public static CompetitionDetailResponse from(
            Competition competition,
            CompetitionStat stat,
            List<GameEntry> gameEntries,
            Long currentUserId
    ) {
        Map<Long, List<GameEntry>> gameEntriesByGameId = new LinkedHashMap<>();

        for (GameEntry gameEntry : gameEntries) {
            gameEntriesByGameId.computeIfAbsent(
                    gameEntry.getGame().getId(),
                    ignored -> new ArrayList<>()
            ).add(gameEntry);
        }

        return new CompetitionDetailResponse(
                competition.getPublicId(),
                competition.getName(),
                competition.getMaleCount(),
                competition.getFemaleCount(),
                competition.getCourtCount(),
                competition.getRounds(),
                competition.getStatus().name(),
                competition.getMode() == null ? Competition.CompetitionMode.FIXED_SCHEDULE.name() : competition.getMode().name(),
                competition.hasAdminPassword(),
                competition.getOwnerUserId() != null,
                currentUserId != null && currentUserId.equals(competition.getOwnerUserId()),
                competition.getCreateDt(),
                CompetitionStatResponse.from(stat),
                gameEntriesByGameId.values().stream()
                        .map(entries -> GameResponse.from(entries.get(0).getGame(), entries))
                        .toList()
        );
    }
}
