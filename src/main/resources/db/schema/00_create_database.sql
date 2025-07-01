-- ==========================================
-- 00_create_database.sql
-- 데이터베이스 생성 및 초기 설정
-- ==========================================

-- 1. 데이터베이스 생성
DROP DATABASE IF EXISTS expert;
CREATE DATABASE expert
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 2. 데이터베이스 선택
USE expert;

-- 3. 실행 확인
SELECT 'Database expert created successfully!' as status;
SHOW DATABASES LIKE 'expert';