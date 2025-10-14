package com.tennisfolio.Tennisfolio.category.repository;

import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.category.domain.Category;
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
    public List<Category> findAll() {
        return categoryJpaRepository.findAll().stream().map(CategoryEntity::toModel).toList();
    }

    @Override
    public List<Category> findByRapidCategoryIdIn(List<String> ids) {
        return categoryJpaRepository.findByRapidCategoryIdIn(ids).stream().map(CategoryEntity::toModel).toList();
    }

    @Override
    public List<Category> findByRapidCategoryIdNotIn(List<String> ids) {
        return categoryJpaRepository.findByRapidCategoryIdNotIn(ids).stream().map(CategoryEntity::toModel).toList();
    }

    @Override
    public Optional<Category> findByRapidCategoryId(String rapidId) {
        return categoryJpaRepository.findByRapidCategoryId(rapidId).map(CategoryEntity::toModel);
    }

    @Override
    public List<Category> saveAll(List<Category> categories) {
        List<CategoryEntity> entities = categories.stream().map(CategoryEntity::fromModel).toList();
        return categoryJpaRepository.saveAll(entities).stream().map(CategoryEntity::toModel).toList();
    }

    @Override
    public Category save(Category category) {
        return categoryJpaRepository.save(CategoryEntity.fromModel(category)).toModel();
    }

    @Override
    public void flush() {
        categoryJpaRepository.flush();
    }
}
