package com.tennisfolio.Tennisfolio.test.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name="tb_test_question")
@NoArgsConstructor
public class TestQuestion extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="QUESTION_ID")
    private Long questionId;
    @ManyToOne
    @JoinColumn(name = "TEST_CATEGORY_ID")
    private TestCategory testCategory;
    @Column(name="QUESTION_TEXT")
    private String questionText;
    @Column(name="QUESTION_ORDER")
    private int questionOrder;
    @OneToMany(mappedBy = "question")
    @JsonIgnore
    private List<TestOption> testOptionList;
}
