package com.tennisfolio.Tennisfolio.api.leagueSeasons;

import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeagueSeasonsMapper implements Mapper<List<LeagueSeasonsDTO>, List<Season>>{
    private final EntityAssemble<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonsAssemble;

    public LeagueSeasonsMapper(EntityAssemble<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonsAssemble) {
        this.leagueSeasonsAssemble = leagueSeasonsAssemble;
    }

    @Override
    public List<Season> map(List<LeagueSeasonsDTO> dto, Object... params) {
        return leagueSeasonsAssemble.assemble(dto, params);
    }
}
