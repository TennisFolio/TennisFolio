package com.tennisfolio.Tennisfolio.test.domain.model;

import com.tennisfolio.Tennisfolio.player.repository.PlayerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="tb_test_player")
@NoArgsConstructor
public class TestPlayer {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="TEST_PLAYER_ID")
    private Long testPlayerId;
    @OneToOne
    @JoinColumn(name="PLAYER_ID")
    private PlayerEntity player;
    @Column(name="PLAYER_NAME")
    private String playerName;
    @Column(name="DESCRIPTION")
    private String description;
    @Column(name="QUERY")
    private String query;
    @Column(name="IMAGE")
    private String image;
}
