package com.tennisfolio.Tennisfolio.api.categoryTournaments;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class CategoryTournamentsMapper implements Mapper<List<CategoryTournamentsDTO>, List<Tournament>> {

    private final EntityAssemble<List<CategoryTournamentsDTO>, List<Tournament>> categoryTournamentsAssemble;

    public CategoryTournamentsMapper(@Qualifier("categoryTournamentsAssemble") EntityAssemble<List<CategoryTournamentsDTO>, List<Tournament>> categoryTournamentsAssemble) {
        this.categoryTournamentsAssemble = categoryTournamentsAssemble;
    }

    @Override
    public List<Tournament> map(List<CategoryTournamentsDTO> dto) {
        return categoryTournamentsAssemble.assemble(dto);
    }
}
