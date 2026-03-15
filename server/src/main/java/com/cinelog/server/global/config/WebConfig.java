package com.cinelog.server.global.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cinelog.server.global.LoginCheckInterceptor;
import com.cinelog.server.global.LoginUserArgumentResolver;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;
    private final LoginUserArgumentResolver loginUserArgumentResolver;

    // 1. 인터셉터 등록
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .order(1) // 여러 인터셉터가 있을 경우 실행 순서
                .addPathPatterns("/api/**") // 기본적으로 모든 API 경로에 문지기를 세움
                // 예외 경로: 로그인, 회원가입, 영화 조회 등은 로그인이 필요 없으므로 통과시켜줌
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/users/signup",
                        "/api/movies/**" // 영화 조회 API는 누구나 볼 수 있게
                );
    }

    // 2. 아규먼트 리졸버 등록
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }
}