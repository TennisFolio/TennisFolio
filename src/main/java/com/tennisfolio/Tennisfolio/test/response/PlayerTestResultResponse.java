package com.tennisfolio.Tennisfolio.test.response;

import com.tennisfolio.Tennisfolio.test.domain.model.TestPlayer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class PlayerTestResultResponse implements TestResultResponse{
    private Long testPlayerId;
    private TestPlayerResponse player;
    private String playerName;
    private String description;
    private String query;
    private String image;

    public PlayerTestResultResponse(TestPlayer testPlayer){
        this.testPlayerId = testPlayer.getTestPlayerId();
        this.player = new TestPlayerResponse(testPlayer.getPlayer());
        this.playerName = testPlayer.getPlayerName();
        this.description = testPlayer.getDescription();
        this.query = testPlayer.getQuery();
        this.image = testPlayer.getImage();
    }

    @Override
    public Long getResultId() {
        return testPlayerId;
    }

    @Override
    public String getName() {
        return playerName;
    }

    @Override
    public String getDescription(){
        return description;
    }

    @Override
    public String getQuery(){
        return query;
    }

    @Override
    public String getImage(){
        return image;
    }
}
