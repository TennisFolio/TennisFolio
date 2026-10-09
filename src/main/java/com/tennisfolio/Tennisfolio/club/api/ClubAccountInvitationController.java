package com.tennisfolio.Tennisfolio.club.api;

import com.tennisfolio.Tennisfolio.club.dto.ClubAccountInvitationAcceptRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubAccountResponse;
import com.tennisfolio.Tennisfolio.club.service.ClubAccountInvitationService;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/club-account-invitations")
public class ClubAccountInvitationController {
    private final ClubAccountInvitationService clubAccountInvitationService;

    public ClubAccountInvitationController(ClubAccountInvitationService clubAccountInvitationService) {
        this.clubAccountInvitationService = clubAccountInvitationService;
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<ResponseDTO<ClubAccountResponse>> acceptInvitation(
            @AuthenticationPrincipal Long userId,
            @PathVariable String token,
            @RequestBody ClubAccountInvitationAcceptRequest request
    ) {
        return ResponseEntity.ok(ResponseDTO.success(
                clubAccountInvitationService.acceptInvitation(token, request, userId)
        ));
    }
}
