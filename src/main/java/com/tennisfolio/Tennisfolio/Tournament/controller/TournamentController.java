package com.tennisfolio.Tennisfolio.Tournament.controller;

import com.tennisfolio.Tennisfolio.Tournament.service.TournamentService;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tournament")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }
    @PostMapping("")
    public ResponseEntity<ResponseDTO> saveTournament(){
        tournamentService.saveTournamentList();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/info")
    public ResponseEntity<ResponseDTO> saveTournamentInfo(){
        tournamentService.saveTournamentInfo();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/details")
    public ResponseEntity<ResponseDTO> saveLeagueDetails(){
        tournamentService.saveLeagueDetails();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
