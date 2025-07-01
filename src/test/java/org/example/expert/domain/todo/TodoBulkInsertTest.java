package org.example.expert.domain.todo;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
@ActiveProfiles("test")
class TodoBulkInsertTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random();

    // 날씨 옵션들
    private final String[] WEATHERS = {"Sunny", "Cloudy", "Rainy", "Snowy", "Windy", "Foggy", "Hail"};

    // 제목 키워드들 (검색 테스트용)
    private final String[] TITLE_KEYWORDS = {
            "회의", "프로젝트", "개발", "테스트", "버그수정", "리뷰", "배포", "문서작성",
            "기획", "설계", "구현", "디버깅", "최적화", "리팩토링", "API", "데이터베이스"
    };

    @Test
    @Rollback(false)
    void createTestDataForQueryDSLPerformance() {
        System.out.println("QueryDSL 성능 테스트용 대용량 데이터 생성 시작!");

        long totalStartTime = System.currentTimeMillis();

        // 0. User가 없으면 먼저 생성
        Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        if (userCount == 0) {
            System.out.println("User 데이터가 없어서 먼저 생성합니다...");
            createUsers(1000000); // 100만명 생성
        } else {
            System.out.println("기존 User 수: " + String.format("%,d", userCount) + "명");
        }

        // 1. Todo 데이터 생성 (200만건 - 유저당 평균 20개)
        createTodos(2000000);

        // 2. Manager 데이터 생성 (Todo당 평균 1.2명 - 현실적)
        createManagers(2400000);

        // 3. Comment 데이터 생성 (Todo당 평균 2개 - 현실적)
        createComments(4000000);

        long totalEndTime = System.currentTimeMillis();
        System.out.println("전체 테스트 데이터 생성 완료!");
        System.out.println("총 소요시간: " + (totalEndTime - totalStartTime) / 1000 + "초");

        // 최종 데이터 확인
        printDataCounts();
    }

    private void createUsers(int totalUsers) {
        System.out.println("User 데이터 생성 중...");
        long startTime = System.currentTimeMillis();

        final String INSERT_USER_SQL =
                "INSERT INTO users (user_name, email, password, user_role, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?)";

        int batchSize = 10000;
        int totalBatches = totalUsers / batchSize;

        for (int batch = 0; batch < totalBatches; batch++) {
            final int currentBatch = batch;

            jdbcTemplate.batchUpdate(INSERT_USER_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int userNumber = currentBatch * batchSize + i;

                    String uniqueUserName = "nickname" + userNumber + "_" +
                            (System.nanoTime() % 100000);

                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());

                    ps.setString(1, uniqueUserName);
                    ps.setString(2, "user" + userNumber + "@test.com");
                    ps.setString(3, "encodedPassword123");
                    ps.setString(4, "USER");
                    ps.setTimestamp(5, now);
                    ps.setTimestamp(6, now);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            if ((batch + 1) % 5 == 0) {
                int completed = (batch + 1) * batchSize;
                double progress = (double) completed / totalUsers * 100;
                System.out.printf("User 진행률: %d/%d (%.1f%%)%n", completed, totalUsers, progress);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("User 생성 완료: " + (endTime - startTime) / 1000 + "초");
    }

    private void createTodos(int totalTodos) {
        System.out.println("Todo 데이터 생성 중...");
        long startTime = System.currentTimeMillis();

        final String INSERT_TODO_SQL =
                "INSERT INTO todos (title, contents, weather, user_id, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?)";

        // 실제 존재하는 User ID 리스트 가져오기
        List<Long> existingUserIds = jdbcTemplate.queryForList("SELECT id FROM users", Long.class);

        if (existingUserIds.isEmpty()) {
            System.out.println("User 데이터가 없습니다! 먼저 User 데이터를 생성해주세요.");
            return;
        }

        System.out.println("실제 User 수: " + existingUserIds.size());

        int batchSize = 10000; // 배치 크기 증가
        int totalBatches = totalTodos / batchSize;

        for (int batch = 0; batch < totalBatches; batch++) {
            final int currentBatch = batch;

            jdbcTemplate.batchUpdate(INSERT_TODO_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int todoNumber = currentBatch * batchSize + i;

                    // 다양한 제목 생성 (검색 테스트용)
                    String keyword = TITLE_KEYWORDS[random.nextInt(TITLE_KEYWORDS.length)];
                    String title = keyword + "_" + todoNumber + "_" + generateRandomString(5);

                    // 다양한 생성일 (최근 2년간)
                    LocalDateTime createdAt = LocalDateTime.now()
                            .minus(random.nextInt(730), ChronoUnit.DAYS)  // 0~730일 전
                            .minus(random.nextInt(24), ChronoUnit.HOURS)   // 0~23시간 전
                            .minus(random.nextInt(60), ChronoUnit.MINUTES); // 0~59분 전

                    // 실제 존재하는 User ID 중에서 랜덤 선택
                    Long randomUserId = existingUserIds.get(random.nextInt(existingUserIds.size()));

                    ps.setString(1, title);
                    ps.setString(2, "내용_" + todoNumber + "_" + generateRandomString(10));
                    ps.setString(3, WEATHERS[random.nextInt(WEATHERS.length)]);
                    ps.setLong(4, randomUserId);
                    ps.setTimestamp(5, Timestamp.valueOf(createdAt));
                    ps.setTimestamp(6, Timestamp.valueOf(createdAt.plusMinutes(random.nextInt(60))));
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            if ((batch + 1) % 10 == 0) {
                int completed = (batch + 1) * batchSize;
                double progress = (double) completed / totalTodos * 100;
                System.out.printf("Todo 진행률: %d/%d (%.1f%%)%n", completed, totalTodos, progress);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Todo 생성 완료: " + (endTime - startTime) / 1000 + "초");
    }

    private void createManagers(int totalManagers) {
        System.out.println("👥 Manager 데이터 생성 중...");
        long startTime = System.currentTimeMillis();

        final String INSERT_MANAGER_SQL =
                "INSERT INTO managers (user_id, todo_id) VALUES (?, ?)";

        // 생성된 Todo ID 범위 확인
        Long maxTodoId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM todos", Long.class);
        Long maxUserId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM users", Long.class);

        int batchSize = 10000; // 배치 크기 증가
        int totalBatches = totalManagers / batchSize;

        for (int batch = 0; batch < totalBatches; batch++) {
            final int currentBatch = batch;

            jdbcTemplate.batchUpdate(INSERT_MANAGER_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    // 랜덤 Todo와 User 매칭 (중복 허용 - 한 Todo에 여러 Manager 가능)
                    long todoId = random.nextInt(maxTodoId.intValue()) + 1;
                    long userId = random.nextInt(maxUserId.intValue()) + 1;

                    ps.setLong(1, userId);
                    ps.setLong(2, todoId);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            if ((batch + 1) % 10 == 0) {
                int completed = (batch + 1) * batchSize;
                double progress = (double) completed / totalManagers * 100;
                System.out.printf("Manager 진행률: %d/%d (%.1f%%)%n", completed, totalManagers, progress);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Manager 생성 완료: " + (endTime - startTime) / 1000 + "초");
    }

    private void createComments(int totalComments) {
        System.out.println("Comment 데이터 생성 중...");
        long startTime = System.currentTimeMillis();

        final String INSERT_COMMENT_SQL =
                "INSERT INTO comments (contents, user_id, todo_id, created_at, modified_at) VALUES (?, ?, ?, ?, ?)";

        Long maxTodoId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM todos", Long.class);
        Long maxUserId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM users", Long.class);

        int batchSize = 10000; // 배치 크기 증가
        int totalBatches = totalComments / batchSize;

        for (int batch = 0; batch < totalBatches; batch++) {
            final int currentBatch = batch;

            jdbcTemplate.batchUpdate(INSERT_COMMENT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int commentNumber = currentBatch * batchSize + i;

                    long todoId = random.nextInt(maxTodoId.intValue()) + 1;
                    long userId = random.nextInt(maxUserId.intValue()) + 1;

                    LocalDateTime createdAt = LocalDateTime.now()
                            .minus(random.nextInt(365), ChronoUnit.DAYS)
                            .minus(random.nextInt(24), ChronoUnit.HOURS);

                    ps.setString(1, "댓글내용_" + commentNumber + "_" + generateRandomString(20));
                    ps.setLong(2, userId);
                    ps.setLong(3, todoId);
                    ps.setTimestamp(4, Timestamp.valueOf(createdAt));
                    ps.setTimestamp(5, Timestamp.valueOf(createdAt.plusMinutes(random.nextInt(30))));
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            if ((batch + 1) % 10 == 0) {
                int completed = (batch + 1) * batchSize;
                double progress = (double) completed / totalComments * 100;
                System.out.printf("Comment 진행률: %d/%d (%.1f%%)%n", completed, totalComments, progress);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Comment 생성 완료: " + (endTime - startTime) / 1000 + "초");
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void printDataCounts() {
        System.out.println("\n=== 최종 데이터 현황 ===");

        Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        Long todoCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM todos", Long.class);
        Long managerCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM managers", Long.class);
        Long commentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM comments", Long.class);

        System.out.println("Users: " + String.format("%,d", userCount) + "건");
        System.out.println("Todos: " + String.format("%,d", todoCount) + "건");
        System.out.println("Managers: " + String.format("%,d", managerCount) + "건");
        System.out.println("Comments: " + String.format("%,d", commentCount) + "건");

        // 평균 통계
        if (todoCount > 0) {
            double avgManagersPerTodo = (double) managerCount / todoCount;
            double avgCommentsPerTodo = (double) commentCount / todoCount;

            System.out.println("\n=== 평균 통계 ===");
            System.out.printf("Todo당 평균 Manager 수: %.2f명%n", avgManagersPerTodo);
            System.out.printf("Todo당 평균 Comment 수: %.2f개%n", avgCommentsPerTodo);
        }

        // 검색 테스트용 샘플 데이터 확인
        System.out.println("\n=== 검색 테스트용 샘플 확인 ===");
        List<String> sampleTitles = jdbcTemplate.queryForList(
                "SELECT title FROM todos WHERE title LIKE '%회의%' LIMIT 5", String.class);
        System.out.println("'회의' 포함 제목 샘플: " + sampleTitles);

        List<String> sampleUsernames = jdbcTemplate.queryForList(
                "SELECT DISTINCT u.user_name FROM users u " +
                        "JOIN managers m ON u.id = m.user_id LIMIT 5", String.class);
        System.out.println("Manager로 등록된 유저명 샘플: " + sampleUsernames);
    }

    @Test
    @Rollback(false)
    void createSpecificTestData() {
        System.out.println("특정 검색 테스트용 데이터 생성...");

        // 특정 패턴의 데이터로 성능 테스트하기 쉽게 생성
        jdbcTemplate.update(
                "INSERT INTO todos (title, contents, weather, user_id, created_at, modified_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                "성능테스트_특정제목_검색용",
                "성능 테스트용 특정 내용",
                "Sunny",
                1L,
                Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(LocalDateTime.now())
        );

        System.out.println("특정 검색 테스트용 데이터 생성 완료");
    }
}