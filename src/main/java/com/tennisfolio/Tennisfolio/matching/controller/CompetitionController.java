package com.tennisfolio.Tennisfolio.matching.controller;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CompetitionController {

    private final CompetitionCommandService competitionCommandService;

    public CompetitionController(CompetitionCommandService competitionCommandService) {
        this.competitionCommandService = competitionCommandService;
    }

    @PostMapping("/competitions")
    public ResponseEntity<ResponseDTO<CompetitionCreateResponse>> createCompetition(
            @RequestBody CompetitionCreateRequest request
    ) {
        CompetitionCreateResponse response = competitionCommandService.createCompetition(request);
        return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
    }
}
