package com.cinelog.server.domain;

import java.util.Objects;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString 
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    private Long id;
    private String name;
    private String password;
    private Role role;
    public User(String userName,String password){
        this(userName, password, Role.USER);
    } 
    public User(String userName,String password,Role role){
        if(userName==null||userName.isBlank())throw new IllegalArgumentException("유저 이름은 공백일수 없습니다.");
        if(password==null||password.isBlank())throw new IllegalArgumentException("비밀번호는 공백일수 없습니다.");
        this.name = userName;
        this.password = password;
        this.role = role;
    }
    
    public boolean checkPassword(String password){
        return Objects.equals(password,this.password);
    }   

    public void setId(Long id) {
        this.id = id;
    }
    public boolean isAdmin(){
        return Role.ADMIN==this.role;
    }

}
