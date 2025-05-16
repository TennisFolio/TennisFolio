package com.tennisfolio.Tennisfolio.test.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="tb_test_option")
@NoArgsConstructor
public class TestOption {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="OPTION_ID")
    private Long optionId;
    @ManyToOne
    @JoinColumn(name="QUESTION_ID")
    private TestQuestion question;
    @Column(name="OPTION_TEXT")
    private String optionText;
}
