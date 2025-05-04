package com.tennisfolio.Tennisfolio.player.controller;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.player.service.PlayerService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService){
        this.playerService = playerService;
    }
    @PostMapping("/{rapidId}/image")
    public ResponseEntity<ResponseDTO<String>> savePlayerImage(@PathVariable("rapidId") String rapidId){

        String answer = playerService.saveTeamImage(rapidId);

        return new ResponseEntity(ResponseDTO.success(answer), HttpStatus.OK);
    }
}
