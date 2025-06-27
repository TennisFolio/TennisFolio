package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class CategoryTournamentsEntityMapper implements EntityMapper<List<CategoryTournamentsDTO>, List<Tournament>> {

    private final EntityAssemble<List<CategoryTournamentsDTO>, List<Tournament>> categoryTournamentsAssemble;

    public CategoryTournamentsEntityMapper(@Qualifier("categoryTournamentsAssemble") EntityAssemble<List<CategoryTournamentsDTO>, List<Tournament>> categoryTournamentsAssemble) {
        this.categoryTournamentsAssemble = categoryTournamentsAssemble;
    }

    @Override
    public List<Tournament> map(List<CategoryTournamentsDTO> dto, Object... parmas) {
        return categoryTournamentsAssemble.assemble(dto);
    }
}
