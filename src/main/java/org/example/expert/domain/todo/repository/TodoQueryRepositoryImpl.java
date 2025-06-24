package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.request.TodoSearchRequest;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TodoQueryRepositoryImpl implements TodoQueryRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Todo> findByIdWithUserUsingQueryDSL(Long todoId) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;

        Todo result = jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(TodoSearchRequest req, Pageable pageable) {
        QTodo t = QTodo.todo;
        QUser u = QUser.user;
        QManager m = QManager.manager;
        QComment c = QComment.comment;

        var content = jpaQueryFactory
                .select(Projections.constructor(TodoSearchResponse.class,
                        t.id,
                        t.title,
                        m.id.count(),
                        c.id.count(),
                        t.createdAt))
                .from(t)
                .leftJoin(t.managers, m)
                .leftJoin(m.user, u)
                .leftJoin(t.comments, c)
                .where(
                        titleContains(req.getTitle(), t),
                        createdAtBetween(req.getStartDate(), req.getEndDate(), t),
                        managerUsernameContains(req.getManagerUsername(), u)
                )
                .groupBy(t.id, t.title, t.createdAt)
                .orderBy(t.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        var total = jpaQueryFactory
                .select(t.id.countDistinct())
                .from(t)
                .leftJoin(t.managers, m)
                .leftJoin(m.user, u)
                .where(
                        titleContains(req.getTitle(), t),
                        createdAtBetween(req.getStartDate(), req.getEndDate(), t),
                        managerUsernameContains(req.getManagerUsername(), u)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);

    }

    private BooleanExpression titleContains(String title, QTodo t) {
        return title != null ? t.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression createdAtBetween(LocalDateTime startDate, LocalDateTime endDate, QTodo t) {
        if (startDate != null && endDate != null) {
            return t.createdAt.between(startDate, endDate);
        } else if (startDate != null) {
            return t.createdAt.goe(startDate);
        } else if (endDate != null) {
            return t.createdAt.loe(endDate);
        }
        return null;
    }

    private BooleanExpression managerUsernameContains(String username, QUser u) {
        return username != null ? u.userName.containsIgnoreCase(username) : null;
    }
}
