-- ==========================================
-- 03_add_composite_indexes.sql
-- Phase 3: 복합 인덱스 추가
-- 복잡한 WHERE 조건 최적화
-- ==========================================

USE expert;

-- 성능 측정 시작
SELECT 'Phase 3: 복합 인덱스 추가 시작' as status, NOW() as start_time;

-- ==========================================
-- Users 테이블 복합 인덱스
-- ==========================================

-- user_name + email 복합 검색용
ALTER TABLE users ADD INDEX idx_user_name_email (user_name, email);
SELECT 'users.idx_user_name_email 생성 완료' as status;

-- user_role + created_at (관리자별 가입일 조회)
ALTER TABLE users ADD INDEX idx_user_role_created (user_role, created_at);
SELECT 'users.idx_user_role_created 생성 완료' as status;

-- ==========================================
-- Todos 테이블 복합 인덱스 (QueryDSL 최적화)
-- ==========================================

-- title + created_at (제목 검색 + 날짜 정렬)
ALTER TABLE todos ADD INDEX idx_title_created_at (title, created_at);
SELECT 'todos.idx_title_created_at 생성 완료' as status;

-- weather + created_at (날씨 필터 + 날짜 정렬)
ALTER TABLE todos ADD INDEX idx_weather_created_at (weather, created_at);
SELECT 'todos.idx_weather_created_at 생성 완료' as status;

-- title + weather + created_at (복합 검색 조건)
ALTER TABLE todos ADD INDEX idx_title_weather_created (title, weather, created_at);
SELECT 'todos.idx_title_weather_created 생성 완료' as status;

-- user_id + created_at (사용자별 할일 + 날짜 정렬)
ALTER TABLE todos ADD INDEX idx_user_created_at (user_id, created_at);
SELECT 'todos.idx_user_created_at 생성 완료' as status;

-- user_id + title (사용자별 할일 제목 검색)
ALTER TABLE todos ADD INDEX idx_user_title (user_id, title);
SELECT 'todos.idx_user_title 생성 완료' as status;

-- ==========================================
-- Managers 테이블 복합 인덱스 (JOIN 최적화)
-- ==========================================

-- todo_id + user_id (QueryDSL JOIN FETCH 최적화)
ALTER TABLE managers ADD INDEX idx_todo_user_fetch (todo_id, user_id);
SELECT 'managers.idx_todo_user_fetch 생성 완료' as status;

-- user_id + todo_id (사용자별 담당 업무 조회)
ALTER TABLE managers ADD INDEX idx_user_todo_fetch (user_id, todo_id);
SELECT 'managers.idx_user_todo_fetch 생성 완료' as status;

-- ==========================================
-- Comments 테이블 복합 인덱스 (N+1 해결)
-- ==========================================

-- todo_id + created_at (댓글 목록 + 시간순 정렬)
ALTER TABLE comments ADD INDEX idx_todo_created_fetch (todo_id, created_at);
SELECT 'comments.idx_todo_created_fetch 생성 완료' as status;

-- todo_id + user_id (댓글 작성자별 조회)
ALTER TABLE comments ADD INDEX idx_todo_user_fetch (todo_id, user_id);
SELECT 'comments.idx_todo_user_fetch 생성 완료' as status;

-- user_id + created_at (사용자별 댓글 + 시간순)
ALTER TABLE comments ADD INDEX idx_user_created_at (user_id, created_at);
SELECT 'comments.idx_user_created_at 생성 완료' as status;

-- ==========================================
-- Manager Logs 테이블 복합 인덱스
-- ==========================================

-- todo_id + action + created_at (로그 분석용)
ALTER TABLE manager_logs ADD INDEX idx_todo_action_created (todo_id, action, created_at);
SELECT 'manager_logs.idx_todo_action_created 생성 완료' as status;

-- manager_user_id + status + created_at (사용자별 로그 상태)
ALTER TABLE manager_logs ADD INDEX idx_manager_status_created (manager_user_id, status, created_at);
SELECT 'manager_logs.idx_manager_status_created 생성 완료' as status;

-- action + status + created_at (액션별 상태 통계)
ALTER TABLE manager_logs ADD INDEX idx_action_status_created (action, status, created_at);
SELECT 'manager_logs.idx_action_status_created 생성 완료' as status;

-- ==========================================
-- 커버링 인덱스 (Covering Index) 추가
-- ==========================================

-- QueryDSL 검색 쿼리 최적화용 커버링 인덱스
-- SELECT t.id, t.title, COUNT(m.id), COUNT(c.id) 쿼리 최적화
ALTER TABLE todos ADD INDEX idx_covering_search (id, title, created_at, user_id);
SELECT 'todos.idx_covering_search (커버링 인덱스) 생성 완료' as status;

-- 매니저 수 계산용 커버링 인덱스
ALTER TABLE managers ADD INDEX idx_covering_count (todo_id, id);
SELECT 'managers.idx_covering_count (커버링 인덱스) 생성 완료' as status;

-- 댓글 수 계산용 커버링 인덱스
ALTER TABLE comments ADD INDEX idx_covering_count (todo_id, id);
SELECT 'comments.idx_covering_count (커버링 인덱스) 생성 완료' as status;

-- ==========================================
-- 인덱스 생성 완료 확인
-- ==========================================

-- 현재 인덱스 현황 출력
CALL ShowCurrentIndexes();

-- 테이블 크기 및 인덱스 크기 확인
SELECT
    TABLE_NAME,
    ROUND(DATA_LENGTH / 1024 / 1024, 2) as 'Data Size (MB)',
    ROUND(INDEX_LENGTH / 1024 / 1024, 2) as 'Index Size (MB)',
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) as 'Total Size (MB)',
    ROUND(INDEX_LENGTH / DATA_LENGTH * 100, 2) as 'Index Ratio (%)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'expert'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;

-- 인덱스 통계 정보
SELECT
    TABLE_NAME,
    COUNT(*) as INDEX_COUNT
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'expert'
GROUP BY TABLE_NAME
ORDER BY INDEX_COUNT DESC;

-- ⏰ 성능 측정 종료
SELECT 'Phase 3: 복합 인덱스 추가 완료!' as status, NOW() as end_time;

-- ==========================================
-- 다음 단계 안내
-- ==========================================
SELECT '다음 단계: 04_add_performance_indexes.sql 실행' as next_step;
SELECT '이제 QueryDSL 성능 테스트를 진행하세요!' as recommendation;