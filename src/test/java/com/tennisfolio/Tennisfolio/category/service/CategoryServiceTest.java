package com.tennisfolio.Tennisfolio.category.service;

import com.tennisfolio.Tennisfolio.category.application.CategoryService;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakeCategoryRepository;
import com.tennisfolio.Tennisfolio.mock.categories.FakeCategoriesApiTemplate;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

public class CategoryServiceTest {

    // api
    ResponseParser<List<CategoryDTO>> fakeResponseParser = resp -> List.of();

    EntityMapper<List<CategoryDTO>, List<Category>> fakeEntityMapper = (categoryList, params) -> {
        Category category4 = Category.builder()
            .rapidCategoryId("213")
            .categoryName("ITF Women")
            .categorySlug("itf-women")
            .build();

        Category category5 = Category.builder()
                .rapidCategoryId("785")
                .categoryName("ITF Men")
                .categorySlug("itf-men")
                .build();

        Category category6 = Category.builder()
                .rapidCategoryId("871")
                .categoryName("WTA 125")
                .categorySlug("wta-125")
                .build();

        List<Category> categories = new ArrayList<>();
        categories.add(category4);
        categories.add(category5);
        categories.add(category6);

        return categories;
    };

    ApiCaller fakeApiCaller = new FakeApiCaller();

    StrategyApiTemplate<List<CategoryDTO>, List<Category>> fakeStrategyApiTemplate
            = new FakeCategoriesApiTemplate(fakeApiCaller, fakeResponseParser, fakeEntityMapper, null, RapidApi.CATEGORIES);

    CategoryRepository categoryRepository = new FakeCategoryRepository();

    CategoryService categoryService = new CategoryService(fakeStrategyApiTemplate, categoryRepository);


    @Test
    public void 카테고리_여러개_저장(){

        Category category1 = Category.builder()
                .rapidCategoryId("3")
                .categoryName("ATP")
                .categorySlug("atp")
                .build();

        Category category2 = Category.builder()
                .rapidCategoryId("6")
                .categoryName("WTA")
                .categorySlug("wta")
                .build();

        Category category3 = Category.builder()
                .rapidCategoryId("76")
                .categoryName("Davis Cup")
                .categorySlug("davis-cup")
                .build();

        categoryRepository.save(category1);
        categoryRepository.save(category2);
        categoryRepository.save(category3);

        categoryService.saveCategory();

        List<Category> allCategories = categoryRepository.findAll();

        assertThat(allCategories)
                .hasSize(6)
                .extracting(Category::getRapidCategoryId, Category::getCategoryName, Category::getCategorySlug)
                .containsExactlyInAnyOrder(
                        tuple("3", "ATP", "atp"),
                        tuple("6", "WTA", "wta"),
                        tuple("76", "Davis Cup", "davis-cup"),
                        tuple("213", "ITF Women", "itf-women"),
                        tuple("785", "ITF Men", "itf-men"),
                        tuple("871", "WTA 125", "wta-125")
                );
    }

    @Test
    public void 동일한_id_저장시_저장X(){
        Category category4 = Category.builder()
                .rapidCategoryId("213")
                .categoryName("ITF Women")
                .categorySlug("itf-women")
                .build();

        Category category5 = Category.builder()
                .rapidCategoryId("785")
                .categoryName("ITF Men")
                .categorySlug("itf-men")
                .build();

        Category category6 = Category.builder()
                .rapidCategoryId("871")
                .categoryName("WTA 125")
                .categorySlug("wta-125")
                .build();

        List<Category> categories = new ArrayList<>();
        categories.add(category4);
        categories.add(category5);
        categories.add(category6);

        categoryService.saveCategory();

        List<Category> allCategories = categoryRepository.findAll();

        assertThat(allCategories)
                .hasSize(3)
                .extracting(Category::getRapidCategoryId, Category::getCategoryName, Category::getCategorySlug)
                .containsExactlyInAnyOrder(
                        tuple("213", "ITF Women", "itf-women"),
                        tuple("785", "ITF Men", "itf-men"),
                        tuple("871", "WTA 125", "wta-125")
                );


    }

    @Test
    public void 존재하는_id_찾기(){
        Category category1 = Category.builder()
                .rapidCategoryId("3")
                .categoryName("ATP")
                .categorySlug("atp")
                .build();

        Category category2 = Category.builder()
                .rapidCategoryId("6")
                .categoryName("WTA")
                .categorySlug("wta")
                .build();

        Category category3 = Category.builder()
                .rapidCategoryId("76")
                .categoryName("Davis Cup")
                .categorySlug("davis-cup")
                .build();


        categoryRepository.save(category1);
        categoryRepository.save(category2);

        List<String> rapidIdList = new ArrayList<String>(Arrays.asList("3", "6", "76"));

        List<Category> categories = categoryService.getByRapidCategoryId(rapidIdList);

        assertThat(categories)
                .hasSize(2)
                .extracting(Category::getRapidCategoryId, Category::getCategoryName, Category::getCategorySlug)
                .containsExactlyInAnyOrder(
                        tuple("3", "ATP", "atp"),
                        tuple("6", "WTA", "wta")
                );

    }

    @Test
    public void 존재하지_않는_id_찾기(){
        Category category1 = Category.builder()
                .rapidCategoryId("3")
                .categoryName("ATP")
                .categorySlug("atp")
                .build();

        Category category2 = Category.builder()
                .rapidCategoryId("6")
                .categoryName("WTA")
                .categorySlug("wta")
                .build();

        Category category3 = Category.builder()
                .rapidCategoryId("76")
                .categoryName("Davis Cup")
                .categorySlug("davis-cup")
                .build();

        categoryRepository.save(category1);
        categoryRepository.save(category2);
        categoryRepository.save(category3);

        List<String> rapidIdList = new ArrayList<String>(Arrays.asList("3", "6"));

        List<Category> categories = categoryService.getByRapidCategoryIdNotIn(rapidIdList);

        assertThat(categories)
                .hasSize(1)
                .extracting(Category::getRapidCategoryId, Category::getCategoryName, Category::getCategorySlug)
                .containsExactlyInAnyOrder(
                        tuple("76", "Davis Cup", "davis-cup")
                );

    }


}
