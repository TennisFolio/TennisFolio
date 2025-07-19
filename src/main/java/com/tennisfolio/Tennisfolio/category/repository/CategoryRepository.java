package com.tennisfolio.Tennisfolio.category.repository;

import com.tennisfolio.Tennisfolio.category.domain.model.Category;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<Category> findByRapidCategoryIds(List<String> ids);
    Optional<Category> findByRapidCategoryId(String rapidId);
    List<Category> saveAll(List<Category> categories);
    Category save(Category category);
}
