-- ==========================================
-- 단순 쿼리 성능 테스트 (JOIN 없음)
-- 인덱스 효과를 정확히 측정하기 위한 테스트
-- ==========================================

USE expert;

-- 성능 측정 설정
SET SESSION wait_timeout = 600;
SET SESSION interactive_timeout = 600;
SET SESSION net_read_timeout = 600;
SET SESSION net_write_timeout = 600;

-- 프로파일링 활성화
SET profiling = 1;
SET profiling_history_size = 100;

SELECT '게시판 시스템에서 특정 조건을 만족하는 게시글에 대한 요약 정보를 조회' as test_name;

EXPLAIN
SELECT
    t.id,
    t.title,
    COUNT(DISTINCT m.id) AS manager_count,
    COUNT(DISTINCT c.id) AS comment_count,
    t.created_at,
    t.weather
FROM todos t
         LEFT JOIN managers m ON t.id = m.todo_id
         LEFT JOIN comments c ON t.id = c.todo_id
    WHERE t.weather = 'SUNNY' OR t.title = '배포_8_iuPtr'
GROUP BY t.id, t.title, t.created_at
ORDER BY t.created_at DESC
LIMIT 20;