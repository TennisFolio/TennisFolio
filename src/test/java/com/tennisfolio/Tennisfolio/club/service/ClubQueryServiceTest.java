package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubQueryServiceTest {
    @Mock ClubMemberRepository clubMemberRepository;
    @Mock ClubAccountRepository clubAccountRepository;
    @Mock ClubAccessService clubAccessService;
    @Mock ClubSkillTierRepository clubSkillTierRepository;
    @InjectMocks ClubQueryService service;

    @Test
    void getMyClubs_returnsOwnerRoleFromClubAccount() {
        Club club = new Club("Morning", null, 10L);
        when(clubAccountRepository.findByAccountIdOrderByIdAsc(10L))
                .thenReturn(List.of(new ClubAccount(club, 10L, ClubAccountRole.OWNER)));
        when(clubMemberRepository.countByClubAndMemberStatus(org.mockito.ArgumentMatchers.eq(club), org.mockito.ArgumentMatchers.any()))
                .thenReturn(2L);

        assertThat(service.getMyClubs(10L)).singleElement().extracting("currentUserRole").isEqualTo("OWNER");
    }
}
