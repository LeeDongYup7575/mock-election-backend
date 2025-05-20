package com.example.mockvoting.security;

import com.example.mockvoting.domain.user.service.UserService;
import com.example.mockvoting.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // OPTIONS 요청은 바로 통과
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            log.debug("OPTIONS 요청 통과: {}", request.getRequestURI());
            response.setStatus(HttpStatus.OK.value());
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
            return; // 필터 체인을 계속 진행하지 않고 즉시 응답
        }

        // 인증이 필요 없는 경로는 바로 통과 (예: 구글 로그인만 제외)
        String path = request.getServletPath();
        String method = request.getMethod();
        if (path.startsWith("/api/users/oauth2/") ||
                path.equals("/api/youtube/videos") ||
                path.startsWith("/api/glossary/search") ||
                path.equals("/api/community/main") ||
                path.startsWith("/api/community/categories") ||
                (path.startsWith("/api/community/posts") && method.equals("GET") && !path.matches(".*/edit$")) ||
//                (path.startsWith("/wss")) ||
                (path.startsWith("/api/election/list")) ||
                (path.startsWith("/api/candidate/list") && method.equals("GET")) ||
                (path.startsWith("/api/candidate/detail") && method.equals("GET")) ||
                (path.startsWith("/api/news/newsdata"))
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 추출
            Optional<String> tokenOpt = jwtUtil.resolveToken(request);

            // 토큰이 있는 경우 처리
            if (tokenOpt.isPresent()) {
                String token = tokenOpt.get();
                log.debug("토큰 발견: {}", token);

                // 토큰 유효성 검증
                if (jwtUtil.validateToken(token)) {
                    String userId = jwtUtil.getUserIdFromToken(token);
                    log.debug("토큰에서 사용자 ID 추출: {}", userId);

                    // 사용자가 활성 상태인지 확인
                    if (!userService.isActiveUser(userId)) {
                        log.warn("비활성화된 사용자 접근 시도: {}", userId);
                        setErrorResponse(response, HttpStatus.FORBIDDEN, "비활성화된 사용자입니다.");
                        return; // 필터 체인 중단
                    }

                    // 사용자 역할 가져오기 (토큰에서)
                    String role = jwtUtil.getRoleFromToken(token);
                    log.debug("사용자 인증: ID={}, 역할={}", userId, role);

                    // 인증 객체 생성 및 SecurityContext에 설정 (역할 반영)
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 요청 속성에 사용자 ID와 역할 설정
                    request.setAttribute("userId", userId);
                    request.setAttribute("role", role);

                    filterChain.doFilter(request, response);
                } else {
                    log.warn("유효하지 않은 JWT 토큰");
                    setErrorResponse(response, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
                }
            } else {
                log.debug("JWT 토큰이 없음: {}", path);
                setErrorResponse(response, HttpStatus.UNAUTHORIZED, "인증 토큰이 없습니다.");
            }
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생", e);
            setErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "인증 처리 중 오류가 발생했습니다.");
        }
    }

    private void setErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Add CORS headers to error responses too
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");

        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}