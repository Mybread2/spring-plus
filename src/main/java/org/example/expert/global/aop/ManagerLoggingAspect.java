package org.example.expert.global.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.log.service.ManagerLogService;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ManagerLoggingAspect {

    private final ManagerLogService managerLogService;

    @AfterReturning("execution(* org.example.expert.domain.manager.service.ManagerService.saveManager(..))")
    public void logManagerSaveSuccess(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        AuthUser authUser = (AuthUser) args[0];
        Long todoId = (Long) args[1];
        ManagerSaveRequest request = (ManagerSaveRequest) args[2];

        managerLogService.saveManagerRegisterLog(
                todoId,
                request.getManagerUserId(),
                "SUCCESS",
                null,
                authUser.getId()
        );
    }

    // 매니저 등록 실패 로그
    @AfterThrowing(value = "execution(* org.example.expert.domain.manager.service.ManagerService.saveManager(..))", throwing = "ex")
    public void logManagerSaveFailure(JoinPoint joinPoint, Exception ex) {
        Object[] args = joinPoint.getArgs();
        AuthUser authUser = (AuthUser) args[0];
        Long todoId = (Long) args[1];
        ManagerSaveRequest request = (ManagerSaveRequest) args[2];

        managerLogService.saveManagerRegisterLog(
                todoId,
                request.getManagerUserId(),
                "FAILED",
                ex.getMessage(),
                authUser.getId()
        );
    }

    // 매니저 삭제 성공 로그
    @AfterReturning("execution(* org.example.expert.domain.manager.service.ManagerService.deleteManager(..))")
    public void logManagerDeleteSuccess(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        AuthUser authUser = (AuthUser) args[0];
        Long todoId = (Long) args[1];
        Long managerId = (Long) args[2];

        managerLogService.saveManagerDeleteLog(
                todoId,
                managerId,
                "SUCCESS",
                null,
                authUser.getId()
        );
    }

    // 매니저 삭제 실패 로그
    @AfterThrowing(value = "execution(* org.example.expert.domain.manager.service.ManagerService.deleteManager(..))", throwing = "ex")
    public void logManagerDeleteFailure(JoinPoint joinPoint, Exception ex) {
        Object[] args = joinPoint.getArgs();
        AuthUser authUser = (AuthUser) args[0];
        Long todoId = (Long) args[1];
        Long managerId = (Long) args[2];

        managerLogService.saveManagerDeleteLog(
                todoId,
                managerId,
                "FAILED",
                ex.getMessage(),
                authUser.getId()
        );
    }

}
