package com.tennisfolio.Tennisfolio.api.categories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CategoriesResponseParser implements ResponseParser<List<CategoryDTO>> {
    private final ObjectMapper objectMapper;

    public CategoriesResponseParser(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }
    @Override
    public List<CategoryDTO> parse(String response) {
        try{
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode categoriesNode = rootNode.path("categories");

            List<CategoryDTO> categories = new ArrayList<>();
            for(JsonNode dataNode : categoriesNode){
                CategoryDTO category = objectMapper.treeToValue(dataNode, CategoryDTO.class);
                categories.add(category);
            }
            return categories;
        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }

    }
}
