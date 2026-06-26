package com.tennisfolio.Tennisfolio.meeting.repository;

import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingAttendanceRepository extends JpaRepository<MeetingAttendance, Long> {

    boolean existsByMeetingAndParticipantNameAndDeletedAtIsNull(Meeting meeting, String participantName);

    boolean existsByMeetingAndParticipantNameAndDeletedAtIsNullAndIdNot(
            Meeting meeting,
            String participantName,
            Long id
    );

    Optional<MeetingAttendance> findByIdAndMeetingAndDeletedAtIsNull(Long id, Meeting meeting);

    long countByMeetingAndAttendanceStatusAndDeletedAtIsNull(Meeting meeting, AttendanceStatus attendanceStatus);

    long countByMeetingAndGenderAndAttendanceStatusAndDeletedAtIsNull(
            Meeting meeting,
            Gender gender,
            AttendanceStatus attendanceStatus
    );
}
