package org.example.expert.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

@SpringBootTest
@ActiveProfiles("test")
public class FewUserInsertTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random();

    @Test
    @Rollback(false)
    void createBulkTodosManagersComments() {
        System.out.println("Bulk Insert로 todos, managers, comments 대량 생성 시작!");

        long startTime = System.currentTimeMillis();

        // 1. todos 20개 생성
        final String INSERT_TODO_SQL = "INSERT INTO todos (title, contents, weather, user_id, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?)";
        int todoCount = 20;

        // 임의 유저 id 범위 (예: 1 ~ 1,000,000)
        int maxUserId = 1_000_000;

        for (int i = 0; i < todoCount; i++) {
            String title = "할일 " + (i + 1);
            String contents = "테스트 내용입니다.";
            String weather = "Sunny";
            int userId = random.nextInt(maxUserId) + 1;
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            jdbcTemplate.update(INSERT_TODO_SQL, title, contents, weather, userId, now, now);
        }

        System.out.println("Todos 20개 생성 완료.");

        // 2. 최근 생성된 todos 20개 아이디 가져오기
        String fetchTodoIdsSql = "SELECT id FROM todos ORDER BY created_at DESC LIMIT 20";
        var todoIds = jdbcTemplate.queryForList(fetchTodoIdsSql, Long.class);

        // 3. managers 20명씩 할당
        final String INSERT_MANAGER_SQL = "INSERT INTO managers (todo_id, user_id) VALUES (?, ?)";
        int managersPerTodo = 20;

        for (Long todoId : todoIds) {
            for (int i = 0; i < managersPerTodo; i++) {
                int userId = random.nextInt(maxUserId) + 1;
                jdbcTemplate.update(INSERT_MANAGER_SQL, todoId, userId);
            }
        }

        System.out.println("Managers 생성 완료.");

        // 4. comments 1000개씩 할당
        final String INSERT_COMMENT_SQL = "INSERT INTO comments (contents, user_id, todo_id, created_at, modified_at) VALUES (?, ?, ?, ?, ?)";
        int commentsPerTodo = 1000;

        for (Long todoId : todoIds) {
            jdbcTemplate.batchUpdate(INSERT_COMMENT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    String contents = "댓글 " + (i + 1);
                    int userId = random.nextInt(maxUserId) + 1;
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());

                    ps.setString(1, contents);
                    ps.setInt(2, userId);
                    ps.setLong(3, todoId);
                    ps.setTimestamp(4, now);
                    ps.setTimestamp(5, now);
                }

                @Override
                public int getBatchSize() {
                    return commentsPerTodo;
                }
            });
        }

        System.out.println("Comments 생성 완료.");

        long endTime = System.currentTimeMillis();
        System.out.println("총 소요시간: " + (endTime - startTime) / 1000 + "초");
    }
}