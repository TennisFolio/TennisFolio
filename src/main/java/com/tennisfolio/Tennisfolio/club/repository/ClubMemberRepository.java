package com.tennisfolio.Tennisfolio.club.repository;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

    boolean existsByClubAndNameAndActiveTrue(Club club, String name);

    boolean existsByClubAndNameAndActiveTrueAndIdNot(Club club, String name, Long id);

    List<ClubMember> findByClubAndActiveTrueOrderByIdAsc(Club club);

    List<ClubMember> findByClubAndActiveTrueOrderByNameAscIdAsc(Club club);

    List<ClubMember> findByClubAndNameContainingIgnoreCaseAndActiveTrueOrderByNameAscIdAsc(Club club, String name);

    List<ClubMember> findByUserIdAndActiveTrueOrderByIdAsc(Long userId);

    Optional<ClubMember> findByClubAndUserIdAndActiveTrue(Club club, Long userId);

    List<ClubMember> findByClubAndNameAndGenderAndActiveTrueOrderByIdAsc(Club club, String name, Gender gender);

    Optional<ClubMember> findByClubAndIdAndActiveTrue(Club club, Long id);

    long countByClubAndRoleAndActiveTrue(Club club, com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole role);

    long countByClubAndActiveTrue(Club club);
}
