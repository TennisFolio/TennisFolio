package com.tennisfolio.Tennisfolio.category.repository;

import com.tennisfolio.Tennisfolio.category.domain.model.Category;
import com.tennisfolio.Tennisfolio.infrastructure.repository.CategoryJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository{
    private final CategoryJpaRepository categoryJpaRepository;

    public CategoryRepositoryImpl(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public List<Category> findByRapidCategoryIds(List<String> ids) {
        return categoryJpaRepository.findByRapidCategoryIds(ids);
    }

    @Override
    public Optional<Category> findByRapidCategoryId(String rapidId) {
        return categoryJpaRepository.findByRapidCategoryId(rapidId);
    }

    @Override
    public List<Category> saveAll(List<Category> categories) {
        return categoryJpaRepository.saveAll(categories);
    }

    @Override
    public Category save(Category category) {
        return categoryJpaRepository.save(category);
    }
}
