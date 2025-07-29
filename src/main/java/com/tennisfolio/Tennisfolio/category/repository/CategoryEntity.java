package com.tennisfolio.Tennisfolio.category.repository;

import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.match.domain.Statistic;
import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import com.tennisfolio.Tennisfolio.match.repository.StatisticEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_category")
@Getter
@NoArgsConstructor
public class CategoryEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="CATEGORY_ID")
    private Long categoryId;
    @Column(name="RAPID_CATEGORY_ID")
    private String rapidCategoryId;
    @Column(name="CATEGORY_NAME")
    private String categoryName;
    @Column(name="CATEGORY_SLUG")
    private String categorySlug;

    public static CategoryEntity fromModel(Category category) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.categoryId = category.getCategoryId();
        categoryEntity.rapidCategoryId = category.getRapidCategoryId();
        categoryEntity.categoryName = category.getCategoryName();
        categoryEntity.categorySlug = category.getCategorySlug();

        return categoryEntity;
    }

    public Category toModel(){
        return Category.builder()
                .categoryId(categoryId)
                .rapidCategoryId(rapidCategoryId)
                .categoryName(categoryName)
                .categorySlug(categorySlug)
                .build();
    }
}
