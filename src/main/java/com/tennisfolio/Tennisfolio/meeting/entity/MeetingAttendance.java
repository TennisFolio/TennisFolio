package com.tennisfolio.Tennisfolio.meeting.entity;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_meeting_attendance")
@Getter
@NoArgsConstructor
public class MeetingAttendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEETING_ATTENDANCE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEETING_ID", nullable = false)
    private Meeting meeting;

    @Column(name = "PARTICIPANT_NAME", nullable = false)
    private String participantName;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENDER", nullable = false)
    private Gender gender;

    @Convert(converter = AttendanceStatusConverter.class)
    @Column(name = "ATTENDANCE_STATUS", nullable = false)
    private AttendanceStatus attendanceStatus;

    @Column(name = "DEL_DT")
    private LocalDateTime deletedAt;

    public MeetingAttendance(
            Meeting meeting,
            String participantName,
            Gender gender,
            AttendanceStatus attendanceStatus
    ) {
        this.meeting = meeting;
        this.participantName = participantName;
        this.gender = gender;
        this.attendanceStatus = attendanceStatus;
    }

    public void update(String participantName, Gender gender, AttendanceStatus attendanceStatus) {
        this.participantName = participantName;
        this.gender = gender;
        this.attendanceStatus = attendanceStatus;
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
