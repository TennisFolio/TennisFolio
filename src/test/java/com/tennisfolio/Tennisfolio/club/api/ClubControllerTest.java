package com.tennisfolio.Tennisfolio.club.api;

import com.tennisfolio.Tennisfolio.club.dto.ClubCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubCreateResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubDetailResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberUpdateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubSummaryResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubUpdateRequest;
import com.tennisfolio.Tennisfolio.club.service.ClubCommandService;
import com.tennisfolio.Tennisfolio.club.service.ClubMemberCommandService;
import com.tennisfolio.Tennisfolio.club.service.ClubQueryService;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubControllerTest {

    @Mock
    ClubCommandService clubCommandService;

    @Mock
    ClubQueryService clubQueryService;

    @Mock
    ClubMemberCommandService clubMemberCommandService;

    @InjectMocks
    ClubController clubController;

    @Test
    void createClub_passesCurrentUserToCommandService() {
        Authentication authentication = auth(10L);
        ClubCreateRequest request = new ClubCreateRequest("Morning Tennis", "Indoor club");
        when(clubCommandService.createClub(request, 10L))
                .thenReturn(new ClubCreateResponse("club-public-id"));

        ResponseEntity<ResponseDTO<ClubCreateResponse>> response =
                clubController.createClub(authentication, request);

        verify(clubCommandService).createClub(request, 10L);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getPublicId()).isEqualTo("club-public-id");
    }

    @Test
    void getMyClubs_passesCurrentUserToQueryService() {
        Authentication authentication = auth(10L);
        when(clubQueryService.getMyClubs(10L))
                .thenReturn(List.of(new ClubSummaryResponse(
                        "club-public-id",
                        "Morning Tennis",
                        "Indoor club",
                        "ADMIN",
                        3L
                )));

        ResponseEntity<ResponseDTO<List<ClubSummaryResponse>>> response =
                clubController.getMyClubs(authentication);

        verify(clubQueryService).getMyClubs(10L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    void getClub_passesCurrentUserToQueryService() {
        Authentication authentication = auth(10L);
        when(clubQueryService.getClub("club-public-id", 10L))
                .thenReturn(new ClubDetailResponse(
                        "club-public-id",
                        "Morning Tennis",
                        "Indoor club",
                        "ADMIN",
                        true,
                        3L
                ));

        ResponseEntity<ResponseDTO<ClubDetailResponse>> response =
                clubController.getClub(authentication, "club-public-id");

        verify(clubQueryService).getClub("club-public-id", 10L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getAdmin()).isTrue();
    }

    @Test
    void updateClub_passesCurrentUserToCommandService() {
        Authentication authentication = auth(10L);
        ClubUpdateRequest request = new ClubUpdateRequest("Evening Tennis", "Outdoor club");

        ResponseEntity<ResponseDTO<Void>> response =
                clubController.updateClub(authentication, "club-public-id", request);

        verify(clubCommandService).updateClub("club-public-id", request, 10L);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void deleteClub_passesCurrentUserToCommandService() {
        Authentication authentication = auth(10L);

        ResponseEntity<Void> response = clubController.deleteClub(authentication, "club-public-id");

        verify(clubCommandService).deleteClub("club-public-id", 10L);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }

    @Test
    void getMembers_passesKeywordAndCurrentUserToQueryService() {
        Authentication authentication = auth(10L);
        when(clubQueryService.getMembers("club-public-id", "jamie", 10L))
                .thenReturn(List.of(new ClubMemberResponse(
                        100L,
                        null,
                        "Jamie Lee",
                        "FEMALE",
                        "MEMBER",
                        "NTRP 3.5",
                        "010",
                        "lefty"
                )));

        ResponseEntity<ResponseDTO<List<ClubMemberResponse>>> response =
                clubController.getMembers(authentication, "club-public-id", "jamie");

        verify(clubQueryService).getMembers("club-public-id", "jamie", 10L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    void addMember_passesCurrentUserToCommandService() {
        Authentication authentication = auth(10L);
        ClubMemberCreateRequest request =
                new ClubMemberCreateRequest("Jamie Lee", "FEMALE", "MEMBER", null, null, null);

        ResponseEntity<ResponseDTO<Void>> response =
                clubController.addMember(authentication, "club-public-id", request);

        verify(clubMemberCommandService).addMember("club-public-id", request, 10L);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void updateMember_passesCurrentUserToCommandService() {
        Authentication authentication = auth(10L);
        ClubMemberUpdateRequest request =
                new ClubMemberUpdateRequest("Jamie Lee", "FEMALE", "MEMBER", null, null, null);

        ResponseEntity<ResponseDTO<Void>> response =
                clubController.updateMember(authentication, "club-public-id", 100L, request);

        verify(clubMemberCommandService).updateMember("club-public-id", 100L, request, 10L);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void deleteMember_passesCurrentUserToCommandService() {
        Authentication authentication = auth(10L);

        ResponseEntity<Void> response = clubController.deleteMember(authentication, "club-public-id", 100L);

        verify(clubMemberCommandService).deleteMember("club-public-id", 100L, 10L);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }

    private static Authentication auth(Long userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }
}
