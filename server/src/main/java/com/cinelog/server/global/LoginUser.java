package com.cinelog.server.global;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 파라미터에만 붙일 수 있도록 설정
@Retention(RetentionPolicy.RUNTIME) // 런타임(실행 중)까지 어노테이션 정보가 살아있도록 설정
public @interface LoginUser {
}
