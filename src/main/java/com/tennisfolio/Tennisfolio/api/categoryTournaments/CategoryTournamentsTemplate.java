package com.tennisfolio.Tennisfolio.api.categoryTournaments;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.api.base.*;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class CategoryTournamentsTemplate extends AbstractApiTemplate<List<CategoryTournamentsDTO>, List<Tournament>> {

    private final ResponseParser<List<CategoryTournamentsDTO>> categoryTournamentParser;
    private final Mapper<List<CategoryTournamentsDTO>, List<Tournament>> categoryTournamentMapper;
    private final SaveStrategy<List<Tournament>> categoryTournamentSaveStrategy;

    public CategoryTournamentsTemplate(DecompressorUtil decompressorUtil
            , @Qualifier("categoryTournamentsResponseParser") ResponseParser<List<CategoryTournamentsDTO>> categoryTournamentParser
            , @Qualifier("categoryTournamentsMapper") Mapper<List<CategoryTournamentsDTO>, List<Tournament>> categoryTournamentMapper
            , @Qualifier("categoryTournamentsSaveStrategy") SaveStrategy<List<Tournament>> categoryTournamentSaveStrategy) {
        super(decompressorUtil);
        this.categoryTournamentParser = categoryTournamentParser;
        this.categoryTournamentMapper = categoryTournamentMapper;
        this.categoryTournamentSaveStrategy = categoryTournamentSaveStrategy;
    }

    @Override
    public List<CategoryTournamentsDTO> toDTO(String response) {
        return categoryTournamentParser.parse(response);
    }

    @Override
    public List<Tournament> toEntity(List<CategoryTournamentsDTO> dto, Object... params) {
        return categoryTournamentMapper.map(dto, params);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.CATEGORYTOURNAMENTS.getParam(params);
    }

    @Override
    public List<Tournament> saveEntity(List<Tournament> entity) {
        return categoryTournamentSaveStrategy.save(entity);
    }
}
