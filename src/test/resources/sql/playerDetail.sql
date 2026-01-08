INSERT INTO tb_category (CATEGORY_ID, rapid_category_id, category_name, category_slug) VALUES(1, '3', 'ATP', 'atp');

INSERT INTO tb_tournament(TOURNAMENT_ID, CATEGORY_ID, rapid_tournament_id, tournament_name) VALUES(1, 1, '2363', 'Australian Open');
INSERT INTO tb_tournament(TOURNAMENT_ID, CATEGORY_ID, rapid_tournament_id, tournament_name) VALUES(2, 1, '2389', 'Dubai');

INSERT INTO tb_season(SEASON_ID, TOURNAMENT_ID, season_name, season_year, start_timestamp, end_timestamp) VALUES(1, 1, 'Australian Open Men Singles 2025', '2025', '20251013090000', '20251019090000');
INSERT INTO tb_season(SEASON_ID, TOURNAMENT_ID, season_name, season_year, start_timestamp, end_timestamp) VALUES(2, 2, 'ATP Dubai, UAE Men Singles 2025', '2025', '20250930090000', '20251010090000');

INSERT INTO tb_round(ROUND_ID, SEASON_ID, ROUND, round_name, round_slug) VALUES(1, 1, 27, 'Quarterfinals', 'quarterfinals');
INSERT INTO tb_round(ROUND_ID, SEASON_ID, ROUND, round_name, round_slug) VALUES(2, 1, 28, 'Semifinals', 'semifinals');
INSERT INTO tb_round(ROUND_ID, SEASON_ID, ROUND, round_name, round_slug) VALUES(3, 1, 29, 'Final', 'final');

INSERT INTO tb_round(ROUND_ID, SEASON_ID, ROUND, round_name, round_slug) VALUES(4, 2, 27, 'Quarterfinals', 'quarterfinals');
INSERT INTO tb_round(ROUND_ID, SEASON_ID, ROUND, round_name, round_slug) VALUES(5, 2, 28, 'Semifinals', 'semifinals');
INSERT INTO tb_round(ROUND_ID, SEASON_ID, ROUND, round_name, round_slug) VALUES(6, 2, 29, 'Final', 'final');

INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR, rapid_player_id, birth, country_Code, turned_Pro, weight, plays, height, image) VALUES
(1, 'Alcaraz', '알카라즈', '275923', '20030505', 'ES', '2018', '74', 'right-handed', '1.83', 'player/275923');
INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR, rapid_player_id, birth, country_Code, turned_Pro, weight, plays, height, image) VALUES
(2, 'Sinner', '시너', '206570', '20010816', 'IT', '2018', '77', 'right-handed', '1.91', 'player/206570');
INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR) VALUES(3, 'Djokobic','조코비치');
INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR) VALUES(4, 'Shelton','쉘튼');
INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR) VALUES(5, 'Arnaldi', '아르날디');
INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR) VALUES(6, 'Thompson', '톰슨');
INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR) VALUES(7, 'Roman','로만');
INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR) VALUES(8, 'Bergs','베르그스');

INSERT INTO tb_player_prize(PRIZE_ID, PLAYER_ID, PRIZE_CURRENT_AMOUNT, prize_current_currency, PRIZE_TOTAL_AMOUNT, prize_total_currency) VALUES(1, 1, 100, 'EUR', 500, 'EUR');
INSERT INTO tb_player_prize(PRIZE_ID, PLAYER_ID, PRIZE_CURRENT_AMOUNT, prize_current_currency, PRIZE_TOTAL_AMOUNT, prize_total_currency) VALUES(2, 2, 200, 'EUR', 600, 'EUR');

INSERT INTO tb_ranking(RANKING_ID, PLAYER_ID, cur_ranking, pre_ranking, best_ranking, cur_points, ranking_last_updated, CATEGORY) VALUES(1, 1, 1, 1, 1, 9500, '20251119', 'ATP');
INSERT INTO tb_ranking(RANKING_ID, PLAYER_ID, cur_ranking, pre_ranking, best_ranking, cur_points, ranking_last_updated, CATEGORY) VALUES(2, 1, 1, 1, 1, 10000, '20251125', 'ATP');
INSERT INTO tb_ranking(RANKING_ID, PLAYER_ID, cur_ranking, pre_ranking, best_ranking, cur_points, ranking_last_updated, CATEGORY) VALUES(3, 2, 2, 2, 1, 8500, '20251119', 'ATP');
INSERT INTO tb_ranking(RANKING_ID, PLAYER_ID, cur_ranking, pre_ranking, best_ranking, cur_points, ranking_last_updated, CATEGORY) VALUES(4, 2, 2, 2, 1, 9000, '20251125', 'ATP');

INSERT INTO tb_match(MATCH_ID, ROUND_ID, rapid_match_id, home_player, away_player,
 home_set1, home_set2, home_set3, home_set4, home_set5,
 home_tie_set1, home_tie_set2, home_tie_set3, home_tie_set4, home_tie_set5,
 away_set1, away_set2, away_set3, away_set4, away_set5,
 away_tie_set1, away_tie_set2, away_tie_set3, away_tie_set4, away_tie_set5,
 status, start_timestamp, winner)
VALUES(1, 1, '1', 1, 2,
       6, 6, 0, 0, 0,
       0, 0, 0, 0, 0,
       3, 2, 0, 0, 0,
       0, 0, 0, 0, 0,
       'Ended', '20251020150000', '1'),
      (2, 1, '2', 1, 3,
       3, 4, 0, 0, 0,
       0, 0, 0, 0, 0,
       6, 6, 0, 0, 0,
       0, 0, 0, 0, 0,
       'Ended', '20251021230000', '2'),
      (3, 1, '3', 1, 4,
       7, 2, 6, 0, 0,
       7, 0, 0, 0, 0,
       6, 6, 3, 0, 0,
       4, 0, 0, 0, 0,
       'Ended', '20251022230000', '1'),
      (4, 2, '4', 5, 1,
       6, 5, 0, 0, 0,
       4, 0, 0, 0, 0,
       7, 7, 0, 0, 0,
       7, 0, 0, 0, 0,
       'Ended', '20251023230000', '2'),
      (5, 2, '5', 6, 1,
       7, 6, 0, 0, 0,
       0, 0, 0, 0, 0,
       5, 2, 0, 0, 0,
       0, 0, 0, 0, 0,
       'Ended', '20251024230000', '1'),
      (6, 3, '6', 7, 1,
       4, 6, 0, 0, 0,
       0, 4, 0, 0, 0,
       6, 7, 0, 0, 0,
       0, 7, 0, 0, 0,
       'Ended', '20251025230000', '2'),
      (7, 4, '7', 8, 1,
       0, 1, 0, 0, 0,
       0, 0, 0, 0, 0,
       6, 6, 0, 0, 0,
       0, 0, 0, 0, 0,
       'Ended', '20251029230000', '2'),
      (8, 4, '8', 2, 3,
       3, 6, 2, 0, 0,
       0, 0, 0, 0, 0,
       6, 1, 6, 0, 0,
       0, 0, 0, 0, 0,
       'Ended', '20251031230000', '2'),
      (9, 4, '9', 2, 4,
       6, 6, 0, 0, 0,
       0, 0, 0, 0, 0,
       4, 4, 0, 0, 0,
       0, 0, 0, 0, 0,
       'Ended', '20251101230000', '1'),
      (10, 5, '10', 1, 5,
        6, 7, 0, 0, 0,
        0, 7, 0, 0, 0,
        2, 6, 0, 0, 0,
        0, 4, 0, 0, 0,
        'Ended', '20251103230000', '1'),
      (11, 5, '11', 1, 6,
       6, 7, 3, 0, 0,
       3, 0, 0, 0, 0,
       7, 5, 2, 0, 0,
       7, 0, 0, 0, 0,
       'Ended', '20251105230000', '2'),
      (12, 6, '12', 4, 5,
       3, 3, 0, 0, 0,
       0, 0, 0, 0, 0,
       6, 6, 0, 0, 0,
       0, 0, 0, 0, 0,
       'Ended', '20251110230000', '2');

