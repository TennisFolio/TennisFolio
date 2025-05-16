package com.tennisfolio.Tennisfolio.test.domain;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="tb_test_category")
@NoArgsConstructor
public class TestCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="TEST_CATEGORY_ID")
    private Long testCategoryId;
    @Column(name="TEST_CATEGORY_NAME")
    private String testCategoryName;
}
