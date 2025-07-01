-- ==========================================
-- 04_add_performance_indexes.sql
-- Phase 4: 고성능 특수 인덱스 추가
-- QueryDSL 최적화 및 CQRS 패턴 준비
-- ==========================================

USE expert;

-- 성능 측정 시작
SELECT 'Phase 4: 고성능 특수 인덱스 추가 시작' as status, NOW() as start_time;

-- ==========================================
-- 풀텍스트 인덱스 (Full-Text Search)
-- ==========================================

-- 제목 및 내용 전문검색용
ALTER TABLE todos ADD FULLTEXT INDEX ft_idx_title_contents (title, contents);
SELECT 'todos.ft_idx_title_contents (풀텍스트 인덱스) 생성 완료' as status;

-- 댓글 내용 전문검색용
ALTER TABLE comments ADD FULLTEXT INDEX ft_idx_contents (contents);
SELECT 'comments.ft_idx_contents (풀텍스트 인덱스) 생성 완료' as status;

-- ==========================================
-- 함수 기반 인덱스 (Expression Index)
-- ==========================================

-- 날짜별 그룹핑용 (년-월)
ALTER TABLE todos ADD INDEX idx_year_month_created
    ((YEAR(created_at)), (MONTH(created_at)));
SELECT 'todos.idx_year_month_created (함수 기반 인덱스) 생성 완료' as status;

-- 요일별 분석용
ALTER TABLE todos ADD INDEX idx_weekday_created
    ((DAYOFWEEK(created_at)));
SELECT 'todos.idx_weekday_created (함수 기반 인덱스) 생성 완료' as status;

-- ==========================================
-- 부분 인덱스 (Prefix Index)
-- ==========================================

-- 제목 앞 20자만 인덱싱 (메모리 절약)
ALTER TABLE todos ADD INDEX idx_title_prefix (title(20));
SELECT 'todos.idx_title_prefix (부분 인덱스) 생성 완료' as status;

-- 이메일 앞 30자 인덱싱
ALTER TABLE users ADD INDEX idx_email_prefix (email(30));
SELECT 'users.idx_email_prefix (부분 인덱스) 생성 완료' as status;

-- ==========================================
-- CQRS 패턴용 특수 인덱스
-- ==========================================

-- 조회 전용 데이터를 위한 인덱스
-- (나중에 이벤트 기반으로 만들 조회용 테이블의 프로토타입)

-- 할일별 통계 정보 조회용
ALTER TABLE todos ADD INDEX idx_cqrs_todo_stats
    (id, title, user_id, created_at, modified_at);
SELECT 'todos.idx_cqrs_todo_stats (CQRS용 인덱스) 생성 완료' as status;

-- 사용자별 활동 통계용
ALTER TABLE users ADD INDEX idx_cqrs_user_activity
    (id, user_name, user_role, created_at);
SELECT 'users.idx_cqrs_user_activity (CQRS용 인덱스) 생성 완료' as status;

-- ==========================================
-- 해시 인덱스 시뮬레이션 (Memory Engine)
-- ==========================================

-- 빠른 조회를 위한 임시 테이블 (인메모리)
CREATE TABLE temp_todo_hash (
                                todo_id BIGINT PRIMARY KEY,
                                title_hash VARCHAR(64),
                                manager_count INT DEFAULT 0,
                                comment_count INT DEFAULT 0,
                                last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                INDEX idx_title_hash (title_hash),
                                INDEX idx_manager_count (manager_count),
                                INDEX idx_comment_count (comment_count)
) ENGINE=MEMORY
  COMMENT='해시 기반 빠른 조회용 임시 테이블';

SELECT 'temp_todo_hash (해시 테이블) 생성 완료' as status;

-- ==========================================
-- 성능 모니터링용 인덱스
-- ==========================================

-- 슬로우 쿼리 감지용
ALTER TABLE manager_logs ADD INDEX idx_performance_monitor
    (created_at, action, status, todo_id);
SELECT 'manager_logs.idx_performance_monitor 생성 완료' as status;

-- 동시성 분석용
ALTER TABLE comments ADD INDEX idx_concurrency_analysis
    (created_at, todo_id, user_id);
SELECT 'comments.idx_concurrency_analysis 생성 완료' as status;

-- ==========================================
-- 클러스터 인덱스 최적화
-- ==========================================

-- 자주 함께 조회되는 데이터를 물리적으로 인접하게 배치
-- (MySQL InnoDB는 Primary Key가 클러스터 인덱스)

-- 할일 조회 최적화를 위한 물리적 정렬 힌트
-- (새로운 데이터 삽입시 created_at 순서로 물리적 배치 유도)
ALTER TABLE todos ADD INDEX idx_clustered_hint
    (created_at, id);
SELECT 'todos.idx_clustered_hint (클러스터 최적화) 생성 완료' as status;

-- ==========================================
-- 인덱스 힌트용 특수 인덱스
-- ==========================================

-- 강제 인덱스 사용을 위한 특수 목적 인덱스
ALTER TABLE todos ADD INDEX idx_force_hint_title_weather
    (title, weather, created_at, id);
SELECT 'todos.idx_force_hint_title_weather (힌트용) 생성 완료' as status;

-- ==========================================
-- 최종 인덱스 상태 확인
-- ==========================================

-- 모든 인덱스 목록
CALL ShowCurrentIndexes();

-- 상세 인덱스 통계
SELECT
    s.TABLE_NAME,
    s.INDEX_NAME,
    s.INDEX_TYPE,
    GROUP_CONCAT(s.COLUMN_NAME ORDER BY s.SEQ_IN_INDEX) as COLUMNS,
    CASE
        WHEN s.INDEX_NAME LIKE 'ft_%' THEN 'FULLTEXT'
        WHEN s.INDEX_NAME LIKE 'idx_year%' THEN 'FUNCTIONAL'
        WHEN s.INDEX_NAME LIKE '%_prefix' THEN 'PREFIX'
        WHEN s.INDEX_NAME LIKE 'idx_cqrs%' THEN 'CQRS'
        WHEN s.INDEX_NAME LIKE 'idx_covering%' THEN 'COVERING'
        ELSE 'STANDARD'
        END as INDEX_CATEGORY
FROM information_schema.STATISTICS s
WHERE s.TABLE_SCHEMA = 'expert'
GROUP BY s.TABLE_NAME, s.INDEX_NAME, s.INDEX_TYPE
ORDER BY s.TABLE_NAME, INDEX_CATEGORY, s.INDEX_NAME;

-- 최종 테이블 크기
SELECT
    TABLE_NAME,
    TABLE_ROWS as 'Estimated Rows',
    ROUND(DATA_LENGTH / 1024 / 1024, 2) as 'Data Size (MB)',
    ROUND(INDEX_LENGTH / 1024 / 1024, 2) as 'Index Size (MB)',
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) as 'Total Size (MB)',
    ROUND(INDEX_LENGTH / (DATA_LENGTH + INDEX_