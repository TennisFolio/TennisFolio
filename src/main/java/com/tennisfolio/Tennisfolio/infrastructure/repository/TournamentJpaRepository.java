package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.category.repository.CategoryEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface TournamentJpaRepository extends JpaRepository<TournamentEntity, Long> {

    @Query("SELECT t FROM TournamentEntity t JOIN FETCH t.categoryEntity c ")
    List<TournamentEntity> findAll();

    @EntityGraph(attributePaths = {"mostTitlePlayer", "titleHolder"})
    List<TournamentEntity> findAllBy();

    @Query("SELECT t FROM TournamentEntity t WHERE t.rapidTournamentId IN :rapidIds")
    List<TournamentEntity> findByRapidTournamentIds(@Param("rapidIds") List<String> rapidIds);

    Optional<TournamentEntity> findByRapidTournamentId(@Param("rapidId") String rapidTournamentId);

    @Query("""
            SELECT t FROM TournamentEntity t
            JOIN FETCH t.categoryEntity
            WHERE t.rapidTournamentId = :rapidId""")
    Optional<TournamentEntity> findWithCategoryByRapidTournamentId(@Param("rapidId") String rapidTournamentId);

    @Query("SELECT t.rapidTournamentId FROM TournamentEntity t")
    Set<String> findAllRapidTournamentIds();

    @Query("SELECT t FROM TournamentEntity t WHERE t.categoryEntity IN :categoryEntities")
    List<TournamentEntity> findByCategoryIn(@Param("categoryEntities")List<CategoryEntity> categoryEntities);
}
