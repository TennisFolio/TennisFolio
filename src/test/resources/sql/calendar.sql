INSERT INTO tb_category (CATEGORY_ID, rapid_category_id, category_name, category_slug) VALUES(1, '3', 'ATP', 'atp');

INSERT INTO tb_tournament(TOURNAMENT_ID, CATEGORY_ID, rapid_tournament_id, tournament_name) VALUES(1, 1, '2363', 'Australian Open');
INSERT INTO tb_tournament(TOURNAMENT_ID, CATEGORY_ID, rapid_tournament_id, tournament_name) VALUES(2, 1, '2389', 'Dubai');

INSERT INTO tb_season(SEASON_ID, TOURNAMENT_ID, season_name, season_year, start_timestamp, end_timestamp) VALUES(1, 1, 'Australian Open Men Singles 2025', '2025', '20251013090000', '20251019090000');
INSERT INTO tb_season(SEASON_ID, TOURNAMENT_ID, season_name, season_year, start_timestamp, end_timestamp) VALUES(2, 2, 'ATP Dubai, UAE Men Singles 2025', '2025', '20250930090000', '20251010090000');

INSERT INTO tb_round(ROUND_ID, SEASON_ID, ROUND, round_name, round_slug) VALUES(1, 1, 29, 'Final', 'final');
INSERT INTO tb_round(ROUND_ID, SEASON_ID, ROUND, round_name, round_slug) VALUES(2, 2, 29, 'Final', 'final');

INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR) VALUES(1, 'Alcaraz', '알카라즈');
INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR) VALUES(2, 'Sinner', '시너');
INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR) VALUES(3, 'Djokobic','조코비치');
INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR) VALUES(4, 'Shelton','쉘튼');

INSERT INTO tb_match(MATCH_ID, ROUND_ID, rapid_match_id, home_score, away_score, home_player, away_player, status, start_timestamp, winner)
VALUES(1, 1, '1', 2, 1, 1, 2, 'Ended', '20251020150000', '1'),
      (2, 2, '2', 0, 0, 3, 4, 'Not started', '20251016230000', '');

