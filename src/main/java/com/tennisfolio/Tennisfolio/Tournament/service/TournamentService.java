package com.tennisfolio.Tennisfolio.Tournament.service;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;

import java.util.List;

public interface TournamentService {
    List<Tournament> saveTournamentList();
    List<Tournament> saveTournamentInfo();
    List<Tournament> saveLeagueDetails();
}
