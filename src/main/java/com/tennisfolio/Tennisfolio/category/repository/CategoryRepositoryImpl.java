package com.tennisfolio.Tennisfolio.category.repository;

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
    public List<CategoryEntity> findAll() {
        return categoryJpaRepository.findAll();
    }

    @Override
    public List<CategoryEntity> findByRapidCategoryIdNotIn(List<String> ids) {
        return categoryJpaRepository.findByRapidCategoryIdNotIn(ids);
    }

    @Override
    public Optional<CategoryEntity> findByRapidCategoryId(String rapidId) {
        return categoryJpaRepository.findByRapidCategoryId(rapidId);
    }

    @Override
    public List<CategoryEntity> saveAll(List<CategoryEntity> categories) {
        return categoryJpaRepository.saveAll(categories);
    }

    @Override
    public CategoryEntity save(CategoryEntity categoryEntity) {
        return categoryJpaRepository.save(categoryEntity);
    }
}
