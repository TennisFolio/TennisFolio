-- 기존 tb_club_account가 이미 생성된 환경에서만 실행한다.
-- 실행 전 백업 후 적용한다. 새 설치에는 2026-09-30-club-account-schema.sql만 사용한다.

ALTER TABLE tb_club_account DROP COLUMN DISPLAY_NAME;
