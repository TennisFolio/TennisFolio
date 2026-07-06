package com.tennisfolio.Tennisfolio.club.repository;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

    boolean existsByClubAndNameAndActiveTrue(Club club, String name);

    List<ClubMember> findByClubAndActiveTrueOrderByIdAsc(Club club);

    Optional<ClubMember> findByClubAndUserIdAndActiveTrue(Club club, Long userId);
}
