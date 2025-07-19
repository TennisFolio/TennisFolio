package com.tennisfolio.Tennisfolio.category.application;

import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;
import com.tennisfolio.Tennisfolio.category.domain.model.Category;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final StrategyApiTemplate<List<CategoryDTO>, List<Category>> categoriesApiTemplate;
    private final CategoryRepository categoryRepository;

    public CategoryService(@Qualifier("categoriesTemplate")StrategyApiTemplate<List<CategoryDTO>, List<Category>> categoriesApiTemplate, CategoryRepository categoryRepository) {
        this.categoriesApiTemplate = categoriesApiTemplate;
        this.categoryRepository = categoryRepository;
    }

    public void saveCategory() {
        List<Category> categories = categoriesApiTemplate.execute("");
        categoryRepository.saveAll(categories);
    }
}
