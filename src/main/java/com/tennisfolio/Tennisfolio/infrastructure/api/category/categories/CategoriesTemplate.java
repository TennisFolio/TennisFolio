package com.tennisfolio.Tennisfolio.infrastructure.api.category.categories;

import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryEntity;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoriesTemplate extends StrategyApiTemplate<List<CategoryDTO>, List<Category>> {

    public CategoriesTemplate(
              EntityMapper<List<CategoryDTO>, List<Category>> categoriesEntityMapper
            , ResponseParser<List<CategoryDTO>> categoriesResponseParser) {
        super( categoriesResponseParser, categoriesEntityMapper,  RapidApi.CATEGORIES);
    }

}
