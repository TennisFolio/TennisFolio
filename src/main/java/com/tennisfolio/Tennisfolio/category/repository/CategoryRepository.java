package com.tennisfolio.Tennisfolio.category.repository;

import com.tennisfolio.Tennisfolio.category.domain.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<Category> findAll();
    List<Category> findByRapidCategoryIdIn(List<String> ids);
    List<Category> findByRapidCategoryIdNotIn(List<String> ids);
    Optional<Category> findByRapidCategoryId(String rapidId);
    List<Category> saveAll(List<Category> categories);
    Category save(Category category);
}
