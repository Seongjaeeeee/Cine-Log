package com.cinelog.server.service;

import org.springframework.stereotype.Service;

import com.cinelog.server.domain.User;
import com.cinelog.server.dto.user.SessionUser;
import com.cinelog.server.dto.user.UserResponse;
import com.cinelog.server.exception.security.UnAuthorizedException;
import com.cinelog.server.global.error.ErrorCode;

@Service
public class AuthFacade {
    private UserService userService;
    private SessionManager sessionManager;
    public AuthFacade(UserService userService,SessionManager sessionManager){
        this.userService = userService;
        this.sessionManager = sessionManager;
    }

    public UserResponse createUser(String userName,String password,String email){
        User user = userService.createUser(userName, password, email);
        return UserResponse.of(user);

    }    
    public UserResponse createAdmin(String userName,String password,String email){
        User admin = userService.createAdmin(userName, password, email);
        return UserResponse.of(admin);
    }    

    public UserResponse login(String username,String password){
        User user = userService.login(username,password);
        SessionUser sessionUser = new SessionUser(user);
        sessionManager.saveLoginUser(sessionUser);
        return UserResponse.of(user);
    }
    public void logout(){
        sessionManager.logout();
    }

    public UserResponse getMeById(){
        SessionUser sessionUser = getUser();
        User user = userService.getUserById(sessionUser.getId());
        return UserResponse.of(user);
    }

    public UserResponse updateUserName(String newName){
        SessionUser sessionUser = getUser();
        User user = userService.updateUserName(newName, sessionUser.getId());
        sessionManager.changeUserName(newName);
        return UserResponse.of(user);
    }
    public UserResponse updatePassword(String currentPassword,String newPassword){
        SessionUser sessionUser = getUser();
        User user = userService.updatePassword(currentPassword, newPassword, sessionUser.getId());
        logout();
        return UserResponse.of(user);
    }
    public UserResponse updateEmail(String email){
        SessionUser sessionUser = getUser();
        User user = userService.updateEmail(email,sessionUser.getId());
        return UserResponse.of(user);
    }
    public void deleteUser(){
        SessionUser sessionUser = getUser();
        userService.deleteUser(sessionUser.getId());
        logout();
    }

    public boolean isLoggedIn(){//인가 위한 인터셉터 로직남음 인터셉터에 이거 필요 없으면 제거
        return sessionManager.isLoggedIn();
    }
    public SessionUser getUser(){
        return sessionManager.getLoginUser()
                .orElseThrow(() -> new UnAuthorizedException(ErrorCode.UNAUTHORIZED));
    }
}
