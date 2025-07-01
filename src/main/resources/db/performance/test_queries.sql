-- ==========================================
-- test_queries.sql
-- QueryDSL 성능 테스트용 쿼리 모음
-- Phase별 성능 비교 측정용
-- ==========================================

USE expert;

-- ==========================================
-- 성능 측정 도구 설정
-- ==========================================

-- 프로파일링 활성화
SET profiling = 1;
SET profiling_history_size = 100;

-- 쿼리 캐시 비활성화 (정확한 성능 측정)
SET SESSION query_cache_type = OFF;

-- ==========================================
-- 🎯 핵심 QueryDSL 테스트 쿼리들
-- ==========================================

-- 📊 Query 1: 기본 복합 검색 (제목 + 날짜 + 담당자)
-- 이 쿼리가 QueryDSL searchTodos()의 핵심 로직
SELECT '🔍 Query 1: 기본 복합 검색 시작' as test_name;

EXPLAIN FORMAT=JSON
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
WHERE t.title LIKE '%프로젝트%'
  AND t.created_at BETWEEN '2024-01-01' AND '2025-12-31'
  AND (u.user_name LIKE '%nickname%' OR u.user_name IS NULL)
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
WHERE t.title LIKE '%프로젝트%'
  AND t.created_at BETWEEN '2024-01-01' AND '2025-12-31'
  AND (u.user_name LIKE '%nickname%' OR u.user_name IS NULL)
GROUP BY t.id, t.title, t.created_at
ORDER BY t.created_at DESC
    LIMIT 20;

-- ==========================================
-- 📊 Query 2: 제목만 검색 (단순 조건)
-- ==========================================

SELECT '🔍 Query 2: 제목만 검색 시작' as test_name;

EXPLAIN
SELECT
    t.id,
    t.title,
    COUNT(DISTINCT m.id) as manager_count,
    COUNT(DISTINCT c.id) as comment_count,
    t.created_at
FROM todos t
         LEFT JOIN managers m ON t.id = m.todo_id
         LEFT JOIN comments c ON t.id = c.todo_id
WHERE t.title LIKE '%개발%'
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
         LEFT JOIN comments c ON t.id = c.todo_id
WHERE t.title LIKE '%개발%'
GROUP BY t.id, t.title, t.created_at
ORDER BY t.created_at DESC
    LIMIT 20;

-- ==========================================
-- 📊 Query 3: 날짜 범위 검색
-- ==========================================

SELECT '🔍 Query 3: 날짜 범위 검색 시작' as test_name;

EXPLAIN
SELECT
    t.id,
    t.title,
    COUNT(DISTINCT m.id) as manager_count,
    COUNT(DISTINCT c.id) as comment_count,
    t.created_at
FROM todos t
         LEFT JOIN managers m ON t.id = m.todo_id
         LEFT JOIN comments c ON t.id = c.todo_id
WHERE t.created_at >= '2024-06-01'
  AND t.created_at < '2024-07-01'
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
         LEFT JOIN comments c ON t.id = c.todo_id
WHERE t.created_at >= '2024-06-01'
  AND t.created_at < '2024-07-01'
GROUP BY t.id, t.title, t.created_at
ORDER BY t.created_at DESC
    LIMIT 20;

-- ==========================================
-- 📊 Query 4: 담당자 닉네임 검색
-- ==========================================

SELECT '🔍 Query 4: 담당자 닉네임 검색 시작' as test_name;

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

-- ==========================================
-- 📊 Query 5: N+1 문제 시뮬레이션
-- ==========================================

SELECT '🔍 Query 5: N+1 문제 시뮬레이션 시작' as test_name;

-- 먼저 Todo 목록 조회
SELECT id, title FROM todos WHERE title LIKE '%테스트%' LIMIT 10;

-- 각 Todo별로 Manager 조회 (N+1 문제 발생)
SELECT m.*, u.user_name
FROM managers m
         JOIN users u ON m.user_id = u.id
WHERE m.todo_id = 1;

-- 각 Todo별로 Comment 조회 (N+1 문제 발생)
SELECT c.*, u.user_name
FROM comments c
         JOIN users u ON c.user_id = u.id
WHERE c.todo_id = 1;

-- ==========================================
-- 📊 Query 6: JOIN FETCH 최적화 버전
-- ==========================================

SELECT '🔍 Query 6: JOIN FETCH 최적화 버전 시작' as test_name;

EXPLAIN
SELECT
    t.id,
    t.title,
    u_mgr.user_name as manager_name,
    u_cmt.user_name as commenter_name
FROM todos t
         LEFT JOIN managers m ON t.id = m.todo_id
         LEFT JOIN users u_mgr ON m.user_id = u_mgr.id
         LEFT JOIN comments c ON t.id = c.todo_id
         LEFT JOIN users u_cmt ON c.user_id = u_cmt.id
WHERE t.title LIKE '%테스트%'
ORDER BY t.created_at DESC
    LIMIT 50;

-- ==========================================
-- 📊 Query 7: 집계 함수 성능 테스트
-- ==========================================

SELECT '🔍 Query 7: 집계 함수 성능 테스트 시작' as test_name;

-- COUNT 성능 테스트
EXPLAIN
SELECT
    t.user_id,
    COUNT(t.id) as todo_count,
    COUNT(DISTINCT m.user_id) as unique_managers,
    COUNT(c.id) as comment_count,
    AVG(DATEDIFF(NOW(), t.created_at)) as avg_days_old
FROM todos t
         LEFT JOIN managers m ON t.id = m.todo_id
         LEFT JOIN comments c ON t.id = c.todo_id
GROUP BY t.user_id
HAVING todo_count > 5
ORDER BY todo_count DESC
    LIMIT 20;

-- ==========================================
-- 📊 Query 8: 풀텍스트 검색 테스트
-- ==========================================

SELECT '🔍 Query 8: 풀텍스트 검색 테스트 시작' as test_name;

-- 기존 LIKE 검색
EXPLAIN
SELECT id, title, contents
FROM todos
WHERE title LIKE '%프로젝트%' OR contents LIKE '%프로젝트%'
    LIMIT 20;

-- 풀텍스트 검색 (Phase 4에서 인덱스 생성한 경우)
EXPLAIN
SELECT id, title, contents,
    MATCH(title, contents) AGAINST('프로젝트' IN NATURAL LANGUAGE MODE) as relevance
FROM todos
WHERE MATCH(title, contents) AGAINST('프로젝트' IN NATURAL LANGUAGE MODE)
ORDER BY relevance DESC
    LIMIT 20;

-- ==========================================
-- 📊 Query 9: 페이징 성능 테스트
-- ==========================================

SELECT '🔍 Query 9: 페이징 성능 테스트 시작' as test_name;

-- 첫 번째 페이지 (빠름)
EXPLAIN
SELECT t.id, t.title, t.created_at
FROM todos t
ORDER BY t.created_at DESC
    LIMIT 20 OFFSET 0;

-- 중간 페이지 (느려질 수 있음)
EXPLAIN
SELECT t.id, t.title, t.created_at
FROM todos t
ORDER BY t.created_at DESC
    LIMIT 20 OFFSET 50000;

-- 마지막 페이지 (매우 느림)
EXPLAIN
SELECT t.id, t.title, t.created_at
FROM todos t
ORDER BY t.created_at DESC
    LIMIT 20 OFFSET 1000000;

-- ==========================================
-- 📊 Query 10: 서브쿼리 vs JOIN 성능 비교
-- ==========================================

SELECT '🔍 Query 10: 서브쿼리 vs JOIN 성능 비교 시작' as test_name;

-- 서브쿼리 버전
EXPLAIN
SELECT t.id, t.title
FROM todos t
WHERE t.id IN (
    SELECT DISTINCT m.todo_id
    FROM managers m
             JOIN users u ON m.user_id = u.id
    WHERE u.user_name LIKE '%nickname%'
)
    LIMIT 20;

-- JOIN 버전
EXPLAIN
SELECT DISTINCT t.id, t.title
FROM todos t
         JOIN managers m ON t.id = m.todo_id
         JOIN users u ON m.user_id = u.id
WHERE u.user_name LIKE '%nickname%'
    LIMIT 20;

-- ==========================================
-- 성능 측정 결과 확인
-- ==========================================

-- 프로파일링 결과 확인
SELECT '📊 성능 측정 결과' as section;
SHOW PROFILES;

-- 상위 10개 느린 쿼리
SELECT
    QUERY_ID,
    DURATION,
    STATE
FROM INFORMATION_SCHEMA.PROFILING
ORDER BY DURATION DESC
    LIMIT 10;

-- ==========================================
-- 인덱스 사용 통계 확인
-- ==========================================

-- 인덱스 사용률 확인
SELECT
    TABLE_SCHEMA,
    TABLE_NAME,
    INDEX_NAME,
    CARDINALITY
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'expert'
  AND CARDINALITY > 0
ORDER BY CARDINALITY DESC;

-- ==========================================
-- 성능 개선 제안
-- ==========================================

SELECT '
성능 개선 분석 포인트:

1️⃣ EXPLAIN 결과에서 확인할 것:
   - type: ALL (전체 스캔) → index/range로 개선 필요
   - rows: 검토 행 수가 적을수록 좋음
   - Extra: Using filesort, Using temporary 주의

2️⃣ 인덱스 최적화:
   - WHERE 조건의 컬럼들이 인덱스에 포함되어 있는지
   - ORDER BY 조건이 인덱스로 처리되는지
   - JOIN 조건이 인덱스를 사용하는지

3️⃣ 쿼리 최적화:
   - LIMIT 사용으로 결과 셋 크기 제한
   - 불필요한 LEFT JOIN 제거
   - SELECT 절에서 필요한 컬럼만 조회

4️⃣ CQRS 패턴 고려사항:
   - 복잡한 집계 쿼리는 이벤트 기반 조회 테이블로 분리
   - 실시간성이 중요하지 않은 통계는 배치 처리
   - 자주 변경되지 않는 참조 데이터는 캐싱

' as performance_guide;