package org.example.expert.domain.log.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.log.entity.ManagerLog;
import org.example.expert.domain.log.repository.ManagerLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerLogService {

    private final ManagerLogRepository managerLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveManagerRegisterLog(Long todoId, Long managerUserId, String status, String errorMessage, Long requestUserId) {
        try {
            ManagerLog managerLog = new ManagerLog(todoId, managerUserId, "REGISTER", status, errorMessage, requestUserId);
            managerLogRepository.save(managerLog);
            log.info("Manager register log saved: todoId={}, managerUserId={}, status={}", todoId, managerUserId, status);
        } catch (Exception e) {
            log.error("Failed to save manager register log: todoId={}, managerUserId={}", todoId, managerUserId, e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveManagerDeleteLog(Long todoId, Long managerId, String status, String errorMessage, Long requestUserId) {
        try {
            ManagerLog managerLog = new ManagerLog(todoId, managerId, "DELETE", status, errorMessage, requestUserId);
            managerLogRepository.save(managerLog);
            log.info("Manager delete log saved: todoId={}, managerId={}, status={}", todoId, managerId, status);
        } catch (Exception e) {
            log.error("Failed to save manager delete log: todoId={}, managerId={}", todoId, managerId, e);
        }
    }
}
