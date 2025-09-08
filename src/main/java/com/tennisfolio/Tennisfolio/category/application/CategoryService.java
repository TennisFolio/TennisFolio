package com.tennisfolio.Tennisfolio.category.application;

import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;
import com.tennisfolio.Tennisfolio.category.repository.CategoryEntity;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final StrategyApiTemplate<List<CategoryDTO>, List<Category>> categoriesApiTemplate;
    private final CategoryRepository categoryRepository;

    public CategoryService(@Qualifier("categoriesTemplate")StrategyApiTemplate<List<CategoryDTO>, List<Category>> categoriesApiTemplate, CategoryRepository categoryRepository) {
        this.categoriesApiTemplate = categoriesApiTemplate;
        this.categoryRepository = categoryRepository;
    }

    public void saveCategory() {
        List<Category> incoming = categoriesApiTemplate.execute("");

        if (incoming.isEmpty()) return;

        List<String> ids = incoming.stream()
                .map(Category::getRapidCategoryId)
                .toList();

        // 이미 있는 것들 조회
        List<Category> existing = categoryRepository.findByRapidCategoryIdIn(ids);
        Set<String> existingIds = existing.stream()
                .map(Category::getRapidCategoryId)
                .collect(Collectors.toSet());

        // 신규만 저장
        List<Category> toSave = incoming.stream()
                .filter(c -> !existingIds.contains(c.getRapidCategoryId()))
                .toList();

        if (!toSave.isEmpty()) {
            categoryRepository.saveAll(toSave);
        }
    }

    public List<Category> getByRapidCategoryIdNotIn(List<String> rapidIds){
        return categoryRepository.findByRapidCategoryIdNotIn(rapidIds);
    }

    public List<Category> getByRapidCategoryId(List<String> rapidIds){
        return categoryRepository.findByRapidCategoryIdIn(rapidIds);
    }
}
