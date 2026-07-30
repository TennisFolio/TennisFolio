package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubSkillTierRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubSkillTierServiceTest {

    @Mock
    ClubSkillTierRepository clubSkillTierRepository;

    @Mock
    ClubMemberRepository clubMemberRepository;

    ClubSkillTierService service;

    @BeforeEach
    void setUp() {
        service = new ClubSkillTierService(clubSkillTierRepository, clubMemberRepository);
    }

    @Test
    void replaceSkillTiers_assignsLevelsFromRequestOrder() {
        Club club = new Club("Morning Tennis", null, 10L);
        when(clubSkillTierRepository.findByClubOrderByLevelDescIdAsc(club)).thenReturn(List.of());

        service.replaceSkillTiers(club, List.of(
                new ClubSkillTierRequest(null, "상급"),
                new ClubSkillTierRequest(null, "중급"),
                new ClubSkillTierRequest(null, "초급"),
                new ClubSkillTierRequest(null, "입문")
        ));

        ArgumentCaptor<ClubSkillTier> captor = ArgumentCaptor.forClass(ClubSkillTier.class);
        verify(clubSkillTierRepository, org.mockito.Mockito.times(4)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(ClubSkillTier::getLevel)
                .containsExactly(4, 3, 2, 1);
    }

    @Test
    void replaceSkillTiers_rejectsMoreThanTenTiers() {
        Club club = new Club("Morning Tennis", null, 10L);
        List<ClubSkillTierRequest> skillTiers = java.util.stream.IntStream.range(0, 11)
                .mapToObj(index -> new ClubSkillTierRequest(null, "등급" + index))
                .toList();

        assertThatThrownBy(() -> service.replaceSkillTiers(club, skillTiers))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(clubSkillTierRepository, never()).findByClubOrderByLevelDescIdAsc(any());
    }

    @Test
    void replaceSkillTiers_rejectsBlankAndDuplicateNames() {
        Club club = new Club("Morning Tennis", null, 10L);

        assertThatThrownBy(() -> service.replaceSkillTiers(
                club,
                List.of(new ClubSkillTierRequest(null, "상급"), new ClubSkillTierRequest(null, " 상급 "))
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatThrownBy(() -> service.replaceSkillTiers(club, List.of(new ClubSkillTierRequest(null, " "))))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
