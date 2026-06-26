package com.tennisfolio.Tennisfolio.meeting.repository;

import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Optional<Meeting> findByPublicIdAndDeletedAtIsNull(String publicId);
}
