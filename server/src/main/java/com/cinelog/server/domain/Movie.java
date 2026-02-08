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

    private String ratingPolicyType;
    private RatingPolicy ratingPolicy;
   
    public Movie(String name, Director director, Genre genre,LocalDate releaseDate,String description, List<Actor> actors){
        validateConstructor(name, director);
        this.name = name;
        this.director = director;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.description = description;
        this.actors.addAll(actors);

        // 기본 정책 설정
        this.ratingPolicy = new BasicRatingPolicy();
        this.ratingPolicyType = "BASIC";
    }
    
    public boolean containsActor(Actor actor){
        return actors.contains(actor);
    }
    public boolean isDirectedBy(Director director){
        return this.director.equals(director);
    }
    public boolean containsKeyword(String keyword){
        if (keyword == null) return false;
        return this.name.toLowerCase().contains(keyword.toLowerCase());
    }
    public boolean containsActorKeyword(String keyword){
        return actors.stream().anyMatch(actor -> actor.containsKeyword(keyword));
    }
    public boolean containsDirectorKeyword(String keyword){
        return this.director.containsKeyword(keyword);
    }
    
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
    
   //별점로직
    public void changeRatingPolicy(RatingPolicy ratingPolicy){
        if(ratingPolicy == null)throw new IllegalArgumentException("평점 정책이 존재하지 않습니다.");
        this.ratingPolicy = ratingPolicy;
        if (ratingPolicy instanceof BasicRatingPolicy) {
            this.ratingPolicyType = "BASIC";
        }
        //계산로직?
    }
    public void addRating(Integer star) {
        validateRatingRange(star);
        calculateAverageRating();
    }

    public void updateRating(Integer oldStar, Integer newStar) {
       //todo
    }

    public void deleteRating(Integer star) {
       //todo
    }


    private void calculateAverageRating() {
        //todo);
    }

    private void validateRatingRange(Integer star) {
        if (star == null || star < 1 || star > 5) {
            throw new IllegalArgumentException("별점은 1점에서 5점 사이여야 합니다.");
        }
    }
    //////////////////////
    private void validateConstructor(String name, Director director) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("영화 제목은 필수입니다.");
        }
        if (director == null) {
            throw new IllegalArgumentException("감독은 필수입니다.");
        }
    }

    public void setId(Long id){
        this.id = id;
    }
}