-- ==========================================
-- test_queries.sql
-- QueryDSL 성능 테스트용 쿼리 모음
-- Phase별 성능 비교 측정용
-- ==========================================

USE expert;

SET SESSION wait_timeout = 600;
SET SESSION interactive_timeout = 600;
SET SESSION net_read_timeout = 600;
SET SESSION net_write_timeout = 600;

-- ==========================================
-- 성능 측정 도구 설정
-- ==========================================

-- 프로파일링 활성화
SET profiling = 1;
SET profiling_history_size = 100;

SELECT '🔍 Query 1: 담당자 닉네임 검색 시작' as test_name;

EXPLAIN
SELECT
    t.id,
    t.title,
    COUNT(DISTINCT m.id) as manager_count,
    COUNT(DISTINCT c.id) as comment_count,
    t.created_at
FROM todos t
         LEFT JOIN managers m ON t.id = m.todo_id
         LEFT JOIN users u ON m.user_id = u.id
         LEFT JOIN comments c ON t.id = c.todo_id
WHERE u.user_name LIKE '%nickname1%'
GROUP BY t.id, t.title, t.created_at
ORDER BY t.created_at DESC
    LIMIT 20;

-- 실제 실행
SELECT
    t.id,
    t.title,
    COUNT(DISTINCT m.id) as manager_count,
    COUNT(DISTINCT c.id) as comment_count,
    t.created_at
FROM todos t
         LEFT JOIN managers m ON t.id = m.todo_id
         LEFT JOIN users u ON m.user_id = u.id
         LEFT JOIN comments c ON t.id = c.todo_id
WHERE u.user_name LIKE '%nickname1%'
GROUP BY t.id, t.title, t.created_at
ORDER BY t.created_at DESC
    LIMIT 20;