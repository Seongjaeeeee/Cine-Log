package com.cinelog.server.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString 
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User{
    private Long id;
    private String name;
    private String password;
    private String email;
    private Role role;
    private boolean deleted;
    private LocalDateTime createdAt;
    public User(String userName,String password,String email){
        this(userName, password, email, Role.USER,false);
    } 
    public User(String userName,String password,String email, Role role){
        this(userName, password, email, role,false);
    } 
    public User(String userName, String password, String email, Role role,boolean deleted){//생성자 체이닝
        if(userName==null||userName.isBlank())throw new IllegalArgumentException("유저 이름은 공백일수 없습니다.");
        if(password==null||password.isBlank())throw new IllegalArgumentException("비밀번호는 공백일수 없습니다.");
        if(email==null||email.isBlank())throw new IllegalArgumentException("이메일은 공백일수 없습니다.");
        this.name = userName;
        this.password = password;
        this.email = email;
        this.role = role;
        this.deleted = deleted; 
    }

    public void changeName(String name){
        if(name==null||name.isBlank())throw new IllegalArgumentException("유저 이름은 공백일수 없습니다.");
        this.name = name;
    }
    public void changePassword(String password){
        if(password==null||password.isBlank())throw new IllegalArgumentException("비밀번호는 공백일수 없습니다.");
        this.password = password;
    }
    public void changeEmail(String email){
        if(password==null||password.isBlank())throw new IllegalArgumentException("이메일은 공백일수 없습니다.");
        this.email = email;
    }

    public void deactivate(){
        this.name = "탈퇴한 회원_"+Long.toString(id);
        this.password = UUID.randomUUID().toString();
        this.deleted = true;
    }
    public boolean isAdmin(){
        return Role.ADMIN==this.role;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setCreatedAt(LocalDateTime time){
        this.createdAt = time;
    }
}
