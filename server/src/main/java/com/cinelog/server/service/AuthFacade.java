package com.cinelog.server.service;

import org.springframework.stereotype.Service;

import com.cinelog.server.domain.User;
import com.cinelog.server.dto.user.SessionUser;
import com.cinelog.server.exception.security.UnAuthorizedException;

@Service
public class AuthFacade {
    private UserService userService;
    private SessionManager sessionManager;
    public AuthFacade(UserService userService,SessionManager sessionManager){
        this.userService = userService;
        this.sessionManager = sessionManager;
    }

    public void createUser(String userName,String password){
        userService.createUser(userName, password);
    }    
    public void createAdmin(String userName,String password){
        userService.createAdmin(userName, password);
    }    

    public void login(String username,String password){
        User user = userService.login(username,password);
        SessionUser sessionUser = new SessionUser(user);
        sessionManager.saveLoginUser(sessionUser);
    }
    public void logout(){
        sessionManager.logout();
    }

    public void updateUserName(String newName){
        SessionUser sessionUser = getUser();
        userService.updateUserName(newName, sessionUser.getId());
        sessionManager.changeUserName(newName);
    }
    public void updatePassword(String currentPassword,String newPassword){
        SessionUser sessionUser = getUser();
        userService.updatePassword(currentPassword, newPassword, sessionUser.getId());
        logout();
    }
    public void deleteUser(){
        SessionUser sessionUser = getUser();
        userService.deleteUser(sessionUser.getId());
        logout();
    }

    public boolean isLoggedIn(){
        return sessionManager.isLoggedIn();
    }
    public SessionUser getUser(){
        return sessionManager.getLoginUser()
                .orElseThrow(() -> new UnAuthorizedException("로그인 되지 않았습니다."));
    }
}
//인가 위한 인터셉터 로직남음
