package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.category.domain.Category;

public class CategoryFixtures {
    // Categories
    public static Category atp() {
        return Category.builder()
                .rapidCategoryId("3")
                .categoryName("ATP")
                .categorySlug("atp")
                .build();
    }

    public static Category wta() {
        return Category.builder()
                .rapidCategoryId("6")
                .categoryName("WTA")
                .categorySlug("wta")
                .build();
    }
}
