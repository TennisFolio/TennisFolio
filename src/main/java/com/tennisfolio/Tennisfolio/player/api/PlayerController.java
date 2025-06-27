package com.tennisfolio.Tennisfolio.player.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageService;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerImageService playerImageService;

    public PlayerController(PlayerImageService playerImageService){
        this.playerImageService = playerImageService;
    }
    @PostMapping("/{rapidId}/image")
    public ResponseEntity<ResponseDTO<String>> savePlayerImage(@PathVariable("rapidId") String rapidId){

        String answer = playerImageService.fetchImage(rapidId);

        return new ResponseEntity(ResponseDTO.success(answer), HttpStatus.OK);
    }

}
