package com.crmbank.erp.comm.handler;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 🔒 [전역 세션 감시자]
 * 시스템 재시작이나 세션 만료 후 기존 화면에서 API 요청 시 
 * 세션 정보가 없으면 즉시 401 에러를 반환하여 프론트엔드의 강제 로그아웃을 유도합니다.
 */
@Slf4j
@Component
@Order(1) // RequestLoggingFilter 다음에 실행되도록 설정
public class SessionCheckFilter implements Filter {

    // 세션 체크를 제외할 경로 리스트
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
        "/api/comm/login",
        "/api/comm/session",
        "/api/crm/inbound/asterisk/check-routing",
        "/api/crm/inbound/log-callback",
        "/api/crm/inbound/play-recording"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();

        // 1. 제외 경로이거나 API 요청이 아닌 경우 통과
        if (!path.startsWith("/api/") || isExcluded(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. 세션 체크
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_session") == null) {
            log.warn("🚫 [세션 유실] 유효하지 않은 접근 차단 (401): {}", path);
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"status\": 401, \"message\": \"세션이 만료되었습니다.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isExcluded(String path) {
        return EXCLUDE_PATHS.stream().anyMatch(path::startsWith);
    }
}
