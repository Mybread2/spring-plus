package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "manager_logs")
public class ManagerLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long todoId;

    @Column(nullable = false)
    private Long managerUserId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String status;

    private String errorMessage;

    @Column(nullable = false)
    private Long requestUserId;

    public ManagerLog(Long todoId, Long managerUserId, String action, String status, String errorMessage, Long requestUserId) {
        this.todoId = todoId;
        this.managerUserId = managerUserId;
        this.action = action;
        this.status = status;
        this.errorMessage = errorMessage;
        this.requestUserId = requestUserId;
    }
}
