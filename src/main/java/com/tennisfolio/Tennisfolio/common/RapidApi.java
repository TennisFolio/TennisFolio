package com.tennisfolio.Tennisfolio.common;

public enum RapidApi {
    ATPRANKINGS("atpRankings", "/tennis/rankings/atp"),
    TEAMDETAILS("teamDetails", "/tennis/team/%s"),
    LIVEEVENTS("liveEvents", "/tennis/events/live"),
    TEAMIMAGE("teamImage", "/tennis/team/%s/image"),
    CATEGORIES("categories", "/tennis/tournament/categories"),
    CATEGORYTOURNAMENTS("categoryTournaments", "/tennis/tournament/all/category/%s"),
    TOURNAMENTINFO("tournamentInfo", "/tennis/tournament/%s/info"),
    LEAGUEDETAILS("leagueDetails", "/tennis/tournament/%s"),
    LEAGUESEASONS("leagueSeasons", "/tennis/tournament/%s/seasons"),
    LEAGUESEASONINFO("leagueSeasonInfo", "/tennis/tournament/%s/season/%s/info"),
    LEAGUEROUNDS("leagueRounds", "/tennis/tournament/%s/season/%s/rounds");

    private final String methodName;
    private final String param;

    RapidApi(String methodName, String param){
        this.methodName = methodName;
        this.param = param;
    }

    public String getMethodName(){
        return this.methodName;
    }

    public String getParam(){
        return this.param;
    }
    public String getParam(Object... params){
        return String.format(param, params);
    }
}
