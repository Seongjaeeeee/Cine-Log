package com.cinelog.server.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Getter
@ToString 
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JDBC용 기본 생성자
public class Movie{
    private Long id;
    private String name;
    private Director director;//없으면 안됨
    private Genre genre;
    private String description;
    private LocalDate releaseDate;
    private List<Actor> actors = new ArrayList<>();

    private Double rating=0.0;
   
    public Movie(String name, Director director, Genre genre,LocalDate releaseDate,String description, List<Actor> actors){
        validateConstructor(name, director);
        this.name = name;
        this.director = director;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.description = description;
        this.actors.addAll(actors);
    }
    //update
    public void updateMovieInfo(String name, Genre genre, LocalDate releaseDate, String description) {
        if (name != null && !name.isBlank()) this.name = name;
        if (genre != null) this.genre = genre;
        if (releaseDate != null) this.releaseDate = releaseDate;
        if (description != null) this.description = description;
    }
    public void changeDirector(Director director){
        if (director == null) {
            throw new IllegalArgumentException("감독 정보는 비어있을 수 없습니다.");
        }
        this.director = director;
    }
    public void addActor(Actor actor){
        if (actor == null) {
        throw new IllegalArgumentException("배우 정보가 누락되었습니다.");
        }
        boolean isExist = actors.contains(actor);
        if(isExist) return;
        actors.add(actor);
    }
    public void removeActor(Actor actor){
        if (actor == null) {
        throw new IllegalArgumentException("배우 정보가 누락되었습니다.");
        }
        
        if (!actors.remove(actor)) {
        throw new IllegalArgumentException("영화에 존재하지 않는 배우입니다.");
        }
    }
    public void updateRating(Double rating){
        if(rating == null||rating<0||rating>5)throw new IllegalArgumentException("평점이 잘못되었습니다.");
        this.rating = rating;
    }

    public void setId(Long id){
        this.id = id;
    }
    public void setRating(Double rating){//db용
        this.rating = rating;
    }

    private void validateConstructor(String name, Director director) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("영화 제목은 필수입니다.");
        }
        if (director == null) {
            throw new IllegalArgumentException("감독은 필수입니다.");
        }
    }
}