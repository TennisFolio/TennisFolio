package com.tennisfolio.Tennisfolio.category.domain.model;

import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_category")
@Getter
@NoArgsConstructor
public class Category extends BaseTimeEntity {
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


    public Category(CategoryDTO dto){
        this.rapidCategoryId = dto.getRapidId();
        this.categoryName = dto.getName();
        this.categorySlug = dto.getSlug();
    }

    public void updateFrom(Category incoming){
        this.rapidCategoryId = incoming.getRapidCategoryId();
        this.categoryName = incoming.getCategoryName();
        this.categorySlug = incoming.getCategorySlug();
    }
}
