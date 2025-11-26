package com.tennisfolio.Tennisfolio.player.application;

import com.tennisfolio.Tennisfolio.player.dto.PlayerDetailResponse;
import com.tennisfolio.Tennisfolio.player.dto.PlayerMatchResponse;
import com.tennisfolio.Tennisfolio.player.repository.PlayerQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerDetailService {
    private final PlayerQueryRepository playerQueryRepository;

    public PlayerDetailService(PlayerQueryRepository playerQueryRepository) {
        this.playerQueryRepository = playerQueryRepository;
    }

    public PlayerDetailResponse findPlayerDetail(Long playerId){
        return playerQueryRepository.findPlayerDetail(playerId);
    }

    public List<PlayerMatchResponse> findPlayerMatch(Long playerId){
        return playerQueryRepository.findMatchesByPlayerAndYear(playerId, "2025");
    }
}
