INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR, rapid_player_id, birth, country_Code, country_name, turned_Pro, weight, plays, height, image) VALUES
(1, 'Alcaraz', '알카라즈', '275923', '20030505', 'ES', 'Spain', '2018', '74', 'right-handed', '1.83', 'player/275923');
INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR, rapid_player_id, birth, country_Code, country_name, turned_Pro, weight, plays, height, image) VALUES
(2, 'Sinner', '시너', '206570', '20010816', 'IT', 'Italy', '2018', '77', 'right-handed', '1.91', 'player/206570');

INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR, rapid_player_id, country_code, country_name) VALUES(3, 'Djokobic','조코비치', '14882', 'RS', 'Serbia');
INSERT INTO tb_player(PLAYER_ID, PLAYER_NAME, PLAYER_NAME_KR, rapid_player_id, country_code, country_name) VALUES(4, 'Shelton','쉘튼 알', '385485', 'US', 'USA');
INSERT INTO `tb_player` (`PLAYER_ID`, `rapid_player_id`, `player_name`, `player_name_kr`, `birth`, `country_name`, `country_code`, `turned_pro`, `weight`, `plays`, `height`, `image`)
VALUES (5, '261015', 'Lorenzo Musetti', '로렌조 무세티', '20020303', 'Italy', 'IT', '2019', '78', 'right-handed', '1.85', 'player/261015');


INSERT INTO tb_ranking(RANKING_ID, PLAYER_ID, cur_ranking, pre_ranking, best_ranking, cur_points, pre_points, ranking_last_updated) VALUES
(1L, 1L, 1L, 1L, 1L, 12050L, 12050L, '20251201');
INSERT INTO tb_ranking(RANKING_ID, PLAYER_ID, cur_ranking, pre_ranking, best_ranking, cur_points, pre_points, ranking_last_updated) VALUES
(2L, 2L, 2L, 2L, 1L, 11500L, 11500L, '20251201');
INSERT INTO tb_ranking(RANKING_ID, PLAYER_ID, cur_ranking, pre_ranking, best_ranking, cur_points, pre_points, ranking_last_updated) VALUES
(3L, 3L, 4L, 4L, 1L, 4830L, 4830L, '20251201');
INSERT INTO tb_ranking(RANKING_ID, PLAYER_ID, cur_ranking, pre_ranking, best_ranking, cur_points, pre_points, ranking_last_updated) VALUES
(4L, 4L, 9L, 9L, 5L, 3970L, 3970L, '20251201');

INSERT INTO tb_ranking(RANKING_ID, PLAYER_ID, cur_ranking, pre_ranking, best_ranking, cur_points, pre_points, ranking_last_updated) VALUES
(5L, 1L, 1L, 2L, 1L, 11050L, 11250L, '20251110');

INSERT INTO `tb_ranking` (`RANKING_ID`, `PLAYER_ID`, `cur_ranking`, `pre_ranking`, `best_ranking`, `cur_points`, `pre_points`, `ranking_last_updated`) VALUES
(6L, 5L, 8, 8, 6, 4040, 4040, '20251201');
