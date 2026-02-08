package com.cinelog.server.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString 
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Actor{
    private Long id;
    private String name;
   
    public Actor(String name){
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        this.name = name;
    }

    public boolean containsKeyword(String keyword){
        if (keyword == null) return false;
        return this.name.toLowerCase().contains(keyword.toLowerCase());
    }
    public void changeName(String newName){
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        this.name = newName;
    }

    public void setId(Long id){
        this.id=id;
    }
}