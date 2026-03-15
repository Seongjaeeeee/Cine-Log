package com.cinelog.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinelog.server.dto.ApiResult;
import com.cinelog.server.dto.user.SignInRequest;
import com.cinelog.server.dto.user.SignUpRequest;
import com.cinelog.server.dto.user.UserResponse;
import com.cinelog.server.service.AuthFacade;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController{
    private final AuthFacade authFacade;
    public AuthController(AuthFacade authFacade){
        this.authFacade = authFacade;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResult<UserResponse>> signUp(@Valid @RequestBody SignUpRequest request){
        UserResponse userResponse = authFacade.createUser(request.userName(), request.password(),request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(userResponse));
    }
    
    @PostMapping("/signin")
    public ResponseEntity<ApiResult<UserResponse>> signIn(@Valid @RequestBody SignInRequest request){
        UserResponse userResponse = authFacade.login(request.userName(), request.password());
        return ResponseEntity.ok().body(ApiResult.success(userResponse));
    } 

    @PostMapping("/logout")
    public ResponseEntity<ApiResult<Void>> logOut(){
        authFacade.logout();
        return ResponseEntity.ok().body(ApiResult.success());
    }
}