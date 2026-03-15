package com.cinelog.server.global;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.cinelog.server.exception.security.UnAuthorizedException; // 작성자님이 만든 예외
import com.cinelog.server.global.error.ErrorCode;
import com.cinelog.server.service.SessionManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final SessionManager sessionManager; // 작성자님이 잘 만들어두신 세션 매니저 활용!

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // HTTP OPTIONS 요청(CORS 프리플라이트)은 통과시킴 (프론트엔드 연동 시 필수)
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        log.info("인증 체크 인터셉터 실행: {}", request.getRequestURI());
        if (!sessionManager.isLoggedIn()) {
            log.warn("미인증 사용자 요청: {}", request.getRequestURI());
            throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED);
        }

        return true; // 로그인 한 유저면 통과 (컨트롤러로 이동)
    }
}