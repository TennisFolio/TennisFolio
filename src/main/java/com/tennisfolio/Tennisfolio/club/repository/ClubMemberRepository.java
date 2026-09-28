package com.tennisfolio.Tennisfolio.club.repository;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberStatus;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
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
