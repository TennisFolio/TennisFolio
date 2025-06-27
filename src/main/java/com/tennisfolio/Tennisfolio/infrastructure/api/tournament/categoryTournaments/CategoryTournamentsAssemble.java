package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments;


import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.category.domain.model.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CategoryTournamentsAssemble implements EntityAssemble<List<CategoryTournamentsDTO>, List<Tournament>> {

    private final CategoryRepository categoryRepository;

    public CategoryTournamentsAssemble(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Tournament> assemble(List<CategoryTournamentsDTO> dto, Object... params) {
        return dto.stream()
                .map(tournament -> {
                    Optional<Category> findCategory = categoryRepository.findByRapidCategoryId(tournament.getCategory().getRapidId());
                    return findCategory.map(category -> new Tournament(tournament, category));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
