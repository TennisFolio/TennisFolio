package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments;


import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
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
        CategoryEntity findCategoryEntity = categoryRepository.findByRapidCategoryId(params[0].toString())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        return dto.stream()
                .map(tournament -> {
                    return new Tournament(tournament, findCategoryEntity.toModel());
                })
                .collect(Collectors.toList());
    }
}
