package com.tennisfolio.Tennisfolio.category.domain;

import com.tennisfolio.Tennisfolio.category.enums.CategoryType;
import com.tennisfolio.Tennisfolio.category.repository.CategoryEntity;
import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Category {

    private Long categoryId;

    private String rapidCategoryId;

    private String categoryName;

    private String categorySlug;


    public void updateFrom(Category incoming){
        this.rapidCategoryId = incoming.getRapidCategoryId();
        this.categoryName = incoming.getCategoryName();
        this.categorySlug = incoming.getCategorySlug();
    }

    public boolean isSupportedCategory(){
        if(this.rapidCategoryId == null || "".equals(this.rapidCategoryId)) return false;

        return this.rapidCategoryId.equals(CategoryType.ATP.getRapidCategoryId()) ||
                this.rapidCategoryId.equals(CategoryType.WTA.getRapidCategoryId()) ||
                this.rapidCategoryId.equals(CategoryType.UNITED_CUP.getRapidCategoryId()) ||
                this.rapidCategoryId.equals(CategoryType.DAVIS_CUP.getRapidCategoryId()) ||
                this.rapidCategoryId.equals(CategoryType.EXHIBITION.getRapidCategoryId()) ||
                this.rapidCategoryId.equals(CategoryType.BILLIE_JEAN_KING_CUP.getRapidCategoryId());
    }
}
