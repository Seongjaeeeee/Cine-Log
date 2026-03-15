package com.cinelog.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinelog.server.dto.ApiResult;
import com.cinelog.server.dto.user.UserEmailUpdateRequest;
import com.cinelog.server.dto.user.UserPasswordUpdateRequest;
import com.cinelog.server.dto.user.UserResponse;
import com.cinelog.server.dto.user.UserUpdateRequest;
import com.cinelog.server.service.AuthFacade;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController{
    private final AuthFacade authFacade;
    public UserController(AuthFacade authFacade){
        this.authFacade = authFacade;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResult<UserResponse>> getUserInfo(){
        UserResponse user = authFacade.getMeById();
        return ResponseEntity.ok().body(ApiResult.success(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResult<UserResponse>> updateUserName(@Valid @RequestBody UserUpdateRequest request){
        UserResponse user = authFacade.updateUserName(request.userName());
        return ResponseEntity.ok().body(ApiResult.success(user));
    }
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResult<UserResponse>> updatePassword(@Valid @RequestBody UserPasswordUpdateRequest request){
        UserResponse user = authFacade.updatePassword(request.password(),request.newPassword());
        return ResponseEntity.ok().body(ApiResult.success(user));
    }
    @PatchMapping("/me/email")
    public ResponseEntity<ApiResult<UserResponse>> updateEmail(@Valid @RequestBody UserEmailUpdateRequest request){
        UserResponse user = authFacade.updateEmail(request.email());
        return ResponseEntity.ok().body(ApiResult.success(user));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResult<Void>> deleteUser(){
        authFacade.deleteUser();
        return ResponseEntity.ok().body(ApiResult.success());
    }
}