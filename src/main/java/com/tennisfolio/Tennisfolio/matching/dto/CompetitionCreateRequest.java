package com.tennisfolio.Tennisfolio.matching.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class CompetitionCreateRequest {
    private final String mode;
    private final String competitionName;
    private final int maleCount;
    private final int femaleCount;
    private final int courtCount;
    private final int totalGames;
    private final Long seed;
    private final List<String> malePlayerNames;
    private final List<String> femalePlayerNames;
    private final boolean sameGenderDoublesOnly;

    @JsonCreator
    public CompetitionCreateRequest(
            @JsonProperty("mode") String mode,
            @JsonProperty("competitionName") String competitionName,
            @JsonProperty("maleCount") int maleCount,
            @JsonProperty("femaleCount") int femaleCount,
            @JsonProperty("courtCount") int courtCount,
            @JsonProperty("totalGames") int totalGames,
            @JsonProperty("seed") Long seed,
            @JsonProperty("malePlayerNames") List<String> malePlayerNames,
            @JsonProperty("femalePlayerNames") List<String> femalePlayerNames,
            @JsonProperty("sameGenderDoublesOnly") boolean sameGenderDoublesOnly
    ) {
        this.mode = mode;
        this.competitionName = competitionName;
        this.maleCount = maleCount;
        this.femaleCount = femaleCount;
        this.courtCount = courtCount;
        this.totalGames = totalGames;
        this.seed = seed;
        this.malePlayerNames = malePlayerNames;
        this.femalePlayerNames = femalePlayerNames;
        this.sameGenderDoublesOnly = sameGenderDoublesOnly;
    }

    public CompetitionCreateRequest(
            String mode,
            String competitionName,
            int maleCount,
            int femaleCount,
            int courtCount,
            int totalGames,
            Long seed,
            List<String> malePlayerNames,
            List<String> femalePlayerNames
    ) {
        this(
                mode,
                competitionName,
                maleCount,
                femaleCount,
                courtCount,
                totalGames,
                seed,
                malePlayerNames,
                femalePlayerNames,
                false
        );
    }
}
