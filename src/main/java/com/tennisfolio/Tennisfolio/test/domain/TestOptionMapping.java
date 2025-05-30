package com.tennisfolio.Tennisfolio.test.domain;

import com.tennisfolio.Tennisfolio.common.TestType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="tb_test_option_mapping")
@NoArgsConstructor
public class TestOptionMapping {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="MAPPING_ID")
    private Long mappingId;
    @ManyToOne
    @JoinColumn(name="OPTION_ID")
    private TestOption option;
    @Enumerated(EnumType.STRING)
    private TestType targetType;
    @Column(name="TARGET_ID")
    private Long targetId;
    @Column(name="SCORE")
    private int score;

}
