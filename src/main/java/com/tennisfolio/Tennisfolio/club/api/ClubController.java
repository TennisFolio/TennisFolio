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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ClubController {

    private final ClubCommandService clubCommandService;
    private final ClubQueryService clubQueryService;
    private final ClubMemberCommandService clubMemberCommandService;

    public ClubController(
            ClubCommandService clubCommandService,
            ClubQueryService clubQueryService,
            ClubMemberCommandService clubMemberCommandService
    ) {
        this.clubCommandService = clubCommandService;
        this.clubQueryService = clubQueryService;
        this.clubMemberCommandService = clubMemberCommandService;
    }

    @PostMapping("/clubs")
    public ResponseEntity<ResponseDTO<ClubCreateResponse>> createClub(
            Authentication authentication,
            @RequestBody ClubCreateRequest request
    ) {
        ClubCreateResponse response =
                clubCommandService.createClub(request, resolveAuthenticatedUserId(authentication));
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/clubs")
    public ResponseEntity<ResponseDTO<List<ClubSummaryResponse>>> getMyClubs(Authentication authentication) {
        List<ClubSummaryResponse> response =
                clubQueryService.getMyClubs(resolveAuthenticatedUserId(authentication));
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/clubs/{clubPublicId}")
    public ResponseEntity<ResponseDTO<ClubDetailResponse>> getClub(
            Authentication authentication,
            @PathVariable String clubPublicId
    ) {
        ClubDetailResponse response =
                clubQueryService.getClub(clubPublicId, resolveAuthenticatedUserId(authentication));
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @PatchMapping("/clubs/{clubPublicId}")
    public ResponseEntity<ResponseDTO<Void>> updateClub(
            Authentication authentication,
            @PathVariable String clubPublicId,
            @RequestBody ClubUpdateRequest request
    ) {
        clubCommandService.updateClub(clubPublicId, request, resolveAuthenticatedUserId(authentication));
        return ResponseEntity.ok(ResponseDTO.success());
    }

    @DeleteMapping("/clubs/{clubPublicId}")
    public ResponseEntity<Void> deleteClub(
            Authentication authentication,
            @PathVariable String clubPublicId
    ) {
        clubCommandService.deleteClub(clubPublicId, resolveAuthenticatedUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clubs/{clubPublicId}/members")
    public ResponseEntity<ResponseDTO<List<ClubMemberResponse>>> getMembers(
            Authentication authentication,
            @PathVariable String clubPublicId,
            @RequestParam(required = false) String keyword
    ) {
        List<ClubMemberResponse> response = clubQueryService.getMembers(
                clubPublicId,
                keyword,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @PostMapping("/clubs/{clubPublicId}/members")
    public ResponseEntity<ResponseDTO<Void>> addMember(
            Authentication authentication,
            @PathVariable String clubPublicId,
            @RequestBody ClubMemberCreateRequest request
    ) {
        clubMemberCommandService.addMember(clubPublicId, request, resolveAuthenticatedUserId(authentication));
        return ResponseEntity.ok(ResponseDTO.success());
    }

    @PatchMapping("/clubs/{clubPublicId}/members/{memberId}")
    public ResponseEntity<ResponseDTO<Void>> updateMember(
            Authentication authentication,
            @PathVariable String clubPublicId,
            @PathVariable Long memberId,
            @RequestBody ClubMemberUpdateRequest request
    ) {
        clubMemberCommandService.updateMember(
                clubPublicId,
                memberId,
                request,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.ok(ResponseDTO.success());
    }

    @DeleteMapping("/clubs/{clubPublicId}/members/{memberId}")
    public ResponseEntity<Void> deleteMember(
            Authentication authentication,
            @PathVariable String clubPublicId,
            @PathVariable Long memberId
    ) {
        clubMemberCommandService.deleteMember(clubPublicId, memberId, resolveAuthenticatedUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Long resolveAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
