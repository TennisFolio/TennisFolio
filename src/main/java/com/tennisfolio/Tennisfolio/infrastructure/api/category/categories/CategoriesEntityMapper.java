package com.tennisfolio.Tennisfolio.infrastructure.api.category.categories;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.category.domain.model.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
public class CategoriesEntityMapper implements EntityMapper<List<CategoryDTO>, List<Category>> {

    @Override
    public List<Category> map(List<CategoryDTO> dto, Object... params) {
        return dto.stream().map(Category::new)
                .collect(Collectors.toList());
    }
}
