package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeagueSeasonsEntityMapper implements EntityMapper<List<LeagueSeasonsDTO>, List<Season>> {
    private final EntityAssemble<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonsAssemble;

    public LeagueSeasonsEntityMapper(EntityAssemble<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonsAssemble) {
        this.leagueSeasonsAssemble = leagueSeasonsAssemble;
    }

    @Override
    public List<Season> map(List<LeagueSeasonsDTO> dto, Object... params) {
        return leagueSeasonsAssemble.assemble(dto, params);
    }
}
