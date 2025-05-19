package com.tennisfolio.Tennisfolio.test.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="tb_test_string")
@NoArgsConstructor
public class TestString {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="STRING_ID")
    private Long stringId;
    @Column(name="STRING_NAME")
    private String stringName;
    @Column(name="STRING_TYPE")
    private String stringType;
    @Column(name="DESCRIPTION")
    private String description;
    @Column(name="IMAGE")
    private String image;
}
