package com.tennisfolio.Tennisfolio.category.enums;

import lombok.Getter;

@Getter
public enum CategoryType {
    UNITED_CUP("1705", "United Cup", "united-cup"),
    ATP("3", "ATP", "atp"),
    DAVIS_CUP("76", "Davis Cup", "davis-cup"),
    WTA("6", "WTA", "wta"),
    EXHIBITION("79", "Exhibition", "exhibition"),
    BILLIE_JEAN_KING_CUP("74", "Billie Jean King Cup", "billie-jean-king-cup");

    private final String rapidCategoryId;
    private final String categoryName;
    private final String categorySlug;


    CategoryType(String rapidCategoryId, String categoryName, String categorySlug) {
        this.rapidCategoryId = rapidCategoryId;
        this.categoryName = categoryName;
        this.categorySlug = categorySlug;
    }


}
