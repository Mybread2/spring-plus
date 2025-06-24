package org.example.expert.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = jwtUtil.substringToken(bearerToken);

            try {
                Claims claims = jwtUtil.extractClaims(token);
                if (claims != null) {
                    Long userId = Long.parseLong(claims.getSubject());
                    String email = claims.get("email", String.class);
                    String username = claims.get("username", String.class);
                    UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

                    AuthUser authUser = new AuthUser(userId, username,email, userRole);

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userRole.name());
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            authUser, null, List.of(authority));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (SecurityException | MalformedJwtException e) {
                log.error("Invalid JWT signature", e);
            } catch (ExpiredJwtException e) {
                log.error("Expired JWT token", e);
            } catch (UnsupportedJwtException e) {
                log.error("Unsupported JWT token", e);
            } catch (Exception e) {
                log.error("JWT processing error", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
