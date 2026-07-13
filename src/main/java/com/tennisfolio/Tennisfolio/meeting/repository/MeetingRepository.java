package com.tennisfolio.Tennisfolio.meeting.repository;

import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Optional<Meeting> findByPublicIdAndDeletedAtIsNull(String publicId);

    Optional<Meeting> findByPublicIdAndOwnerUserIdAndDeletedAtIsNull(String publicId, Long ownerUserId);

    Optional<Meeting> findByPublicIdAndClubIdAndDeletedAtIsNull(String publicId, Long clubId);

    Optional<Meeting> findByCompetitionIdAndDeletedAtIsNull(Long competitionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select meeting
            from Meeting meeting
            where meeting.publicId = :publicId
              and meeting.deletedAt is null
            """)
    Optional<Meeting> findByPublicIdAndDeletedAtIsNullForUpdate(@Param("publicId") String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select meeting
            from Meeting meeting
            where meeting.publicId = :publicId
              and meeting.ownerUserId = :ownerUserId
              and meeting.deletedAt is null
            """)
    Optional<Meeting> findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate(
            @Param("publicId") String publicId,
            @Param("ownerUserId") Long ownerUserId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select meeting
            from Meeting meeting
            where meeting.publicId = :publicId
              and meeting.clubId = :clubId
              and meeting.deletedAt is null
            """)
    Optional<Meeting> findByPublicIdAndClubIdAndDeletedAtIsNullForUpdate(
            @Param("publicId") String publicId,
            @Param("clubId") Long clubId
    );

    List<Meeting> findByOwnerUserIdAndDeletedAtIsNullOrderByStartAtDescIdDesc(Long ownerUserId);

    List<Meeting> findByClubIdAndDeletedAtIsNullOrderByStartAtDescIdDesc(Long clubId);
}
