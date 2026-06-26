package com.tennisfolio.Tennisfolio.meeting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MeetingDetailResponse {
    private String publicId;
    private Long ownerUserId;
    private Long competitionId;
    private String title;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endAt;
    private String note;
    private Integer maxParticipants;
    private Integer maxMaleParticipants;
    private Integer maxFemaleParticipants;
    private Integer courtCount;
    private Integer totalGames;
    private String status;
    private Boolean ownedByCurrentUser;
    private Boolean competitionCreated;

    public static MeetingDetailResponse from(Meeting meeting, Long currentUserId) {
        return new MeetingDetailResponse(
                meeting.getPublicId(),
                meeting.getOwnerUserId(),
                meeting.getCompetitionId(),
                meeting.getTitle(),
                meeting.getStartAt(),
                meeting.getEndAt(),
                meeting.getNote(),
                meeting.getMaxParticipants(),
                meeting.getMaxMaleParticipants(),
                meeting.getMaxFemaleParticipants(),
                meeting.getCourtCount(),
                meeting.getTotalGames(),
                meeting.getStatus().name(),
                meeting.isOwnedBy(currentUserId),
                meeting.hasCompetition()
        );
    }
}
