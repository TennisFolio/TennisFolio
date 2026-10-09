package com.tennisfolio.Tennisfolio.club.api;

import com.tennisfolio.Tennisfolio.club.dto.ClubAccountInvitationAcceptRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubAccountResponse;
import com.tennisfolio.Tennisfolio.club.service.ClubAccountInvitationService;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubAccountInvitationControllerTest {

    @Mock ClubAccountInvitationService clubAccountInvitationService;
    @InjectMocks ClubAccountInvitationController controller;

    @Test
    void acceptInvitation_passesTokenRequestAndCurrentUserToService() {
        ClubAccountInvitationAcceptRequest request = new ClubAccountInvitationAcceptRequest("Jamie");
        when(clubAccountInvitationService.acceptInvitation("token", request, 20L))
                .thenReturn(new ClubAccountResponse(2L, 20L, "Jamie", "ADMIN"));

        ResponseEntity<ResponseDTO<ClubAccountResponse>> response = controller.acceptInvitation(20L, "token", request);

        verify(clubAccountInvitationService).acceptInvitation("token", request, 20L);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}
