package com.tennisfolio.Tennisfolio.mock.eventschedules;

import com.tennisfolio.Tennisfolio.fixtures.EventSchedulesFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules.EventSchedulesDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;

import java.util.List;

public class FakeEventSchedulesMapper implements EntityMapper<List<EventSchedulesDTO>, List<Match>> {
    @Override
    public List<Match> map(List<EventSchedulesDTO> dto, Object... params) {
        List<Match> list = List.of(EventSchedulesFixtures.wimbledonMen2025QuarterFinalsMatch1(), EventSchedulesFixtures.wimbledonMen2025QuarterFinalsMatch2()
                , EventSchedulesFixtures.wimbledonMen2025QuarterFinalsMatch3(), EventSchedulesFixtures.wimbledonMen2025QuarterFinalsMatch4());
        String year = params[2].toString();
        String month = params[1].toString();
        String day = params[0].toString();
        return list.stream().filter(p -> p.getStartTimestamp().contains(year+month+day)).toList();
    }
}
