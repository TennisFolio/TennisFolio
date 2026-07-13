package com.tennisfolio.Tennisfolio.matching.repository;

import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    Optional<Competition> findByName(String name);

    Optional<Competition> findByPublicId(String publicId);

    Optional<Competition> findByPublicIdAndDeletedAtIsNull(String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select competition
            from Competition competition
            where competition.publicId = :publicId
              and competition.deletedAt is null
            """)
    Optional<Competition> findByPublicIdAndDeletedAtIsNullForUpdate(@Param("publicId") String publicId);

    Optional<Competition> findByIdAndDeletedAtIsNull(Long id);

    Optional<Competition> findByPublicIdAndOwnerUserId(String publicId, Long ownerUserId);

    Optional<Competition> findByIdAndOwnerUserIdAndDeletedAtIsNull(Long id, Long ownerUserId);

    List<Competition> findByStatus(Competition.CompetitionStatus status);

    @Query("SELECT c FROM Competition c WHERE c.isModified = true")
    List<Competition> findModifiedCompetitions();

    @Query("""
            SELECT c
            FROM Competition c
            WHERE c.ownerUserId = :ownerUserId
              AND c.deletedAt IS NULL
            ORDER BY c.createDt DESC, c.id DESC
            """)
    List<Competition> findByOwnerUserIdOrderByCreateDtDesc(@Param("ownerUserId") Long ownerUserId);
}

