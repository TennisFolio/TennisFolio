package com.tennisfolio.Tennisfolio.club.repository;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import com.tennisfolio.Tennisfolio.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
class ClubSkillTierRepositoryTest {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubSkillTierRepository clubSkillTierRepository;

    @Test
    void findByClubOrderByLevelDescIdAsc_returnsOnlyTargetClubTiersInDescendingLevelOrder() {
        Club targetClub = clubRepository.saveAndFlush(new Club("Target club", "desc", 1L));
        Club otherClub = clubRepository.saveAndFlush(new Club("Other club", "desc", 2L));

        clubSkillTierRepository.saveAndFlush(new ClubSkillTier(targetClub, "초급", 1));
        clubSkillTierRepository.saveAndFlush(new ClubSkillTier(targetClub, "상급", 3));
        clubSkillTierRepository.saveAndFlush(new ClubSkillTier(otherClub, "다른 클럽", 9));

        List<ClubSkillTier> result = clubSkillTierRepository.findByClubOrderByLevelDescIdAsc(targetClub);

        assertThat(result)
                .extracting(ClubSkillTier::getName)
                .containsExactly("상급", "초급");
        assertThat(result)
                .extracting(ClubSkillTier::getLevel)
                .containsExactly(3, 1);
    }
}
