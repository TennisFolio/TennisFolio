package com.tennisfolio.Tennisfolio.club.repository;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberStatus;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

    boolean existsByClubAndName(Club club, String name);

    boolean existsByClubAndNameAndIdNot(Club club, String name, Long id);

    List<ClubMember> findByClubAndMemberStatusOrderByIdAsc(Club club, ClubMemberStatus memberStatus);

    List<ClubMember> findByClubAndMemberStatusOrderByNameAscIdAsc(Club club, ClubMemberStatus memberStatus);

    List<ClubMember> findByClubAndNameContainingIgnoreCaseAndMemberStatusOrderByNameAscIdAsc(Club club, String name, ClubMemberStatus memberStatus);

    @Query("""
            SELECT member
            FROM ClubMember member
            WHERE member.club = :club
            ORDER BY CASE member.memberStatus
                WHEN com.tennisfolio.Tennisfolio.club.entity.ClubMemberStatus.ACTIVE THEN 0
                WHEN com.tennisfolio.Tennisfolio.club.entity.ClubMemberStatus.SUSPENDED THEN 1
                ELSE 2
            END,
            CASE member.role
                WHEN com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole.ADMIN THEN 0
                ELSE 1
            END,
            member.name ASC,
            member.id ASC
            """)
    List<ClubMember> findManagementByClubOrderByStatusAndRole(@Param("club") Club club);

    @Query("""
            SELECT member
            FROM ClubMember member
            WHERE member.club = :club
              AND LOWER(member.name) LIKE LOWER(CONCAT('%', :name, '%'))
            ORDER BY CASE member.memberStatus
                WHEN com.tennisfolio.Tennisfolio.club.entity.ClubMemberStatus.ACTIVE THEN 0
                WHEN com.tennisfolio.Tennisfolio.club.entity.ClubMemberStatus.SUSPENDED THEN 1
                ELSE 2
            END,
            CASE member.role
                WHEN com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole.ADMIN THEN 0
                ELSE 1
            END,
            member.name ASC,
            member.id ASC
            """)
    List<ClubMember> findManagementByClubAndNameContainingIgnoreCaseOrderByStatusAndRole(
            @Param("club") Club club,
            @Param("name") String name
    );

    List<ClubMember> findByUserIdAndMemberStatusOrderByIdAsc(Long userId, ClubMemberStatus memberStatus);

    Optional<ClubMember> findByClubAndUserIdAndMemberStatus(Club club, Long userId, ClubMemberStatus memberStatus);

    Optional<ClubMember> findByClubIdAndUserIdAndMemberStatus(Long clubId, Long userId, ClubMemberStatus memberStatus);

    List<ClubMember> findByClubAndNameAndGenderAndMemberStatusOrderByIdAsc(Club club, String name, Gender gender, ClubMemberStatus memberStatus);

    Optional<ClubMember> findByClubAndIdAndMemberStatus(Club club, Long id, ClubMemberStatus memberStatus);

    Optional<ClubMember> findByClubAndId(Club club, Long id);

    List<ClubMember> findBySkillTier(ClubSkillTier skillTier);

    long countByClubAndRoleAndMemberStatus(Club club, ClubMemberRole role, ClubMemberStatus memberStatus);

    long countByClubAndMemberStatus(Club club, ClubMemberStatus memberStatus);

}
