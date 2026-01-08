package com.tennisfolio.Tennisfolio.player.domain;

import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static com.tennisfolio.Tennisfolio.util.FiledUpdateUtil.updated;

@Getter
@Builder
@AllArgsConstructor
public class Player {

    private Long playerId;

    private String rapidPlayerId;

    private String playerName;

    private String playerNameKr;

    private String birth;

    private Country country;

    private String turnedPro;

    private String weight;

    private String height;

    private String plays;

    private String image;

    private String gender;

    private PlayerPrize prize;

    public Player(TeamDetailsApiDTO rapidDTO){
        this.rapidPlayerId = rapidDTO.getPlayerRapidId();
        this.playerName = rapidDTO.getPlayerName();
        this.birth = rapidDTO.getBirthDate();
        this.country = new Country(rapidDTO.getCountry());
        this.turnedPro = rapidDTO.getTurnedPro();
        this.weight = rapidDTO.getWeight();
        this.height = rapidDTO.getHeight();
        this.plays = rapidDTO.getPlays();
        this.gender = rapidDTO.getGender();
    }

    public void updateFrom(Player player){
        this.rapidPlayerId = updated(this.rapidPlayerId, player.getRapidPlayerId());
        this.playerName = updated(this.playerName, player.getPlayerName());
        this.birth = updated(this.birth, player.getBirth());
        this.turnedPro = updated(this.turnedPro, player.getTurnedPro());
        this.weight = updated(this.weight, player.getWeight());
        this.height = updated(this.height, player.getHeight());
        this.plays = updated(this.plays, player.getPlays());
        this.gender = updated(this.gender, player.getGender());
        Country newCountry = player.getCountry();
        if(!newCountry.equals(this.country)) this.country = newCountry;
    }

    public void updateProfileImage(String image){
        this.image = image;
    }

    public void updatePrize(PlayerPrize prize){
        this.prize = prize;
    }
}
