package com.cinelog.server.domain;

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
    private Role role;
    private boolean deleted;
    public User(String userName,String password){
        this(userName, password, Role.USER,false);
    } 
    public User(String userName,String password, Role role){
        this(userName, password, role,false);
    } 
    public User(String userName,String password,Role role,boolean deleted){//생성자 체이닝
        if(userName==null||userName.isBlank())throw new IllegalArgumentException("유저 이름은 공백일수 없습니다.");
        if(password==null||password.isBlank())throw new IllegalArgumentException("비밀번호는 공백일수 없습니다.");
        this.name = userName;
        this.password = password;
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
}
