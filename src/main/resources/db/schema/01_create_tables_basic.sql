-- ==========================================
-- 01_create_tables_basic.sql
-- Phase 1: 기본 테이블 생성 (최소 인덱스)
-- 성능 측정 기준점
-- ==========================================

USE expert;

-- 기존 테이블 삭제 (의존성 순서 고려)
DROP VIEW IF EXISTS v_data_summary;
DROP PROCEDURE IF EXISTS ShowCurrentIndexes;
DROP TABLE IF EXISTS manager_logs;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS managers;
DROP TABLE IF EXISTS todos;
DROP TABLE IF EXISTS users;

-- ==========================================
-- 1. Users 테이블 (100만건 예상)
-- ==========================================
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       user_name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,  -- JPA 요구사항으로 UNIQUE 필수
                       password VARCHAR(255) NOT NULL,
                       user_role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
                       created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                       modified_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='사용자 테이블 - Phase 1 (기본 상태)';

-- ==========================================
-- 2. Todos 테이블 (200만건 예상)
-- ==========================================
CREATE TABLE todos (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       contents TEXT,
                       weather VARCHAR(50),
                       user_id BIGINT NOT NULL,
                       created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                       modified_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    -- 필수 외래키 (성능에 영향주지만 데이터 무결성을 위해 필요)
                       CONSTRAINT fk_todos_user_id
                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='할일 테이블 - Phase 1 (기본 상태)';

-- ==========================================
-- 3. Managers 테이블 (500만건 예상)
-- ==========================================
CREATE TABLE managers (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          todo_id BIGINT NOT NULL,

    -- 필수 외래키
                          CONSTRAINT fk_managers_user_id
                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                          CONSTRAINT fk_managers_todo_id
                              FOREIGN KEY (todo_id) REFERENCES todos(id) ON DELETE CASCADE,

    -- 중복 방지 (같은 Todo에 같은 User가 여러번 Manager 되는 것 방지)
                          CONSTRAINT uk_manager_todo_user
                              UNIQUE KEY (todo_id, user_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='담당자 테이블 - Phase 1 (기본 상태)';

-- ==========================================
-- 4. Comments 테이블 (800만건 예상)
-- ==========================================
CREATE TABLE comments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          contents TEXT NOT NULL,
                          user_id BIGINT NOT NULL,
                          todo_id BIGINT NOT NULL,
                          created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                          modified_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    -- 필수 외래키
                          CONSTRAINT fk_comments_user_id
                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                          CONSTRAINT fk_comments_todo_id
                              FOREIGN KEY (todo_id) REFERENCES todos(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='댓글 테이블 - Phase 1 (기본 상태)';

-- ==========================================
-- 5. Manager Logs 테이블 (CQRS 패턴용)
-- ==========================================
CREATE TABLE manager_logs (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              todo_id BIGINT NOT NULL,
                              manager_user_id BIGINT NOT NULL,
                              action ENUM('REGISTER', 'DELETE') NOT NULL,
                              status ENUM('SUCCESS', 'FAILED') NOT NULL,
                              error_message TEXT,
                              request_user_id BIGINT NOT NULL,
                              created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='매니저 로그 테이블 - CQRS 패턴용';

-- ==========================================
-- 데이터 확인용 뷰
-- ==========================================
CREATE VIEW v_data_summary AS
SELECT
    'users' as table_name, COUNT(*) as record_count FROM users
UNION ALL
SELECT
    'todos' as table_name, COUNT(*) as record_count FROM todos
UNION ALL
SELECT
    'managers' as table_name, COUNT(*) as record_count FROM managers
UNION ALL
SELECT
    'comments' as table_name, COUNT(*) as record_count FROM comments
UNION ALL
SELECT
    'manager_logs' as table_name, COUNT(*) as record_count FROM manager_logs;

-- ==========================================
-- 현재 인덱스 현황 확인용 프로시저
-- ==========================================
DELIMITER //
CREATE PROCEDURE ShowCurrentIndexes()
BEGIN
    SELECT
        TABLE_NAME,
        INDEX_NAME,
        GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) as COLUMNS,
        -- NON_UNIQUE을 GROUP BY에 추가하거나 집계함수 사용
        CASE WHEN MAX(NON_UNIQUE) = 0 THEN 'UNIQUE' ELSE 'INDEX' END as INDEX_TYPE
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'expert'
    GROUP BY TABLE_NAME, INDEX_NAME
    ORDER BY TABLE_NAME, INDEX_NAME;
END //
DELIMITER ;

-- ==========================================
-- 실행 결과 확인
-- ==========================================
SHOW TABLES;
SELECT * FROM v_data_summary;
CALL ShowCurrentIndexes();

SELECT 'Phase 1: 기본 테이블 생성 완료!' as status;