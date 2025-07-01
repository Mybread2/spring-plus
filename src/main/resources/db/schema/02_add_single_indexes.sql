-- ==========================================
-- 02_add_single_indexes.sql
-- Phase 2: 단일 컬럼 인덱스 추가
-- 기본적인 검색 성능 향상
-- ==========================================

USE expert;

-- 성능 측정 시작
SELECT 'Phase 2: 단일 인덱스 추가 시작' as status, NOW() as start_time;

-- ==========================================
-- Users 테이블 인덱스
-- ==========================================

-- user_name 검색용 (QueryDSL에서 담당자 닉네임 검색)
ALTER TABLE users ADD INDEX idx_user_name (user_name);
SELECT 'users.idx_user_name 생성 완료' as status;

-- created_at 정렬용
ALTER TABLE users ADD INDEX idx_created_at (created_at);
SELECT 'users.idx_created_at 생성 완료' as status;

-- user_role 필터링용
ALTER TABLE users ADD INDEX idx_user_role (user_role);
SELECT 'users.idx_user_role 생성 완료' as status;

-- ==========================================
-- Todos 테이블 인덱스
-- ==========================================

-- title 검색용 (QueryDSL에서 제목 부분 검색)
ALTER TABLE todos ADD INDEX idx_title (title);
SELECT 'todos.idx_title 생성 완료' as status;

-- weather 필터링용 (QueryDSL에서 날씨 조건)
ALTER TABLE todos ADD INDEX idx_weather (weather);
SELECT 'todos.idx_weather 생성 완료' as status;

-- created_at 기간 검색 및 정렬용
ALTER TABLE todos ADD INDEX idx_created_at (created_at DESC);
SELECT 'todos.idx_created_at 생성 완료' as status;

-- modified_at 정렬용
ALTER TABLE todos ADD INDEX idx_modified_at (modified_at);
SELECT 'todos.idx_modified_at 생성 완료' as status;

-- ==========================================
-- Managers 테이블 인덱스
-- ==========================================

-- todo_id로 담당자 조회용 (N+1 문제 해결)
ALTER TABLE managers ADD INDEX idx_todo_id (todo_id);
SELECT 'managers.idx_todo_id 생성 완료' as status;

-- user_id로 담당 업무 조회용
ALTER TABLE managers ADD INDEX idx_user_id (user_id);
SELECT 'managers.idx_user_id 생성 완료' as status;

-- ==========================================
-- Comments 테이블 인덱스
-- ==========================================

-- todo_id로 댓글 조회용 (N+1 문제 해결)
ALTER TABLE comments ADD INDEX idx_todo_id (todo_id);
SELECT 'comments.idx_todo_id 생성 완료' as status;

-- user_id로 사용자별 댓글 조회용
ALTER TABLE comments ADD INDEX idx_user_id (user_id);
SELECT 'comments.idx_user_id 생성 완료' as status;

-- created_at 정렬용
ALTER TABLE comments ADD INDEX idx_created_at (created_at);
SELECT 'comments.idx_created_at 생성 완료' as status;