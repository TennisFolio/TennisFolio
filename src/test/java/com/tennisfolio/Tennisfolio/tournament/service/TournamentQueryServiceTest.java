package com.tennisfolio.Tennisfolio.tournament.service;

import com.tennisfolio.Tennisfolio.Tournament.application.TournamentQueryService;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.mock.FakeTournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TournamentQueryServiceTest {
    private TournamentQueryService tournamentQueryService;
    private TournamentRepository tournamentRepository = new FakeTournamentRepository();

    @BeforeEach
    public void init(){
            
    }

    @Test
    public void 카테고리를_통한_토너먼트_조회(){

    }
}
