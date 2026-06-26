package com.tennisfolio.Tennisfolio.meeting.repository;

import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingAttendanceRepository extends JpaRepository<MeetingAttendance, Long> {

    boolean existsByMeetingAndParticipantNameAndDeletedAtIsNull(Meeting meeting, String participantName);
}
