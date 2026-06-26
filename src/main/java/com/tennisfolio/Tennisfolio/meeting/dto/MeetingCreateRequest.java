package com.tennisfolio.Tennisfolio.meeting.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MeetingCreateRequest {
    private final String title;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime startAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime endAt;
    private final String note;
    private final Integer maxParticipants;
    private final Integer maxMaleParticipants;
    private final Integer maxFemaleParticipants;
    private final Integer courtCount;
    private final Integer totalGames;

    @JsonCreator
    public MeetingCreateRequest(
            @JsonProperty("title") String title,
            @JsonProperty("startAt") LocalDateTime startAt,
            @JsonProperty("endAt") LocalDateTime endAt,
            @JsonProperty("note") String note,
            @JsonProperty("maxParticipants") Integer maxParticipants,
            @JsonProperty("maxMaleParticipants") Integer maxMaleParticipants,
            @JsonProperty("maxFemaleParticipants") Integer maxFemaleParticipants,
            @JsonProperty("courtCount") Integer courtCount,
            @JsonProperty("totalGames") Integer totalGames
    ) {
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.note = note;
        this.maxParticipants = maxParticipants;
        this.maxMaleParticipants = maxMaleParticipants;
        this.maxFemaleParticipants = maxFemaleParticipants;
        this.courtCount = courtCount;
        this.totalGames = totalGames;
    }
}
