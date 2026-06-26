package com.tennisfolio.Tennisfolio.meeting.entity;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_meeting")
@Getter
@NoArgsConstructor
public class Meeting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEETING_ID")
    private Long id;

    @Column(name = "PUBLIC_ID", nullable = false, unique = true, updatable = false, length = 36)
    private String publicId = UUID.randomUUID().toString();

    @Column(name = "OWNER_USER_ID", nullable = false)
    private Long ownerUserId;

    @Column(name = "COMPETITION_ID", unique = true)
    private Long competitionId;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "START_AT", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "END_AT", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "NOTE")
    private String note;

    @Column(name = "MAX_PARTICIPANTS")
    private Integer maxParticipants;

    @Column(name = "MAX_MALE_PARTICIPANTS")
    private Integer maxMaleParticipants;

    @Column(name = "MAX_FEMALE_PARTICIPANTS")
    private Integer maxFemaleParticipants;

    @Column(name = "COURT_COUNT", nullable = false)
    private Integer courtCount;

    @Column(name = "TOTAL_GAMES", nullable = false)
    private Integer totalGames;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private MeetingStatus status = MeetingStatus.OPEN;

    @Column(name = "DEL_DT")
    private LocalDateTime deletedAt;

    public Meeting(
            Long ownerUserId,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String note,
            Integer maxParticipants,
            Integer maxMaleParticipants,
            Integer maxFemaleParticipants,
            Integer courtCount,
            Integer totalGames
    ) {
        this.ownerUserId = ownerUserId;
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

    public void updateDetails(
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String note,
            Integer maxParticipants,
            Integer maxMaleParticipants,
            Integer maxFemaleParticipants,
            Integer courtCount,
            Integer totalGames
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

    public void updateStatus(MeetingStatus status) {
        this.status = status;
    }

    public void connectCompetition(Long competitionId) {
        this.competitionId = competitionId;
    }

    public void clearCompetition() {
        this.competitionId = null;
    }

    public boolean hasCompetition() {
        return competitionId != null;
    }

    public boolean isOwnedBy(Long userId) {
        return ownerUserId != null && ownerUserId.equals(userId);
    }

    public void delete(LocalDateTime deletedAt) {
        if (this.deletedAt == null) {
            this.deletedAt = deletedAt;
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

}
