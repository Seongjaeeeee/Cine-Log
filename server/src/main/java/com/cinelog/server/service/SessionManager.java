package com.cinelog.server.service;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.cinelog.server.dto.user.SessionUser;
import com.cinelog.server.exception.security.UnAuthorizedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Component
@RequestScope//필요없긴한데 안정성 위해 추가
public class SessionManager {
    private final HttpServletRequest request;
    private static final String LOGIN_USER = "loginUser";
    public SessionManager(HttpServletRequest request) {
        this.request = request;
    }

    public void saveLoginUser(SessionUser user) {//세션 고정공격 방어
        HttpSession session = request.getSession(false);
        if(session!=null)session.invalidate();
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(LOGIN_USER, user);//사물함 안에 유저 설정
    }

    public void logout() {
        HttpSession session = request.getSession(false);
        if(session!=null) session.invalidate();
    }

    public void changeUserName(String name){
        SessionUser currentUser = getLoginUser().orElseThrow(() -> new UnAuthorizedException("로그인 상태가 아닙니다."));
        SessionUser newUser = new SessionUser(currentUser.getId(),name,currentUser.getRole());
        
        HttpSession session = request.getSession(false);
        session.setAttribute(LOGIN_USER, newUser);
    }

    public boolean isLoggedIn(){
        return !getLoginUser().isEmpty();
    }

    public Optional<SessionUser> getLoginUser() {
        HttpSession session = request.getSession(false);
        if(session==null)return Optional.empty();
        return Optional.ofNullable((SessionUser) session.getAttribute(LOGIN_USER));
    }
}
