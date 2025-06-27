package com.tennisfolio.Tennisfolio.infrastructure.api.category.categories;

import com.tennisfolio.Tennisfolio.category.domain.model.Category;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoriesTemplate extends StrategyApiTemplate<List<CategoryDTO>, List<Category>> {

    public CategoriesTemplate(
              EntityMapper<List<CategoryDTO>, List<Category>> categoriesEntityMapper
            , ResponseParser<List<CategoryDTO>> categoriesResponseParser, EntitySaver<List<Category>> categoriesEntitySaver) {
        super( categoriesResponseParser, categoriesEntityMapper, categoriesEntitySaver, RapidApi.CATEGORIES);
    }

}
