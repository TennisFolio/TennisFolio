package com.tennisfolio.Tennisfolio.category.controller;

import com.tennisfolio.Tennisfolio.category.service.CategoryService;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("")
    public ResponseEntity<ResponseDTO> saveCategory(){
        categoryService.saveCategory();
        return new ResponseEntity(ResponseDTO.success(), HttpStatus.OK);
    }
}
