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

@SpringBootTest
@ActiveProfiles("test")
class UserBulkInsertTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Rollback(false)
    void createMillionUsersWithBulkInsert() {
        System.out.println("🚀 Bulk Insert로 100만건 유저 생성 시작!");

        long startTime = System.currentTimeMillis();

        // Bulk insert SQL - 100만건 유저 생성용
        final String INSERT_USER_SQL = "INSERT INTO users (user_name, email, password, user_role, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?)";

        int batchSize = 10000; // 1만개씩 bulk insert
        int totalUsers = 1000000; // 100만개
        int totalBatches = totalUsers / batchSize; // 100번 반복

        for (int batch = 0; batch < totalBatches; batch++) {
            final int currentBatch = batch;

            jdbcTemplate.batchUpdate(INSERT_USER_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int userNumber = currentBatch * batchSize + i;

                    // 유니크한 닉네임(userName) 생성 (중복 방지)
                    String uniqueUserName = "nickname" + userNumber + "_" +
                            (System.nanoTime() % 100000);

                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());

                    ps.setString(1, uniqueUserName);                   // user_name (닉네임)
                    ps.setString(2, "user" + userNumber + "@test.com"); // email
                    ps.setString(3, "encodedPassword123");              // password
                    ps.setString(4, "USER");                           // user_role
                    ps.setTimestamp(5, now);                           // created_at
                    ps.setTimestamp(6, now);                           // modified_at
                }

                @Override
                public int getBatchSize() {
                    return batchSize; // 1만개씩 처리
                }
            });

            // 진행상황 출력 (10배치마다)
            if ((batch + 1) % 10 == 0) {
                int completed = (batch + 1) * batchSize;
                double progress = (double) completed / totalUsers * 100;
                long elapsed = System.currentTimeMillis() - startTime;

                System.out.printf("진행률: %d/%d (%.1f%%) - 경과시간: %d초%n",
                        completed, totalUsers, progress, elapsed / 1000);
            }
        }

        long endTime = System.currentTimeMillis();
        long totalTime = (endTime - startTime) / 1000;

        System.out.println("🎉 Bulk Insert로 100만건 완료!");
        System.out.println("⏰ 총 소요시간: " + totalTime + "초");

        // 실제 생성된 개수 확인
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        System.out.println("📊 실제 생성된 유저 수: " + count + "명");
    }
}