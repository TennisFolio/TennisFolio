package com.tennisfolio.Tennisfolio.club.repository;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubSkillTierRepository extends JpaRepository<ClubSkillTier, Long> {

    List<ClubSkillTier> findByClubOrderByLevelDescIdAsc(Club club);
}
