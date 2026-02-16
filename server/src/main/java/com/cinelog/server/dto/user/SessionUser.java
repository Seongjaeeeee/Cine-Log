package com.cinelog.server.dto.user;

import java.io.Serializable;

import com.cinelog.server.domain.Role;
import com.cinelog.server.domain.User;

import lombok.Value;

@Value
public class SessionUser implements Serializable{
    private Long id;
    private String name;
    private Role role;

    public SessionUser(Long id, String name, Role role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public SessionUser(User user){
        this.id = user.getId();
        this.name = user.getName();
        this.role = user.getRole();
    }

}
