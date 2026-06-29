package com.tennisfolio.Tennisfolio.meeting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MeetingSummaryResponse {
    private String publicId;
    private String title;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endAt;
    private Integer courtCount;
    private Integer totalGames;
    private Long attendingCount;
    private Long maybeCount;
    private Long notAttendingCount;
    private String status;
    private Boolean competitionCreated;

    public static MeetingSummaryResponse from(
            Meeting meeting,
            long attendingCount,
            long maybeCount,
            long notAttendingCount
    ) {
        return new MeetingSummaryResponse(
                meeting.getPublicId(),
                meeting.getTitle(),
                meeting.getStartAt(),
                meeting.getEndAt(),
                meeting.getCourtCount(),
                meeting.getTotalGames(),
                attendingCount,
                maybeCount,
                notAttendingCount,
                meeting.getStatus().name(),
                meeting.hasCompetition()
        );
    }
}
