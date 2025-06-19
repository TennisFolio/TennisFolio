package com.tennisfolio.Tennisfolio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.api.categories.CategoriesResponseParser;
import com.tennisfolio.Tennisfolio.api.categories.CategoriesTemplate;
import com.tennisfolio.Tennisfolio.api.categories.CategoryDTO;
import com.tennisfolio.Tennisfolio.api.teamdetails.CountryDTO;
import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsResponseParser;
import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsTemplate;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ResponseParserTest {

}
