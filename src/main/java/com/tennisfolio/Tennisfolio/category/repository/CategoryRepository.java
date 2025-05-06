package com.tennisfolio.Tennisfolio.category.repository;


import com.tennisfolio.Tennisfolio.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE c.rapidCategoryId IN :ids")
    List<Category> findByRapidCategoryIds(@Param("ids") List<String> ids);

    Optional<Category> findByRapidCategoryId(String rapidId);
}
