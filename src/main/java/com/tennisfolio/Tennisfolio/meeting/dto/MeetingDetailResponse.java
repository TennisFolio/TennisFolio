package com.tennisfolio.Tennisfolio.meeting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MeetingDetailResponse {
    private String publicId;
    private Long competitionId;
    private String competitionPublicId;
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
    private String ownerNickName;
    private Boolean competitionCreated;
    private String clubName;
    private Boolean clubMeeting;
    private List<MeetingAttendanceResponse> attendances;

    public MeetingDetailResponse(
            String publicId,
            Long competitionId,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String note,
            Integer maxParticipants,
            Integer maxMaleParticipants,
            Integer maxFemaleParticipants,
            Integer courtCount,
            Integer totalGames,
            String status,
            Boolean ownedByCurrentUser,
            String ownerNickName,
            Boolean competitionCreated,
            List<MeetingAttendanceResponse> attendances
    ) {
        this(
                publicId,
                competitionId,
                null,
                title,
                startAt,
                endAt,
                note,
                maxParticipants,
                maxMaleParticipants,
                maxFemaleParticipants,
                courtCount,
                totalGames,
                status,
                ownedByCurrentUser,
                ownerNickName,
                competitionCreated,
                null,
                false,
                attendances
        );
    }

    public MeetingDetailResponse(
            String publicId,
            Long competitionId,
            String competitionPublicId,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String note,
            Integer maxParticipants,
            Integer maxMaleParticipants,
            Integer maxFemaleParticipants,
            Integer courtCount,
            Integer totalGames,
            String status,
            Boolean ownedByCurrentUser,
            String ownerNickName,
            Boolean competitionCreated,
            List<MeetingAttendanceResponse> attendances
    ) {
        this(
                publicId,
                competitionId,
                competitionPublicId,
                title,
                startAt,
                endAt,
                note,
                maxParticipants,
                maxMaleParticipants,
                maxFemaleParticipants,
                courtCount,
                totalGames,
                status,
                ownedByCurrentUser,
                ownerNickName,
                competitionCreated,
                null,
                attendances
        );
    }

    public MeetingDetailResponse(
            String publicId,
            Long competitionId,
            String competitionPublicId,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String note,
            Integer maxParticipants,
            Integer maxMaleParticipants,
            Integer maxFemaleParticipants,
            Integer courtCount,
            Integer totalGames,
            String status,
            Boolean ownedByCurrentUser,
            String ownerNickName,
            Boolean competitionCreated,
            Boolean clubMeeting,
            List<MeetingAttendanceResponse> attendances
    ) {
        this(
                publicId,
                competitionId,
                competitionPublicId,
                title,
                startAt,
                endAt,
                note,
                maxParticipants,
                maxMaleParticipants,
                maxFemaleParticipants,
                courtCount,
                totalGames,
                status,
                ownedByCurrentUser,
                ownerNickName,
                competitionCreated,
                null,
                false,
                attendances
        );
    }

    public static MeetingDetailResponse from(Meeting meeting, Long currentUserId) {
        return from(meeting, currentUserId, null, List.of());
    }

    public static MeetingDetailResponse from(
            Meeting meeting,
            Long currentUserId,
            List<MeetingAttendance> attendances
    ) {
        return from(meeting, currentUserId, null, attendances);
    }

    public static MeetingDetailResponse from(
            Meeting meeting,
            Long currentUserId,
            String competitionPublicId,
            List<MeetingAttendance> attendances
    ) {
        return from(meeting, currentUserId, competitionPublicId, null, attendances);
    }

    public static MeetingDetailResponse from(
            Meeting meeting,
            Long currentUserId,
            String competitionPublicId,
            String ownerNickName,
            List<MeetingAttendance> attendances
    ) {
        return from(meeting, currentUserId, competitionPublicId, ownerNickName, null, attendances);
    }

    public static MeetingDetailResponse from(
            Meeting meeting,
            Long currentUserId,
            String competitionPublicId,
            String ownerNickName,
            String clubName,
            List<MeetingAttendance> attendances
    ) {
        return new MeetingDetailResponse(
                meeting.getPublicId(),
                meeting.getCompetitionId(),
                competitionPublicId,
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
                ownerNickName,
                meeting.hasCompetition(),
                clubName,
                meeting.getClubId() != null,
                attendances.stream()
                        .map(MeetingAttendanceResponse::from)
                        .toList()
        );
    }
}
