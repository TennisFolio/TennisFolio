package com.tennisfolio.Tennisfolio.player.domain;

import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsApiDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_player")
@Getter
@Setter
@NoArgsConstructor
public class Player{

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
    @Column(name="COUNTRY")
    private String country;
    @Column(name="TURNED_PRO")
    private String turnedPro;
    @Column(name="WEIGHT")
    private String weight;
    @Column(name="HEIGHT")
    private String height;
    @Column(name="PLAYS")
    private String plays;
    @Column(name="PRIZE_CURRENT")
    private Long prizeCurrent;
    @Column(name="PRIZE_TOTAL")
    private Long prizeTotal;
    @Column(name="IMAGE")
    private String image;

    public Player(TeamDetailsApiDTO rapidDTO){
        this.rapidPlayerId = rapidDTO.getPlayerRapidId();
        this.playerName = rapidDTO.getPlayerName();
        this.birth = rapidDTO.getBirthDate();
        this.country = rapidDTO.getCountry() != null ? rapidDTO.getCountry().getAlpha() : "";
        this.turnedPro = rapidDTO.getTurnedPro();
        this.weight = rapidDTO.getWeight();
        this.height = rapidDTO.getHeight();
        this.plays = rapidDTO.getPlays();
        this.prizeCurrent = rapidDTO.getPrizeCurrent();
        this.prizeTotal = rapidDTO.getPrizeTotal();
    }
}