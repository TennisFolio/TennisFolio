package com.tennisfolio.Tennisfolio.tournament.service;

import com.tennisfolio.Tennisfolio.Tournament.application.TournamentQueryService;
import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.mock.FakeCategoryRepository;
import com.tennisfolio.Tennisfolio.mock.FakePlayerRepository;
import com.tennisfolio.Tennisfolio.mock.FakeTournamentRepository;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class TournamentQueryServiceTest {

    private TournamentRepository tournamentRepository = new FakeTournamentRepository();
    private CategoryRepository categoryRepository = new FakeCategoryRepository();
    private PlayerRepository playerRepository = new FakePlayerRepository();
    private TournamentQueryService tournamentQueryService = new TournamentQueryService(tournamentRepository);

    @BeforeEach
    public void init(){
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

        Player nadal = Player.builder()
                .playerName("Rafael Nadal")
                .playerNameKr("라파엘 나달")
                .rapidPlayerId("14486")
                .build();

        Player alcaraz = Player.builder()
                .playerName("Alcaraz")
                .playerNameKr("알카라즈")
                .rapidPlayerId("275923")
                .build();

        playerRepository.save(nadal);
        playerRepository.save(alcaraz);

        Category atp = categoryRepository.save(category1);
        Category wta = categoryRepository.save(category2);

        Tournament rorlandGarros = Tournament.builder()
                .rapidTournamentId("2480")
                .category(atp)
                .tournamentName("Roland Garros")
                .city("Paris")
                .groundType("Clay")
                .mostTitles("14")
                .mostTitlePlayer(nadal)
                .titleHolder(alcaraz)
                .build();

        Tournament wtaRorlandGarros = Tournament.builder()
                .rapidTournamentId("2577")
                .category(wta)
                .tournamentName("Roland Garros")
                .city("Paris")
                .groundType("Clay")
                .build();

        Tournament wimbledon = Tournament.builder()
                .rapidTournamentId("2361")
                .category(atp)
                .tournamentName("Wimbledon")
                .city("London")
                .groundType("Grass")
                .mostTitles("8")
                .mostTitlePlayer(alcaraz)
                .titleHolder(nadal)
                .build();



        Tournament wtaWimbledon = Tournament.builder()
                .rapidTournamentId("2600")
                .category(wta)
                .tournamentName("Wimbledon")
                .city("London")
                .groundType("Grass")
                .build();

        tournamentRepository.save(rorlandGarros);
        tournamentRepository.save(wtaRorlandGarros);
        tournamentRepository.save(wimbledon);
        tournamentRepository.save(wtaWimbledon);

    }

    @Test
    public void 카테고리를_통한_토너먼트_조회(){

        List<Category> categoryList = categoryRepository.findAll();

        List<Tournament> result = tournamentQueryService.getByCategory(categoryList);

        // then
        assertThat(result).hasSize(4);
        assertThat(result)
                .extracting(Tournament::getTournamentName, t -> t.getCategory().getCategoryName())
                .containsExactlyInAnyOrder(
                        tuple("Roland Garros", "ATP"),
                        tuple("Roland Garros", "WTA"),
                        tuple("Wimbledon", "ATP"),
                        tuple("Wimbledon", "WTA")
                );
    }

    @Test
    void ATP만_넣으면_ATP_2개만_조회() {
        // given
        Category atp = categoryRepository.findAll().stream()
                .filter(c -> "ATP".equals(c.getCategoryName()))
                .findFirst().orElseThrow();

        // when
        List<Tournament> result = tournamentQueryService.getByCategory(List.of(atp));

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Tournament::getTournamentName)
                .containsExactlyInAnyOrder("Roland Garros", "Wimbledon");
    }

    @Test
    void 빈_목록이면_빈_결과() {
        // when
        List<Tournament> result = tournamentQueryService.getByCategory(Collections.emptyList());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 플레이어_매핑_확인() {
        // given
        List<Category> categories = categoryRepository.findAll();

        // when
        List<Tournament> result = tournamentQueryService.getByCategory(categories);

        // then
        // Wimbledon(ATP)에는 mostTitlePlayer = titleHolder(알카라즈), titleHolder = 라파엘 나달 로 세팅됨
        Tournament wimbledonAtp = result.stream()
                .filter(t -> "2361".equals(t.getRapidTournamentId()))
                .filter(t -> "ATP".equals(t.getCategory().getCategoryName()))
                .findFirst().orElseThrow();

        assertThat(wimbledonAtp.getMostTitles()).isEqualTo("8");
        assertThat(wimbledonAtp.getMostTitlePlayer().getPlayerName()).isEqualTo("Alcaraz");
        assertThat(wimbledonAtp.getMostTitlePlayer().getRapidPlayerId()).isEqualTo("275923");
        assertThat(wimbledonAtp.getTitleHolder().getPlayerName()).isEqualTo("Rafael Nadal");
        assertThat(wimbledonAtp.getTitleHolder().getRapidPlayerId()).isEqualTo("14486");

        // Roland Garros(ATP)에는 mostTitlePlayer=라파엘 나달(14회), titleHolder=알카라즈
        Tournament rgAtp = result.stream()
                .filter(t -> "Roland Garros".equals(t.getTournamentName()))
                .filter(t -> "ATP".equals(t.getCategory().getCategoryName()))
                .findFirst().orElseThrow();

        assertThat(rgAtp.getMostTitles()).isEqualTo("14");
        assertThat(rgAtp.getMostTitlePlayer().getPlayerName()).isEqualTo("Rafael Nadal");
        assertThat(rgAtp.getTitleHolder().getPlayerName()).isEqualTo("Alcaraz");
    }

}
