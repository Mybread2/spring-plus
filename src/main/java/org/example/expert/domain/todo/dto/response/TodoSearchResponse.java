package org.example.expert.domain.todo.dto.response;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class TodoSearchResponse {

    private final Long id;
    private final String title;
    private final Long managerCount;
    private final Long commentCount;
    private final LocalDateTime createdAt;

    public TodoSearchResponse(Long id, String title, Long managerCount, Long commentCount, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.managerCount = managerCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
    }
}
