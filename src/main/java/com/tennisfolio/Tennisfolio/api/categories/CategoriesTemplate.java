package com.tennisfolio.Tennisfolio.api.categories;

import com.tennisfolio.Tennisfolio.api.base.*;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoriesTemplate extends AbstractApiTemplate<List<CategoryDTO>, List<Category>> {
    private final @Qualifier("categoriesMapper")Mapper<List<CategoryDTO>, List<Category>> categoriesMapper;
    private final @Qualifier("categoriesResponseParser")ResponseParser<List<CategoryDTO>> categoriesResponseParser;
    private final @Qualifier("categoriesSaveStrategy")SaveStrategy<List<Category>> categoriesSaveStrategy;


    public CategoriesTemplate(DecompressorUtil decompressorUtil
            , Mapper<List<CategoryDTO>, List<Category>> categoriesMapper
            , ResponseParser<List<CategoryDTO>> categoriesResponseParser, SaveStrategy<List<Category>> categoriesSaveStrategy) {
        super(decompressorUtil);
        this.categoriesMapper = categoriesMapper;
        this.categoriesResponseParser = categoriesResponseParser;
        this.categoriesSaveStrategy = categoriesSaveStrategy;
    }

    @Override
    public List<CategoryDTO> toDTO(String response) {
        return categoriesResponseParser.parse(response);
    }

    @Override
    public List<Category> toEntity(List<CategoryDTO> dto) {
        return categoriesMapper.map(dto);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.CATEGORIES.getParam(params);
    }

    @Override
    public List<Category> saveEntity(List<Category> entity) {
        return categoriesSaveStrategy.save(entity);
    }
}
