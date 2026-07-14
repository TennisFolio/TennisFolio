package com.tennisfolio.Tennisfolio.matching.controller;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionAdminPasswordRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionAdminTokenResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionDetailResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionResultResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionStatResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionUpdateResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CourtCountUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.GameEntryUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.GameEntryUpdateResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameScoreUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.GameStatusUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionAdminAuthorizationService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionEntryCommandService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionEntryQueryService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionGameCommandService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CompetitionController {

    private final CompetitionCommandService competitionCommandService;
    private final CompetitionQueryService competitionQueryService;
    private final CompetitionEntryCommandService competitionEntryCommandService;
    private final CompetitionEntryQueryService competitionEntryQueryService;
    private final CompetitionGameCommandService competitionGameCommandService;
    private final CompetitionAdminAuthorizationService competitionAdminAuthorizationService;

    public CompetitionController(
            CompetitionCommandService competitionCommandService,
            CompetitionQueryService competitionQueryService,
            CompetitionEntryCommandService competitionEntryCommandService,
            CompetitionEntryQueryService competitionEntryQueryService,
            CompetitionGameCommandService competitionGameCommandService,
            CompetitionAdminAuthorizationService competitionAdminAuthorizationService
    ) {
        this.competitionCommandService = competitionCommandService;
        this.competitionQueryService = competitionQueryService;
        this.competitionEntryCommandService = competitionEntryCommandService;
        this.competitionEntryQueryService = competitionEntryQueryService;
        this.competitionGameCommandService = competitionGameCommandService;
        this.competitionAdminAuthorizationService = competitionAdminAuthorizationService;
    }

    @PostMapping("/competitions")
    public ResponseEntity<ResponseDTO<CompetitionCreateResponse>> createCompetition(
            Authentication authentication,
            @RequestBody CompetitionCreateRequest request
    ) {
        CompetitionCreateResponse response = competitionCommandService.createCompetition(
                request,
                resolveAuthenticatedUserId(authentication)
        );
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @GetMapping("/competitions/{publicId}")
    public ResponseEntity<ResponseDTO<CompetitionDetailResponse>> getCompetition(
            Authentication authentication,
            @PathVariable String publicId
    ) {
        CompetitionDetailResponse response = competitionQueryService.getCompetition(
                publicId,
                resolveAuthenticatedUserId(authentication)
        );
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @GetMapping("/competitions/{publicId}/result")
    public ResponseEntity<ResponseDTO<CompetitionResultResponse>> getCompetitionResult(
            @PathVariable String publicId
    ) {
        CompetitionResultResponse response = competitionQueryService.getCompetitionResult(publicId);
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @PostMapping("/competitions/{publicId}/admin-password")
    public ResponseEntity<ResponseDTO<CompetitionAdminTokenResponse>> setAdminPassword(
            Authentication authentication,
            @PathVariable String publicId,
            @RequestHeader(value = "X-Competition-Admin-Token", required = false) String adminToken,
            @RequestBody CompetitionAdminPasswordRequest request
    ) {
        String token = competitionAdminAuthorizationService.setAdminPassword(
                publicId,
                resolveAuthenticatedUserId(authentication),
                adminToken,
                request.getPassword()
        );
        return new ResponseEntity<>(
                ResponseDTO.success(new CompetitionAdminTokenResponse(token)),
                HttpStatus.OK
        );
    }

    @PostMapping("/competitions/{publicId}/admin-login")
    public ResponseEntity<ResponseDTO<CompetitionAdminTokenResponse>> adminLogin(
            @PathVariable String publicId,
            @RequestBody CompetitionAdminPasswordRequest request
    ) {
        String token = competitionAdminAuthorizationService.login(publicId, request.getPassword());
        return new ResponseEntity<>(
                ResponseDTO.success(new CompetitionAdminTokenResponse(token)),
                HttpStatus.OK
        );
    }

    @PatchMapping("/competitions/{publicId}")
    public ResponseEntity<ResponseDTO<CompetitionUpdateResponse>> updateCompetition(
            Authentication authentication,
            @PathVariable String publicId,
            @RequestHeader(value = "X-Competition-Admin-Token", required = false) String adminToken,
            @RequestBody CompetitionUpdateRequest request
    ) {
        CompetitionUpdateResponse response = competitionCommandService.updateCompetition(
                publicId, request, resolveAuthenticatedUserId(authentication), adminToken
        );
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @PatchMapping("/competitions/{publicId}/court-count")
    public ResponseEntity<ResponseDTO<CompetitionStatResponse>> updateCourtCount(
            Authentication authentication,
            @PathVariable String publicId,
            @RequestHeader(value = "X-Competition-Admin-Token", required = false) String adminToken,
            @RequestBody CourtCountUpdateRequest request
    ) {
        CompetitionStatResponse response = competitionGameCommandService.updateCourtCount(
                publicId, resolveAuthenticatedUserId(authentication), adminToken, request
        );
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @GetMapping("/competitions/{publicId}/entries")
    public ResponseEntity<ResponseDTO<List<CompetitionEntryResponse>>> getCompetitionEntries(
            @PathVariable String publicId
    ) {
        List<CompetitionEntryResponse> response = competitionEntryQueryService.getCompetitionEntries(publicId);
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @PostMapping("/competitions/{publicId}/entries")
    public ResponseEntity<ResponseDTO<CompetitionEntryResponse>> createCompetitionEntry(
            Authentication authentication,
            @PathVariable String publicId,
            @RequestHeader(value = "X-Competition-Admin-Token", required = false) String adminToken,
            @RequestBody CompetitionEntryCreateRequest request
    ) {
        CompetitionEntryResponse response = competitionEntryCommandService.createCompetitionEntry(
                publicId,
                resolveAuthenticatedUserId(authentication),
                adminToken,
                request
        );
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @PatchMapping("/competitions/{publicId}/entries/{entryId}")
    public ResponseEntity<ResponseDTO<CompetitionEntryResponse>> updateCompetitionEntry(
            Authentication authentication,
            @PathVariable String publicId,
            @PathVariable Long entryId,
            @RequestHeader(value = "X-Competition-Admin-Token", required = false) String adminToken,
            @RequestBody CompetitionEntryUpdateRequest request
    ) {
        CompetitionEntryResponse response = competitionEntryCommandService.updateCompetitionEntry(
                publicId,
                entryId,
                resolveAuthenticatedUserId(authentication),
                adminToken,
                request
        );
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @PatchMapping("/competitions/{publicId}/games/{gameId}/entries")
    public ResponseEntity<ResponseDTO<GameEntryUpdateResponse>> updateGameEntries(
            Authentication authentication,
            @PathVariable String publicId,
            @PathVariable Long gameId,
            @RequestHeader(value = "X-Competition-Admin-Token", required = false) String adminToken,
            @RequestBody GameEntryUpdateRequest request
    ) {
        GameEntryUpdateResponse response = competitionGameCommandService.updateGameEntries(
                publicId,
                gameId,
                resolveAuthenticatedUserId(authentication),
                adminToken,
                request
        );
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @PostMapping("/competitions/{publicId}/courts/{court}/games")
    public ResponseEntity<ResponseDTO<GameResponse>> createNextCourtGame(
            Authentication authentication,
            @PathVariable String publicId,
            @PathVariable Integer court,
            @RequestHeader(value = "X-Competition-Admin-Token", required = false) String adminToken
    ) {
        GameResponse response = competitionGameCommandService.createNextCourtGame(
                publicId, court, resolveAuthenticatedUserId(authentication), adminToken
        );
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @PatchMapping("/competitions/{publicId}/games/{gameId}/status")
    public ResponseEntity<ResponseDTO<GameResponse>> updateGameStatus(
            Authentication authentication,
            @PathVariable String publicId,
            @PathVariable Long gameId,
            @RequestHeader(value = "X-Competition-Admin-Token", required = false) String adminToken,
            @RequestBody GameStatusUpdateRequest request
    ) {
        GameResponse response = competitionGameCommandService.updateGameStatus(
                publicId, gameId, resolveAuthenticatedUserId(authentication), adminToken, request
        );
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @DeleteMapping("/competitions/{publicId}/games/{gameId}")
    public ResponseEntity<ResponseDTO<Void>> deleteGame(
            Authentication authentication,
            @PathVariable String publicId,
            @PathVariable Long gameId,
            @RequestHeader(value = "X-Competition-Admin-Token", required = false) String adminToken
    ) {
        competitionGameCommandService.deleteGame(publicId, gameId, resolveAuthenticatedUserId(authentication), adminToken);
        return new ResponseEntity<>(ResponseDTO.success(null), HttpStatus.OK);
    }

    @PatchMapping("/competitions/{publicId}/games/{gameId}/score")
    public ResponseEntity<ResponseDTO<GameResponse>> updateGameScore(
            Authentication authentication,
            @PathVariable String publicId,
            @PathVariable Long gameId,
            @RequestHeader(value = "X-Competition-Admin-Token", required = false) String adminToken,
            @RequestBody GameScoreUpdateRequest request
    ) {
        GameResponse response = competitionGameCommandService.updateGameScore(
                publicId, gameId, resolveAuthenticatedUserId(authentication), adminToken, request
        );
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
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
