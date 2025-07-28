package com.tennisfolio.Tennisfolio.category.repository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<CategoryEntity> findAll();
    List<CategoryEntity> findByRapidCategoryIdNotIn(List<String> ids);
    Optional<CategoryEntity> findByRapidCategoryId(String rapidId);
    List<CategoryEntity> saveAll(List<CategoryEntity> categories);
    CategoryEntity save(CategoryEntity categoryEntity);
}
