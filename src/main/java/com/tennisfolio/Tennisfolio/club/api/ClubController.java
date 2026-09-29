package com.tennisfolio.Tennisfolio.club.api;

import com.tennisfolio.Tennisfolio.club.dto.ClubCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubCreateResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubDetailResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberBulkCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberManagementResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberUpdateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberStatusChangeRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubSummaryResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubUpdateRequest;
import com.tennisfolio.Tennisfolio.club.service.ClubCommandService;
import com.tennisfolio.Tennisfolio.club.service.ClubDashboardQueryService;
import com.tennisfolio.Tennisfolio.club.service.ClubMemberCommandService;
import com.tennisfolio.Tennisfolio.club.service.ClubQueryService;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import java.time.DateTimeException;
import java.time.YearMonth;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMemberFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ClubController {

    private final ClubCommandService clubCommandService;
    private final ClubQueryService clubQueryService;
    private final ClubMemberCommandService clubMemberCommandService;
    private final ClubDashboardQueryService clubDashboardQueryService;

    public ClubController(
            ClubCommandService clubCommandService,
            ClubQueryService clubQueryService,
            ClubMemberCommandService clubMemberCommandService,
            ClubDashboardQueryService clubDashboardQueryService
    ) {
        this.clubCommandService = clubCommandService;
        this.clubQueryService = clubQueryService;
        this.clubMemberCommandService = clubMemberCommandService;
        this.clubDashboardQueryService = clubDashboardQueryService;
    }

    @PostMapping("/clubs")
    public ResponseEntity<ResponseDTO<ClubCreateResponse>> createClub(
            @AuthenticationPrincipal Long userId,
            @RequestBody ClubCreateRequest request
    ) {
        ClubCreateResponse response =
                clubCommandService.createClub(request, userId);
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/clubs")
    public ResponseEntity<ResponseDTO<List<ClubSummaryResponse>>> getMyClubs(@AuthenticationPrincipal Long userId) {
        List<ClubSummaryResponse> response =
                clubQueryService.getMyClubs(userId);
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/clubs/{clubPublicId}")
    public ResponseEntity<ResponseDTO<ClubDetailResponse>> getClub(
            @AuthenticationPrincipal Long userId,
            @PathVariable String clubPublicId
    ) {
        ClubDetailResponse response =
                clubQueryService.getClub(clubPublicId, userId);
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/clubs/{clubPublicId}/dashboard")
    public ResponseEntity<ResponseDTO<ClubDashboardResponse>> getDashboard(
            @AuthenticationPrincipal Long userId,
            @PathVariable String clubPublicId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "all") String memberFilter,
            @RequestParam(defaultValue = "0") int page
    ) {
        ClubDashboardResponse response =
                clubDashboardQueryService.getDashboard(
                        clubPublicId,
                        userId,
                        resolveDashboardMonth(year, month),
                        parseMemberFilter(memberFilter),
                        Math.max(page, 0)
                );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    private YearMonth resolveDashboardMonth(Integer year, Integer month) {
        if (year == null && month == null) {
            return YearMonth.now();
        }
        if (year == null || month == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "year와 month를 함께 입력해주세요.");
        }
        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "월 값이 올바르지 않습니다.");
        }
    }

    private ClubDashboardMemberFilter parseMemberFilter(String memberFilter) {
        try {
            return ClubDashboardMemberFilter.valueOf(memberFilter.toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "회원 참여 필터 값이 올바르지 않습니다.");
        }
    }

    @PatchMapping("/clubs/{clubPublicId}")
    public ResponseEntity<ResponseDTO<Void>> updateClub(
            @AuthenticationPrincipal Long userId,
            @PathVariable String clubPublicId,
            @RequestBody ClubUpdateRequest request
    ) {
        clubCommandService.updateClub(clubPublicId, request, userId);
        return ResponseEntity.ok(ResponseDTO.success());
    }

    @DeleteMapping("/clubs/{clubPublicId}")
    public ResponseEntity<Void> deleteClub(
            @AuthenticationPrincipal Long userId,
            @PathVariable String clubPublicId
    ) {
        clubCommandService.deleteClub(clubPublicId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clubs/{clubPublicId}/members")
    public ResponseEntity<ResponseDTO<List<ClubMemberResponse>>> getMembers(
            @AuthenticationPrincipal Long userId,
            @PathVariable String clubPublicId,
            @RequestParam(required = false) String keyword
    ) {
        List<ClubMemberResponse> response = clubQueryService.getMembers(
                clubPublicId,
                keyword,
                userId
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/clubs/{clubPublicId}/members/management")
    public ResponseEntity<ResponseDTO<List<ClubMemberManagementResponse>>> getManagementMembers(
            @AuthenticationPrincipal Long userId,
            @PathVariable String clubPublicId,
            @RequestParam(required = false) String memberStatus,
            @RequestParam(required = false) String keyword
    ) {
        List<ClubMemberManagementResponse> response = clubQueryService.getManagementMembers(
                clubPublicId,
                memberStatus,
                keyword,
                userId
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/clubs/{clubPublicId}/members/management/{memberId}")
    public ResponseEntity<ResponseDTO<ClubMemberManagementResponse>> getManagementMember(
            @AuthenticationPrincipal Long userId,
            @PathVariable String clubPublicId,
            @PathVariable Long memberId
    ) {
        ClubMemberManagementResponse response = clubQueryService.getManagementMember(
                clubPublicId,
                memberId,
                userId
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @PostMapping("/clubs/{clubPublicId}/members")
    public ResponseEntity<ResponseDTO<Void>> addMember(
            @AuthenticationPrincipal Long userId,
            @PathVariable String clubPublicId,
            @RequestBody ClubMemberCreateRequest request
    ) {
        clubMemberCommandService.addMember(clubPublicId, request, userId);
        return ResponseEntity.ok(ResponseDTO.success());
    }

    @PostMapping("/clubs/{clubPublicId}/members/bulk")
    public ResponseEntity<ResponseDTO<Void>> addMembers(
            @AuthenticationPrincipal Long userId,
            @PathVariable String clubPublicId,
            @RequestBody ClubMemberBulkCreateRequest request
    ) {
        clubMemberCommandService.addMembers(clubPublicId, request, userId);
        return ResponseEntity.ok(ResponseDTO.success());
    }

    @PatchMapping("/clubs/{clubPublicId}/members/{memberId}")
    public ResponseEntity<ResponseDTO<Void>> updateMember(
            @AuthenticationPrincipal Long userId,
            @PathVariable String clubPublicId,
            @PathVariable Long memberId,
            @RequestBody ClubMemberUpdateRequest request
    ) {
        clubMemberCommandService.updateMember(
                clubPublicId,
                memberId,
                request,
                userId
        );
        return ResponseEntity.ok(ResponseDTO.success());
    }

    @PatchMapping("/clubs/{clubPublicId}/members/{memberId}/status")
    public ResponseEntity<ResponseDTO<Void>> changeMemberStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable String clubPublicId,
            @PathVariable Long memberId,
            @RequestBody ClubMemberStatusChangeRequest request
    ) {
        clubMemberCommandService.changeMemberStatus(
                clubPublicId,
                memberId,
                request,
                userId
        );
        return ResponseEntity.ok(ResponseDTO.success());
    }

    @DeleteMapping("/clubs/{clubPublicId}/members/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @AuthenticationPrincipal Long userId,
            @PathVariable String clubPublicId,
            @PathVariable Long memberId
    ) {
        clubMemberCommandService.deleteMember(clubPublicId, memberId, userId);
        return ResponseEntity.noContent().build();
    }

}
