package com.tennisfolio.Tennisfolio.infrastructure.api.category.categories;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import com.tennisfolio.Tennisfolio.category.domain.model.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
@Component
public class CategoriesEntitySaver implements EntitySaver<List<Category>> {
    private final CategoryRepository categoryRepository;

    public CategoriesEntitySaver(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> save(List<Category> entity) {
        // 저장할 category id
        List<String> ids = entity.stream()
                .map(Category::getRapidCategoryId)
                .collect(Collectors.toList());

        // 현재 DB에 있는 데이터
        Map<String, Category> existingMap = categoryRepository.findByRapidCategoryIds(ids).stream()
                .collect(Collectors.toMap(Category::getRapidCategoryId, Function.identity()));

        List<Category> toSave = entity.stream()
                .map(incoming -> {
                    Category existing = existingMap.get(incoming.getRapidCategoryId());
                    if(existing != null){
                        existing.updateFrom(incoming);
                    }
                    return incoming;
                }).collect(Collectors.toList());

        return categoryRepository.saveAll(toSave);
    }
}
