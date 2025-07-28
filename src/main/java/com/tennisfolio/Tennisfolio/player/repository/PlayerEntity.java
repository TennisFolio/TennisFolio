package com.tennisfolio.Tennisfolio.player.repository;

import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.prize.repository.PlayerPrizeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_player")
@Getter
@NoArgsConstructor
public class PlayerEntity extends BaseTimeEntity {

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
    private CountryEntity countryEntity;
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
    @OneToOne(mappedBy="playerEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PlayerPrizeEntity prizeEntity;

    public static PlayerEntity fromModel(Player player) {
        PlayerEntity playerEntity = new PlayerEntity();
        playerEntity.playerId = player.getPlayerId();
        playerEntity.rapidPlayerId = player.getRapidPlayerId();
        playerEntity.playerName = player.getPlayerName();
        playerEntity.birth = player.getBirth();
        playerEntity.countryEntity = player.getCountry() != null
                ? CountryEntity.fromModel(player.getCountry())
                : null;
        playerEntity.turnedPro = player.getTurnedPro();
        playerEntity.weight = player.getWeight();
        playerEntity.height = player.getHeight();
        playerEntity.plays = player.getPlays();
        playerEntity.image = player.getImage();
        //playerEntity.prizeEntity = PlayerPrizeEntity.fromModel(player.getPrize());

        return playerEntity;
    }

    public Player toModel(){
        return Player.builder()
                .playerId(playerId)
                .rapidPlayerId(rapidPlayerId)
                .playerName(playerName)
                .birth(birth)
                .country(countryEntity != null
                        ? countryEntity.toModel()
                        : null)
                .turnedPro(turnedPro)
                .weight(weight)
                .height(height)
                .plays(plays)
                .image(image)
                //.prize(prizeEntity.toModel())
                .build();
    }

}