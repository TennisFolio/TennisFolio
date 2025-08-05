package com.tennisfolio.Tennisfolio.infrastructure.repository;


import com.tennisfolio.Tennisfolio.category.repository.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    @Query("SELECT c FROM CategoryEntity c WHERE c.rapidCategoryId NOT IN :rapidIds")
    List<CategoryEntity> findByRapidCategoryIdNotIn(@Param("rapidIds") List<String> rapidIds);

    @Query("SELECT c FROM CategoryEntity c WHERE c.rapidCategoryId IN :rapidIds")
    List<CategoryEntity> findByRapidCategoryIdIn(@Param("rapidIds") List<String> rapidIds);

    Optional<CategoryEntity> findByRapidCategoryId(String rapidId);
}
