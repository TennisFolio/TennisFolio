package com.tennisfolio.Tennisfolio.matching.controller;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionDetailResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionResultResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionUpdateResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameEntryUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.GameEntryUpdateResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameScoreUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionEntryCommandService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionEntryQueryService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionGameCommandService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    public CompetitionController(
            CompetitionCommandService competitionCommandService,
            CompetitionQueryService competitionQueryService,
            CompetitionEntryCommandService competitionEntryCommandService,
            CompetitionEntryQueryService competitionEntryQueryService,
            CompetitionGameCommandService competitionGameCommandService
    ) {
        this.competitionCommandService = competitionCommandService;
        this.competitionQueryService = competitionQueryService;
        this.competitionEntryCommandService = competitionEntryCommandService;
        this.competitionEntryQueryService = competitionEntryQueryService;
        this.competitionGameCommandService = competitionGameCommandService;
    }

    @PostMapping("/competitions")
    public ResponseEntity<ResponseDTO<CompetitionCreateResponse>> createCompetition(
            @RequestBody CompetitionCreateRequest request
    ) {
        CompetitionCreateResponse response = competitionCommandService.createCompetition(request);
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @GetMapping("/competitions/{publicId}")
    public ResponseEntity<ResponseDTO<CompetitionDetailResponse>> getCompetition(
            @PathVariable String publicId
    ) {
        CompetitionDetailResponse response = competitionQueryService.getCompetition(publicId);
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @GetMapping("/competitions/{publicId}/result")
    public ResponseEntity<ResponseDTO<CompetitionResultResponse>> getCompetitionResult(
            @PathVariable String publicId
    ) {
        CompetitionResultResponse response = competitionQueryService.getCompetitionResult(publicId);
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @PatchMapping("/competitions/{publicId}")
    public ResponseEntity<ResponseDTO<CompetitionUpdateResponse>> updateCompetition(
            @PathVariable String publicId,
            @RequestHeader(value = "X-Competition-Edit-Token", required = false) String editToken,
            @RequestBody CompetitionUpdateRequest request
    ) {
        CompetitionUpdateResponse response = competitionCommandService.updateCompetition(publicId, request, editToken);
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @GetMapping("/competitions/{publicId}/entries")
    public ResponseEntity<ResponseDTO<List<CompetitionEntryResponse>>> getCompetitionEntries(
            @PathVariable String publicId
    ) {
        List<CompetitionEntryResponse> response = competitionEntryQueryService.getCompetitionEntries(publicId);
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @PatchMapping("/competitions/{publicId}/entries/{entryId}")
    public ResponseEntity<ResponseDTO<CompetitionEntryResponse>> updateCompetitionEntry(
            @PathVariable String publicId,
            @PathVariable Long entryId,
            @RequestHeader(value = "X-Competition-Edit-Token", required = false) String editToken,
            @RequestBody CompetitionEntryUpdateRequest request
    ) {
        CompetitionEntryResponse response = competitionEntryCommandService.updateCompetitionEntry(
                publicId,
                entryId,
                editToken,
                request
        );
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @PatchMapping("/competitions/{publicId}/games/{gameId}/entries")
    public ResponseEntity<ResponseDTO<GameEntryUpdateResponse>> updateGameEntries(
            @PathVariable String publicId,
            @PathVariable Long gameId,
            @RequestHeader(value = "X-Competition-Edit-Token", required = false) String editToken,
            @RequestBody GameEntryUpdateRequest request
    ) {
        GameEntryUpdateResponse response = competitionGameCommandService.updateGameEntries(
                publicId,
                gameId,
                editToken,
                request
        );
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }

    @PatchMapping("/competitions/{publicId}/games/{gameId}/score")
    public ResponseEntity<ResponseDTO<GameResponse>> updateGameScore(
            @PathVariable String publicId,
            @PathVariable Long gameId,
            @RequestBody GameScoreUpdateRequest request
    ) {
        GameResponse response = competitionGameCommandService.updateGameScore(publicId, gameId, request);
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }
}
