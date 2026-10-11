package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubMemberCreateRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubMemberCommandServiceTest {
    @Mock ClubMemberRepository clubMemberRepository;
    @Mock ClubSkillTierRepository clubSkillTierRepository;
    @Mock ClubAccessService clubAccessService;
    @InjectMocks ClubMemberCommandService service;

    @Test
    void addMember_allowsOwnerAndDoesNotCreateAccountLink() {
        Club club = new Club("Morning", null, 10L);
        when(clubAccessService.requireOperator("club", 10L)).thenReturn(club);
        when(clubMemberRepository.existsByClubAndName(club, "Jamie")).thenReturn(false);

        service.addMember("club", new ClubMemberCreateRequest("Jamie", "FEMALE", null, null, null, null), 10L);

        ArgumentCaptor<ClubMember> member = ArgumentCaptor.forClass(ClubMember.class);
        verify(clubMemberRepository).save(member.capture());
        assertThat(member.getValue().getName()).isEqualTo("Jamie");
        verify(clubAccessService).requireOperator("club", 10L);
    }
}
