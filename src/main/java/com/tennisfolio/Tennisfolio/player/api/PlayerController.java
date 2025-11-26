package com.tennisfolio.Tennisfolio.player.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageService;
import com.tennisfolio.Tennisfolio.player.application.PlayerDetailService;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.dto.PlayerDetailResponse;
import com.tennisfolio.Tennisfolio.player.dto.PlayerMatchResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerImageService playerImageService;
    private final PlayerDetailService playerDetailService;

    public PlayerController(PlayerImageService playerImageService, PlayerDetailService playerDetailService){
        this.playerImageService = playerImageService;
        this.playerDetailService = playerDetailService;
    }
    @PostMapping("/{rapidId}/image")
    public ResponseEntity<ResponseDTO<String>> savePlayerImage(@PathVariable("rapidId") String rapidId){

        String answer = playerImageService.fetchImage(rapidId);

        return new ResponseEntity(ResponseDTO.success(answer), HttpStatus.OK);
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<ResponseDTO<PlayerDetailResponse>> getPlayerDetail(@PathVariable("playerId") Long playerId){
        PlayerDetailResponse playerDetail = playerDetailService.findPlayerDetail(playerId);
        if(playerDetail == null) return new ResponseEntity<>(ResponseDTO.error("404", "Player not found"), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(ResponseDTO.success(playerDetail), HttpStatus.OK);
    }

    @GetMapping("/{playerId}/match")
    public ResponseEntity<ResponseDTO<List<PlayerMatchResponse>>> getPlayerMatch(@PathVariable("playerId") Long playerId){
        List<PlayerMatchResponse> results = playerDetailService.findPlayerMatch(playerId);

        return new ResponseEntity<>(ResponseDTO.success(results), HttpStatus.OK);
    }

}
