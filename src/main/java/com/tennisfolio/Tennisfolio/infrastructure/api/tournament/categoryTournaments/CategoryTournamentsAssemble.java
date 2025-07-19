package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments;


import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.category.domain.model.Category;
import com.tennisfolio.Tennisfolio.infrastructure.repository.CategoryJpaRepository;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CategoryTournamentsAssemble implements EntityAssemble<List<CategoryTournamentsDTO>, List<Tournament>> {

    private final CategoryJpaRepository categoryJpaRepository;

    public CategoryTournamentsAssemble(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public List<Tournament> assemble(List<CategoryTournamentsDTO> dto, Object... params) {
        return dto.stream()
                .map(tournament -> {
                    Optional<Category> findCategory = categoryJpaRepository.findByRapidCategoryId(tournament.getCategory().getRapidId());
                    return findCategory.map(category -> new Tournament(tournament, category));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
