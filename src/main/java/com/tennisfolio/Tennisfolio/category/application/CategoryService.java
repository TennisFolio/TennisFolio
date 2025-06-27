package com.tennisfolio.Tennisfolio.category.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;
import com.tennisfolio.Tennisfolio.category.domain.model.Category;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final StrategyApiTemplate<List<CategoryDTO>, List<Category>> categoriesApiTemplate;

    public CategoryService(@Qualifier("categoriesTemplate")StrategyApiTemplate<List<CategoryDTO>, List<Category>> categoriesApiTemplate) {
        this.categoriesApiTemplate = categoriesApiTemplate;
    }


    public void saveCategory() {
        categoriesApiTemplate.execute("");
    }
}
