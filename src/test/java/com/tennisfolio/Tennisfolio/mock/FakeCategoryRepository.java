package com.tennisfolio.Tennisfolio.mock;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeCategoryRepository implements CategoryRepository {
    private final Map<Long, Category> data = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public List<Category> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public List<Category> findByRapidCategoryIdIn(List<String> ids) {
        return data.values().stream().filter(category ->
            ids.contains(category.getRapidCategoryId())
        ).collect(Collectors.toList());
    }

    @Override
    public List<Category> findByRapidCategoryIdNotIn(List<String> ids) {
        return data.values().stream().filter(category ->
                !ids.contains(category.getRapidCategoryId())
        ).collect(Collectors.toList());
    }

    @Override
    public Optional<Category> findByRapidCategoryId(String rapidId) {
        return data.values().stream().filter(category -> category.getRapidCategoryId().equals(rapidId)).findFirst();
    }

    @Override
    public List<Category> saveAll(List<Category> categories) {
        return categories.stream().map(this::save).toList();
    }

    @Override
    public Category save(Category category) {
        Category saved = category;
        if(category.getCategoryId() == null || category.getCategoryId() == 0L){
           saved = Category.builder()
                   .categoryId(seq.getAndIncrement())
                   .rapidCategoryId(category.getRapidCategoryId())
                   .categorySlug(category.getCategorySlug())
                   .categoryName(category.getCategoryName())
                   .build();
        }
        data.put(saved.getCategoryId(), saved);
        return saved;
    }

    @Override
    public void flush() {

    }
}
