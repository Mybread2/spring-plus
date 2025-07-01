USE expert;
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
ALTER TABLE todos ADD INDEX idx_title_created_at (title, created_at DESC);
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
ALTER TABLE managers ADD INDEX idx_todo_manager_fetch (todo_id, user_id);
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