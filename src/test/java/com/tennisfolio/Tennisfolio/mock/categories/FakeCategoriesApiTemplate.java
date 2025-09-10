package com.tennisfolio.Tennisfolio.mock.categories;

import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;

import java.util.List;

public class FakeCategoriesApiTemplate extends StrategyApiTemplate<List<CategoryDTO>, List<Category>> {
    public FakeCategoriesApiTemplate(ApiCaller apiCaller, ResponseParser<List<CategoryDTO>> parser, EntityMapper<List<CategoryDTO>, List<Category>> mapper, RapidApi endpoint) {
        super(apiCaller, parser, mapper, endpoint);
    }
}
