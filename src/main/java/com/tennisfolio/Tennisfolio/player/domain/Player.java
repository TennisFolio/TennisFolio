package com.tennisfolio.Tennisfolio.player.domain;

import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.tennisfolio.Tennisfolio.util.FiledUpdateUtil.updated;

@Entity
@Table(name = "tb_player")
@Getter
@NoArgsConstructor
public class Player extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PLAYER_ID")
    private Long playerId;
    @Column(name="RAPID_PLAYER_ID")
    private String rapidPlayerId;
    @Column(name="PLAYER_NAME")
    private String playerName;
    @Column(name="BIRTH")
    private String birth;
    @Embedded
    private Country country;
    @Column(name="TURNED_PRO")
    private String turnedPro;
    @Column(name="WEIGHT")
    private String weight;
    @Column(name="HEIGHT")
    private String height;
    @Column(name="PLAYS")
    private String plays;
    @Column(name="IMAGE")
    private String image;
    @OneToOne(mappedBy="player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
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
    }

    public void updateFrom(Player player){
        this.rapidPlayerId = updated(this.rapidPlayerId, player.getRapidPlayerId());
        this.playerName = updated(this.playerName, player.getPlayerName());
        this.birth = updated(this.birth, player.getBirth());
        this.turnedPro = updated(this.turnedPro, player.getTurnedPro());
        this.weight = updated(this.weight, player.getWeight());
        this.height = updated(this.height, player.getHeight());
        this.plays = updated(this.plays, player.getPlays());

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