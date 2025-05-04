package com.tennisfolio.Tennisfolio.category.service;

import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.categories.CategoryDTO;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{
    private final AbstractApiTemplate<List<CategoryDTO>, List<Category>> categoriesApiTemplate;

    public CategoryServiceImpl(@Qualifier("categoriesTemplate")AbstractApiTemplate<List<CategoryDTO>, List<Category>> categoriesApiTemplate) {
        this.categoriesApiTemplate = categoriesApiTemplate;
    }

    @Override
    public void saveCategory() {
        categoriesApiTemplate.execute("");
    }
}
