package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments;


import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.category.repository.CategoryEntity;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryTournamentsAssemble implements EntityAssemble<List<CategoryTournamentsDTO>, List<Tournament>> {

    private final CategoryRepository categoryRepository;

    public CategoryTournamentsAssemble(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Tournament> assemble(List<CategoryTournamentsDTO> dto, Object... params) {
        Category findCategory = categoryRepository.findByRapidCategoryId(params[0].toString())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        return dto.stream()
                .map(tournament -> {
                    Category category = Category.builder()
                            .categoryId(findCategory.getCategoryId())
                            .rapidCategoryId(findCategory.getRapidCategoryId())
                            .categoryName(findCategory.getCategoryName())
                            .categorySlug(findCategory.getCategorySlug())
                            .build();
                    return Tournament.builder()
                            .category(category)
                            .rapidTournamentId(tournament.getTournamentRapidId())
                            .tournamentName(tournament.getTournamentName())
                            .build();

                })
                .collect(Collectors.toList());
    }
}
