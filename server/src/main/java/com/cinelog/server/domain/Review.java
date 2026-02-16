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
public class Review {
    private Long id;
    private String content;
    private Integer rating;
    private Movie movie;
    private User user;

    private Review(String content, Integer rating, User user, Movie movie) {
        this.content = content;
        this.rating = rating;
        this.movie = movie;
        this.user = user;
    }
    public static Review create(String content, Integer rating, User user, Movie movie) {//정적 팩토리 메서드
        if(rating==null||rating>5||rating<1||user==null||movie==null)throw new IllegalArgumentException("리뷰 생성을 위한 인자가 적절하지 않습니다.");
        Review review = new Review(content, rating, user, movie);
        return review;
    }
    public static Review reconstitute(Long id, String content, Integer rating, User user, Movie movie) {//db에서 객체 가져오기위한 메서드임
        Review review = new Review(content, rating, user, movie);
        review.setId(id);
        return review; // movie.addRating()을 호출하지 않음
    }   
    
    public void update(String content,Integer rating){
        if(rating==null||rating>5||rating<1)throw new IllegalArgumentException("별점이 올바르지 않습니다.");
        this.content = content;
        this.rating = rating;
    }
    
    public boolean isOwner(User user){//권한체크용
        return Objects.equals(this.user,user);
    }
    
    public void setId(Long id){
        this.id=id;
    }
}