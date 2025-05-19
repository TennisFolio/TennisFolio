package com.tennisfolio.Tennisfolio.test.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="tb_test_racket")
@NoArgsConstructor
public class TestRacket {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="RACKET_ID")
    private Long racketId;
    @Column(name="BRAND")
    private String brand;
    @Column(name="MODEL_NAME")
    private String modelName;
    @Column(name="DESCRIPTION")
    private String description;
    @Column(name="IMAGE")
    private String image;
}
