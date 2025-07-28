package com.tennisfolio.Tennisfolio.Tournament.api;

import com.tennisfolio.Tennisfolio.Tournament.application.TournamentSyncService;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tournament")
public class TournamentController {

    private final TournamentSyncService tournamentSyncService;

    public TournamentController(TournamentSyncService tournamentSyncService) {
        this.tournamentSyncService = tournamentSyncService;
    }
    @PostMapping("")
    public ResponseEntity<ResponseDTO> saveTournament(){
        tournamentSyncService.saveTournamentList();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/detail")
    public ResponseEntity<ResponseDTO> saveTournamentDetail(){
        ResponseDTO responseDTO;
        try{
            tournamentSyncService.saveTournamentDetail();
            responseDTO = ResponseDTO.success();
        }catch(Exception e){
            responseDTO = ResponseDTO.error("500", e.getMessage());
        }


        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
