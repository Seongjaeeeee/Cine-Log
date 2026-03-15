package com.cinelog.server.global;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.cinelog.server.dto.user.SessionUser;
import com.cinelog.server.exception.security.UnAuthorizedException;
import com.cinelog.server.global.error.ErrorCode;
import com.cinelog.server.service.SessionManager;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final SessionManager sessionManager;

    // 1. 이 리졸버가 언제 작동할지 조건을 정합니다.
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(LoginUser.class);
        // 파라미터의 타입이 SessionUser 클래스인가?
        boolean hasSessionUserType = SessionUser.class.isAssignableFrom(parameter.getParameterType());
        
        return hasLoginAnnotation && hasSessionUserType; // 둘 다 만족할 때만 작동!
    }

    // 2. 조건이 맞으면 세션에서 유저 정보를 꺼내서 넘겨줍니다.
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // SessionManager에서 안전하게 유저를 꺼내서 반환합니다. (이 값이 컨트롤러의 파라미터로 들어감)
        return sessionManager.getLoginUser()
                .orElseThrow(() -> new UnAuthorizedException(ErrorCode.UNAUTHORIZED));
    }
}