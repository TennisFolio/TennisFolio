package com.tennisfolio.Tennisfolio.api.categories;

import com.tennisfolio.Tennisfolio.api.base.Mapper;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
public class CategoriesMapper implements Mapper<List<CategoryDTO>, List<Category>> {

    @Override
    public List<Category> map(List<CategoryDTO> dto, Object... params) {
        return dto.stream().map(Category::new)
                .collect(Collectors.toList());
    }
}
