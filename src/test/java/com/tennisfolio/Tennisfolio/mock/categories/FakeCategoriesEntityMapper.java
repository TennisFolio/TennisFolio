package com.tennisfolio.Tennisfolio.mock.categories;

import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;

import java.util.ArrayList;
import java.util.List;

public class FakeCategoriesEntityMapper implements EntityMapper<List<CategoryDTO>, List<Category>> {
    @Override
    public List<Category> map(List<CategoryDTO> dto, Object... params) {
        Category category4 = Category.builder()
                .rapidCategoryId("213")
                .categoryName("ITF Women")
                .categorySlug("itf-women")
                .build();

        Category category5 = Category.builder()
                .rapidCategoryId("785")
                .categoryName("ITF Men")
                .categorySlug("itf-men")
                .build();

        Category category6 = Category.builder()
                .rapidCategoryId("871")
                .categoryName("WTA 125")
                .categorySlug("wta-125")
                .build();

        List<Category> categories = new ArrayList<>();
        categories.add(category4);
        categories.add(category5);
        categories.add(category6);

        return categories;
    }
}
