package com.tennisfolio.Tennisfolio.meeting.repository;

import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Optional<Meeting> findByPublicIdAndDeletedAtIsNull(String publicId);

    Optional<Meeting> findByPublicIdAndOwnerUserIdAndDeletedAtIsNull(String publicId, Long ownerUserId);

    List<Meeting> findByOwnerUserIdAndDeletedAtIsNullOrderByStartAtDescIdDesc(Long ownerUserId);
}
