package com.tennisfolio.Tennisfolio.matching.domain;

import java.util.List;

public class ScheduleGenerationRequest {
    private final List<ScheduleParticipant> participants;
    private final int courtCount;
    private final int totalGames;
    private final long seed;
    private final ScheduleGenerationOptions options;

    public ScheduleGenerationRequest(List<ScheduleParticipant> participants, int courtCount, int totalGames, long seed, ScheduleGenerationOptions options) {
        this.participants = List.copyOf(participants);
        this.courtCount = courtCount;
        this.totalGames = totalGames;
        this.seed = seed;
        this.options = options;
    }

    public List<ScheduleParticipant> getParticipants() { return participants; }
    public int getCourtCount() { return courtCount; }
    public int getTotalGames() { return totalGames; }
    public long getSeed() { return seed; }
    public ScheduleGenerationOptions getOptions() { return options; }
}
